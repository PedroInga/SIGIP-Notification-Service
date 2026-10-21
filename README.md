# SIGIP-Notification-Service

Notificaciones: avisos al cliente generados por eventos de pedidos.

## Funcionalidad

- Consume la cola `sigip.notificaciones` (exchange `sigip.pedidos`, routing `pedido.*`).
- Idempotente: un mismo `eventId` no genera dos avisos. Tras 3 reintentos fallidos el mensaje va a `sigip.notificaciones.dlq`.
- `GET /api/v1/notificaciones` (el cliente ve las suyas), `GET /no-leidas`, `PATCH /{id}/leida`.
- POST, PUT y DELETE para ADMIN.
- Sin RabbitMQ: `EVENTOS_HABILITADOS=false`.

## Pruebas

- `NotificacionRepositoryTest`, `NotificacionIntegrationTest` (idempotencia, propiedad, CRUD)

## Ejecución

Puerto `8099`. Base local `sigip_notification` (MySQL); en la nube, esquema propio en PostgreSQL.

Variables: `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (o `DB_URL` completa).

## Repositorio

Este repositorio es uno de los 12 de SIGIP (un repositorio por microservicio). Es un proyecto Maven
independiente; su única dependencia propia es la librería **SIGIP-Common** (`pe.edu.cibertec.sigip:sigip-common`),
que vive en su propio repositorio y se instala en el Maven local una sola vez.

## Compilar y ejecutar

Requisitos: Java 25 (Maven no hace falta, cada repo trae `mvnw`).

```powershell
# 1. Una sola vez (y cada vez que cambie SIGIP-Common): instalar la librería compartida
cd ..\SIGIP-Common
.\mvnw.cmd install -DskipTests

# 2. Este servicio
cd ..\SIGIP-Notification-Service
.\mvnw.cmd clean verify          # compila y ejecuta las pruebas
.\mvnw.cmd spring-boot:run       # arranca en el puerto 8099
```

En Linux/macOS usa `./mvnw` en lugar de `.\mvnw.cmd`.

## Docker

La imagen compila también SIGIP-Common, así que necesita su carpeta como contexto adicional:

```powershell
docker build --build-context common=..\SIGIP-Common -t sigip/notification-service .
```

Para levantar todo SIGIP junto (los 12 repos, MySQL y RabbitMQ) usa el `docker-compose.yml` de la carpeta
`SIGIP` que contiene a los 12 repositorios.
