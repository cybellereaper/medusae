require "./spec_helper"

describe Medusae::Http::RetryPolicy do
  it "returns exponential delays when jitter is disabled" do
    policy = Medusae::Http::RetryPolicy.new(4, 100.milliseconds, 1.second, 0)

    policy.delay_for_attempt(1).should eq(100.milliseconds)
    policy.delay_for_attempt(2).should eq(200.milliseconds)
    policy.delay_for_attempt(3).should eq(400.milliseconds)
    policy.delay_for_attempt(4).should eq(800.milliseconds)
    policy.delay_for_attempt(10).should eq(1.second)
  end

  it "marks retryable statuses" do
    policy = Medusae::Http::RetryPolicy.default

    [408, 425, 429, 500, 503].each { |status| policy.retryable_status?(status).should be_true }
    [200, 400, 404].each { |status| policy.retryable_status?(status).should be_false }
  end
end
