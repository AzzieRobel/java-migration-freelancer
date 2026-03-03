package org.whispersystems.textsecuregcm.spring.account;

import java.util.Optional;
import java.util.UUID;

public interface AccountService {

  Optional<AccountDto> findById(UUID id);

  Optional<AccountDto> findByPhoneNumber(String e164);

  Optional<AccountDto> updateDiscoverableByPhoneNumber(UUID id, boolean discoverableByPhoneNumber);
}

