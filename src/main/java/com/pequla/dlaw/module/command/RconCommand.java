package com.pequla.dlaw.module.command;

import com.pequla.dlaw.DLAW;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

import java.awt.*;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class RconCommand implements SlashCommand {

    private final DLAW plugin;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Server server = plugin.getServer();
        Logger logger = plugin.getLogger();

        OptionMapping option = event.getOption("command");
        if (option == null) {
            throw new RuntimeException("Minecraft command is a requited command argument");
        }

        String command = option.getAsString();
        server.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            CommandSender sender = server.getConsoleSender();
            if (server.dispatchCommand(sender, command)) {
                // Execution successful
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle(MarkdownUtil.bold("Successfully executed command"))
                        .setDescription("Command `" + command + "` executed")
                        .build()).queue();
                logger.info("Executed command: " + command);
                return;
            }

            // Execution failed
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle(MarkdownUtil.bold("Command execution failed"))
                    .setDescription("Command `" + command + "` could not be executed")
                    .build()).queue();
            logger.info("Failed executing command: " + command);
        });
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("rcon", "Executes a command on the server").addOptions(
                new OptionData(OptionType.STRING,
                        "command",
                        "Minecraft command",
                        true)
        );
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }
}
