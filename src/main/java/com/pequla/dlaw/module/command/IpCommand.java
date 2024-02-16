package com.pequla.dlaw.module.command;

import com.pequla.dlaw.DLAW;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

public class IpCommand implements SlashCommand {

    @Override
    public void execute(SlashCommandInteractionEvent event, DLAW plugin) {
        String address = plugin.getConfig().getString("minecraft.address");
        event.getHook().sendMessage("IP: " + MarkdownUtil.bold(address)).queue();
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("ip", "Shows server address");
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
