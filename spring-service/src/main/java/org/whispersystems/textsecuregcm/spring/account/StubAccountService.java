package org.whispersystems.textsecuregcm.spring.account;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

// Temporary in-memory fallback; not used when CassandraAccountService is present.
// Remove or annotate with @Primary(false) once all callers are backed by Cassandra.
@Service
public class StubAccountService implements AccountService {

  @Override
  public Optional<AccountDto> findById(UUID id) {
    // Placeholder implementation; will be backed by Cassandra later.
    return Optional.of(new AccountDto(id, "+10000000000", true, Instant.EPOCH));
  }

  @Override
  public Optional<AccountDto> findByPhoneNumber(String e164) {
    return Optional.of(new AccountDto(UUID.randomUUID(), e164, true, Instant.EPOCH));
  }

  @Override
  public Optional<AccountDto> updateDiscoverableByPhoneNumber(UUID id, boolean discoverableByPhoneNumber) {
    // In this stub, just echo back a DTO with the requested flag set.
    return Optional.of(new AccountDto(id, "+10000000000", discoverableByPhoneNumber, Instant.EPOCH));
  }

  @Override
  public void setRegistrationLock(UUID id, String registrationLock) {
    // Stub: no-op for now.
  }

  @Override
  public void clearRegistrationLock(UUID id) {
    // Stub: no-op for now.
  }
}

