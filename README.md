# Authorization with OAuth2 for messages coming thorugh RammitMQ

## DONE

- Local provisioning of containers with:
  - RabbitMq with OAuth2 plugin enabled;
  - Keycloak;
  - Postgresql db for Keycloak, with data persisted locally.

## TODO

- Implement the client credentials flow:
  1. Make so that Rabbitmq gets a JWK set from Keycloak;
  2. See that messages without proper tokens get rejected;
  3. Make the client obtain tokens when sending.