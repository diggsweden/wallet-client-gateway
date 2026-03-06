# Docker compose

Starts the necessary infrastructure to run.
Does not enable successfully calling endpoints as underlying services are missing.

## Start

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
