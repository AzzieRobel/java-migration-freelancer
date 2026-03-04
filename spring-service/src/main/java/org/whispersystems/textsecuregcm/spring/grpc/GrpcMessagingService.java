package org.whispersystems.textsecuregcm.spring.grpc;

import io.grpc.Status;
import java.util.UUID;
import org.signal.grpc.simple.ServerCalls;
import org.springframework.stereotype.Service;
import org.whispersystems.textsecuregcm.spring.message.CreateMessageRequest;
import org.whispersystems.textsecuregcm.spring.message.MessageDto;
import org.whispersystems.textsecuregcm.spring.message.MessageEventPublisher;
import org.whispersystems.textsecuregcm.spring.message.MessageService;
import org.whispersystems.textsecuregcm.spring.message.RedisMessageRateLimiter;

@Service
public class GrpcMessagingService extends SimpleMessagingServiceGrpc.MessagingServiceImplBase {

  private final MessageService messageService;
  private final RedisMessageRateLimiter rateLimiter;
  private final MessageEventPublisher messageEventPublisher;

  public GrpcMessagingService(
      MessageService messageService,
      RedisMessageRateLimiter rateLimiter,
      MessageEventPublisher messageEventPublisher) {
    this.messageService = messageService;
    this.rateLimiter = rateLimiter;
    this.messageEventPublisher = messageEventPublisher;
  }

  @Override
  public SendMessageResponse sendMessage(SendMessageRequest request) throws Exception {
    UUID senderId = parseUuidOrThrow(request.getSenderId(), "sender_id");
    UUID recipientId = parseUuidOrThrow(request.getRecipientId(), "recipient_id");

    if (!rateLimiter.isAllowed(senderId)) {
      throw Status.RESOURCE_EXHAUSTED.withDescription("Rate limit exceeded").asRuntimeException();
    }

    MessageDto messageDto =
        messageService.send(new CreateMessageRequest(senderId, recipientId, request.getBody()));

    messageEventPublisher.messageSent(messageDto);

    return SendMessageResponse.newBuilder()
        .setMessage(
            Message.newBuilder()
                .setMessageId(messageDto.messageId().toString())
                .setSenderId(messageDto.senderId().toString())
                .setRecipientId(messageDto.recipientId().toString())
                .setTimestampMs(messageDto.timestamp().toEpochMilli())
                .setBody(messageDto.body())
                .setStatus(messageDto.status())
                .build())
        .build();
  }

  @Override
  public ListMessagesResponse listMessages(ListMessagesRequest request) throws Exception {
    UUID recipientId = parseUuidOrThrow(request.getRecipientId(), "recipient_id");
    int limit = request.getLimit() > 0 ? request.getLimit() : 50;

    var messages = messageService.listForRecipient(recipientId, limit);

    ListMessagesResponse.Builder response = ListMessagesResponse.newBuilder();
    for (MessageDto m : messages) {
      response.addMessages(
          Message.newBuilder()
              .setMessageId(m.messageId().toString())
              .setSenderId(m.senderId().toString())
              .setRecipientId(m.recipientId().toString())
              .setTimestampMs(m.timestamp().toEpochMilli())
              .setBody(m.body())
              .setStatus(m.status())
              .build());
    }

    return response.build();
  }

  private static UUID parseUuidOrThrow(String value, String fieldName) throws Exception {
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException e) {
      throw Status.INVALID_ARGUMENT
          .withDescription(fieldName + " must be a valid UUID")
          .asRuntimeException();
    }
  }
}

