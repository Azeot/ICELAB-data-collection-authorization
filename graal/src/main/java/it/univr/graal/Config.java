package it.univr.graal;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {"classpath:application.properties",
        "file:${user.home}/.idca.properties"}, ignoreResourceNotFound = true)
public class Config {

    @Bean
    public ConnectionFactory connectionFactory(
            @Value("${rabbitmq.host}") String host,
            @Value("${rabbitmq.vhost}") String vhost,
            @Value("${rabbitmq.port}") int port,
            @Value("${rabbitmq.user}") String user,
            @Value("${rabbitmq.password}") String password
    ) {
        final var factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setVirtualHost(vhost);
        factory.setUsername(user);
        factory.setPassword(password);
        factory.setPort(port);
        return factory;
    }

    @Bean
    public RabbitmqListener rabbitmqListener(
            ConnectionFactory connectionFactory,
            @Value("${rabbitmq.requestQueue}") String requestQueue) {
        return new RabbitmqListener(connectionFactory, requestQueue);
    }
}
