package com.pequla.dlaw.module.command;

import com.pequla.dlaw.DLAW;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.MarkdownUtil;

@RequiredArgsConstructor
public class IpCommand implements SlashCommand {

    private final DLAW plugin;

    @Override
    public void execute(SlashCommandEvent event) {
        String address = plugin.getConfig().getString("minecraft.address");
        event.getHook().sendMessage("IP: " + MarkdownUtil.bold(address)).queue();
    }

    @Override
    public CommandData getCommandData() {
        return new CommandData("ip", "Shows server address");
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
