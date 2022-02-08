package it.univr.whitebunny;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConnectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitmqClient {
    final static Logger logger = LoggerFactory.getLogger(RabbitmqClient.class);

    private final ConnectionFactory connectionFactory;
    private final String requestQueue;

    public RabbitmqClient(ConnectionFactory connectionFactory, String requestQueue) {
        this.connectionFactory = connectionFactory;
        this.requestQueue = requestQueue;

    }

    public String publish(String token, String payload) {
        connectionFactory.setPassword(token);
        try (final var connection = connectionFactory.newConnection();
             final var channel = connection.createChannel()) {
            final var corrId = UUID.randomUUID().toString();
            final var msg = payload.replaceAll("corr_id", corrId);
            logger.trace(msg);

            final var replyQueueName = channel.queueDeclare().getQueue();
            final var props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .replyTo(replyQueueName)
                    .build();
            channel.basicPublish("", requestQueue, props, msg.getBytes(StandardCharsets.UTF_8));
            final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

            final var ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
                if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                    response.offer(new String(delivery.getBody(), StandardCharsets.UTF_8));
                }
            }, consumerTag -> {
            });

            final var result = response.take();
            channel.basicCancel(ctag);
            return result;
        } catch (InterruptedException | IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
