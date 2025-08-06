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

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class FcmNotificationServiceImpl implements FcmNotificationService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FcmNotificationServiceImpl.class);

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
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            log.debug("FCM response: " + responseBody);
            if (response.isSuccessful()) {
                // Notifica inviata correttamente!
                return true;
            } else {
                // Qui puoi analizzare il responseBody per capire se il problema è il token
                // scaduto/non valido
                if (responseBody.contains("UNREGISTERED") || responseBody.contains("INVALID_ARGUMENT")) {
                    // Qui potresti gestire la pulizia del token nel DB o lanciare una eccezione
                    // custom
                    // Esempio: throw new InvalidFcmTokenException("Token non valido o non
                    // registrato");
                }
                // Logga sempre il responseBody per debug!
                System.err.println("Errore invio FCM: " + responseBody);
                return false;
            }
        }
    }

}