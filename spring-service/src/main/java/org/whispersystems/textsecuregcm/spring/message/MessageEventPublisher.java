package org.whispersystems.textsecuregcm.spring.message;

import static org.whispersystems.textsecuregcm.spring.config.RabbitMqConfig.MESSAGE_EVENTS_EXCHANGE;
import static org.whispersystems.textsecuregcm.spring.config.RabbitMqConfig.MESSAGE_SENT_ROUTING_KEY;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageEventPublisher {

  private final RabbitTemplate rabbitTemplate;

  public MessageEventPublisher(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void messageSent(MessageDto message) {
    String payload =
        "messageId="
            + message.messageId()
            + ",senderId="
            + message.senderId()
            + ",recipientId="
            + message.recipientId();

    rabbitTemplate.convertAndSend(MESSAGE_EVENTS_EXCHANGE, MESSAGE_SENT_ROUTING_KEY, payload);
  }
}

