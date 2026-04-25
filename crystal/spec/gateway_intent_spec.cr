require "spec"
require "../src/medusae"

describe Medusae::Gateway::GatewayIntent do
  it "combines intent bits" do
    value = Medusae::Gateway::GatewayIntent.combine(
      Medusae::Gateway::GatewayIntent::Guilds,
      Medusae::Gateway::GatewayIntent::MessageContent
    )

    value.should eq((1 | (1 << 15)))
  end
end
