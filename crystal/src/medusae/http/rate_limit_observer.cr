module Medusae
  module Http
    module RateLimitObserver
      def on_bucket_blocked(bucket_id : String, duration : Time::Span, source : String) : Nil
      end

      def on_rate_limited_response(bucket_id : String, retry_after : Time::Span, global : Bool, status_code : Int32) : Nil
      end

      def on_retry_scheduled(method : String, path : String, attempt : Int32, backoff : Time::Span, reason : String) : Nil
      end

      def on_request_completed(method : String, path : String, attempts : Int32, status_code : Int32, duration : Time::Span) : Nil
      end
    end

    class NullRateLimitObserver
      include RateLimitObserver
    end
  end
end
