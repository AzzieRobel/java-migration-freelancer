package org.whispersystems.textsecuregcm.spring.message;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

@Service
public class InMemoryMessageService implements MessageService {

  private final Map<UUID, List<MessageDto>> messagesByRecipient = new ConcurrentHashMap<>();

  @Override
  public MessageDto send(CreateMessageRequest request) {
    UUID messageId = UUID.randomUUID();
    Instant timestamp = Instant.now();

    MessageDto message =
        new MessageDto(
            messageId,
            request.senderId(),
            request.recipientId(),
            timestamp,
            request.body(),
            "DELIVERED");

    messagesByRecipient
        .computeIfAbsent(request.recipientId(), key -> new CopyOnWriteArrayList<>())
        .add(message);

    return message;
  }

  @Override
  public List<MessageDto> listForRecipient(UUID recipientId, int limit) {
    List<MessageDto> all = messagesByRecipient.getOrDefault(recipientId, List.of());
    if (all.isEmpty()) {
      return List.of();
    }

    List<MessageDto> copy = new ArrayList<>(all);
    copy.sort(Comparator.comparing(MessageDto::timestamp).reversed());

    if (limit <= 0 || limit >= copy.size()) {
      return Collections.unmodifiableList(copy);
    }

    return Collections.unmodifiableList(copy.subList(0, limit));
  }
}

