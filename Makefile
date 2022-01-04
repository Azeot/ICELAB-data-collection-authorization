build-all:
	mvn clean package

local-rabbitmq:
	docker run -it --rm \
		--name rabbitmq-dev \
		--hostname rabbitmq-dev \
		-p 172.17.0.1:5672:5672 \
		-p 172.17.0.1:15672:15672 \
		-v ${PWD}/local/rabbitmq-conf:/etc/rabbitmq/:ro \
		-v ${PWD}/local/rabbitmq:/var/lib/rabbitmq \
		rabbitmq:3.9-management

local-db:
	docker run -it --rm \
		--name postgres-dev \
		-e POSTGRES_PASSWORD=password \
		-p 172.17.0.1:5432:5432 \
		-v ${PWD}/local/postgres-init:/docker-entrypoint-initdb.d/:ro \
		-v ${PWD}/local/postgres:/var/lib/postgresql/data \
		postgres:bullseye

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

k8s-rabbitmq:
	minikube kubectl -- apply -f "https://github.com/rabbitmq/cluster-operator/releases/latest/download/cluster-operator.yml"
	minikube kubectl -- -n rabbitmq-system apply -f ${PWD}/k8s/rabbitmq-test-deploy.yml

remove-k8s-rabbitmq:
	minikube kubectl -- -n rabbitmq-system delete -f ${PWD}/k8s/rabbitmq-test-deploy.yml
	minikube kubectl -- delete -f "https://github.com/rabbitmq/cluster-operator/releases/latest/download/cluster-operator.yml"

view-k8s-deployment:
	minikube kubectl -- get all -n rabbitmq-system -l app.kubernetes.io/name=rabbitmq-idca-deployment

minikube:
	minikube start
	minikube dashboard
