SHELL := /bin/bash

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

minikube:
	minikube start && minikube addons enable ingress && minikube dashboard

k8s-db: k8s/auth_secret.yml
	minikube kubectl -- apply -f ${PWD}/k8s/auth_namespace.yml
	minikube kubectl -- apply -f ${PWD}/k8s/auth_secret.yml
	minikube kubectl -- apply -f ${PWD}/k8s/postgres.yml

k8s/auth_secret.yml:
	@read -p "Enter DB password: " db_pwd;\
   	read -p "Enter Keycloak admin user: " kyc_usr;\
   	read -p "Enter Keycloak admin password: " kyc_pwd;\
   	sed -e s/%DB_PWD%/$$(echo $$db_pwd | base64 -)/g\
   	-e s/%KYC_USR%/$$(echo $$kyc_usr | base64 -)/g\
   	-e s/%KYC_PWD%/$$(echo $$kyc_pwd | base64 -)/g\
   	${PWD}/k8s/templates/auth_secret_template.yml > ${PWD}/k8s/auth_secret.yml

remove-k8s-db:
	minikube kubectl -- -n auth delete -f ${PWD}/k8s/postgres.yml
	minikube kubectl -- delete -f ${PWD}/k8s/auth_namespace.yml

k8s-keycloak:
	minikube kubectl -- apply -f ${PWD}/k8s/keycloak.yml

remove-k8s-keycloak:
	minikube kubectl -- -n auth delete -f ${PWD}/k8s/keycloak.yml

view-k8s-keycloak:
	@echo "Keycloak:                 http://keycloak.$$(minikube ip).nip.io/auth"
	@echo "Keycloak Admin Console:   http://keycloak.$$(minikube ip).nip.io/auth/admin"
	@echo ""
	@minikube kubectl -- get all -n auth

k8s-rabbitmq: k8s/rabbitmq.yml
	minikube kubectl -- apply -f "https://github.com/rabbitmq/cluster-operator/releases/latest/download/cluster-operator.yml"
	minikube kubectl -- -n rabbitmq-system apply -f ${PWD}/k8s/rabbitmq.yml

k8s/rabbitmq.yml:
	@read ip port <<< $$(minikube kubectl -- get service -n auth --no-headers | head -n 1 | awk '{split($$5, a, ":"); split(a[2], port, "/"); print $$3,port[1]}');\
	read -p "Enter realm name: " realm;\
	read -p "Enter resource server id: " res_srv;\
    sed -e s/%KEYCLOAK_IP%/$$ip/g\
    -e s/%KEYCLOAK_PORT%/$$port/g\
    -e s/%EXT_IP%/$$(minikube ip)/g\
    -e s/%REALM%/$$realm/g\
    -e s/%RES_SRV%/$$res_srv/g\
    ${PWD}/k8s/templates/rabbitmq_template.yml > ${PWD}/k8s/rabbitmq.yml

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
