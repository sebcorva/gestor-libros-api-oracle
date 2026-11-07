# PRD — Gestor de Libros (AF2: "Aplicando un patrón de diseño a nuestro desarrollo")

> Creado: 2026-11-06 · Actividad formativa 2, semana 3 (individual)

## 1. Contexto y objetivo

Construir una aplicación web en **Angular** que gestione los libros de una biblioteca y que consuma el **microservicio de la semana 1** (CRUD de `/libros` sobre Oracle). El frontend debe construirse siguiendo un **patrón de diseño** y su **arquetipo**.

El backend de la semana 1 se recupera desde el commit `bfdbf23` del repo `gestor-biblioteca`. Ese repo después evolucionó hacia el foro de publicaciones con JWT, y no se toca.

## 2. Alcance

| Repo | Carpeta | Contenido |
|---|---|---|
| Backend | `PROGRAMACION/gestor-libros-ms` | Spring Boot + Oracle Autonomous (wallet), CRUD `/libros` de la semana 1 + CORS |
| Frontend | `PROGRAMACION/gestor-libros-front` | Angular CLI (standalone), consume `gestor-libros-ms` |

**Fuera de alcance:** autenticación/JWT, la migración a PostgreSQL (eso es el proyecto de portafolio `Portafolio/gestor-biblio`) y el despliegue en la nube. El backend corre en local.

## 3. Modelo de datos

`Libro`, tabla `libros` en Oracle:

| Campo (JSON) | Tipo Java | Columna | Reglas |
|---|---|---|---|
| `id` | `Long` | `id` (IDENTITY) | generado por la BD |
| `titulo` | `String` | `titulo` | obligatorio |
| `autor` | `String` | `autor` | obligatorio |
| `anioPublicacion` | `Integer` | `anio_publicacion` | obligatorio |
| `genero` | `String` | `genero` | obligatorio |

En TypeScript se replica como `interface Libro { id?: number; titulo: string; autor: string; anioPublicacion: number; genero: string; }`. Los nombres deben coincidir con el JSON del backend.

## 4. Contrato de la API (backend)

Base URL local: `http://localhost:8080` (o `8081` si convive con `gestor-biblioteca`; definirlo en el issue #1).

| Método | Ruta | Uso en el front | Respuesta esperada |
|---|---|---|---|
| GET | `/libros` | Listado | `200` + `Libro[]` |
| GET | `/libros/{id}` | Detalle / cargar el formulario de edición | `200` + `Libro` (ver issue #2: caso inexistente) |
| POST | `/libros` | Crear | `200`/`201` + `Libro` creado |
| PUT | `/libros/{id}` | Editar | `200` + `Libro` actualizado |
| DELETE | `/libros/{id}` | Eliminar | `200`/`204` |

**CORS:** permitir el origen `http://localhost:4200` para `GET, POST, PUT, DELETE`.

## 5. Patrón de diseño y arquetipo (frontend)

- **Arquetipo:** el proyecto se genera con **Angular CLI** (`ng new`) y los artefactos con `ng generate`. Eso da la estructura estándar (`src/app`, `environments`, `app.routes.ts`, `app.config.ts`).
- **Patrón principal: MVC aplicado a Angular, con Service + Inyección de Dependencias.**
  - **Model:** `models/libro.model.ts` (interface `Libro`).
  - **View:** los templates `.html` de cada componente.
  - **Controller:** la clase `.ts` de cada componente. Maneja los eventos de la vista y delega en el servicio.
  - **Service:** `services/libro.service.ts` con `@Injectable({ providedIn: 'root' })`, una única instancia (Singleton) inyectada por DI. Es la única pieza que conoce la URL del backend y usa `HttpClient`.
- **Patrón de apoyo: Observer.** Los métodos del servicio devuelven `Observable<T>` (RxJS) y los componentes se suscriben.
- **Justificación (para el README):** separa responsabilidades. Si mañana cambia la URL o el backend, solo cambian `environment` y el servicio, y los componentes no se tocan. Además cada parte se puede probar por separado.

Estructura objetivo:

```
src/app/
├── models/libro.model.ts
├── services/libro.service.ts
├── components/
│   ├── libro-list/        # tabla con todos los libros + acciones
│   ├── libro-detail/      # detalle por ID
│   └── libro-form/        # crear y editar (mismo componente)
├── app.routes.ts
└── app.config.ts          # provideHttpClient()
src/environments/environment.ts   # apiUrl
```

Rutas: `/libros` (listado, por defecto), `/libros/nuevo`, `/libros/:id`, `/libros/:id/editar`.

## 6. Requisitos funcionales

1. **RF1 — Listar:** ver todos los libros en una tabla (ID, título, autor, año, género).
2. **RF2 — Detalle:** buscar o ver un libro por su ID. Si no existe, mostrar un mensaje claro.
3. **RF3 — Crear:** formulario con validación en el cliente (campos obligatorios, año numérico y positivo).
4. **RF4 — Editar:** el mismo formulario precargado con `GET /libros/{id}`, que envía `PUT`.
5. **RF5 — Eliminar:** pedir confirmación, enviar `DELETE` y refrescar el listado.
6. **RF6 — Errores:** si el backend está caído o responde un error, mostrar un aviso en la UI en vez de dejar la pantalla en blanco.

## 7. Backlog (issues)

### Backend — `gestor-libros-ms`
- [ ] **#1 Recuperar el código de `bfdbf23`** en esta carpeta, crear un repo git nuevo y levantarlo contra Oracle (verificar la tabla `libros` y los datos; definir el puerto).
- [ ] **#2 Revisar las respuestas HTTP:** ¿qué devuelve `GET /libros/{id}` cuando el ID no existe? Decidir si se agrega 404 con `ResponseEntity`.
- [ ] **#3 CORS** para `http://localhost:4200`.
- [ ] **#4 (Opcional)** DTOs + `@Valid` + manejo global de errores (se pueden reutilizar los de `Portafolio/gestor-biblio`).

### Frontend — `gestor-libros-front`
- [ ] **#5 Arquetipo:** `ng new` + repo git + estructura de carpetas.
- [ ] **#6 Model + environment** (`apiUrl`).
- [ ] **#7 `LibroService`:** los 5 métodos con `HttpClient`.
- [ ] **#8 `libro-list`:** listado + eliminar.
- [ ] **#9 `libro-detail`:** detalle por ID.
- [ ] **#10 `libro-form`:** crear/editar con Reactive Forms.
- [ ] **#11 Rutas + navegación + manejo de errores.**
- [ ] **#12 Entrega:** README de cada repo (cómo levantar, patrón explicado, capturas) y zip.

## 8. Criterios de aceptación

- Los 5 endpoints se consumen desde la UI contra la URL local del microservicio.
- Se muestran los 5 atributos del libro.
- El código del front respeta la estructura del patrón (los componentes nunca usan `HttpClient` directamente).
- El README explica el patrón y el arquetipo usados.
