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
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class StatusCommand implements SlashCommand {

    private final DLAW plugin;

    @Override
    public void execute(SlashCommandEvent event) {
        Server server = plugin.getServer();
        List<Player> players = new ArrayList<>(server.getOnlinePlayers());
        event.getHook().sendMessageEmbeds(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle(MarkdownUtil.bold("Server status"))
                .addField("Online:", String.valueOf(players.size()), true)
                .addField("Max:", String.valueOf(server.getMaxPlayers()), true)
                .addField("List:", players.toString(), false)
                .build()).queue();
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
