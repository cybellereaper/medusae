require "json"
require "../src/medusae"

class DemoBot
  include Medusae::Client::CommandBot

  slash_command "ping" do |interaction|
    puts "slash command received: #{interaction}"
    respond_with_message(interaction, "Pong from macro bot")
  end

  component "confirm" do |interaction|
    puts "button clicked: #{interaction}"
  end

  global_component do |interaction|
    puts "fallback component handler: #{interaction}"
  end
end

bot = DemoBot.new(
  ->(id : String, token : String, type : Int32, data : Hash(String, JSON::Any)?) {
    puts "Responding id=#{id} token=#{token} type=#{type} data=#{data}"
  }
)

bot.handle_interaction(JSON.parse(%({"id":"1","token":"abc","type":2,"data":{"name":"ping"}})))
bot.handle_interaction(JSON.parse(%({"id":"2","token":"def","type":3,"data":{"custom_id":"confirm"}})))
bot.handle_interaction(JSON.parse(%({"id":"3","token":"ghi","type":3,"data":{"custom_id":"unknown"}})))
