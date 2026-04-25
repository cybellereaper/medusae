require "spec"
require "../src/medusae"

describe Medusae::Client::DiscordClientConfig do
  it "normalizes url and builds api uri" do
    config = Medusae::Client::DiscordClientConfig.new("token", api_base_url: "https://discord.com/api///", api_version: 10)
    config.api_uri("channels/1").should eq("https://discord.com/api/v10/channels/1")
  end

  it "defaults empty base url and timeout" do
    config = Medusae::Client::DiscordClientConfig.new("token", api_base_url: "   ")
    config.api_base_url.should eq("https://discord.com/api")
    config.request_timeout.should eq(30.seconds)
  end

  it "validates shard settings" do
    expect_raises(ArgumentError, /shard_count/) do
      Medusae::Client::DiscordClientConfig.new("token", shard_count: 0)
    end

    expect_raises(ArgumentError, /shard_id/) do
      Medusae::Client::DiscordClientConfig.new("token", shard_id: 1, shard_count: 1)
    end
  end
end
