local-rabbitmq:
	docker run -it --rm \
		--name rabbitmq-dev \
		-p 172.17.0.1:5672:5672 \
		-p 172.17.0.1:15672:15672 \
		-v ${PWD}/local/rabbitmq:/etc/rabbitmq/:ro \
		rabbitmq:3.9-management

# Ok avere password qua, tanto in ansible arriverà da vault
# Setto la cartella data in modo da ritrovarmi i dati fra restart
# Metto script iniziali in /docker-entrypoint-initdb.d/ che girano se in data non c'è già qualcosa
local-db:
	docker run -it --rm \
		--name postgres-dev \
		-e POSTGRES_PASSWORD=password \
		-p 172.17.0.1:5432:5432 \
		-v ${PWD}/local/postgres-init:/docker-entrypoint-initdb.d/:ro \
		-v ${PWD}/local/postgres:/var/lib/postgresql/data \
		postgres:bullseye

# Da vault su ansible saranno: database_user e password, admin_user e password
local-keycloak:
	docker run --rm -it \
		--name keycloak-dev \
		-p 172.17.0.1:8081:8080 \
		-e KEYCLOAK_DATABASE_HOST=172.17.0.1 \
		-e KEYCLOAK_DATABASE_NAME=keycloak \
		-e KEYCLOAK_DATABASE_USER=postgres \
		-e KEYCLOAK_DATABASE_PASSWORD=password \
		-e KEYCLOAK_ADMIN_USER=admin \
		-e KEYCLOAK_ADMIN_PASSWORD=admin \
		bitnami/keycloak:15.0.2-debian-10-r72