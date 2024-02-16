package com.pequla.dlaw.module.command;

import com.pequla.dlaw.DLAW;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class StatusCommand implements SlashCommand {

    private final DLAW plugin;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Server server = plugin.getServer();
        FileConfiguration config = plugin.getConfig();
        String address = config.getString("minecraft.address");
        List<String> players = server.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(config.getInt("color.command"))
                .setTitle(MarkdownUtil.bold("Server status"))
                .setThumbnail("https://api.mcsrvstat.us/icon/" + address)
                .addField("Online:", String.valueOf(players.size()), true)
                .addField("Max:", String.valueOf(server.getMaxPlayers()), true)
                .addField("Version:", server.getVersion(), false)
                .setTimestamp(Instant.now());

        if (players.size() > 0) {
            builder.addField("List:", players.toString(), false);
        }

        // Send response
        event.getHook().sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("status", "Shows server status");
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
