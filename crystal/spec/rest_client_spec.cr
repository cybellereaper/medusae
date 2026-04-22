require "./spec_helper"

private class ScriptedTransport
  include Medusae::Http::Transport

  getter requests = [] of Medusae::Http::Request

  def initialize(@responses : Array(Medusae::Http::Response | Exception))
  end

  def execute(request : Medusae::Http::Request) : Medusae::Http::Response
    @requests << request
    item = @responses.shift?
    raise "transport script exhausted" if item.nil?
    raise item.as(Exception) if item.is_a?(Exception)
    item.as(Medusae::Http::Response)
  end
end

private class CollectingObserver
  include Medusae::Http::RateLimitObserver

  getter retries = [] of {String, String, Int32, Time::Span, String}
  getter completed = [] of {String, String, Int32, Int32}

  def on_retry_scheduled(method : String, path : String, attempt : Int32, backoff : Time::Span, reason : String) : Nil
    @retries << {method, path, attempt, backoff, reason}
  end

  def on_request_completed(method : String, path : String, attempts : Int32, status_code : Int32, duration : Time::Span) : Nil
    @completed << {method, path, attempts, status_code}
  end
end

describe Medusae::Http::RestClient do
  it "retries retryable statuses then returns JSON" do
    transport = ScriptedTransport.new([
      Medusae::Http::Response.new(status_code: 500, body: %({"error":"boom"})),
      Medusae::Http::Response.new(status_code: 200, body: %({"ok":true})),
    ] of (Medusae::Http::Response | Exception))
    observer = CollectingObserver.new
    sleeps = [] of Time::Span
    policy = Medusae::Http::RetryPolicy.new(3, 1.millisecond, 2.milliseconds, 0)

    client = Medusae::Http::RestClient.new(transport, "token", policy, observer, ->{ Time.utc }, ->(span : Time::Span) { sleeps << span })

    result = client.request_json("GET", "/gateway/bot")

    result["ok"].as_bool.should be_true
    observer.retries.size.should eq(1)
    observer.retries.first[4].should eq("http_500")
    sleeps.should eq([1.millisecond])
    observer.completed.last.should eq({"GET", "/gateway/bot", 2, 200})
  end

  it "retries transport errors" do
    transport = ScriptedTransport.new([
      Medusae::Http::TransportError.new("oops"),
      Medusae::Http::Response.new(status_code: 204),
    ] of (Medusae::Http::Response | Exception))
    observer = CollectingObserver.new

    client = Medusae::Http::RestClient.new(
      transport,
      "token",
      Medusae::Http::RetryPolicy.new(2, 1.millisecond, 1.millisecond, 0),
      observer,
      ->{ Time.utc },
      ->(span : Time::Span) { }
    )

    client.request_json("DELETE", "/channels/1/messages/2")

    observer.retries.map(&.[4]).should eq(["transport"])
  end

  it "raises on non-retryable failures" do
    transport = ScriptedTransport.new([
      Medusae::Http::Response.new(status_code: 400, body: %({"message":"bad request"})),
    ] of (Medusae::Http::Response | Exception))

    client = Medusae::Http::RestClient.new(transport, "token")

    expect_raises(Medusae::Http::DiscordHttpError) do
      client.request_json("POST", "/channels/1/messages", JSON.parse(%({"content":"x"})))
    end
  end
end
