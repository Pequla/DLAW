package com.pequla.dlaw.module.command;

import com.pequla.dlaw.DLAW;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class StatusCommand implements SlashCommand {

    private final DLAW plugin;

    @Override
    public void execute(SlashCommandEvent event) {
        Server server = plugin.getServer();
        String address = plugin.getConfig().getString("minecraft.address");
        List<String> players = server.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.GRAY)
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
        return new CommandData("status", "Shows server status");
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
