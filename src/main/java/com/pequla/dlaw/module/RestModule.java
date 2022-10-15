package com.pequla.dlaw.module;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pequla.dlaw.DLAW;
import com.pequla.dlaw.model.*;
import com.pequla.dlaw.service.DataService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import spark.Spark;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RestModule implements Runnable{

    private final DLAW main;
    private final Server server;
    private final FileConfiguration config;

    public RestModule(DLAW main) {
        this.main = main;
        this.server = main.getServer();
        this.config = main.getConfig();
    }

    @Override
    public void run() {
        // Check if enabled
        if (!config.getBoolean("api.enable")) return;

        DataService service = DataService.getInstance();
        Spark.port(config.getInt("api.port"));
        Spark.after((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "*");
            response.type("application/json");
        });

        Spark.get("/api/status", (request, response) -> {
            List<PluginData> plugins = Arrays.stream(server.getPluginManager().getPlugins())
                    .map(plugin -> {
                        PluginDescriptionFile desc = plugin.getDescription();
                        return PluginData.builder()
                                .name(plugin.getName())
                                .version(desc.getVersion())
                                .authors(desc.getAuthors())
                                .description(desc.getDescription())
                                .website(desc.getWebsite())
                                .build();
                    })
                    .collect(Collectors.toList());

            World world = server.getWorlds().get(0);
            return service.getMapper().writeValueAsString(ServerStatus.builder()
                    .players(getPlayerStatus())
                    .plugins(plugins)
                    .world(WorldData.builder()
                            .seed(String.valueOf(world.getSeed()))
                            .time(world.getTime())
                            .type(server.getWorldType())
                            .build())
                    .version(server.getVersion())
                    .build());
        });

        Spark.get("/api/status/players", (request, response) ->
                service.getMapper().writeValueAsString(getPlayerStatus()));

        Spark.get("/api/user", (request, response) -> {
            String uuid = request.queryParams("uuid");
            if (uuid != null) {
                UUID converted;
                try {
                    converted = UUID.fromString(uuid);
                } catch (IllegalArgumentException ae) {
                    // Adding dashes to uuid string
                    try {
                        converted = UUID.fromString(uuid.replaceFirst(
                                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
                        ));
                    } catch (Exception ex) {
                        // Bad uuid
                        response.status(400);
                        return generateError("Invalid uuid");
                    }
                }

                // No data found in cache
                DiscordModel model = main.getPlayers().get(converted);
                if (model == null) {
                    Guild guild = main.getJda().getGuildById(config.getLong("discord.guild"));
                    if (guild == null) {
                        response.status(404);
                        return generateError("Discord server not found");
                    }
                    DataModel data = service.getLinkData(converted.toString());
                    Member member = guild.retrieveMemberById(data.getUser().getDiscordId()).complete();
                    if (member == null) {
                        response.status(404);
                        return generateError("Member not found");
                    }
                    return service.getMapper().writeValueAsString(DiscordModel.builder()
                            .id(member.getId())
                            .name(MarkdownSanitizer.sanitize(member.getEffectiveName()))
                            .avatar(member.getEffectiveAvatarUrl())
                            .build());
                }
                return service.getMapper().writeValueAsString(model);
            }
            response.status(400);
            return generateError("Required param uuid not found");
        });
    }

    public static String generateError(String error) throws JsonProcessingException {
        ObjectMapper mapper = DataService.getInstance().getMapper();
        return mapper.writeValueAsString(SparkError.builder()
                .message(error)
                .timestamp(System.currentTimeMillis())
                .build());
    }

    private PlayerStatus getPlayerStatus() {
        HashSet<PlayerData> list = new HashSet<>();
        server.getOnlinePlayers().forEach(player -> {
            PlayerData data = new PlayerData();
            data.setName(player.getName());
            data.setDisplayName(player.getDisplayName());
            data.setId(player.getUniqueId().toString());
            list.add(data);
        });

        return PlayerStatus.builder()
                .max(server.getMaxPlayers())
                .online(list.size())
                .list(list)
                .build();
    }
}
