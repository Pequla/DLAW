package com.pequla.dlaw.module.command;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface SlashCommand {
    void execute(SlashCommandEvent event);

    CommandData getCommandData();

    boolean isAdminOnly();
}
