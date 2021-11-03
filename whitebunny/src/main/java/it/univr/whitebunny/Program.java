package it.univr.whitebunny;

import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

public class Program {
    final static Logger logger = LoggerFactory.getLogger(Program.class);
    private final static String QUEUE_NAME = "thequeue";

    public static void main(String[] args) throws IOException, TimeoutException {
        final var message = Stream.of(args).findFirst().orElse("Some message");
        final var factory = new ConnectionFactory();
        factory.setHost("172.17.0.1");
        factory.setVirtualHost("rpcmsg");
        factory.setUsername("IDontMatter");
        factory.setPassword("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJtdDAyRXY1T3NRdUR2RnhHLVZydlpOXzloSGVZNHlydXhCNDNlU0xHQ0NJIn0.eyJleHAiOjE2MzU5NzY1OTAsImlhdCI6MTYzNTk3NjI5MCwianRpIjoiMWY3NzY4ZDYtMjE2MS00OTE4LWI5NzYtOTdkM2Q0MDM0OGRhIiwiaXNzIjoiaHR0cDovLzE3Mi4xNy4wLjE6ODA4MS9hdXRoL3JlYWxtcy9kYyIsImF1ZCI6ImFydGh1ciIsInN1YiI6IjRhMmZiNDFmLTM0NTUtNGQ1MC1hZjY4LTJmNThmNjM4YThlZCIsInR5cCI6IkJlYXJlciIsImF6cCI6IndoaXRlYnVubnkiLCJhY3IiOiIxIiwic2NvcGUiOiJhcnRodXIuY29uZmlndXJlOnJwY21zZy8qIGFydGh1ci53cml0ZTpycGNtc2cvKiIsImNsaWVudElkIjoid2hpdGVidW5ueSIsImNsaWVudEhvc3QiOiIxNzIuMTcuMC4xIiwiY2xpZW50QWRkcmVzcyI6IjE3Mi4xNy4wLjEifQ.Axz80zhBjt6epMz7L86cmSsxa_LEI9usiRcl4sbXgeQXrsTwP4VusD_l_2r2-HIVLGe4QBgk6CqVHFTzeUC03kH3iuE9XM9gllX6rARkBP1GsDmluzxMM_QUJyxAIfgmWcm0XMKFucX7D0zkRo9x9O00FH0PzzDQlcqxFqSdHEG7vJB8txWz4sQ3tpb5kMJXtd0XDS4g2iY7FJQEUxVf3XMJ-G_3Ck2TpHRVYeBb3ZgdPYSfr_y1_HOXo5IXN0brR3avk3rcJkj6TPcfBJcewkQCynLzVg7rg2JNxVtpcnomyx3-N7OJc1qkVmWpo1gy8-6iGctWHeCON6fOP_AVfw");
        try (final var connection = factory.newConnection();
             final var channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            logger.info("Sent '{}'", message);
        }
    }
}
