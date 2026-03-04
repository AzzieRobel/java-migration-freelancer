package org.whispersystems.textsecuregcm.spring.message;

import java.util.UUID;

public record CreateMessageRequest(
    UUID senderId,
    UUID recipientId,
    String body) {
}

