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
	minikube kubectl -- -n rabbitmq-system apply -f ${PWD}/k8s/rabbitmq.yml

remove-k8s-rabbitmq:
	minikube kubectl -- -n rabbitmq-system delete -f ${PWD}/k8s/rabbitmq.yml
	minikube kubectl -- delete -f "https://github.com/rabbitmq/cluster-operator/releases/latest/download/cluster-operator.yml"

view-k8s-rabbitmq:
	@echo "Username: $$(minikube kubectl -- -n rabbitmq-system get secret icerabbitmq-default-user -o jsonpath="{.data.username}" | base64 --decode)"
	@echo "Password: $$(minikube kubectl -- -n rabbitmq-system get secret icerabbitmq-default-user -o jsonpath="{.data.password}" | base64 --decode)"
	@echo "Rabbitmq: $$(minikube service --url icerabbitmq -n rabbitmq-system | head -n 1)"
	@echo "Rabbitmq management: http://rabbitmq-management.$$(minikube ip).nip.io"
	@echo ""
	@minikube kubectl -- get all -n rabbitmq-system

minikube:
	minikube start
	minikube dashboard

k8s-db:
	minikube kubectl -- apply -f ${PWD}/k8s/postgres.yml

k8s-keycloak:
	minikube kubectl -- apply -f ${PWD}/k8s/keycloak.yml

remove-k8s-db:
	minikube kubectl -- -n auth delete -f ${PWD}/k8s/postgres.yml

remove-k8s-keycloak:
	minikube kubectl -- -n auth delete -f ${PWD}/k8s/keycloak.yml

view-k8s-keycloak:
	@echo "Keycloak:                 http://keycloak.$$(minikube ip).nip.io/auth" 
	@echo "Keycloak Admin Console:   http://keycloak.$$(minikube ip).nip.io/auth/admin" 
	@echo "Keycloak Account Console: http://keycloak.$$(minikube ip).nip.io/auth/realms/myrealm/account"
	@echo ""
	@minikube kubectl -- get all -n auth