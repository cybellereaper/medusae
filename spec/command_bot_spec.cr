require "spec"
require "../src/medusae"

private class MacroBotExample
  include Medusae::Client::CommandBot

  @events = [] of String

  getter events : Array(String)

  slash_command "ping" do |interaction|
    @events << "slash:#{interaction.dig?("data", "name").try(&.as_s?) || "unknown"}"
    respond_with_message(interaction, "pong")
  end

  component "confirm" do |_interaction|
    @events << "component:confirm"
  end

  global_component do
    @events << "component:global"
  end

  autocomplete "ping" do |_interaction|
    @events << "autocomplete:ping"
  end
end

describe Medusae::Client::CommandBot do
  it "registers macro-defined slash command handlers" do
    responses = [] of Tuple(String, String, Int32, Hash(String, JSON::Any)?)
    bot = MacroBotExample.new(->(id : String, token : String, type : Int32, data : Hash(String, JSON::Any)?) {
      responses << {id, token, type, data}
    })

    bot.handle_interaction(JSON.parse(%({"id":"1","token":"abc","type":2,"data":{"name":"ping"}})))

    bot.events.should eq(["slash:ping"])
    responses.size.should eq(1)
    responses.first[2].should eq(4)
    responses.first[3].should_not be_nil
    responses.first[3].not_nil!["content"].as_s.should eq("pong")
  end

  it "prefers exact component handlers over global handlers" do
    bot = MacroBotExample.new(->(_id : String, _token : String, _type : Int32, _data : Hash(String, JSON::Any)?) { })

    bot.handle_interaction(JSON.parse(%({"id":"1","token":"abc","type":3,"data":{"custom_id":"confirm"}})))

    bot.events.should eq(["component:confirm"])
  end

  it "falls back to global component handlers" do
    bot = MacroBotExample.new(->(_id : String, _token : String, _type : Int32, _data : Hash(String, JSON::Any)?) { })

    bot.handle_interaction(JSON.parse(%({"id":"1","token":"abc","type":3,"data":{"custom_id":"missing"}})))

    bot.events.should eq(["component:global"])
  end

  it "supports autocomplete handlers defined with macros" do
    bot = MacroBotExample.new(->(_id : String, _token : String, _type : Int32, _data : Hash(String, JSON::Any)?) { })

    bot.handle_interaction(JSON.parse(%({"id":"1","token":"abc","type":4,"data":{"name":"ping"}})))

    bot.events.should eq(["autocomplete:ping"])
  end

  it "rejects duplicate macro command registration" do
    expect_raises(ArgumentError, /already registered/) do
      DuplicateMacroBot.new(->(_id : String, _token : String, _type : Int32, _data : Hash(String, JSON::Any)?) { })
    end
  end
end

private class DuplicateMacroBot
  include Medusae::Client::CommandBot

  slash_command "ping" do
  end

  slash_command "ping" do
  end
end
