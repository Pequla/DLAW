package com.pequla.dlaw;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pequla.dlaw.model.*;
import com.pequla.dlaw.module.ChatModule;
import com.pequla.dlaw.module.JoinModule;
import com.pequla.dlaw.module.OtherModule;
import com.pequla.dlaw.service.DataService;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import spark.Spark;

import javax.security.auth.login.LoginException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public final class DLAW extends JavaPlugin {

    private final Map<UUID, DiscordModel> players = new HashMap<>();
    private JDA jda;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ChatModule chatModule = new ChatModule(this);
        try {
            jda = JDABuilder.createDefault(getConfig().getString("discord.token"))
                    .setActivity(Activity.playing("Minecraft"))
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .addEventListeners(chatModule)
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

            // WEB API
            DataService service = DataService.getInstance();
            Spark.port(getConfig().getInt("api.port"));
            Spark.after((request, response) -> {
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "*");
                response.type("application/json");
            });

            Spark.get("/api/status", (request, response) -> {
                PlayerStatus status = new PlayerStatus();
                status.setMax(getServer().getMaxPlayers());

                HashSet<PlayerData> list = new HashSet<>();
                getServer().getOnlinePlayers().forEach(player -> {
                    PlayerData data = new PlayerData();
                    data.setName(player.getName());
                    data.setId(player.getUniqueId().toString());
                    list.add(data);
                });

                status.setOnline(list.size());
                status.setList(list);

                List<PluginData> plugins = Arrays.stream(getServer().getPluginManager().getPlugins())
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

                World world = getServer().getWorlds().get(0);
                WorldData wd = new WorldData();
                wd.setSeed(String.valueOf(world.getSeed()));
                wd.setTime(world.getTime());
                wd.setType(getServer().getWorldType());

                ServerStatus ss = new ServerStatus();
                ss.setPlayers(status);
                ss.setPlugins(plugins);
                ss.setWorld(wd);
                ss.setVersion(getServer().getVersion());
                return service.getMapper().writeValueAsString(ss);
            });

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
                    DiscordModel model = players.get(converted);
                    if (model == null) {
                        Guild guild = jda.getGuildById(getConfig().getLong("discord.guild"));
                        if (guild == null) {
                            response.status(404);
                            return generateError("Discord server not found");
                        }
                        DataModel data = service.getLinkData(converted.toString());
                        Member member = guild.retrieveMemberById(data.getDiscordId()).complete();
                        if (member == null) {
                            response.status(404);
                            return generateError("Member not found");
                        }
                        DiscordModel discord = DiscordModel.builder()
                                .id(member.getId())
                                .name(MarkdownSanitizer.sanitize(member.getEffectiveName()))
                                .avatar(member.getEffectiveAvatarUrl())
                                .build();
                        return service.getMapper().writeValueAsString(discord);
                    }
                    return service.getMapper().writeValueAsString(model);
                }
                response.status(400);
                return generateError("Required param uuid not found");
            });
        } catch (LoginException e) {
            handleException(e);
        }

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(chatModule, this);
        manager.registerEvents(new JoinModule(this), this);
        manager.registerEvents(new OtherModule(this), this);
    }

    @Override
    public void onDisable() {
        if (jda != null) {
            sendSystemEmbed("Server stopped");
            getLogger().info("Disconnecting from Discord API");
            jda.shutdown();
        }
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
                    .setFooter(model.getId(), getMinecraftAvatarUrl(player)));
        }
    }

    public String getMinecraftAvatarUrl(Player player) {
        return "https://visage.surgeplay.com/face/" + player.getUniqueId().toString().replace("-", "");
    }

    public static String generateError(String error) throws JsonProcessingException {
        ObjectMapper mapper = DataService.getInstance().getMapper();
        return mapper.writeValueAsString(SparkError.builder()
                .message(error)
                .timestamp(System.currentTimeMillis())
                .build());
    }
}
