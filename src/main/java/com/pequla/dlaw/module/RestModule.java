package com.pequla.dlaw.module;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pequla.dlaw.DLAW;
import com.pequla.dlaw.PluginUtils;
import com.pequla.dlaw.model.*;
import com.pequla.dlaw.model.backend.DataModel;
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

import javax.security.auth.login.LoginException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RestModule implements Runnable {

    private final DLAW main;
    private final Server server;
    private final FileConfiguration config;
    private final ObjectMapper mapper;

    private static final Pattern UUID_WITHOUT_HYPHENS = Pattern.compile("^[0-9a-fA-F]{32}$");

    public RestModule(DLAW main) {
        this.main = main;
        this.server = main.getServer();
        this.config = main.getConfig();
        this.mapper = DataService.getInstance().getMapper();
    }

    @Override
    public void run() {
        // Check if enabled
        if (!config.getBoolean("api.enable")) return;

        main.getLogger().info("Enabling REST API");
        Spark.port(config.getInt("api.port"));
        Spark.after((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "*");
            response.type("application/json");
        });

        Spark.get("/api/auth/:uuid", (request, response) -> {
            try {
                String uuid = request.params("uuid");
                main.getLogger().info("REST API Authenticating player " + uuid);
                DiscordModel auth = PluginUtils.authenticatePlayer(main, uuid);
                return mapper.writeValueAsString(auth);
            } catch (Exception e) {
                if (e instanceof LoginException) {
                    response.status(401);
                    return generateError(e.getMessage());
                }

                response.status(400);
                return generateError("Bad Request");
            }
        });

        Spark.get("/api/members", ((request, response) -> {
            String id = config.getString("discord.guild");
            Guild guild = main.getJda().getGuildById(id);
            if (guild == null) {
                response.status(500);
                generateError("Guild not found");
            }
            return mapper.writeValueAsString(guild.getMembers().stream()
                    .filter(m -> !m.getUser().isBot())
                    .map(m -> MemberModel.builder()
                            .id(m.getId())
                            .name(m.getEffectiveName())
                            .joinedAt(m.getTimeJoined().toLocalDateTime().toString())
                            .build())
                    .collect(Collectors.toList()));
        }));

        Spark.get("/api/players", ((request, response) ->
                mapper.writeValueAsString(Arrays.stream(main.getServer().getOfflinePlayers()).map(p ->
                        PlayerData.builder()
                                .id(p.getUniqueId().toString())
                                .name(p.getName())
                                .build()).collect(Collectors.toList()))));

        Spark.get("/api/players/:uuid", (request, response) -> {
            try {
                UUID uuid = parseUUID(request.params("uuid"));
                PlayerData player = Arrays.stream(main.getServer().getOfflinePlayers())
                        .filter(p -> p.getUniqueId().equals(uuid))
                        .map(p -> PlayerData.builder()
                                .id(p.getUniqueId().toString())
                                .name(p.getName())
                                .build())
                        .findFirst()
                        .orElse(null);

                if (player == null) {
                    response.status(404);
                    return generateError("Player not found");
                }
                return mapper.writeValueAsString(player);

            } catch (IllegalArgumentException e) {
                response.status(400);
                return generateError("Invalid UUID format");
            }
        });

        Spark.get("/api/status", (request, response) ->
                mapper.writeValueAsString(ServerStatus.builder()
                        .players(getPlayerStatus())
                        .plugins(getPluginData())
                        .world(getWorldData())
                        .version(server.getVersion())
                        .build()));

        Spark.get("/api/status/players", (request, response) ->
                mapper.writeValueAsString(getPlayerStatus()));

        Spark.get("/api/status/plugins", (request, response) ->
                mapper.writeValueAsString(getPluginData()));

        Spark.get("/api/status/world", (request, response) ->
                mapper.writeValueAsString(getWorldData()));

        Spark.get("/api/user/:uuid", (request, response) -> {
            try {
                DataService service = DataService.getInstance();
                UUID uuid = parseUUID(request.params("uuid"));

                // No data found in cache
                DiscordModel model = main.getPlayers().get(uuid);
                if (model == null) {
                    Guild guild = main.getJda().getGuildById(config.getLong("discord.guild"));
                    if (guild == null) {
                        response.status(404);
                        return generateError("Discord server not found");
                    }
                    DataModel data = service.getData(uuid.toString());
                    Member member = guild.retrieveMemberById(data.getUser().getDiscordId()).complete();
                    if (member == null) {
                        response.status(404);
                        return generateError("Member not found");
                    }
                    return service.getMapper().writeValueAsString(DiscordModel.builder()
                            .id(member.getId())
                            .name(MarkdownSanitizer.sanitize(member.getUser().getEffectiveName()))
                            .nickname(MarkdownSanitizer.sanitize(member.getEffectiveName()))
                            .avatar(member.getEffectiveAvatarUrl())
                            .build());
                }
                return service.getMapper().writeValueAsString(model);
            } catch (IllegalArgumentException e) {
                response.status(400);
                return generateError("Invalid UUID format");
            }
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
            list.add(PlayerData.builder()
                    .id(player.getUniqueId().toString())
                    .name(player.getName())
                    .build());
        });

        return PlayerStatus.builder()
                .max(server.getMaxPlayers())
                .online(list.size())
                .list(list)
                .build();
    }

    private List<PluginData> getPluginData() {
        return Arrays.stream(server.getPluginManager().getPlugins())
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
    }

    private WorldData getWorldData() {
        World world = server.getWorlds().get(0);
        return WorldData.builder()
                .seed(String.valueOf(world.getSeed()))
                .time(world.getTime())
                .type(server.getWorldType())
                .build();
    }

    private UUID parseUUID(String uuidStr) {
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException ignored) {
        }

        if (UUID_WITHOUT_HYPHENS.matcher(uuidStr).matches()) {
            String formattedUUID = uuidStr.replaceFirst(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                    "$1-$2-$3-$4-$5"
            );
            return UUID.fromString(formattedUUID);
        }

        throw new IllegalArgumentException("Invalid UUID format");
    }
}
