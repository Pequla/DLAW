package com.pequla.dlaw.module.command;

import com.pequla.dlaw.DLAW;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

public class SeedCommand implements SlashCommand {

    @Override
    public void execute(SlashCommandInteractionEvent event, DLAW plugin) {
        String seed = String.valueOf(plugin.getServer().getWorlds().get(0).getSeed());
        event.getHook().sendMessage("Seed: " + MarkdownUtil.bold(seed)).queue();
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("seed", "Displays world seed");
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
