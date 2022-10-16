package com.pequla.dlaw.module.command;

import com.pequla.dlaw.service.DataService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class UnverifyCommand implements SlashCommand {

    @Override
    public void execute(SlashCommandEvent event) {
        try {
            User user = event.getUser();
            DataService.getInstance().deleteData(user.getId());
            event.getHook().sendMessage("You have successfully removed your verification!").queue();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public CommandData getCommandData() {
        return new CommandData("unverify", "Unlinks discord and minecraft account");
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
