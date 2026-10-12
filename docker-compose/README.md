# Docker compose

Starts the necessary infrastructure to run.
Does not enable successfully calling endpoints as underlying services are missing.

## Start

When that is done you can test the application with

```bash
podman compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

or with Docker installed, to let Spring Boot start the containers you can run

```bash
SPRING_DOCKER_COMPOSE_ENABLED=true mvn spring-boot:run -Dspring-boot.run.profiles=dev -Denv=dev
```

## Notes
