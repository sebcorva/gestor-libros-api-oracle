# PRD — Gestor de Libros (S3 "Aplicando un patrón de diseño a nuestro desarrollo")


## 1. Contexto y objetivo

Construir una aplicación web en **Angular** que gestione los libros de una biblioteca y que consuma el **microservicio de la semana 1** (CRUD de `/libros` sobre Oracle). El frontend debe construirse siguiendo un **patrón de diseño** y su **arquetipo**.

El backend de la semana 1 se recupera desde el commit `bfdbf23` del repo `gestor-biblioteca`. Ese repo después evolucionó hacia el foro de publicaciones con JWT, y no se toca.

Este documento cubre **solo el backend**. El frontend tiene su propio PRD en `gestor-libros-front/docs/PRD.md`.

## 2. Alcance

| Repo | Carpeta | Contenido |
|---|---|---|
| Backend | `PROGRAMACION/gestor-libros-ms` | Spring Boot 4.1 / Java 17 + Oracle Autonomous (wallet), CRUD `/libros` + CORS + manejo de errores |

**Consumidor:** el frontend Angular `gestor-libros-front`, que corre en `http://localhost:4200` (de ahí el origen CORS de §4).

**Fuera de alcance:** autenticación/JWT, la migración a PostgreSQL (eso es el proyecto de portafolio `Portafolio/gestor-biblio`), el despliegue en la nube, Docker y las variables de entorno para las credenciales (se mantienen en `application.properties` a propósito).

## 3. Modelo de datos

`Libro`, tabla `libros` en Oracle:

| Campo (JSON) | Tipo Java | Columna | Reglas |
|---|---|---|---|
| `id` | `Long` | `id` (IDENTITY) | generado por la BD (se ignora si viene en el body) |
| `titulo` | `String` | `titulo` | obligatorio |
| `autor` | `String` | `autor` | obligatorio |
| `anioPublicacion` | `Integer` | `anio_publicacion` `NUMBER(4)` | obligatorio, positivo |
| `genero` | `String` | `genero` | obligatorio |

Los nombres del JSON son el contrato con el frontend: no se cambian.

## 4. Contrato de la API (backend)

Base URL local: `http://localhost:8080`.

| Método | Ruta | Éxito | Error |
|---|---|---|---|
| GET | `/libros` | `200` + `Libro[]` | — |
| GET | `/libros/{id}` | `200` + `Libro` | `404` |
| POST | `/libros` | `201` + `Libro` creado | `400` (validación) |
| PUT | `/libros/{id}` | `200` + `Libro` actualizado | `404`, `400` |
| DELETE | `/libros/{id}` | `204` sin body | `404` |

Formato de error (igual al del profesor):
- 404: `{ "status": 404, "message": "No existe el libro con id 99", "timestamp": "..." }`
- 400 por validación: `{ "status": 400, "errors": { "titulo": "El titulo es obligatorio", ... }, "timestamp": "..." }`
- 400 por body o parámetro inválido: `{ "status": 400, "message": "...", "timestamp": "..." }`
- 500: `{ "status": 500, "message": "Ocurrio un error inesperado", "timestamp": "..." }`, sin stacktrace (el detalle queda en el log).

Regla para el front: si viene `errors`, es un mapa campo→mensaje; si no, se muestra `message`.

**CORS:** `CorsConfig implements WebMvcConfigurer`, mapping `/libros/**`, origen `http://localhost:4200` (leído de `app.cors.allowed-origins`), métodos `GET, POST, PUT, DELETE`.

### 4.1 Estructura del backend

Arquitectura en capas de Spring:

| `gestor-libros-ms` (`aa5ec52`) | Estado | Qué falta | Issue |
|---|---|---|---|
| `controller/LibroController` | ✅ | — (`ResponseEntity` en todos los endpoints desde el #3) | — |
| `config/CorsConfig` | ✅ | — | — |
| `service/LibroService` + `LibroServiceImpl` | ✅ | — | — |
| `repository/LibroRepository` | ✅ | — | — |
| `model/Libro` | ✅ | — (entidad JPA pura desde el #2) | — |
| `dto/request/LibroRequest` | ✅ | — | — |
| `dto/response/LibroResponse` | ✅ | — | — |
| `dto/mapper/LibroMapper` | ✅ | — | — |
| `exception/LibroNoEncontradoException` | ✅ | — | — |
| `exception/GlobalExceptionHandler` | ✅ | — | — |
| `sql/libros.sql` (DDL + DML) | ✅ | — | — |
| `app.cors.allowed-origins` en properties | ✅ | — | — |

## 5. Backlog (issues)

- [x] **#1 Recuperar el código de `bfdbf23`** en esta carpeta, crear un repo git nuevo y levantarlo contra Oracle (verificar la tabla `libros` y los datos; definir el puerto).
  > Hecho: extraído con `git archive` y commiteado (`aa5ec52`), remoto `sebcorva/gestor-libros-api-oracle`. Levanta en el **8080** y conecta a Oracle con `ADMIN`. La tabla `libros` existía pero estaba vacía: se cargaron los 4 libros del script original por `POST` (ids **21–24**, porque la IDENTITY ya iba en 20). El primer arranque falló con `Not a managed type: Libro`, porque VS Code recompiló `target/classes` en plena partida de DevTools; se resolvió volviendo a arrancar.
  >
  > **Estado "antes"** (lo que corrigen los issues #2–#5):
  > | Prueba | Respuesta actual | Esperado |
  > |---|---|---|
  > | `GET /libros/999` | `200` + `null` | `404` |
  > | `PUT /libros/999` | `500` + stacktrace (`IllegalArgumentException`) | `404` |
  > | `DELETE /libros/999` | `200` vacío (no revisa nada) | `404` |
  > | `GET /libros/0` | `500` + stacktrace | `404` o `400` |
  > | `POST {}` | `500` (valida Hibernate al persistir, no Spring) | `400` con mapa de campos |
  > | `POST` válido | `200` | `201` |
  > | Origen `localhost:4200` | sin `Access-Control-Allow-Origin` → el navegador bloquea | header presente |
- [x] **#2 DTOs:** no exponer la entidad JPA en el contrato HTTP.
  > Hecho: `dto/request/LibroRequest` (un solo record para POST y PUT, porque los dos reciben lo mismo; nombre en par con `LibroResponse` y con la convención del foro), `dto/response/LibroResponse` y `dto/mapper/LibroMapper` (clase utilitaria `final` con constructor privado; `toEntity` reutiliza `updateEntity`). Partió de los DTOs de `Portafolio/gestor-biblio`, uniendo `CrearLibroRequest` y `ActualizarLibroRequest`. El **service** recibe `LibroRequest` y devuelve `LibroResponse`, así que el controller ya no conoce la entidad. `Libro` quedó como entidad JPA pura (validaciones fuera, `nullable = false` en las columnas). Se **adelantó `@Valid`** en POST y PUT: al sacar las validaciones de la entidad, sin `@Valid` nada habría validado, y un `POST {}` habría llegado hasta el `NOT NULL` de Oracle. Por eso un `POST {}` ya responde `400` (con el cuerpo por defecto de Spring; el formato JSON propio queda para el #5).
  >
  > Verificado con curl: el JSON de respuesta es idéntico al de antes; un `POST` con `"id": 21` crea un libro **nuevo** y el 21 no cambia; un `PUT /libros/22` con `"id": 99` actualiza el 22; `POST {}` y un año negativo dan `400`. Sin cambios a propósito (son el #3): `GET /libros/999` → `200` + `null`.
- [x] **#3 404 en JSON + `ResponseEntity`:** responder 404 cuando el libro no existe y unificar las respuestas del controller.
  > Hecho: `exception/LibroNoEncontradoException` (`RuntimeException`, para no arrastrar `throws` por todas las firmas) y `exception/GlobalExceptionHandler` con `@RestControllerAdvice` y el handler del 404 (`message`, `status`, `timestamp`). El handler arma el cuerpo con un helper privado `respuesta(...)`, que reutilizarán el 400 y el 500 del #5. En el service, un helper privado `buscarLibro(id)` hace `findById(...).orElseThrow(...)` y lo usan `getLibroById` (que ahora devuelve `LibroResponse`, no `Optional`) y `updateLibro`. `deleteLibro` revisa `existsById`, porque `deleteById` ignora en silencio un id que no existe. Se **eliminaron las validaciones `id <= 0`**: un id 0 o negativo simplemente no existe, así que da 404 y ya no queda ningún `IllegalArgumentException` que termine en 500.
  >
  > Por la decisión de usar **`ResponseEntity`** (mismo estilo que el foro), el controller quedó con los status definitivos y se **adelantaron los 201 y 204 del #5**: `ok(...)` en GET y PUT, `status(HttpStatus.CREATED).body(...)` en POST (201; sin header `Location`, porque el front toma el `id` del body) y `noContent().build()` en DELETE (204).
  >
  > Verificado con curl: `GET /libros/999`, `/0` y `/-5`, `PUT /libros/999` y `DELETE /libros/999` → `404` con JSON; `PUT /libros/22` → `200`; `POST` → `201`; `DELETE` de un libro existente → `204`, y repetirlo → `404`. Nota: Oracle reserva la IDENTITY en bloques de 20, así que los ids nuevos saltan (41, 61…); es normal.
- [x] **#4 CORS:** `CorsConfig` para `http://localhost:4200`, con el origen en `application.properties`.
  > Hecho: `config/CorsConfig implements WebMvcConfigurer` (en `config/` y no en `controller/`, porque es configuración, igual que `SecurityConfig` en el foro), con mapping `/libros/**`, métodos `GET, POST, PUT, DELETE` y el origen leído con `@Value("${app.cors.allowed-origins}")` como `String[]` (admite varios separados por coma). En `application.properties`: `app.cors.allowed-origins=http://localhost:4200`. Así, cambiar el origen (otro puerto o un despliegue) no requiere recompilar.
  >
  > Verificado con curl: el *preflight* `OPTIONS` desde `localhost:4200` responde `200` con `Access-Control-Allow-Origin: http://localhost:4200` para GET, POST, PUT y DELETE; `PATCH` y un origen ajeno → `403`. Las respuestas de error también llevan el header (`404` y `400` con `Origin: 4200`), así que Angular puede leer el mensaje. Sin `Origin` (Postman) todo sigue igual: CORS es una regla del **navegador**, no del servidor.
- [x] **#5 Errores 400 y 500:** en `GlobalExceptionHandler`, 400 con mapa de campos (`MethodArgumentNotValidException`, que ya se lanza gracias al `@Valid` del #2) y 500 genérico sin stacktrace. Revisar también los 400 que hoy salen con el cuerpo por defecto de Spring (body malformado, `GET /libros/abc`).
  > Hecho: cuatro handlers nuevos que usan el mismo helper `respuesta(...)`:
  > - `MethodArgumentNotValidException` → `400` con `errors` (campo → mensaje). Usa `putIfAbsent` para que, si un campo falla dos reglas, quede el primer mensaje y no se pise.
  > - `HttpMessageNotReadableException` → `400` con `message` (JSON mal formado, body vacío o `"anioPublicacion": "abc"`).
  > - `MethodArgumentTypeMismatchException` → `400` con `message` (`GET /libros/abc`).
  > - `Exception` → `500` con mensaje genérico y `log.error(...)` con el detalle (`@Slf4j`). **Trampa evitada:** este handler también atrapa excepciones propias de Spring, así que si la excepción implementa `ErrorResponse` (ruta inexistente 404, método no permitido 405, `Content-Type` no soportado 415) se respeta su status en vez de devolver 500.
  >
  > La prueba del 500 encontró un hueco de validación: `genero` no tenía `@Size` y un valor de 300 caracteres llegaba a Oracle (`ORA-12899`, máximo 255) → 500. Se agregó `@Size(max = 100)` en `LibroRequest`, el mismo límite que `autor`, y ahora da `400`.
  >
  > Verificado con curl: `POST {}` → `400` con los 4 campos; campos en blanco o año negativo → `400` solo con los campos que fallan; `PUT` sin autor → `400`; JSON malformado, sin body o año como texto → `400`; `/libros/abc` → `400`; `/libros/999` → `404`; `/no-existe` → `404`; `PATCH` → `405`; `text/plain` → `415`; los errores llevan el header CORS.
- [x] **#6 Entrega:** README actualizado (cómo levantar, endpoints con los status reales, DTOs, CORS, errores), `sql/libros.sql`, colección Postman con los 5 requests y los casos de error, y zip.
  > Hecho:
  > - **README** reescrito con la estructura del del foro: contexto (AF1 → AF2, extraído de `bfdbf23`), tecnologías, cómo ejecutar (aviso del puerto 8080 compartido con el foro), endpoints con los status reales, ejemplos de request y response, tabla de validaciones, errores (formatos `errors`/`message` y tabla de códigos), CORS, Postman, base de datos y estructura de paquetes. Se corrigieron las ambigüedades del README de la semana 1 ("200 / 201", "404 o vacío", `NUMBER(10)`).
  > - **`sql/libros.sql`**: DDL de `libros` + los 4 libros de prueba. Los largos de las columnas (255) son mayores o iguales a los límites de `LibroRequest`, así la API rechaza con 400 antes que Oracle.
  > - **`postman/gestor-libros-ms.postman_collection.json`** (v2.1, sin environment, variables de colección `baseUrl`, `libroId`, `idInexistente`, `origenFront`): 5 carpetas en orden (Consultar, Crear, Actualizar, CORS, Eliminar), cubre los 5 endpoints, los 400/404 y CORS (origen permitido, error con header y origen ajeno → 403). Crea sus propios libros y los borra, así que deja la base como estaba. Corrida con el Postman CLI: **19 requests, 36 assertions, 0 fallos**.
  > - **`script-zip.sh`** (copiado del foro): genera `gestor-libros-ms-entrega.zip` (59 KB) sin `target/`, `.git/`, `docs/`, `.vscode/` ni scripts; incluye la wallet. El zip está en `.gitignore`.

## 6. Criterios de aceptación

- Los 5 endpoints responden según §4: 200/201/204 en éxito, y 404/400 en JSON y sin stacktrace en error.
- Una llamada desde `http://localhost:4200` recibe el header `Access-Control-Allow-Origin`, y el navegador no muestra errores de CORS.
- El JSON respeta los nombres de §3, que son los que usa el frontend.
