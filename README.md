# Authorization with OAuth2 for messages coming thorugh RammitMQ

## DONE

- Local provisioning of containers with:
  - RabbitMq with OAuth2 plugin enabled, data and conf persisted locally;
    - Username and password for receiver client with internal auth;
  - Keycloak and realm;
  - Postgresql db for Keycloak, with data persisted locally.
- Receiver client and sender client stubs in Java;

## TODO

- Implement the client credentials flow:
  1. Make so that Rabbitmq gets a JWK set from Keycloak; DONE
  2. See that messages without proper tokens get rejected; DONE
  3. Get manually a token and see that sender clients gets authorized; DONE
     1. Try not to use wildcards for permissions;
  4. Make the client obtain tokens when sending.

## NOTES

- First time running
  - Setup keycloak
    - must create a new realm on keycloak. 'dc' for Data Collection was used.
    - create a client scope e.g. arthur.write:rpcmsg/opcua. to the scope add a mapper for the audience (aud) claim with the same resource_server_id
    - scope with 'configure' might also be necessary for example if declaring a queue
  - if rabbitmq is exposed on 172.17.0.1, default username and password (guest/guest) are not usable. Must create new user/pwd for receiver client.
    - create new virtualhost
      - rabbitmqctl add_vhost <vhName>
    - rabbitmqctl add_user <username> <password>
    - rabbitmqctl set_permissions -p "vhName" "username" ".*" ".*" ".*"
    - --hostname and volume on /var/lib/rabbitmq are necessary to persist configuration across restarts.