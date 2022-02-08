package it.univr.whitebunny;

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
            @Value("${rabbitmq.port}") int port
    ) {
        final var factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setVirtualHost(vhost);
        factory.setUsername("");
        factory.setPort(port);
        return factory;
    }

    @Bean
    public RabbitmqClient rabbitmqClient(
            ConnectionFactory connectionFactory,
            @Value("${rabbitmq.requestQueue}") String requestQueue) {
        return new RabbitmqClient(connectionFactory, requestQueue);
    }

    @Bean
    public AuthClient authClient(
            @Value("${auth.realm}") String realm,
            @Value("${auth.client.id}") String clientId,
            @Value("${auth.client.secret}") String secret,
            @Value("${auth.host}") String host,
            @Value("${auth.port}") int port
    ) {
        return new AuthClient(realm, clientId, secret, host, port);
    }
}
