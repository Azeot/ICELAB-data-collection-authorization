package it.univr.graal;

import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class Program {
    final static Logger logger = LoggerFactory.getLogger(Program.class);
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        final var factory = new ConnectionFactory();
        factory.setHost("172.17.0.1");
        factory.setUsername("black_knight");
        factory.setPassword("scratch");
        final var connection = factory.newConnection();
        final var channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        logger.info("Waiting for messages. To exit press CTRL+C");

        channel.basicConsume(QUEUE_NAME,
                true,
                (consumerTag, delivery) -> {
                    final var message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    logger.info("{} received '{}'", consumerTag, message);
                },
                (consumerTag) -> {
                });

    }
}
