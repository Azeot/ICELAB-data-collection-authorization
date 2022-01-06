package it.univr.whitebunny;

import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class RabbitmqClient {
    final static Logger logger = LoggerFactory.getLogger(RabbitmqClient.class);

    private final ConnectionFactory connectionFactory;
    private final String exchange;

    public RabbitmqClient(ConnectionFactory connectionFactory, String exchange) {
        this.connectionFactory = connectionFactory;
        this.exchange = exchange;
    }

    public void publish(String token, String message) {
        connectionFactory.setPassword(token);
        try (final var connection = connectionFactory.newConnection();
             final var channel = connection.createChannel()) {
            channel.exchangeDeclare(exchange, "fanout");
            channel.basicPublish(exchange, "", null, message.getBytes(StandardCharsets.UTF_8));
            logger.info("Sent '{}'", message);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
