package com.pequla.dlaw.module;

import com.pequla.dlaw.DLAW;
import com.pequla.dlaw.module.command.*;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class CommandModule extends ListenerAdapter {

    private final Map<String, SlashCommand> commands = new HashMap<>();
    private final DLAW plugin;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        // Retrieve guild id
        String id = plugin.getConfig().getString("discord.guild");
        if (id == null) {
            throw new RuntimeException("Discord guild not set");
        }

        // Retrieve guild
        Guild guild = event.getJDA().getGuildById(id);
        if (guild == null) {
            throw new RuntimeException("Bot is not a member of the main guild");
        }

        // Adding commands
        registerCommand(new StatusCommand(plugin));
        registerCommand(new SeedCommand(plugin));
        registerCommand(new IpCommand(plugin));
        registerCommand(new VerifyCommand());
        registerCommand(new UnverifyCommand());

        // Upsert guild commands
        commands.values().forEach(
                command -> guild.upsertCommand(command.getCommandData()).queue()
        );
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        try {
            String name = event.getName();
            if (commands.containsKey(name)) {
                event.deferReply().queue();
                commands.get(name).execute(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle(MarkdownUtil.bold(e.getClass().getSimpleName()))
                    .setDescription(e.getMessage())
                    .setTimestamp(Instant.now())
                    .build()).queue();
        }
    }

    private void registerCommand(SlashCommand command) {
        plugin.getLogger().info("Registering command " + command.getClass().getSimpleName());
        commands.put(command.getCommandData().getName(), command);
    }
}
