package com.pequla.dlaw.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pequla.dlaw.ex.ServiceException;
import com.pequla.dlaw.model.DataModel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Getter
public class DataService {

    private static DataService instance;

    private final HttpClient client;
    private final ObjectMapper mapper;

    private DataService() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        // Register json mapper
        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static DataService getInstance() {
        if (instance == null) {
            instance = new DataService();
        }
        return instance;
    }

    public DataModel getLinkData(String uuid) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://link.samifying.com/api/data/uuid/" + uuid))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        HttpResponse<String> tokenRsp = client.send(req, HttpResponse.BodyHandlers.ofString());
        handleStatusCode(tokenRsp);
        return mapper.readValue(tokenRsp.body(), DataModel.class);
    }

    private void handleStatusCode(@NotNull HttpResponse<String> rsp) throws ServiceException {
        int code = rsp.statusCode();
        if (code == 200 || code == 204) {
            return;
        }
        throw new ServiceException("Response code " + code);
    }
}
