package org.whispersystems.textsecuregcm.spring.grpc;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcServerConfig {

  private final SimpleMessagingServiceGrpc.MessagingServiceImplBase messagingService;
  private final int port;

  private Server server;
  private ExecutorService executor;

  public GrpcServerConfig(
      SimpleMessagingServiceGrpc.MessagingServiceImplBase messagingService,
      @Value("${grpc.messaging.port:9090}") int port) {
    this.messagingService = messagingService;
    this.port = port;
  }

  @PostConstruct
  public void start() throws IOException {
    this.executor = Executors.newSingleThreadExecutor();

    this.server =
        NettyServerBuilder.forAddress(new InetSocketAddress("0.0.0.0", port))
            .addService(messagingService)
            .build();

    executor.submit(
        () -> {
          try {
            server.start();
            server.awaitTermination();
          } catch (IOException | InterruptedException ignored) {
            // Server lifecycle will be controlled by Spring context.
          }
        });
  }

  @PreDestroy
  public void stop() {
    if (server != null) {
      server.shutdown();
    }
    if (executor != null) {
      executor.shutdownNow();
    }
  }
}

