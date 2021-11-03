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


## NOTES
- First time running
  - must create a new realm on keycloak. 'dc' for Data Collection was used.
  - if rabbitmq is exposed on 172.17.0.1, default username and password (guest/guest) are not usable. Must create new user/pwd for receiver client.
    - rabbitmqctl add_user <username> <password>
    - rabbitmqctl set_permissions -p "/" "username" ".*" ".*" ".*"
    - --hostname and volume on /var/lib/rabbitmq are necessary to persist configuration across restarts.