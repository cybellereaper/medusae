# Medusae (Crystal Edition)

Medusae is now a **Crystal-first** Discord interaction toolkit focused on building payloads and routing interaction events with a strongly typed API.

## Features

- Gateway intent bitmask helpers (`Medusae::Gateway::GatewayIntent`)
- Discord client configuration model (`Medusae::Client::DiscordClientConfig`)
- Interaction router (`Medusae::Client::SlashCommandRouter`)
- Rich payload builders:
  - messages and embeds
  - buttons and action rows
  - string/entity/channel select menus
  - modals and text inputs

## Install

Add to your shard:

```yaml
dependencies:
  medusae:
    github: cybellereaper/medusae.cr
```

## Development

```bash
shards install
crystal spec
```

## Example: build a component-rich message

```crystal
require "medusae"

button_row = Medusae::Client::DiscordActionRow.of([
  Medusae::Client::DiscordButton.primary("confirm", "Confirm"),
  Medusae::Client::DiscordButton.link("https://discord.com/developers/docs", "Docs"),
])

select_row = Medusae::Client::DiscordActionRow.of([
  Medusae::Client::DiscordStringSelectMenu.of("theme", [
    Medusae::Client::DiscordSelectOption.of("Dark", "dark").as_default,
    Medusae::Client::DiscordSelectOption.of("Light", "light"),
  ]).with_placeholder("Choose a theme").with_selection_range(1, 1),
])

payload = Medusae::Client::DiscordMessage.of_content("Choose your settings")
  .with_components([button_row, select_row])
  .as_ephemeral
  .to_payload

puts payload.to_json
```

## Example: route Discord interactions

```crystal
require "json"
require "medusae"

router = Medusae::Client::SlashCommandRouter.new(
  ->(id : String, token : String, type : Int32, data : Hash(String, JSON::Any)?) {
    puts "Responding id=#{id} token=#{token} type=#{type} data=#{data}"
  }
)

router.register_slash_handler("ping") do |interaction|
  puts "slash command received: #{interaction}"
end

router.register_component_handler("confirm") do |interaction|
  puts "button clicked: #{interaction}"
end

router.handle_interaction(JSON.parse(%({"id":"1","token":"abc","type":2,"data":{"name":"ping"}})))
router.handle_interaction(JSON.parse(%({"id":"2","token":"def","type":3,"data":{"custom_id":"confirm"}})))
```

See runnable files in [`examples/`](examples).
