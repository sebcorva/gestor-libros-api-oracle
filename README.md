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

## Cómo ejecutar

La wallet de Oracle (`src/main/resources/wallet/`) y la configuración (`application.properties`) vienen incluidas en el repositorio, así que no hace falta configurar nada.

```bash
cd gestor-libros-ms
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080/libros`. La tabla se crea sola al arrancar (`ddl-auto=update`); para cargar los datos de prueba ver [Base de datos](#base-de-datos).

> Si `gestor-biblioteca` (el foro) está corriendo, ocupa el mismo puerto `8080`: detenerlo antes.

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

[`sql/libros.sql`](sql/libros.sql) contiene el **DDL** de la tabla `libros` y los **4 libros de prueba**, para ejecutar conectado como `ADMIN` (SQL Developer Web o SQL Developer). Hibernate crea la tabla sola con `ddl-auto=update`, así que el script sirve para recrearla desde cero con los datos.

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
