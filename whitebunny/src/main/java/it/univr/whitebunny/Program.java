package it.univr.whitebunny;

import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

public class Program {
    final static Logger logger = LoggerFactory.getLogger(Program.class);
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] args) throws IOException, TimeoutException {
        final var message = Stream.of(args).findFirst().orElse("Some message");
        final var factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (final var connection = factory.newConnection(); final var channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            logger.info("Sent '{}'", message);
        }
    }
}
