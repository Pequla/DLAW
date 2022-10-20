package com.pequla.dlaw.module.command;

import com.pequla.dlaw.PluginUtils;
import com.pequla.dlaw.model.PlayerData;
import com.pequla.dlaw.model.backend.DataModel;
import com.pequla.dlaw.model.backend.LinkModel;
import com.pequla.dlaw.service.DataService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.awt.*;
import java.time.Instant;

public class VerifyCommand implements SlashCommand {

    @Override
    public void execute(SlashCommandEvent event) {
        OptionMapping option = event.getOption("minecraft-username");
        if (option == null) {
            throw new RuntimeException("Minecraft username is a required command argument");
        }
        try {
            DataService service = DataService.getInstance();
            PlayerData account = service.getAccount(option.getAsString());
            User user = event.getUser();
            DataModel data = service.saveData(LinkModel.builder()
                    .uuid(account.getId())
                    .userId(user.getId())
                    .guildId(event.getGuild().getId())
                    .build());

            event.getHook().sendMessage(user.getAsMention() + " You have successfully linked your minecraft account")
                    .addEmbeds(new EmbedBuilder()
                            .setColor(Color.GRAY)
                            .setTitle(MarkdownUtil.bold("Verification data"))
                            .addField("User:", user.getAsTag(), false)
                            .addField("Username:", account.getName(), false)
                            .setThumbnail(user.getEffectiveAvatarUrl())
                            .setImage("https://visage.surgeplay.com/bust/" + PluginUtils.cleanUUID(account.getId()))
                            .setTimestamp(Instant.now())
                            .setFooter("Database ID: " + data.getId())
                            .build()
                    ).queue();
        } catch (Exception ex) {
            // Return it back
            throw new RuntimeException(ex);
        }
    }

    @Override
    public CommandData getCommandData() {
        return new CommandData("verify", "Links a Minecraft account").addOptions(
                new OptionData(OptionType.STRING,
                        "minecraft-username",
                        "Accepts only Minecraft Java usernames from paid accounts",
                        true)
        );
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
