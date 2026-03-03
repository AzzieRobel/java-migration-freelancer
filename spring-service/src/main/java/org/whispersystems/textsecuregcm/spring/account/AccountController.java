package org.whispersystems.textsecuregcm.spring.account;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

  private final AccountService accountService;

  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  @GetMapping("/{id}")
  public ResponseEntity<AccountDto> getById(@PathVariable("id") UUID id) {
    Optional<AccountDto> maybe = accountService.findById(id);
    return maybe.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<AccountDto> getByPhoneNumber(@RequestParam("phone") String phone) {
    Optional<AccountDto> maybe = accountService.findByPhoneNumber(phone);
    return maybe.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}/phone-number-discoverability")
  public ResponseEntity<AccountDto> setPhoneNumberDiscoverability(
      @PathVariable("id") UUID id,
      @RequestBody PhoneNumberDiscoverabilityRequest request) {

    Optional<AccountDto> maybe = accountService.updateDiscoverableByPhoneNumber(id, request.discoverableByPhoneNumber());
    return maybe.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}/registration-lock")
  public ResponseEntity<Void> setRegistrationLock(
      @PathVariable("id") UUID id,
      @RequestBody RegistrationLockRequest request) {

    accountService.setRegistrationLock(id, request.registrationLock());
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}/registration-lock/clear")
  public ResponseEntity<Void> clearRegistrationLock(@PathVariable("id") UUID id) {
    accountService.clearRegistrationLock(id);
    return ResponseEntity.noContent().build();
  }
}

