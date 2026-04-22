module Medusae
  module Http
    record Request, method : String, path : String, headers : HTTP::Headers, body : String? = nil
    record Response, status_code : Int32, headers : HTTP::Headers = HTTP::Headers.new, body : String = ""

    module Transport
      abstract def execute(request : Request) : Response
    end

    class RestClient
      def initialize(
        @transport : Transport,
        @token : String,
        @retry_policy : RetryPolicy = RetryPolicy.default,
        @observer : RateLimitObserver = NullRateLimitObserver.new,
        @clock : Proc(Time) = ->{ Time.utc },
        @sleeper : Proc(Time::Span, Nil) = ->(span : Time::Span) { sleep(span) }
      )
        raise ArgumentError.new("token must not be blank") if @token.blank?
        @rate_limit_manager = RateLimitManager.new(@observer, @clock, @sleeper)
        @route_to_bucket = {} of String => String
      end

      def request_json(method : String, path : String, body : JSON::Any? = nil) : JSON::Any
        route_key = "#{method} #{path}"
        bucket_id = @route_to_bucket[route_key]? || route_key
        started_at = @clock.call
        request = build_request(method, path, body)

        (1..@retry_policy.max_attempts).each do |attempt|
          @rate_limit_manager.await(bucket_id)

          response = @transport.execute(request)
          bucket_id = update_rate_limit_bucket(route_key, bucket_id, response)
          @rate_limit_manager.update_from_headers(bucket_id, response.headers)

          if response.status_code == 429
            retry_after_seconds = @rate_limit_manager.update_from_429(bucket_id, parse_or_empty(response.body))
            if attempt < @retry_policy.max_attempts
              backoff = ([1_i64, (retry_after_seconds * 1000).round.to_i64].max).milliseconds
              @observer.on_retry_scheduled(method, path, attempt.to_i, backoff, "rate_limit")
              @sleeper.call(backoff)
              next
            end
          elsif response.status_code < 200 || response.status_code >= 300
            if @retry_policy.retryable_status?(response.status_code) && attempt < @retry_policy.max_attempts
              retry_with_backoff(method, path, attempt.to_i, "http_#{response.status_code}")
              next
            end

            record_completion(method, path, attempt.to_i, response.status_code, started_at)
            raise DiscordHttpError.new(response.status_code, response.body)
          else
            record_completion(method, path, attempt.to_i, response.status_code, started_at)
            return parse_or_empty(response.body)
          end

          record_completion(method, path, attempt.to_i, response.status_code, started_at)
          raise DiscordHttpError.new(response.status_code, response.body)
        rescue ex : TransportError
          if attempt < @retry_policy.max_attempts
            retry_with_backoff(method, path, attempt.to_i, "transport")
            next
          end

          raise ex
        end

        raise "request attempts exhausted unexpectedly"
      end

      private def retry_with_backoff(method : String, path : String, attempt : Int32, reason : String) : Nil
        backoff = @retry_policy.delay_for_attempt(attempt)
        @observer.on_retry_scheduled(method, path, attempt, backoff, reason)
        @sleeper.call(backoff)
      end

      private def update_rate_limit_bucket(route_key : String, current_bucket_id : String, response : Response) : String
        discovered_bucket = response.headers["X-RateLimit-Bucket"]?
        return current_bucket_id if discovered_bucket.nil? || discovered_bucket.blank?

        @route_to_bucket[route_key] = discovered_bucket
        discovered_bucket
      end

      private def build_request(method : String, path : String, body : JSON::Any?) : Request
        headers = HTTP::Headers{
          "Authorization" => "Bot #{@token}",
          "Accept"        => "application/json",
          "User-Agent"    => "medusae-crystal/0.1.0",
        }

        if body
          headers["Content-Type"] = "application/json"
          Request.new(method: method, path: path, headers: headers, body: body.to_json)
        else
          Request.new(method: method, path: path, headers: headers)
        end
      end

      private def parse_or_empty(payload : String?) : JSON::Any
        return JSON.parse("{}") if payload.nil? || payload.blank?
        JSON.parse(payload)
      end

      private def record_completion(method : String, path : String, attempts : Int32, status_code : Int32, started_at : Time) : Nil
        @observer.on_request_completed(method, path, attempts, status_code, @clock.call - started_at)
      end
    end
  end
end
