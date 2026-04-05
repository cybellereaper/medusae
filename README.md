# Jellycord

Jellycord is a lightweight Java client for building Discord bots using both the Gateway and REST APIs.

## Features

- Gateway connection and event subscription
- Slash commands, context menus, autocomplete, and component interactions
- Message builders for embeds and components
- REST helpers for common Discord resources through `DiscordApi`

## Installation

Add Jellycord as a dependency in your Gradle build (replace with your published version once released):

```gradle
implementation 'com.github.cybellereaper:jellycord:1.0.0'
```

## Quick Start

```java
String token = System.getenv("DISCORD_BOT_TOKEN");

DiscordClientConfig config = DiscordClientConfig.builder(token)
        .intents(GatewayIntent.combine(GatewayIntent.GUILDS, GatewayIntent.GUILD_MESSAGES))
        .build();

try (DiscordClient client = DiscordClient.create(config)) {
    client.onSlashCommand("ping", interaction -> client.respondWithMessage(interaction, "pong"));

    client.registerGlobalSlashCommand("ping", "Reply with pong");
    client.login();
    Thread.currentThread().join();
}
```

## REST API Helper

Use `client.api()` for convenient access to common REST resources:

```java
JsonNode currentUser = client.api().getCurrentUser();
JsonNode channel = client.api().getChannel("1234567890");
client.api().deleteMessage("1234567890", "9876543210");
```

For custom calls, use:

```java
JsonNode response = client.api().request("GET", "/guilds/1234567890", null);
```

## Running tests

```bash
./gradlew test
```
