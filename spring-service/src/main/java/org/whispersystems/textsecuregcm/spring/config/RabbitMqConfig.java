package org.whispersystems.textsecuregcm.spring.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

  public static final String MESSAGE_EVENTS_EXCHANGE = "spring.message.events";
  public static final String MESSAGE_SENT_ROUTING_KEY = "message.sent";
  public static final String MESSAGE_EVENTS_QUEUE = "spring.message.events.queue";

  @Bean
  public TopicExchange messageEventsExchange() {
    return new TopicExchange(MESSAGE_EVENTS_EXCHANGE, true, false);
  }

  @Bean
  public Queue messageEventsQueue() {
    return new Queue(MESSAGE_EVENTS_QUEUE, true);
  }

  @Bean
  public Binding messageEventsBinding(Queue messageEventsQueue, TopicExchange messageEventsExchange) {
    return BindingBuilder.bind(messageEventsQueue)
        .to(messageEventsExchange)
        .with(MESSAGE_SENT_ROUTING_KEY);
  }
}

