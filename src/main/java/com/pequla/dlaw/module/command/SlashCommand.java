package com.pequla.dlaw.module.command;

import com.pequla.dlaw.DLAW;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface SlashCommand {
    void execute(SlashCommandInteractionEvent event, DLAW plugin);

    CommandData getCommandData();

    boolean isAdminOnly();
}
