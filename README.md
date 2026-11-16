# gestor-libros-ms

Microservicio REST para la **gestión de libros de una biblioteca** (CRUD) sobre **Oracle Autonomous Database**.

Es el microservicio de la actividad formativa 1 (semana 1), reutilizado en la semana 3 como backend del frontend Angular **`gestor-libros-front`**. El código base se extrajo del repo `gestor-biblioteca` (commit `bfdbf23`) y se le agregaron DTOs, manejo de errores en JSON y CORS para que el frontend pueda consumirlo.

```
Navegador (Angular, localhost:4200) ──HTTP + CORS──► gestor-libros-ms (localhost:8080) ──JPA──► Oracle (tabla LIBROS)
```

## Tecnologías

- Java 17 y Spring Boot 4.1.2 (Web MVC, Data JPA, Validation)
- Oracle Autonomous Database (conexión con wallet, driver `ojdbc11`)
- Lombok
- H2 en memoria (solo para los tests)
- Docker, GitHub Actions y Amazon ECR (CI/CD)

## Configuración

El repositorio **no contiene credenciales ni el wallet de Oracle**. Se entregan con variables de entorno:

| Variable | Para qué | Obligatoria |
|---|---|---|
| `DB_USERNAME` | Usuario de Oracle (`ADMIN`) | Sí |
| `DB_PASSWORD` | Contraseña de Oracle | Sí |
| `TNS_ADMIN` | Carpeta del wallet. **Solo para ejecutar con Maven**; en Docker la imagen ya usa `/app/wallet` | Solo con Maven |
| `ORACLE_WALLET_BASE64` | Zip del wallet codificado en base64. **Solo para Docker** (Compose, VM o ECS) | Solo con Docker |
| `DB_TNS_ALIAS` | Alias de `tnsnames.ora` a usar | No (por defecto `q5x4fu7bstfvswj5_high`) |

En local se guardan en un archivo **`.env`** en la raíz del proyecto (está en `.gitignore`, nunca se sube). Partir de la plantilla:

```bash
cp .env.example .env   # y completar los valores
```

### Generar `ORACLE_WALLET_BASE64`

El wallet es un zip binario y las variables de entorno solo aceptan texto, por eso se codifica en base64 (no es cifrado, solo una traducción a texto). El zip debe tener los archivos **en la raíz, sin carpeta** (igual que el que se descarga de Oracle Cloud):

```bash
# Si solo tienes la carpeta del wallet, crear el zip "plano" (-j quita la carpeta)
zip -j -X Wallet_XXXX.zip Wallet_XXXX/* -x '*.DS_Store'

# Codificar en una sola línea
base64 -i Wallet_XXXX.zip | tr -d '\n'   # macOS
base64 -w0 Wallet_XXXX.zip               # Linux
```

El resultado (empieza con `UEsD…`, la firma de todo zip) se pega en `.env` como `ORACLE_WALLET_BASE64=UEsD...`.

Al arrancar el contenedor, [`docker-entrypoint.sh`](docker-entrypoint.sh) decodifica la variable, valida el zip (sin rutas peligrosas y con `tnsnames.ora`), lo extrae en `/app/wallet`, borra la variable y recién entonces inicia Java.

## Cómo ejecutar

La API queda disponible en `http://localhost:8080/libros`. La tabla se crea sola al arrancar (`ddl-auto=update`); para cargar los datos de prueba ver [Base de datos](#base-de-datos).

> Si `gestor-biblioteca` (el foro) está corriendo, ocupa el mismo puerto `8080`: detenerlo antes.

### Opción 1: con Maven

Usa `DB_USERNAME`, `DB_PASSWORD` y `TNS_ADMIN` del `.env` (Spring lo lee solo gracias a `spring.config.import=optional:file:.env[.properties]`).

```bash
./mvnw spring-boot:run
```

### Opción 2: con Docker Compose

Construye la imagen y la ejecuta igual que en la nube: el wallet llega como `ORACLE_WALLET_BASE64` desde el `.env`.

```bash
docker compose up --build      # Ctrl+C para detener
docker compose down            # eliminar el contenedor
```

### Tests

```bash
./mvnw test
```

Usan el perfil `test` ([`application-test.properties`](src/test/resources/application-test.properties)) con H2 en memoria, así que **no necesitan Oracle, wallet ni `.env`**. Así corren también en GitHub Actions.

## CI/CD (GitHub Actions → Amazon ECR)

[`.github/workflows/ci-cd.yml`](.github/workflows/ci-cd.yml) tiene dos jobs:

| Job | Cuándo corre | Qué hace |
|---|---|---|
| `test` | Pull request hacia `main`, push a `main` y ejecución manual | Prueba `docker-entrypoint.sh` con un wallet y un `java` falsos, y ejecuta `mvn verify` (H2) |
| `publish` | Solo push a `main` o ejecución manual, y solo si `test` pasó | Verifica la cuenta de AWS, crea el repo de ECR si no existe, construye la imagen (`linux/amd64`, con caché) y la publica |

La imagen queda en `843987653213.dkr.ecr.us-east-1.amazonaws.com/gestor-libros-ms` con dos etiquetas:

- `:<sha del commit>`: única, identifica exactamente qué código lleva.
- `:latest`: siempre apunta a la más reciente.

**Secrets requeridos** (GitHub → *Settings → Secrets and variables → Actions*): `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` y `AWS_SESSION_TOKEN`.

> **AWS Academy:** las credenciales del Learner Lab caducan cada vez que la sesión termina. Antes de hacer push a `main`, copiarlas de nuevo desde *AWS Details → AWS CLI* a los 3 secrets. Si caducaron, el job `publish` falla con `ExpiredToken`; tras actualizarlas se puede re-ejecutar desde *Actions → Run workflow* sin hacer otro commit.

La imagen **no contiene el wallet ni credenciales**: se entregan al ejecutar el contenedor.

## Ejecutar en una VM (EC2)

Pasos para correr la imagen publicada en ECR en una máquina virtual de AWS Academy.

### 1. Crear la instancia

En la consola de EC2 → *Launch instance*:

| Opción | Valor | Por qué |
|---|---|---|
| AMI | Amazon Linux 2023 | Trae la AWS CLI instalada |
| Arquitectura / tipo | **x86_64**, `t3.small` | La imagen es `linux/amd64` (no usar tipos Graviton como `t4g`); Spring Boot necesita ~1 GB de RAM |
| Key pair | Crear o elegir uno (`.pem`) | Para conectarse por SSH |
| Security group | Entrada **22** (SSH) y **8080** (API) desde *My IP* | Abrir solo lo necesario y solo a tu IP |
| *Advanced details → IAM instance profile* | **`LabInstanceProfile`** | Le da permisos a la VM (rol `LabRole`) para descargar de ECR **sin copiar claves de AWS** a la VM |

### 2. Instalar Docker en la VM

```bash
ssh -i mi-llave.pem ec2-user@<IP_PUBLICA>

sudo dnf install -y docker
sudo systemctl enable --now docker      # iniciar Docker ahora y en cada reinicio
sudo usermod -aG docker ec2-user        # usar docker sin sudo
exit                                    # salir y volver a entrar para que aplique el grupo
```

### 3. Copiar las variables de entorno a la VM

Desde **tu equipo**, generar un `.env` para la VM **sin `TNS_ADMIN`** y copiarlo:

```bash
grep -v '^TNS_ADMIN=' .env > vm.env
scp -i mi-llave.pem vm.env ec2-user@<IP_PUBLICA>:~/.env
rm vm.env
```

> **¿Por qué sin `TNS_ADMIN`?** En tu `.env` apunta a la carpeta de tu equipo. `--env-file` sobrescribe el `TNS_ADMIN=/app/wallet` de la imagen, y el entrypoint intentaría extraer el wallet en una ruta que no existe en el contenedor (`cannot create extraction directory`). La VM solo necesita `DB_USERNAME`, `DB_PASSWORD` y `ORACLE_WALLET_BASE64` (más `DB_TNS_ALIAS` si se usa).

En la VM, proteger el archivo para que solo lo lea `ec2-user`:

```bash
chmod 600 ~/.env
```

### 4. Descargar y ejecutar la imagen

```bash
REGISTRY=843987653213.dkr.ecr.us-east-1.amazonaws.com

# Login en ECR (usa los permisos de LabInstanceProfile)
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin $REGISTRY

docker run -d --name gestor-libros \
  --restart unless-stopped \
  -p 8080:8080 \
  --env-file ~/.env \
  $REGISTRY/gestor-libros-ms:latest

docker logs -f gestor-libros            # esperar "Started GestorBibliotecaApplication" (Ctrl+C para salir)
curl http://localhost:8080/libros
```

| Opción | Qué hace |
|---|---|
| `-d` | Corre en segundo plano |
| `--restart unless-stopped` | Reinicia el contenedor si falla o si la VM se reinicia |
| `-p 8080:8080` | Expone el puerto de la API en la VM |
| `--env-file ~/.env` | Entrega credenciales y wallet al contenedor |

Desde tu navegador o Postman: `http://<IP_PUBLICA>:8080/libros`.

### 5. Actualizar a una nueva versión

Después de que el workflow publique una imagen nueva:

```bash
docker pull $REGISTRY/gestor-libros-ms:latest
docker rm -f gestor-libros
docker run -d --name gestor-libros --restart unless-stopped -p 8080:8080 --env-file ~/.env $REGISTRY/gestor-libros-ms:latest
```

Para fijar una versión exacta, usar `:<sha>` en vez de `:latest`.

### Notas

- **CORS:** si el frontend Angular llama a la API de la VM, agregar su origen sin recompilar con `APP_CORS_ALLOWEDORIGINS=http://localhost:4200,http://<IP_DEL_FRONT>` en el `~/.env` de la VM (Spring traduce esa variable a `app.cors.allowed-origins`).
- **Oracle:** si la Autonomous Database tiene una lista de control de acceso (ACL), agregar la IP pública de la VM en Oracle Cloud.
- **AWS Academy:** al terminar la sesión del lab la instancia se detiene. Al volver a iniciarla **cambia la IP pública**, y el contenedor arranca solo gracias a `--restart unless-stopped`.

## Endpoints

| Método | Ruta | Descripción | Éxito | Error |
|---|---|---|---|---|
| GET | `/libros` | Lista todos los libros | `200` | — |
| GET | `/libros/{id}` | Obtiene un libro por id | `200` | `404`, `400` (id no numérico) |
| POST | `/libros` | Crea un libro | `201` | `400` |
| PUT | `/libros/{id}` | Actualiza un libro | `200` | `404`, `400` |
| DELETE | `/libros/{id}` | Elimina un libro | `204` (sin cuerpo) | `404` |

Cuerpo de `POST` y `PUT` (`LibroRequest`):

```json
{
  "titulo": "Rayuela",
  "autor": "Julio Cortázar",
  "anioPublicacion": 1963,
  "genero": "Novela"
}
```

Respuesta (`LibroResponse`, la misma forma en todos los endpoints):

```json
{
  "id": 21,
  "titulo": "Rayuela",
  "autor": "Julio Cortázar",
  "anioPublicacion": 1963,
  "genero": "Novela"
}
```

El `id` no forma parte del request: lo genera Oracle en el `POST` y en el `PUT` viaja en la URL. Si se envía un `id` en el body, se ignora (no se puede pisar otro libro).

### Validaciones

| Campo | Reglas |
|---|---|
| `titulo` | Obligatorio, máximo 150 caracteres |
| `autor` | Obligatorio, máximo 100 caracteres |
| `anioPublicacion` | Obligatorio, número positivo |
| `genero` | Obligatorio, máximo 100 caracteres |

## Errores

Todas las respuestas de error tienen `status`, `timestamp` y **`errors`** (validación) o **`message`** (el resto):

```json
{ "status": 400, "errors": { "titulo": "El titulo es obligatorio", "anioPublicacion": "El año de publicacion debe ser positivo" }, "timestamp": "2026-11-07T01:21:49" }
```

```json
{ "status": 404, "message": "No existe el libro con id 999", "timestamp": "2026-11-07T01:21:49" }
```

| Código | Cuándo |
|---|---|
| `400` | Validación fallida (`errors` indica cada campo), JSON mal formado o vacío, tipo incorrecto (`"anioPublicacion": "abc"`), id no numérico (`/libros/abc`) |
| `404` | Libro inexistente, o ruta inexistente |
| `405` | Método HTTP no permitido (por ejemplo `PATCH`) |
| `415` | `Content-Type` distinto de `application/json` |
| `500` | Error inesperado: mensaje genérico, sin detalles internos (el detalle queda en el log del servidor) |

## CORS

El frontend Angular corre en otro puerto (`4200`), así que el navegador bloquea sus llamadas a menos que la API lo permita. `config/CorsConfig` habilita el origen definido en `application.properties`:

```properties
app.cors.allowed-origins=http://localhost:4200
```

- Aplica a `/libros/**` con los métodos `GET`, `POST`, `PUT` y `DELETE`.
- Para agregar otro origen (otro puerto o un despliegue), se agrega separado por coma, sin recompilar.
- Las respuestas de error también llevan el header CORS, así el frontend puede leer el mensaje.
- Postman y curl no aplican CORS (es una regla del navegador), por eso funcionan con o sin esta configuración.

## Prueba con Postman

En Postman: **Import** → [`postman/gestor-libros-ms.postman_collection.json`](postman/gestor-libros-ms.postman_collection.json).

- No necesita environment: la URL (`baseUrl`) y el id del libro creado (`libroId`) se guardan como **variables de la colección**.
- Ejecutar las carpetas en orden (o **Run collection**): `1. Consultar` → `2. Crear` → `3. Actualizar` → `4. CORS` → `5. Eliminar`. La carpeta 2 crea un libro y la 5 lo elimina, así que la colección **deja la base como estaba**.
- Cada request tiene tests con el código esperado y el contenido de la respuesta: **19 requests, 36 assertions**.

También se puede correr desde la terminal con el Postman CLI:

```bash
postman collection run postman/gestor-libros-ms.postman_collection.json
```

## Base de datos

[`sql/Script_libros.sql`](sql/Script_libros.sql) contiene el **DDL** de la tabla `libros` y los **4 libros de prueba**, para ejecutar conectado como `ADMIN` (SQL Developer Web o SQL Developer). Hibernate crea la tabla sola con `ddl-auto=update`, así que el script sirve para recrearla desde cero con los datos.

> Oracle reserva los valores de la `IDENTITY` en bloques de 20, así que los ids pueden saltar (21, 41, 61…). Es normal.

## Estructura

```
src/main/java/com/duoc/gestor_biblioteca/
├── config/        CorsConfig (permite el frontend Angular)
├── controller/    LibroController (REST /libros, responde con ResponseEntity)
├── dto/
│   ├── request/   LibroRequest (record con las validaciones, sin id)
│   ├── response/  LibroResponse (record con los 5 campos)
│   └── mapper/    LibroMapper (entidad ⇄ DTOs)
├── exception/     LibroNoEncontradoException y GlobalExceptionHandler (errores en JSON)
├── model/         Libro (entidad JPA, tabla LIBROS)
├── repository/    LibroRepository (JpaRepository)
└── service/       LibroService + LibroServiceImpl (reglas: 404 si no existe)
```

Arquitectura en capas: el controller solo conoce los DTOs, el service mapea con `LibroMapper` y es el único que usa la entidad y el repositorio.

Archivos de despliegue en la raíz:

```
├── Dockerfile               Imagen multi-stage (Maven/JDK 17 → JRE 17, usuario sin privilegios)
├── docker-entrypoint.sh     Reconstruye el wallet desde ORACLE_WALLET_BASE64 e inicia Java
├── docker-compose.yml       Ejecución local de la imagen con el .env
├── .dockerignore            Excluye .env, wallet, .git y artefactos del build
├── .env.example             Plantilla de variables de entorno
└── .github/workflows/
    └── ci-cd.yml            Tests + publicación en ECR
```
