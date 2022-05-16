package com.pequla.dlaw.module;

import com.pequla.dlaw.DLAW;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

public class ChatModule extends ListenerAdapter implements Listener {

    private final DLAW plugin;

    public ChatModule(DLAW plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        User author = event.getAuthor();
        Message message = event.getMessage();

        // In these cases we reject the check
        if (author.isBot() || author.isSystem() || message.isWebhookMessage()) {
            return;
        }

        String content = message.getContentStripped();
        if (content.isBlank()) {
            return;
        }

        TextChannel channel = event.getChannel();
        Server server = plugin.getServer();
        FileConfiguration config = plugin.getConfig();

        // Chat channel
        if (channel.getIdLong() == config.getLong("discord.channel.chat")) {
            server.broadcastMessage(ChatColor.LIGHT_PURPLE + author.getAsTag() + ": " +
                    ChatColor.WHITE + content);
            return;
        }

        // Bot channel
        if (channel.getIdLong() == config.getLong("discord.channel.bot")) {
            if (content.equals(".ip")) {
                channel.sendMessage(MarkdownUtil.bold("mc.pequla.one")).queue();
                return;
            }
            if (content.equals(".help")) {
                channel.sendMessage("In order to join please make sure you have linked your accounts using the command !verify. If that doesn't help make sure to contact staff").queue();
                return;
            }
            if (content.equals(".status")) {
                channel.sendMessage("There is currently **" + server.getOnlinePlayers().size() + "** out of **" + server.getMaxPlayers() + "** players online").queue();
                return;
            }
            if (content.equals(".seed")) {
                channel.sendMessage(String.valueOf(server.getWorlds().get(0).getSeed())).queue();
            }
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        new Thread(() -> {
            Player player = event.getPlayer();
            FileConfiguration config = plugin.getConfig();
            TextChannel channel = plugin.getJda().getTextChannelById(config.getLong("discord.channel.chat"));
            if (channel != null) {
                channel.sendMessageEmbeds(new EmbedBuilder()
                        .setAuthor(player.getName(), null, plugin.getMinecraftAvatarUrl(player))
                        .setDescription(MarkdownSanitizer.sanitize(event.getMessage()))
                        .build()).queue();
            }
        }).start();
    }
}
