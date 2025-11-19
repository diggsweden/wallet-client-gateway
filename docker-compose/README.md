# Start
First, copy keystore-wallet-app-bff-local.p12 into src/main/resources.

```shell
cp keystore-wallet-app-bff-local.p12 ../src/main/resources
```

When that is done you can test the application with
```bash
docker-compose up -d
mvn spring-boot:run
```