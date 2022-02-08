package it.univr.whitebunny;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class Program {
    public static void main(String[] argv) throws IOException {
        final var ctx = new AnnotationConfigApplicationContext(Config.class);
        final var authClient = ctx.getBean(AuthClient.class);
        final var rabbitmqClient = ctx.getBean(RabbitmqClient.class);
        final var oauthResponse = authClient.getOauthResponse();
        try (final var file = new ClassPathResource("payload.json").getInputStream();
             final var reader = new BufferedReader(new InputStreamReader(file))) {
            final var payload = reader.lines().collect(Collectors.joining());
            System.out.printf("Response: %s", rabbitmqClient.publish(oauthResponse.accessToken, payload));
        }
    }
}
