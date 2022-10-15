package com.pequla.dlaw;

import com.pequla.dlaw.model.DiscordModel;
import com.pequla.dlaw.module.*;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public final class DLAW extends JavaPlugin {

    private final Map<UUID, DiscordModel> players = new HashMap<>();
    private JDA jda;

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();
            ChatModule chatModule = new ChatModule(this);

            jda = JDABuilder.createDefault(getConfig().getString("discord.token"))
                    .setActivity(Activity.playing("Minecraft"))
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .enableIntents(GatewayIntent.DIRECT_MESSAGES)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES)
                    .addEventListeners(chatModule)
                    .addEventListeners(new CommandModule(this))
                    .build();
            try {
                jda.awaitReady();
                sendSystemEmbed("Server starting");
                getLogger().info("Successfully connected to Discord API");
                SelfUser bot = jda.getSelfUser();
                getLogger().info("Name: " + bot.getName());
                getLogger().info("ID: " + bot.getId());
                getLogger().info("Servers: " + jda.getGuilds().size());
            } catch (InterruptedException e) {
                handleException(e);
            }

            // Setting up REST API
            new Thread(new RestModule(this)).start();

            // Registering event listeners
            PluginManager manager = getServer().getPluginManager();
            manager.registerEvents(chatModule, this);
            manager.registerEvents(new JoinModule(this), this);
            manager.registerEvents(new OtherModule(this), this);

        } catch (LoginException e) {
            handleException(e);
        }
    }

    @Override
    public void onDisable() {
        if (jda != null) {
            sendSystemEmbed("Server stopped");
            getLogger().info("Disconnecting from Discord API");
            jda.shutdown();
        }

        // Cleanup
        players.clear();
    }

    public void handleException(Exception e) {
        getLogger().severe(e.getClass().getName() + ": " + e.getMessage());
    }

    public void sendLogEmbed(EmbedBuilder builder) {
        TextChannel channel = jda.getTextChannelById(getConfig().getLong("discord.channel.chat"));
        if (channel != null) {
            channel.sendMessageEmbeds(builder.setTimestamp(Instant.now()).build()).queue();
        }
    }

    public void sendSystemEmbed(String text) {
        sendLogEmbed(new EmbedBuilder()
                .setColor(getConfig().getInt("color.system"))
                .setDescription(MarkdownUtil.bold(text)));
    }

    public void sendPlayerEmbed(Player player, String colorPath, EmbedBuilder builder) {
        if (players.containsKey(player.getUniqueId())) {
            DiscordModel model = players.get(player.getUniqueId());
            sendLogEmbed(builder.setColor(getConfig().getInt(colorPath))
                    .setAuthor(model.getName(), null, model.getAvatar())
                    .setThumbnail(getMinecraftAvatarUrl(player))
                    .setFooter(model.getId()));
        }
    }

    public String getMinecraftAvatarUrl(Player player) {
        return "https://visage.surgeplay.com/face/" + PluginUtils.cleanUUID(player.getUniqueId());
    }
}
