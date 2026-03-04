package org.whispersystems.textsecuregcm.spring.message;

import java.time.Instant;
import java.util.UUID;

public record MessageDto(
    UUID messageId,
    UUID senderId,
    UUID recipientId,
    Instant timestamp,
    String body,
    String status) {
}

