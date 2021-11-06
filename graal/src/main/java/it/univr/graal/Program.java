package it.univr.graal;

import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class Program {
    final static Logger logger = LoggerFactory.getLogger(Program.class);
    private final static String EXCHANGE_NAME = "opcua";
    private final static String QUEUE_IP = "172.17.0.1";
    private final static String VHOST = "rpcmsg";

    public static void main(String[] argv) throws Exception {
        final var arguments = List.of(argv);
        final var user = arguments.stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Missing RabbitMQ user as first argument"));
        final var password = arguments.stream().skip(1).findFirst().orElseThrow(() -> new IllegalArgumentException("Missing RabbitMQ user password as second argument"));
        final var factory = new ConnectionFactory();
        factory.setHost(QUEUE_IP);
        factory.setVirtualHost(VHOST);
        factory.setUsername(user);
        factory.setPassword(password);
        final var connection = factory.newConnection();
        final var channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        final var queue = channel.queueDeclare().getQueue();
        channel.queueBind(queue, EXCHANGE_NAME, "");

        logger.info("Waiting for messages. To exit press CTRL+C");

        channel.basicConsume(queue,
                true,
                (consumerTag, delivery) -> {
                    final var message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    logger.info("Received '{}'", message);
                },
                (consumerTag) -> {
                });

    }
}
