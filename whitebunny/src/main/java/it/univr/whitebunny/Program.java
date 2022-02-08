package it.univr.whitebunny;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Program {
    public static void main(String[] argv) throws IOException {
        final var ctx = new AnnotationConfigApplicationContext(Config.class);
        final var authClient = ctx.getBean(AuthClient.class);
        final var rabbitmqClient = ctx.getBean(RabbitmqClient.class);
        final var file = Path.of("/Users/azeot/projects/payload");
        final var payload = Files.readString(file);
        final var message = Stream.of(argv).findFirst().orElse("Hello world");
        final var oauthResponse = authClient.getOauthResponse();
        System.out.println(rabbitmqClient.publish(oauthResponse.accessToken, payload));
        
    }
}
