package com.pequla.dlaw.module;

import com.pequla.dlaw.DLAW;
import com.pequla.dlaw.module.command.*;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class CommandModule extends ListenerAdapter {

    private final Map<String, SlashCommand> commands = new HashMap<>();
    private final DLAW plugin;
    private Guild guild;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        // Retrieve guild id
        FileConfiguration config = plugin.getConfig();
        String id = config.getString("discord.guild");
        if (id == null) {
            throw new RuntimeException("Discord guild not set");
        }

        // Retrieve guild
        guild = event.getJDA().getGuildById(id);
        if (guild == null) {
            throw new RuntimeException("Bot is not a member of the main guild");
        }

        // Adding commands
        registerCommand(new StatusCommand());
        registerCommand(new SeedCommand());
        registerCommand(new IpCommand());
        registerCommand(new RconCommand());
        registerCommand(new LookupCommand());

        // Adding authenticated commands
        if (config.getBoolean("auth.enabled")) {
            registerCommand(new VerifyCommand());
            registerCommand(new UnverifyCommand());
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        try {
            String name = event.getName();
            if (commands.containsKey(name)) {
                event.deferReply().queue();

                SlashCommand command = commands.get(name);
                if (command.isAdminOnly()) {
                    Member member = event.getMember();
                    String role = plugin.getConfig().getString("discord.role.staff");
                    if (member != null && member.getRoles().stream().anyMatch(r -> r.getId().equals(role))) {
                        command.execute(event, plugin);
                        return;
                    }

                    // Not admin
                    throw new RuntimeException("You don't have the permission to use this command");
                }
                // Regular command
                command.execute(event, plugin);
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

    public void registerCommand(SlashCommand command) {
        plugin.getLogger().info("Registering command " + command.getClass().getSimpleName());
        commands.put(command.getCommandData().getName(), command);
        guild.upsertCommand(command.getCommandData()).queue();
    }
}
