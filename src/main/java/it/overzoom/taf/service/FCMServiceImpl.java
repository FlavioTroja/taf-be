package it.overzoom.taf.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.nimbusds.jose.shaded.gson.Gson;

import okhttp3.*;

@Service
public class FCMServiceImpl implements FCMService {

    @Value("${firebase.service-account-file}")
    private Resource serviceAccount;

    @Value("${firebase.fcm-api-url}")
    private String fcmApiUrl;

    private String accessToken;
    private long tokenExpiration = 0;

    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    // Ottieni token di accesso per la service account
    private String getAccessToken() throws IOException {
        if (accessToken == null || System.currentTimeMillis() > tokenExpiration) {
            GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(serviceAccount.getInputStream())
                    .createScoped(List.of("https://www.googleapis.com/auth/firebase.messaging"));
            googleCredentials.refreshIfExpired();
            accessToken = googleCredentials.getAccessToken().getTokenValue();
            tokenExpiration = googleCredentials.getAccessToken().getExpirationTime().getTime() - 60_000; // -1 min
                                                                                                         // margine
        }
        return accessToken;
    }

    // Invia la notifica
    public boolean sendNotification(String targetToken, String title, String body, Map<String, String> data)
            throws IOException {
        String accessToken = getAccessToken();

        Map<String, Object> notification = Map.of(
                "title", title,
                "body", body);

        Map<String, Object> message = new HashMap<>();
        message.put("token", targetToken);
        message.put("notification", notification);
        if (data != null && !data.isEmpty())
            message.put("data", data);

        Map<String, Object> payload = Map.of("message", message);

        RequestBody requestBody = RequestBody.create(
                gson.toJson(payload), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(fcmApiUrl)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json; UTF-8")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        }
    }
}