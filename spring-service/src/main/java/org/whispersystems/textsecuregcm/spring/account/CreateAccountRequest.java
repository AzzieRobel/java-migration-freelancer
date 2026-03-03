package org.whispersystems.textsecuregcm.spring.account;

public record CreateAccountRequest(String phoneNumber, boolean discoverableByPhoneNumber) {
}

