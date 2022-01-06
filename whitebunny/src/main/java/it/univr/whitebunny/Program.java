package it.univr.whitebunny;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.stream.Stream;

public class Program {
    public static void main(String[] argv) {
        final var ctx = new AnnotationConfigApplicationContext(Config.class);
        final var authClient = ctx.getBean(AuthClient.class);
        final var rabbitmqClient = ctx.getBean(RabbitmqClient.class);
        final var message = Stream.of(argv).findFirst().orElse("Hello world");
        final var oauthResponse = authClient.getOauthResponse();
        rabbitmqClient.publish(oauthResponse.accessToken, message);
    }
}
