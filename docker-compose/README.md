# Docker compose

Starts the necessary infrastructure to run.
Does not enable successfully calling endpoints as underlying services are missing.

## Start

First, copy keystore-wallet-app-bff-local.p12 into src/main/resources.

```shell
cp keystore-wallet-app-bff-local.p12 ../src/main/resources
```

When that is done you can test the application with

```bash
docker-compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

or to let spring boot start the containers you can run

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev -Denv=dev
```

## Notes

The keycloak is bootstrapped with two users: test1/test1 and test2/test2.

These two users have fake swedish personal numbers as last names, and the keycloak will add those personal numbers as claims.
