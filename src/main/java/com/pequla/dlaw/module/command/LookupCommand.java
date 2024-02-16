package com.pequla.dlaw.module.command;

import com.pequla.dlaw.DLAW;
import com.pequla.dlaw.PluginUtils;
import com.pequla.dlaw.model.PlayerData;
import com.pequla.dlaw.model.backend.DataModel;
import com.pequla.dlaw.service.DataService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.io.IOException;
import java.time.Instant;

@RequiredArgsConstructor
public class LookupCommand implements SlashCommand {

    private final DLAW plugin;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping option = event.getOption("minecraft-username");
        if (option == null) {
            throw new RuntimeException("Minecraft username is a required command argument");
        }

        try {
            String username = option.getAsString();
            PlayerData account = DataService.getInstance().getAccount(username);
            DataModel data = DataService.getInstance().getData(account.getId());

            event.getJDA().retrieveUserById(data.getUser().getDiscordId()).queue(user ->
                    event.getHook().sendMessage("Flowing data was found")
                            .addEmbeds(new EmbedBuilder()
                                    .setColor(plugin.getConfig().getInt("color.command"))
                                    .setTitle(MarkdownUtil.bold("Verification data"))
                                    .addField("User:", user.getEffectiveName(), false)
                                    .addField("Username:", account.getName(), false)
                                    .setThumbnail(user.getEffectiveAvatarUrl())
                                    .setImage(PluginUtils.playerBustUrl(account.getId()))
                                    .setTimestamp(Instant.now())
                                    .build()
                            ).queue());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("lookup", "Looks up a Minecraft account").addOptions(
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
