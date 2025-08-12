package it.overzoom.taf.service;

import java.io.IOException;
import java.util.Map;

public interface FcmNotificationService {

    boolean sendNotification(String targetToken, String title, String body, Map<String, String> data)
            throws IOException;
}
