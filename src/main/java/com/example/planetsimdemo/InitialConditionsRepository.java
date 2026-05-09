package com.example.planetsimdemo;

import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InitialConditionsRepository {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public List<String> listSystems(AuthSession session) throws Exception {
        String url = FirebaseClientConfig.FIRESTORE_BASE_URL
                + "/users/" + encode(session.uid()) + "/systems";

        HttpRequest request = authorizedRequest(session, URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new IOException("List systems failed: " + response.body());
        }

        return FirestoreJsonMapper.extractDocumentIds(response.body());
    }

    public void saveSystem(AuthSession session, String systemName, SolarSystemState state) throws Exception {
        String url = FirebaseClientConfig.FIRESTORE_BASE_URL
                + "/users/" + encode(session.uid())
                + "/systems/" + encode(systemName);

        String body = FirestoreJsonMapper.toFirestoreDocument(systemName, state);

        HttpRequest request = authorizedRequest(session, URI.create(url))
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("Save failed: " + response.body());
        }
    }

    public SolarSystemState loadSystem(AuthSession session, String systemName) throws Exception {
        String url = FirebaseClientConfig.FIRESTORE_BASE_URL
                + "/users/" + encode(session.uid())
                + "/systems/" + encode(systemName);

        HttpRequest request = authorizedRequest(session, URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            return new SolarSystemState();
        }
        if (response.statusCode() >= 400) {
            throw new IOException("Load failed: " + response.body());
        }

        return FirestoreJsonMapper.fromFirestoreDocument(response.body());
    }

    public void deleteSystem(AuthSession session, String systemName) throws Exception {
        String url = FirebaseClientConfig.FIRESTORE_BASE_URL
                + "/users/" + encode(session.uid())
                + "/systems/" + encode(systemName);

        HttpRequest request = authorizedRequest(session, URI.create(url)).DELETE().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new IOException("Delete failed: " + response.body());
        }
    }

    private HttpRequest.Builder authorizedRequest(AuthSession session, URI uri) {
        return HttpRequest.newBuilder(uri)
                .header("Authorization", "Bearer " + session.idToken())
                .header("Content-Type", "application/json");
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
