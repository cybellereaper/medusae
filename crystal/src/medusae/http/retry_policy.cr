module Medusae
  module Http
    record RetryPolicy,
      max_attempts : Int32,
      base_delay : Time::Span,
      max_delay : Time::Span,
      jitter_factor : Float64 do
      def initialize(@max_attempts : Int32, @base_delay : Time::Span, @max_delay : Time::Span, @jitter_factor : Float64)
        raise ArgumentError.new("max_attempts must be >= 1") if @max_attempts < 1
        raise ArgumentError.new("base_delay must be > 0") if @base_delay <= Time::Span.zero
        raise ArgumentError.new("max_delay must be > 0") if @max_delay <= Time::Span.zero
        raise ArgumentError.new("max_delay must be >= base_delay") if @max_delay < @base_delay
        raise ArgumentError.new("jitter_factor must be between 0 and 1") unless (0.0..1.0).includes?(@jitter_factor)
      end

      def self.default : RetryPolicy
        new(max_attempts: 4, base_delay: 250.milliseconds, max_delay: 5.seconds, jitter_factor: 0.2)
      end

      def retryable_status?(status_code : Int32) : Bool
        status_code == 408 || status_code == 425 || status_code == 429 || status_code >= 500
      end

      def delay_for_attempt(attempt : Int32, random : Random = Random.new) : Time::Span
        shift = (attempt - 1).clamp(0, 30)
        multiplier = 1_i64 << shift
        capped_ms = {(@base_delay.total_milliseconds * multiplier).to_i64, @max_delay.total_milliseconds.to_i64}.min

        return capped_ms.milliseconds if @jitter_factor.zero?

        jitter_window = capped_ms * @jitter_factor
        min = {1_i64, (capped_ms - jitter_window).round.to_i64}.max
        max = {min, (capped_ms + jitter_window).round.to_i64}.max
        random.rand(min..max).milliseconds
      end
    end
  end
end
