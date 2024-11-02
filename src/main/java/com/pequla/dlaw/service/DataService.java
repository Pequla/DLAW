package com.pequla.dlaw.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pequla.dlaw.PluginUtils;
import com.pequla.dlaw.model.PlayerData;
import com.pequla.dlaw.model.backend.BanModel;
import com.pequla.dlaw.model.backend.DataModel;
import com.pequla.dlaw.model.backend.ErrorModel;
import com.pequla.dlaw.model.backend.LinkModel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Getter
public class DataService {

    private static DataService instance;
    private static ObjectNode advancements;

    private final HttpClient client;
    private final ObjectMapper mapper;

    private DataService() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        // Register json mapper
        this.mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static DataService getInstance() {
        if (instance == null) {
            instance = new DataService();
        }
        return instance;
    }

    public DataModel getData(String uuid) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://link.samifying.com/api/data/uuid/" + PluginUtils.cleanUUID(uuid)))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        HttpResponse<String> json = client.send(req, HttpResponse.BodyHandlers.ofString());
        handleStatusCode(json);
        return mapper.readValue(json.body(), DataModel.class);
    }

    public BanModel getBanByUserDiscordId(String id) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://link.samifying.com/api/ban/user/" + id))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        HttpResponse<String> json = client.send(req, HttpResponse.BodyHandlers.ofString());
        handleStatusCode(json);
        return mapper.readValue(json.body(), BanModel.class);
    }

    public PlayerData getAccount(String name) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://link.samifying.com/api/cache/name/" + name))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        HttpResponse<String> json = client.send(req, HttpResponse.BodyHandlers.ofString());
        handleStatusCode(json);
        return mapper.readValue(json.body(), PlayerData.class);
    }

    public DataModel saveData(LinkModel model, String user, String token) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://link.samifying.com/api/data"))
                .header("Content-Type", "application/json")
                .header("x-user", user)
                .header("x-token", token)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(model)))
                .build();
        HttpResponse<String> json = client.send(req, HttpResponse.BodyHandlers.ofString());
        handleStatusCode(json);
        return mapper.readValue(json.body(), DataModel.class);
    }

    public void deleteData(String userId, String user, String token) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://link.samifying.com/api/data/discord/" + userId))
                .header("x-user", user)
                .header("x-token", token)
                .DELETE()
                .build();
        handleStatusCode(client.send(req, HttpResponse.BodyHandlers.ofString()));
    }

    private void handleStatusCode(@NotNull HttpResponse<String> rsp) throws JsonProcessingException {
        int code = rsp.statusCode();
        if (code == 200 || code == 204) {
            return;
        }
        if (code == 500) {
            ErrorModel model = mapper.readValue(rsp.body(), ErrorModel.class);
            throw new RuntimeException(model.getMessage());
        }
        throw new RuntimeException("Response code " + code);
    }

    public ObjectNode getAdvancements() throws IOException {
        if (advancements == null) {
            InputStream is = DataService.class.getClassLoader().getResourceAsStream("advancements.json");
            advancements = mapper.readValue(is, ObjectNode.class);
        }
        return advancements;
    }
}
