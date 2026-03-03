package org.whispersystems.textsecuregcm.spring.account;

import java.util.Optional;
import java.util.UUID;

public interface AccountService {

  Optional<AccountDto> findById(UUID id);

  Optional<AccountDto> findByPhoneNumber(String e164);

  Optional<AccountDto> updateDiscoverableByPhoneNumber(UUID id, boolean discoverableByPhoneNumber);

  void setRegistrationLock(UUID id, String registrationLock);

  void clearRegistrationLock(UUID id);

  void setFcmRegistration(UUID id, String fcmRegistrationId);

  void clearFcmRegistration(UUID id);

  void setApnRegistration(UUID id, String apnRegistrationId);

  void clearApnRegistration(UUID id);
}

