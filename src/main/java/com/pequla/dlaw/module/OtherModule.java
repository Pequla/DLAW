package com.pequla.dlaw.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pequla.dlaw.DLAW;
import com.pequla.dlaw.service.DataService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

@RequiredArgsConstructor
public class OtherModule implements Listener {

    private static final InputStream ADVANCEMENTS = OtherModule.class.getClassLoader()
            .getResourceAsStream("advancements.json");
    private final DLAW plugin;

    @EventHandler
    public void onWorldLoadEvent(@NotNull WorldLoadEvent event) {
        new Thread(() -> {
            if (plugin.getServer().getWorlds().get(0).equals(event.getWorld())) {
                plugin.sendSystemEmbed("Loading the world");
            }
        }).start();
    }

    @EventHandler
    public void onServerLoadEvent(@NotNull ServerLoadEvent event) {
        new Thread(() -> {
            if (event.getType() == ServerLoadEvent.LoadType.STARTUP) {
                plugin.sendSystemEmbed("Server loaded");
            }
        }).start();
    }

    @EventHandler
    public void onPlayerDeathEvent(@NotNull PlayerDeathEvent event) {
        new Thread(() -> {
            Player player = event.getEntity();
            Location location = player.getLocation();

            // Send notification to guild
            plugin.sendPlayerEmbed(player, "color.death",
                    new EmbedBuilder().setDescription(MarkdownUtil.bold(event.getDeathMessage())));
            // Send death cords to player
            plugin.getJda().openPrivateChannelById(plugin.getPlayers().get(player.getUniqueId()).getId()).queue(ch -> ch.sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.GRAY)
                    .setThumbnail(plugin.getMinecraftAvatarUrl(player))
                    .addField("Death message:", event.getDeathMessage(), false)
                    .addField("World:", worldName(location.getWorld()), false)
                    .addField("X:", String.valueOf(location.getBlockX()), true)
                    .addField("Y:", String.valueOf(location.getBlockY()), true)
                    .addField("Z:", String.valueOf(location.getBlockZ()), true)
                    .setTimestamp(Instant.now())
                    .build()).queue());
        }).start();
    }

    @EventHandler
    public void onPlayerAdvancementDoneEvent(@NotNull PlayerAdvancementDoneEvent event) {
        new Thread(() -> {
            String[] advancement = event.getAdvancement().getKey().getKey().split("/");
            // Recipes should not be displayed
            if (advancement[0].equalsIgnoreCase("recipes")) {
                return;
            }

            ObjectMapper mapper = DataService.getInstance().getMapper();
            String category = advancement[0];
            String key = advancement[1];

            try {
                //minecraft:adventure/kill_a_mob
                ObjectNode node = mapper.readValue(ADVANCEMENTS, ObjectNode.class);
                String title = "advancements." + category + "." + key + ".title";
                String desc = "advancements." + category + "." + key + ".description";
                if (node.has(title) && node.has(desc)) {
                    Player player = event.getPlayer();
                    plugin.sendPlayerEmbed(player, "color.advancement", new EmbedBuilder()
                            .setDescription(MarkdownUtil.bold(player.getName() + " made an advancement"))
                            .addField(node.get(title).asText(), node.get(desc).asText(), false));
                }
            } catch (IOException e) {
                plugin.handleException(e);
            }
        }).start();
    }

    private String worldName(World world) {
        String name = world.getName();
        if (name.equals("world")) {
            return "Overworld";
        }
        if (name.equals("world_nether")) {
            return "Nether";
        }
        if (name.equals("world_the_end")) {
            return "The End";
        }
        return name;
    }
}
