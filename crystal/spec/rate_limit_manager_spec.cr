require "./spec_helper"

private class FakeObserver
  include Medusae::Http::RateLimitObserver
  getter blocked = [] of {String, Time::Span, String}

  def on_bucket_blocked(bucket_id : String, duration : Time::Span, source : String) : Nil
    @blocked << {bucket_id, duration, source}
  end
end

describe Medusae::Http::RateLimitManager do
  it "blocks from response headers and sleeps while blocked" do
    now = Time.utc
    slept = [] of Time::Span
    clock = ->{ now }
    sleeper = ->(span : Time::Span) { slept << span }
    observer = FakeObserver.new

    manager = Medusae::Http::RateLimitManager.new(observer, clock, sleeper)
    headers = HTTP::Headers{
      "X-RateLimit-Remaining"   => "0",
      "X-RateLimit-Reset-After" => "0.250",
    }

    manager.update_from_headers("bucket-1", headers)
    manager.await("bucket-1")

    slept.size.should eq(1)
    slept.first.total_milliseconds.round.should eq(250)
    observer.blocked.size.should eq(1)
    observer.blocked.first[2].should eq("headers")
  end

  it "parses retry_after on 429 and defaults safely" do
    observer = FakeObserver.new
    manager = Medusae::Http::RateLimitManager.new(observer)

    seconds = manager.update_from_429("bucket-2", JSON.parse(%({"retry_after":1.5, "global":true})))
    seconds.should eq(1.5)

    fallback = manager.update_from_429("bucket-2", JSON.parse("{}"))
    fallback.should eq(1.0)
  end
end
