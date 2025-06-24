package it.overzoom.taf.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class NotificationDTO extends BaseDTO {
    @NotNull(message = "Il messaggio non può essere nullo")
    @Schema(description = "Il messaggio della notifica", example = "Hai un nuovo messaggio!")
    private String message;

    @NotNull(message = "Il destinatario non può essere nullo")
    @Schema(description = "ID del destinatario della notifica", example = "user123")
    private String recipientId;

    @Schema(description = "ID del mittente della notifica", example = "admin456")
    private String senderId;

    @Schema(description = "Indica se la notifica è stata letta", example = "false")
    private boolean isRead = false;

    @Schema(description = "Timestamp della notifica (in formato UNIX)", example = "1685000000")
    private long timestamp = LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC);

    @NotNull(message = "Il comune non può essere nullo")
    @Schema(description = "ID del comune associato alla notifica", example = "6852b4b11170095376c87d96")
    private String municipalityId;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMunicipalityId() {
        return municipalityId;
    }

    public void setMunicipalityId(String municipalityId) {
        this.municipalityId = municipalityId;
    }
}
