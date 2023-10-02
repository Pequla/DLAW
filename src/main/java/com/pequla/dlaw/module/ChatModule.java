package com.pequla.dlaw.module;

import com.pequla.dlaw.DLAW;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
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
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
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

        TextChannel channel = event.getChannel().asTextChannel();
        Server server = plugin.getServer();
        FileConfiguration config = plugin.getConfig();

        // Chat channel
        if (channel.getIdLong() == config.getLong("discord.channel.chat")) {
            server.broadcastMessage(ChatColor.LIGHT_PURPLE + author.getEffectiveName() + ": " +
                    ChatColor.WHITE + content);
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        new Thread(() -> {
            Player player = event.getPlayer();
            FileConfiguration config = plugin.getConfig();
            TextChannel channel = plugin.getJda().getTextChannelById(config.getLong("discord.channel.chat"));
            if (channel != null) {
                channel.sendMessage(
                        MarkdownUtil.bold(player.getName()) + ": `" + MarkdownSanitizer.sanitize(event.getMessage()) + "`"
                ).queue();
            }
        }).start();
    }
}
