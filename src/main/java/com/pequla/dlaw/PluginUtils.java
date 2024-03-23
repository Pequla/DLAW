package com.pequla.dlaw;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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
}
