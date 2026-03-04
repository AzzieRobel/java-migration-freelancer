package org.whispersystems.textsecuregcm.spring.message;

import java.util.List;
import java.util.UUID;

public interface MessageService {

  MessageDto send(CreateMessageRequest request);

  List<MessageDto> listForRecipient(UUID recipientId, int limit);
}

