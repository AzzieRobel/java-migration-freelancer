package org.whispersystems.textsecuregcm.spring.account;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

// In-memory implementation of AccountService for development and testing.
@Service
public class StubAccountService implements AccountService {

  private static final class InMemoryAccount {
    private AccountDto dto;
    private String registrationLock;
    private String fcmRegistrationId;
    private String apnRegistrationId;
  }

  private final Map<UUID, InMemoryAccount> accountsById = new ConcurrentHashMap<>();
  private final Map<String, UUID> idByPhone = new ConcurrentHashMap<>();

  @Override
  public AccountDto createAccount(CreateAccountRequest request) {
    UUID id = UUID.randomUUID();
    Instant createdAt = Instant.now();
    AccountDto dto = new AccountDto(id, request.phoneNumber(), request.discoverableByPhoneNumber(), createdAt);

    InMemoryAccount stored = new InMemoryAccount();
    stored.dto = dto;

    accountsById.put(id, stored);
    idByPhone.put(request.phoneNumber(), id);

    return dto;
  }

  @Override
  public Optional<AccountDto> findById(UUID id) {
    InMemoryAccount stored = accountsById.get(id);
    return stored == null ? Optional.empty() : Optional.of(stored.dto);
  }

  @Override
  public Optional<AccountDto> findByPhoneNumber(String e164) {
    UUID id = idByPhone.get(e164);
    return id == null ? Optional.empty() : findById(id);
  }

  @Override
  public Optional<AccountDto> updateDiscoverableByPhoneNumber(UUID id, boolean discoverableByPhoneNumber) {
    InMemoryAccount stored = accountsById.get(id);
    if (stored == null) {
      return Optional.empty();
    }
    stored.dto = new AccountDto(
        stored.dto.id(),
        stored.dto.phoneNumber(),
        discoverableByPhoneNumber,
        stored.dto.createdAt());
    return Optional.of(stored.dto);
  }

  @Override
  public void setRegistrationLock(UUID id, String registrationLock) {
    InMemoryAccount stored = accountsById.get(id);
    if (stored != null) {
      stored.registrationLock = registrationLock;
    }
  }

  @Override
  public void clearRegistrationLock(UUID id) {
    InMemoryAccount stored = accountsById.get(id);
    if (stored != null) {
      stored.registrationLock = null;
    }
  }

  @Override
  public void setFcmRegistration(UUID id, String fcmRegistrationId) {
    InMemoryAccount stored = accountsById.get(id);
    if (stored != null) {
      stored.fcmRegistrationId = fcmRegistrationId;
    }
  }

  @Override
  public void clearFcmRegistration(UUID id) {
    InMemoryAccount stored = accountsById.get(id);
    if (stored != null) {
      stored.fcmRegistrationId = null;
    }
  }

  @Override
  public void setApnRegistration(UUID id, String apnRegistrationId) {
    InMemoryAccount stored = accountsById.get(id);
    if (stored != null) {
      stored.apnRegistrationId = apnRegistrationId;
    }
  }

  @Override
  public void clearApnRegistration(UUID id) {
    InMemoryAccount stored = accountsById.get(id);
    if (stored != null) {
      stored.apnRegistrationId = null;
    }
  }
}

