import com.github.cybellereaper.client.AutocompleteChoice;
import com.github.cybellereaper.client.DiscordActionRow;
import com.github.cybellereaper.client.DiscordButton;
import com.github.cybellereaper.client.DiscordClient;
import com.github.cybellereaper.client.DiscordClientConfig;
import com.github.cybellereaper.client.DiscordEmbed;
import com.github.cybellereaper.client.DiscordMessage;
import com.github.cybellereaper.client.DiscordPermissions;
import com.github.cybellereaper.client.DiscordSelectOption;
import com.github.cybellereaper.client.DiscordStringSelectMenu;
import com.github.cybellereaper.client.SlashCommandDefinition;
import com.github.cybellereaper.client.SlashCommandOptionDefinition;
import com.github.cybellereaper.gateway.GatewayIntent;

import java.util.List;

void main() throws Exception {
    String token = System.getenv("DISCORD_BOT_TOKEN");
    String guildId = System.getenv("DISCORD_GUILD_ID");

    DiscordClientConfig config = DiscordClientConfig.builder(token)
            .intents(GatewayIntent.combine(
                    GatewayIntent.GUILDS,
                    GatewayIntent.MESSAGE_CONTENT,
                    GatewayIntent.GUILD_MESSAGES
            ))
            .build();

    try (DiscordClient client = DiscordClient.create(config)) {
        List<SlashCommandDefinition> commands = List.of(
                SlashCommandDefinition.simple("ping", "Reply with pong")
                        .withDefaultMemberPermissions(DiscordPermissions.of(
                                DiscordPermissions.SEND_MESSAGES,
                                DiscordPermissions.USE_APPLICATION_COMMANDS
                        )),
                new SlashCommandDefinition(
                        "echo",
                        "Echo input text back",
                        List.of(SlashCommandOptionDefinition.autocompletedString("text", "Text to echo", true))
                ),
                SlashCommandDefinition.userContextMenu("Inspect User"),
                SlashCommandDefinition.messageContextMenu("Quote Message")
        );

        if (guildId == null || guildId.isBlank()) {
            client.registerGlobalSlashCommands(commands);
        } else {
            client.registerGuildSlashCommands(guildId, commands);
        }

        client.on("MESSAGE_CREATE", message -> {
            String content = message.path("content").asText("");
            String channelId = message.path("channel_id").asText();

            if ("!ping".equals(content)) {
                DiscordActionRow buttons = DiscordActionRow.of(List.of(
                        DiscordButton.primary("confirm_button", "Confirm").withEmoji("✅"),
                        DiscordButton.link("https://discord.com/developers/docs/interactions", "Docs")
                ));

                client.sendMessage(channelId, DiscordMessage.ofEmbeds("pong", List.of(
                                new DiscordEmbed("Legacy Ping", "Handled via message command", 0x57F287)
                                        .withImage("https://images.unsplash.com/photo-1516117172878-fd2c41f4a759")
                        ))
                        .withComponents(List.of(buttons)));
            }
        });

        client.onSlashCommandContext("ping", context -> {
            DiscordActionRow selectMenuRow = DiscordActionRow.of(List.of(
                    DiscordStringSelectMenu.of("theme_select", List.of(
                                    DiscordSelectOption.of("Light", "light"),
                                    DiscordSelectOption.of("Dark", "dark").asDefault(),
                                    DiscordSelectOption.of("System", "system")
                            ))
                            .withPlaceholder("Choose a theme")
                            .withSelectionRange(1, 1)
            ));

            context.respondWithMessage(DiscordMessage.ofEmbeds("pong", List.of(
                            new DiscordEmbed("Slash Ping", "Interaction response", 0x5865F2)
                                    .withThumbnail("https://cdn.discordapp.com/embed/avatars/0.png")
                    ))
                    .withComponents(List.of(selectMenuRow)));
        });

        client.onSlashCommandContext("echo", context -> {
            String text = context.optionString("text");
            if (text == null || text.isBlank()) {
                context.respondEphemeral("Missing required option: text");
                return;
            }

            context.respondWithEmbeds(text, List.of(
                    new DiscordEmbed("Echo", text, 0xFEE75C)
                            .withUrl("https://discord.com/developers/docs")
            ));
        });

        client.onAutocompleteContext("echo", context -> {
            String prefix = context.optionString("text");
            String safePrefix = prefix == null ? "" : prefix.toLowerCase();
            List<AutocompleteChoice> choices = List.of("hello", "hey", "hola", "bonjour").stream()
                    .filter(choice -> choice.startsWith(safePrefix))
                    .limit(25)
                    .map(choice -> new AutocompleteChoice(choice, choice))
                    .toList();

            context.respondWithAutocompleteChoices(choices);
        });

        client.onUserContextMenuContext("Inspect User", context ->
                context.respondEphemeral("User inspection invoked."));

        client.onMessageContextMenuContext("Quote Message", context ->
                context.respondEphemeral("Message quote command invoked."));

        client.onComponentInteractionContext("confirm_button", context ->
                context.respondEphemeral("Confirmed!"));

        client.onComponentInteractionContext("theme_select", context ->
                context.respondEphemeral("Theme updated."));

        client.onModalSubmitContext("feedback_modal", context ->
                context.respondEphemeralWithEmbeds("Thanks for the feedback!", List.of(
                        new DiscordEmbed("Feedback", "Received successfully", 0x57F287)
                                .withThumbnail("https://cdn.discordapp.com/embed/avatars/1.png")
                )));

        client.login();
        Thread.currentThread().join();
    }
}
