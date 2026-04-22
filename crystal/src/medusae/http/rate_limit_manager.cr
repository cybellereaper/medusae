module Medusae
  module Http
    class RateLimitManager
      alias Sleeper = Proc(Time::Span, Nil)
      alias Clock = Proc(Time)

      def initialize(
        @observer : RateLimitObserver = NullRateLimitObserver.new,
        @clock : Clock = ->{ Time.utc },
        @sleeper : Sleeper = ->(span : Time::Span) { sleep(span) }
      )
        @blocked_until_by_bucket = {} of String => Time
      end

      def await(bucket_id : String) : Nil
        blocked_until = @blocked_until_by_bucket[bucket_id]?
        return if blocked_until.nil?

        remaining = blocked_until - @clock.call
        if remaining <= Time::Span.zero
          @blocked_until_by_bucket.delete(bucket_id)
          return
        end

        @sleeper.call(remaining)
      end

      def update_from_headers(bucket_id : String, headers : HTTP::Headers) : Nil
        remaining = headers["X-RateLimit-Remaining"]?.try(&.to_i64?) || -1_i64
        reset_after = headers["X-RateLimit-Reset-After"]?.try(&.to_f64?) || 0.0
        return unless remaining == 0 && reset_after.positive?

        block_for(bucket_id, reset_after, "headers")
      end

      def update_from_429(bucket_id : String, body : JSON::Any) : Float64
        retry_after_seconds = body["retry_after"]?.try(&.as_f?) || 1.0
        global = body["global"]?.try(&.as_bool?) || false
        retry_after = ([1_i64, (retry_after_seconds * 1000).round.to_i64].max).milliseconds

        @observer.on_rate_limited_response(bucket_id, retry_after, global, 429)
        block_for(bucket_id, retry_after_seconds, "429")
        retry_after_seconds
      end

      private def block_for(bucket_id : String, seconds : Float64, source : String) : Nil
        millis = [1_i64, (seconds * 1000).round.to_i64].max
        duration = millis.milliseconds
        @blocked_until_by_bucket[bucket_id] = @clock.call + duration
        @observer.on_bucket_blocked(bucket_id, duration, source)
      end
    end
  end
end
