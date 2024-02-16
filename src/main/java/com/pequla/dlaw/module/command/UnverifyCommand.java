package com.pequla.dlaw.module.command;

import com.pequla.dlaw.DLAW;
import com.pequla.dlaw.service.DataService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.bukkit.configuration.file.FileConfiguration;

public class UnverifyCommand implements SlashCommand {

    @Override
    public void execute(SlashCommandInteractionEvent event, DLAW plugin) {
        try {
            FileConfiguration config = plugin.getConfig();
            User user = event.getUser();
            DataService.getInstance().deleteData(
                    user.getId(),
                    config.getString("auth.user"),
                    config.getString("auth.token")
            );
            event.getHook().sendMessage("You have successfully removed your verification!").queue();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("unverify", "Unlinks discord and minecraft account");
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
