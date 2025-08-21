# Simple helpers for local dev

PROJECT ?= car_rental_obs
COMPOSE ?= docker compose -p $(PROJECT)

.PHONY: up down run-api run-worker logs es-ready kafka-ready

up:
	$(COMPOSE) up -d

down:
	$(COMPOSE) down --remove-orphans --volumes

run-api:
	mvn -q -f booking-api/pom.xml spring-boot:run

run-worker:
	mvn -q -f inventory-worker/pom.xml spring-boot:run

logs:
	$(COMPOSE) logs -f

es-ready:
	@echo 'Waiting for Elasticsearch on http://localhost:9200 ...'; \
	until curl -sSf http://localhost:9200 >/dev/null; do sleep 2; done; echo 'Elasticsearch is up.'

kafka-ready:
	@echo 'Waiting for Kafka on localhost:9092 ...'; \
	until echo > /dev/tcp/localhost/9092 2>/dev/null; do sleep 2; done; echo 'Kafka is up.'
