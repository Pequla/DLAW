package com.pequla.dlaw.module.command;

import com.pequla.dlaw.PluginUtils;
import com.pequla.dlaw.model.DiscordModel;
import com.pequla.dlaw.model.PlayerData;
import com.pequla.dlaw.service.DataService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;

public class LookupCommand implements SlashCommand {
    @Override
    public void execute(SlashCommandEvent event) {
        OptionMapping option = event.getOption("minecraft-username");
        if (option == null) {
            throw new RuntimeException("Minecraft username is a required command argument");
        }

        try {
            String username = option.getAsString();
            DiscordModel model = DataService.getInstance().lookupForName(username);
            PlayerData account = DataService.getInstance().getAccount(username);
            event.getHook().sendMessage("Flowing data was found")
                    .addEmbeds(new EmbedBuilder()
                            .setColor(Color.GRAY)
                            .setTitle(MarkdownUtil.bold("Verification data"))
                            .addField("User:", model.getName(), false)
                            .addField("Username:", account.getName(), false)
                            .setThumbnail(model.getAvatar())
                            .setImage("https://visage.surgeplay.com/bust/" + PluginUtils.cleanUUID(account.getId()))
                            .setTimestamp(Instant.now())
                            .build()
                    ).queue();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CommandData getCommandData() {
        return new CommandData("lookup", "Looks up a Minecraft account").addOptions(
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
