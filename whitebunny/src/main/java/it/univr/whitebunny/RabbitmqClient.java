package it.univr.whitebunny;

import java.io.IOException;
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
    private final String exchange;

    public RabbitmqClient(ConnectionFactory connectionFactory, String exchange) {
        this.connectionFactory = connectionFactory;
        this.exchange = exchange;

    }

    public String publish(String token, String message) {
        connectionFactory.setPassword(token);
        try (final var connection = connectionFactory.newConnection();
             final var channel = connection.createChannel()) {
                final String corrId = UUID.randomUUID().toString();
                
                final String msg = message.replaceAll("corr_id", "corrId");

                String replyQueueName = channel.queueDeclare().getQueue();
                AMQP.BasicProperties props = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(corrId)
                        .replyTo(replyQueueName)
                        .build();
        
                channel.basicPublish("", exchange, props, msg.getBytes("UTF-8"));
        
                final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);
        
                String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
                    if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                        response.offer(new String(delivery.getBody(), "UTF-8"));
                    }
                }, consumerTag -> {
                });
        
                String result = response.take();
                channel.basicCancel(ctag);
                return result;
        } catch (InterruptedException | IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
