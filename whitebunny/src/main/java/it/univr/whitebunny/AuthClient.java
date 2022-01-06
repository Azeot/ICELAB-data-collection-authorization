package it.univr.whitebunny;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AuthClient {
    final static Logger logger = LoggerFactory.getLogger(AuthClient.class);

    private final String realm;
    private final String clientId;
    private final String secret;
    private final String host;
    private final int port;

    public AuthClient(String realm, String clientId, String secret, String host, int port) {
        this.realm = realm;
        this.clientId = clientId;
        this.secret = secret;
        this.host = host;
        this.port = port;
    }

    public OauthResponse getOauthResponse() {
        logger.info("Requesting access token at http://{}:{}/auth/realms/{}/protocol/openid-connect/token", host, port, realm);
        final var credentials = String.format("%s:%s", clientId, secret);
        final var encodedCredentials = new String(Base64.getMimeEncoder().encode(credentials.getBytes(StandardCharsets.UTF_8)));
        final var client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        final var request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://%s:%d/auth/realms/%s/protocol/openid-connect/token", host, port, realm)))
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials", StandardCharsets.UTF_8))
                .header("Accept", "application/json;charset=UTF-8")
                .header("Content-Type", " application/x-www-form-urlencoded;charset=UTF-8")
                .header("Authorization", String.format("Basic %s", encodedCredentials))
                .build();

        try {
            final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (400 <= response.statusCode()) {
                throw new IllegalArgumentException(response.body());
            }
            final var objectMapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final var oathResponse = objectMapper.readValue(response.body(), OauthResponse.class);
            logger.trace(oathResponse.toString());
            return oathResponse;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static class OauthResponse {
        @JsonProperty("access_token")
        public String accessToken;
        @JsonProperty("expires_in")
        public int ttl;

        @Override
        public String toString() {
            return "OauthResponse{" +
                    "accessToken='" + accessToken + '\'' +
                    ", ttl=" + ttl +
                    '}';
        }
    }
}
