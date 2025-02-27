package com.pequla.dlaw.module;

import com.pequla.dlaw.DLAW;
import com.pequla.dlaw.PluginUtils;
import com.pequla.dlaw.model.DiscordModel;
import com.pequla.dlaw.model.backend.BanModel;
import com.pequla.dlaw.model.backend.DataModel;
import com.pequla.dlaw.service.DataService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class JoinModule implements Listener {

    private final DLAW plugin;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLoginEvent(@NotNull PlayerLoginEvent event) {
        Server server = plugin.getServer();
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();

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
            DataService service = DataService.getInstance();
            DataModel model = service.getData(PluginUtils.cleanUUID(player.getUniqueId()));

            // Check if user is globally banned
            if (config.getBoolean("discord.include-global-bans")) {
                try {
                    BanModel ban = service.getBanByUserDiscordId(model.getUser().getDiscordId());
                    String reason = ban.getReason();
                    if (reason == null) reason = "You have been globally banned";

                    plugin.getLogger().info("Player banned: " + reason);
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, reason);
                    return;
                } catch (Exception ignored) {
                    plugin.getLogger().info("No global ban found for player");
                }
            }

            Guild guild = plugin.getJda().getGuildById(config.getLong("discord.guild"));
            if (guild == null) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Discord server not found");
                return;
            }

            // Getting player as a discord member
            Member member = guild.retrieveMemberById(model.getUser().getDiscordId()).complete();

            if (config.getBoolean("discord.role.join.enabled")) {
                if (member.getRoles().stream().noneMatch(role -> role.getId().equals(config.getString("discord.role.join.id")))) {
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "You don't have the required role");
                    return;
                }
            }

            DiscordModel discord = DiscordModel.builder()
                    .id(member.getId())
                    .name(MarkdownSanitizer.sanitize(member.getUser().getEffectiveName()))
                    .nickname(MarkdownSanitizer.sanitize(member.getEffectiveName()))
                    .avatar(member.getEffectiveAvatarUrl())
                    .build();
            plugin.getPlayers().put(player.getUniqueId(), discord);
            plugin.getLogger().info(PluginUtils.cleanName(player) + " authenticated as: " + member.getEffectiveName() + " [ID: " + member.getId() + "]");

            // Assign verified role
            String role = config.getString("discord.role.verified");
            PluginUtils.addRoleToMember(member, role);

        } catch (ErrorResponseException re) {
            if (re.getErrorResponse() == ErrorResponse.UNKNOWN_MEMBER) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "You are not a member of the Discord server");
                return;
            }
            if (re.getErrorResponse() == ErrorResponse.UNKNOWN_USER) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Discord user not found");
            }
        } catch (Exception e) {
            // On any error player will get kicked
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "You haven't linked your account");
            plugin.handleException(e);
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
