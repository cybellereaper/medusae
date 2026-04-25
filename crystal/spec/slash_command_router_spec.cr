require "spec"
require "../src/medusae"

describe Medusae::Client::SlashCommandRouter do
  it "responds pong to ping interactions" do
    response = [] of Tuple(String, String, Int32, Hash(String, JSON::Any)?)
    router = Medusae::Client::SlashCommandRouter.new(->(id : String, token : String, type : Int32, data : Hash(String, JSON::Any)?) {
      response << {id, token, type, data}
    })

    interaction = JSON.parse(%({"id":"1","token":"abc","type":1}))
    router.handle_interaction(interaction)

    response.size.should eq(1)
    response.first[2].should eq(1)
  end

  it "dispatches slash handlers by command name" do
    called = false
    router = Medusae::Client::SlashCommandRouter.new(->(_id : String, _token : String, _type : Int32, _data : Hash(String, JSON::Any)?) {})
    router.register_slash_handler("ping") { called = true }

    interaction = JSON.parse(%({"id":"1","token":"abc","type":2,"data":{"name":"ping"}}))
    router.handle_interaction(interaction)

    called.should be_true
  end

  it "runs global component handler when key-specific handler is absent" do
    called = false
    router = Medusae::Client::SlashCommandRouter.new(->(_id : String, _token : String, _type : Int32, _data : Hash(String, JSON::Any)?) {})
    router.register_global_component_handler { called = true }

    interaction = JSON.parse(%({"id":"1","token":"abc","type":3,"data":{"custom_id":"unknown"}}))
    router.handle_interaction(interaction)

    called.should be_true
  end

  it "rejects duplicate handlers" do
    router = Medusae::Client::SlashCommandRouter.new(->(_id : String, _token : String, _type : Int32, _data : Hash(String, JSON::Any)?) {})
    router.register_component_handler("my-button") { }

    expect_raises(ArgumentError, /already registered/) do
      router.register_component_handler("my-button") { }
    end
  end

  it "requires interaction id and token when responding" do
    router = Medusae::Client::SlashCommandRouter.new(->(_id : String, _token : String, _type : Int32, _data : Hash(String, JSON::Any)?) {})
    interaction = JSON.parse(%({"type":1}))

    expect_raises(ArgumentError, /id and token/) do
      router.handle_interaction(interaction)
    end
  end
end
