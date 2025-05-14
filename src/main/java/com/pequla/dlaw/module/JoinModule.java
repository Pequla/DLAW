package com.pequla.dlaw.module;

import com.pequla.dlaw.DLAW;
import com.pequla.dlaw.PluginUtils;
import com.pequla.dlaw.model.DiscordModel;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;

@RequiredArgsConstructor
public class JoinModule implements Listener {

    private final DLAW plugin;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLoginEvent(@NotNull PlayerLoginEvent event) {
        Server server = plugin.getServer();
        Player player = event.getPlayer();

        // Whitelist is on and the player is not whitelisted
        if (server.hasWhitelist() && !player.isWhitelisted()) {
            // Player is not whitelisted
            return;
        }

        // Player is banned
        if (server.getBannedPlayers().stream().anyMatch(p -> p.getUniqueId().equals(player.getUniqueId()))) {
            return;
        }

        // Checking if player has been verified
        try {
            DiscordModel discord = PluginUtils.authenticatePlayer(plugin, player.getUniqueId().toString());
            plugin.getPlayers().put(player.getUniqueId(), discord);
            plugin.getLogger().info(String.format("%s authenticated as: %s [ID: %s]",
                    PluginUtils.cleanName(player),
                    discord.getNickname(),
                    discord.getId()
            ));
        } catch (LoginException e) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, e.getMessage());
        }
    }

    @EventHandler
    public void OnPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        sendMessage(player, "color.join", ChatColor.stripColor(event.getJoinMessage()), getOnline(), false);
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        sendMessage(player, "color.leave", ChatColor.stripColor(event.getQuitMessage()), getOnline() - 1, true);
    }

    private void sendMessage(Player player, String color, String title, int online, boolean quit) {
        int max = plugin.getServer().getMaxPlayers();
        new Thread(() -> {
            // Update bot activity
            String text = online + " online";
            if (online == 0) {
                text = "alone";
            }
            plugin.getJda().getPresence().setActivity(Activity.playing(text));

            // Send chat message
            plugin.sendPlayerEmbed(player, color, new EmbedBuilder()
                    .setDescription(MarkdownUtil.bold(title))
                    .addField("Online:", online + "/" + max, false));

            if (quit) {
                // Removing the player from cache
                plugin.getPlayers().remove(player.getUniqueId());
            }
        }).start();
    }

    private int getOnline() {
        return plugin.getServer().getOnlinePlayers().size();
    }
}
