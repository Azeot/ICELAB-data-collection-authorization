package it.univr.graal;

import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class RabbitmqListener {
    final static Logger logger = LoggerFactory.getLogger(RabbitmqListener.class);

    private final ConnectionFactory connectionFactory;
    private final String exchange;

    public RabbitmqListener(ConnectionFactory connectionFactory, String exchange) {
        this.connectionFactory = connectionFactory;
        this.exchange = exchange;
    }

    public void listen() {
        try {
            final var connection = connectionFactory.newConnection();
            final var channel = connection.createChannel();
            channel.exchangeDeclare(exchange, "fanout");
            final var queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue, exchange, "");
            logger.info("Waiting for messages. To exit press CTRL+C");
            channel.basicConsume(queue,
                    true,
                    (consumerTag, delivery) -> {
                        final var message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                        logger.info("Received '{}'", message);
                    },
                    (consumerTag) -> {
                    });
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
