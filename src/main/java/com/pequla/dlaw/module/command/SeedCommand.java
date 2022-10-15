package com.pequla.dlaw.module.command;

import com.pequla.dlaw.DLAW;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.MarkdownUtil;

@RequiredArgsConstructor
public class SeedCommand implements SlashCommand{

    private final DLAW plugin;

    @Override
    public void execute(SlashCommandEvent event) {
        String seed = String.valueOf(plugin.getServer().getWorlds().get(0).getSeed());
        event.getHook().sendMessage("Seed: "+ MarkdownUtil.bold(seed)).queue();
    }

    @Override
    public CommandData getCommandData() {
        return new CommandData("seed", "Displays world seed");
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
