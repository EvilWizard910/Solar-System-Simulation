package com.example.planetsimdemo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FirebaseAuthenticationService {
    private static final String BASE_URL = "https://identitytoolkit.googleapis.com/v1/accounts:";
    private final HttpClient httpClient= HttpClient.newHttpClient();

    public AuthSession signIn(String email, String password) throws IOException, InterruptedException {
        String url = BASE_URL + "signInWithPassword?key=" + FirebaseClientConfig.WEB_API_KEY;
        String json= """
                {
                "email": "%s",
                "password": "%s",
                "returnSecureToken": true
                }
                """.formatted(escape(email),escape(password));

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type","application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new IOException("Sign in failed " + response.body());
        }
        return parseAuthSession(response.body());
    }

    public AuthSession signUp(String email, String password) throws IOException, InterruptedException {
        String url = BASE_URL + "signUp?key=" + FirebaseClientConfig.WEB_API_KEY;
        String json= """
                {
                "email": "%s",
                "password": "%s",
                "returnSecureToken": true
                }
        """.formatted(escape(email),escape(password));
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type","application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new IOException("Sign up failed " + response.body());
        }
        return  parseAuthSession(response.body());
    }
    private AuthSession parseAuthSession(String json) throws IOException{
        String uid = extract(json,"localId");
        String email = extract(json,"email");
        String idToken = extract(json,"idToken");
        String refreshToken = extract(json,"refreshToken");
        String expiresIn = extract(json,"expiresIn");

        if (uid == null || idToken == null || refreshToken == null){
            throw new IOException("Invalid Firebase Authentication Response"+json);
        }
        long expires = expiresIn == null ? 0 : Long.parseLong(expiresIn);
        return new AuthSession(uid,email,idToken,refreshToken,expires);
    }

    private String extract(String json, String field){
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1): null;
    }

    private String escape(String value){
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
