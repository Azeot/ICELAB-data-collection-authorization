# Authorization in ICE Lab Data Collection Architecture

Questo progetto è uno stub esplorativo della configurazione di un sistema di autorizzazione basato su OAuth2 nell'ambito dell'architettura di raccolta dati dell'ICE Lab.

## Panoramica

Una sorgente dati si interfaccia con un client OPC-UA, che deve ricevere dei messaggi (comandi) da un controller; questi messaggi transitano per un broker di messaggi, [RabbitMQ](https://www.rabbitmq.com/).

```text
                                                         ----------
 ----------------         ----------------              | RabbitMQ |             -----------------------
| HW Data Source | <---> |  OPC UA Client | <---cmds--- |   Msg    | <---cmds---| Automation Controller |
 ----------------         ----------------              |  Broker  |             -----------------------
                                                         ----------
```

Il sistema di autorizzazione si inserisce fra broker e controller e permette di verificare che i messaggi che giungono a un client OPC-UA dispongano dei necessari permessi.

Trattandosi di una comunicazione machine-to-machine (M2M), si è scelto d'implementare il flusso [client credentials](https://auth0.com/docs/authorization/flows/client-credentials-flow), dove:

- Il sistema è del tutto trasparente dal punto di vista del PC UA Client.
- Il resource server è il broker RabbitMQ;
- Le risorse sono gli exchange su cui vengono indirizzati i messaggi;
- Il client è l'automation controller;
- L'authorization server è un'istanza di [Keycloak](https://www.keycloak.org/) che persiste i dati in un database [PostgreSQL](https://www.postgresql.org/).

## OAuth2 in RabbitMQ

Il supporto per OAuth2 in RabbitMQ viene dato da un [plug-in](https://github.com/rabbitmq/rabbitmq-server/tree/master/deps/rabbitmq_auth_backend_oauth2).

Messaggi provenienti da un publisher sono indirizzati su exchange facenti parte di un virtual host (VHost), che a loro volta accodano i messaggi su una o più code, su cui sono in ascolto dei consumer.

Per poter indirizzare un messaggio su un determinato exchange, un publisher deve presentare a RabbitMQ un token contenente uno scope costruito nel modo seguente:

```text
RESOURCE_SERVER_ID.<permission>:<vhost_pattern>/<name_pattern>
```

Dove:

- ```<permission>``` definisce il tipo di accesso (```configure```, ```read```, o ```write```);
- ```<vhost_pattern>``` è una pattern per i VHost a cui il token da accesso;
- ```<name_pattern>``` è una pattern per gli exchange a cui il token da accesso;
- ```RESOURCE_SERVER_ID``` da modo di suddividere logicamente gli scope in maniera ortogonale alla suddivisone per VHost.

Il token contente gli scope è un JWT firmato con chiave asimmetrica. Per verificare la firma, RabbitMQ può ottenere il JWKSet direttamente dalla REST API di Keycloak.

L'API di RabbitMQ non è nativamente pensata con il supporto per OAuth2, pertanto il token viene passato dal publisher a RabbitMQ al momento di apertura della connessione, tramite il campo solitamente riservato alla password utente (il nome utente viene ignorato).

RabbitMQ verifica il time to live del token per determinare se esso sia scaduto, sia al momento di apertura della connessione, _sia al momento della pubblicazione di un messaggio_.

## Utilizzo

Sono richiesti [Git](https://git-scm.com/), [GNU Make](https://www.gnu.org/software/make/), [Maven](https://maven.apache.org/), [Docker](https://www.docker.com/) e Java 11.

Dopo aver clonato il progetto, deve essere creato l'ambiente di test locale.

Da terminale, portarsi nella cartella contenente il Makefile e digitare i comandi:

```shell
make local-db
make local-keycloak
make local-rabbitmq
```

Se è il primo setup, bisogna configurare il [container Keycloak](###Configurazione-di-Keycloak) ed il [container RabbitMQ](###Configurazione-di-RabbitMQ).

Poi digitare:

```shell
make build-all
java -jar graal/target/graal-1.0-SNAPSHOT.jar <username> <password>
```

Dove `<username>` e `<password>` sono le credenziali per il client consumer.

Poi in un'altra shell aperta sulla stessa cartella:

```shell
java -jar whitebunny/target/whitebunny-1.0-SNAPSHOT.jar <clientSecret> [<message>]
```

Dove `<clientSecret>` è il secret del client publisher, e `<message>` è un argomento opzionale per specificare il testo del messaggio.

### Configurazione di Keycloak

La seguente configurazione di Keycloak va eseguita solamente la prima volta. Viene automaticamente persistita e riutilizzata in successivi restart dei container docker.

Tramite browser collegarsi al backoffice amministrativo di Keycloak su `http://172.17.0.1:8081/` e creare un nuovo realm di nome `dc`. Le credenziali di default per accedere al backoffice sono rispettivamente `admin`, `admin`.

Nella tab "Client Scopes", creare due nuovi scope:

```text
arthur.configure:rpcmsg/opcua
arthur.write:rpcmsg/opcua
```

![Scope configuration](doc/images/scope_config.png "Scope configuration")

Per ognuno, sotto la inner tab "Mappers", creare un nuovo mapper con le proprietà "name" `audience`, "mapper type" `Audience` e "Include Custom Audience" `arthur`.

Nella tab "Clients", creare un nuovo client con la seguente configurazione:

![Client configuration](doc/images/client_config.png "Client configuration")

Fare click sul pulsante "Salva" in fondo.

Nella inner tab "Client Scopes", aggiungere agli "Assigned Default Client Scopes" gli scopes creati in precedenza.

Infine, nell'inner tab "Credentials" è riportato il secret per ottenere un access token per il client publisher.

### Configurazione di RabbitMQ

Anche la seguente configurazione di RabbitMQ va eseguita solamente la prima volta.

Digitare il comando:

```shell
docker exec -it rabbitmq-dev /bin/bash
```

Seguito da:

```shell
rabbitmqctl add_vhost "rpcmsg"
rabbitmqctl add_user <username> <password>
rabbitmqctl set_permissions -p "rpcmsg" <username> ".*" ".*" ".*"
exit
```

Dove `<username>` e `<password>` sono le credenziali per il client consumer.