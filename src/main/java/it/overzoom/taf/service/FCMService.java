package it.overzoom.taf.service;

import java.io.IOException;
import java.util.Map;

public interface FCMService {

    boolean sendNotification(String targetToken, String title, String body, Map<String, String> data)
            throws IOException;
}
