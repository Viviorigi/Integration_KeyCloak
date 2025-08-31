# KeyCloak integration

## Prerequisites

### Install KeyCloak on Docker

`docker pull quay.io/keycloak/keycloak:26.0.6`

Run keyCloak on port 8180

`docker run -d --name keycloak-26.0.6 -p 8180:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.0.6 start-dev`
