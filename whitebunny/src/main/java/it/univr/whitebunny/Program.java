package it.univr.whitebunny;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Program {
    final static Logger logger = LoggerFactory.getLogger(Program.class);

    private final static String QUEUE_NAME = "thequeue";
    private final static String CLIENT_ID = "whitebunny";
    private final static String OAUTH_IP = "172.17.0.1";
    private final static int OAUTH_PORT = 8081;
    private final static String REALM = "dc";
    private final static String QUEUE_IP = "172.17.0.1";
    private final static String VHOST = "rpcmsg";


    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        final var arguments = List.of(args);

        final var objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        final var clientSecret = arguments.stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Missing client secret argument"));
        final var credentials = String.format("%s:%s", CLIENT_ID, clientSecret);
        final var encodedCredentials = new String(Base64.getMimeEncoder().encode(credentials.getBytes(StandardCharsets.UTF_8)));
        final var client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        final var request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://%s:%d/auth/realms/%s/protocol/openid-connect/token", OAUTH_IP, OAUTH_PORT, REALM)))
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials", StandardCharsets.UTF_8))
                .header("Accept", "application/json;charset=UTF-8")
                .header("Content-Type", " application/x-www-form-urlencoded;charset=UTF-8")
                .header("Authorization", String.format("Basic %s", encodedCredentials))
                .build();

        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (400 <= response.statusCode()) {
            throw new IllegalArgumentException(response.body());
        }

        final var oathResp = objectMapper.readValue(response.body(), OathResp.class);

        final var message = arguments.stream().skip(1).findFirst().orElse("Some default message");
        final var factory = new ConnectionFactory();
        factory.setHost(QUEUE_IP);
        factory.setVirtualHost(VHOST);
        factory.setUsername("");
        factory.setPassword(oathResp.accessToken);
        try (final var connection = factory.newConnection();
             final var channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            logger.info("Sent '{}'", message);
        }
    }

    private static class OathResp {
        @JsonProperty("access_token")
        public String accessToken;
    }
}
