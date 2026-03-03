package org.whispersystems.textsecuregcm.spring.account;

import java.time.Instant;
import java.util.UUID;

public record AccountDto(
    UUID id,
    String phoneNumber,
    boolean discoverableByPhoneNumber,
    Instant createdAt
) {
}

