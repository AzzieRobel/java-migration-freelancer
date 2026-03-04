package org.whispersystems.textsecuregcm.spring.message;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

  private final MessageService messageService;
  private final MessageEventPublisher messageEventPublisher;

  public MessageController(MessageService messageService, MessageEventPublisher messageEventPublisher) {
    this.messageService = messageService;
    this.messageEventPublisher = messageEventPublisher;
  }

  @PostMapping
  public ResponseEntity<MessageDto> send(@RequestBody CreateMessageRequest request) {
    MessageDto created = messageService.send(request);
    messageEventPublisher.messageSent(created);
    return ResponseEntity.ok(created);
  }

  @GetMapping("/{recipientId}")
  public ResponseEntity<List<MessageDto>> listForRecipient(
      @PathVariable("recipientId") UUID recipientId,
      @RequestParam(name = "limit", defaultValue = "50") int limit) {

    List<MessageDto> messages = messageService.listForRecipient(recipientId, limit);
    return ResponseEntity.ok(messages);
  }
}

