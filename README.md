# Car Rental Platform — Step 2: Observability (ELK)

Dodano **ELK** i wysyłanie logów JSON z aplikacji Spring do Logstash (TCP :5000).

## Start infrastruktury
docker compose -p car_rental_obs up -d

## Uruchom aplikacje
mvn -q -pl inventory-worker spring-boot:run  
mvn -q -pl booking-api spring-boot:run

## Kibana
http://localhost:5601 → utwórz Data View: `carrental-*` → Discover.

## Uwaga
Jeśli masz stare kontenery o nazwach `carrental_*`:
docker compose -p carrental down --remove-orphans --volumes
