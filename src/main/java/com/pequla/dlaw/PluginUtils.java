package com.pequla.dlaw;

import com.pequla.dlaw.model.DiscordModel;
import com.pequla.dlaw.model.backend.BanModel;
import com.pequla.dlaw.model.backend.DataModel;
import com.pequla.dlaw.service.DataService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.UUID;

public class PluginUtils {
    public static String cleanUUID(UUID uuid) {
        return cleanUUID(uuid.toString());
    }

    public static String cleanUUID(String uuid) {
        return uuid.replace("-", "");
    }

    public static String cleanName(Player player) {
        return ChatColor.stripColor(player.getName());
    }

    public static void addRoleToMember(Member member, String id) {
        if (member == null) return;
        if (id == null) return;

        // Member already has the role
        if (member.getRoles().stream().anyMatch(r -> r.getId().equals(id))) return;

        // Retrieve the role
        Role role = member.getGuild().getRoleById(id);
        if (role == null) return;

        // Assign the role
        member.getGuild().addRoleToMember(member, role).queue();
    }

    public static void removeRoleFromMember(Member member, String id) {
        if (member == null) return;
        if (id == null) return;

        // Member has the role
        if (member.getRoles().stream().anyMatch(r -> r.getId().equals(id))) {

            // Retrieve the role
            Role role = member.getGuild().getRoleById(id);
            if (role == null) return;

            member.getGuild().removeRoleFromMember(member, role).queue();
        }
    }

    public static String playerBustUrl(String uuid) {
        return "https://visage.surgeplay.com/bust/" + cleanUUID(uuid);
    }

    public static DiscordModel authenticatePlayer(DLAW plugin, String uuid) throws LoginException {
        try {
            FileConfiguration config = plugin.getConfig();
            DataService service = DataService.getInstance();
            DataModel model = service.getData(PluginUtils.cleanUUID(uuid));

            // Check if user is globally banned
            if (config.getBoolean("discord.include-global-bans")) {
                try {
                    BanModel ban = service.getBanByUserDiscordId(model.getUser().getDiscordId());
                    String reason = ban.getReason();
                    if (reason == null) reason = "You have been globally banned";

                    plugin.getLogger().info("Player banned: " + reason);
                    throw new LoginException(reason);
                } catch (Exception ignored) {
                    plugin.getLogger().info("No global ban found for player");
                }
            }

            Guild guild = plugin.getJda().getGuildById(config.getLong("discord.guild"));
            if (guild == null) {
                throw new LoginException("Discord server not found");
            }

            // Getting player as a discord member
            Member member = guild.retrieveMemberById(model.getUser().getDiscordId()).complete();

            if (config.getBoolean("discord.role.join.enabled")) {
                if (member.getRoles().stream().noneMatch(role -> role.getId().equals(config.getString("discord.role.join.id")))) {
                    throw new LoginException("You don't have the required role");
                }
            }

            // Assign verified role
            String role = config.getString("discord.role.verified");
            PluginUtils.addRoleToMember(member, role);

            return DiscordModel.builder()
                    .id(member.getId())
                    .name(MarkdownSanitizer.sanitize(member.getUser().getEffectiveName()))
                    .nickname(MarkdownSanitizer.sanitize(member.getEffectiveName()))
                    .avatar(member.getEffectiveAvatarUrl())
                    .build();

        } catch (ErrorResponseException re) {
            if (re.getErrorResponse() == ErrorResponse.UNKNOWN_MEMBER) {
                throw new LoginException("You are not a member of the Discord server");
            }
            if (re.getErrorResponse() == ErrorResponse.UNKNOWN_USER) {
                throw new LoginException("Discord user not found");
            }
        } catch (IOException | InterruptedException ex) {
            throw new LoginException("Backend unreachable");
        }

        throw new LoginException("Something went wrong");
    }
}
