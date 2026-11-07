# Microservicio Gestor de Biblioteca 📚

Microservicio RESTful desarrollado en **Java 17** y **Spring Boot** para la gestión básica (CRUD) de libros en una biblioteca, con persistencia en **Oracle Autonomous Database** (Oracle Cloud) utilizando **Spring Data JPA**.

---

## 🛠️ Tecnologías y Herramientas

- **Java:** 17
- **Framework:** Spring Boot 4.x
- **Persistencia / ORM:** Spring Data JPA / Hibernate
- **Base de Datos:** Oracle Cloud Autonomous AI Database (con autenticación por Wallet)
- **Driver:** Oracle JDBC Driver (`ojdbc11`)
- **Productividad:** Lombok
- **Gestor de Dependencias:** Maven
- **Pruebas de API:** Postman / cURL

---

## 🏗️ Arquitectura del Sistema

El proyecto sigue una arquitectura en capas desacoplada basada en buenas prácticas y principios **SOLID**:

```
src/main/java/com/duoc/gestor_biblioteca/
├── GestorBibliotecaApplication.java      # Punto de entrada de la aplicación
├── controller/
│   └── LibroController.java              # Manejo de solicitudes HTTP y códigos de estado
├── service/
│   ├── LibroService.java                 # Interfaz con el contrato de lógica de negocio
│   └── LibroServiceImpl.java             # Implementación y validaciones defensivas
├── repository/
│   └── LibroRepository.java              # Interfaz de persistencia (extiende JpaRepository)
└── model/
    └── Libro.java                        # Entidad JPA mapeada a la tabla de Oracle
```

---

## 📋 Modelo de Datos (`Libro`)

La entidad se mapea a la tabla `LIBROS` en Oracle con clave primaria autoincremental:

| Campo | Tipo Java | Tipo Oracle | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `NUMBER(19)` | Clave primaria autoincremental (`IDENTITY`) |
| `titulo` | `String` | `VARCHAR2(255)` | Título del libro (obligatorio) |
| `autor` | `String` | `VARCHAR2(255)` | Autor del libro (obligatorio) |
| `anioPublicacion` | `int` / `Integer` | `NUMBER(10)` | Año de edición o publicación |
| `genero` | `String` | `VARCHAR2(255)` | Género literario |

---

## 🚀 Endpoints de la API REST

**URL Base:** `http://localhost:8080/libros`

### 1. Obtener todos los libros
- **Método:** `GET`
- **Ruta:** `/libros`
- **Respuesta Exitosa:** `200 OK`
```json
[
  {
    "id": 1,
    "titulo": "Cien años de soledad",
    "autor": "Gabriel García Márquez",
    "anioPublicacion": 1967,
    "genero": "Realismo mágico"
  }
]
```

---

### 2. Obtener un libro por ID
- **Método:** `GET`
- **Ruta:** `/libros/{id}`
- **Ejemplo:** `GET /libros/1`
- **Respuesta Exitosa:** `200 OK`
- **Respuesta si no existe:** `404 Not Found` (o vacío si se usa retorno directo)

---

### 3. Crear un nuevo libro
- **Método:** `POST`
- **Ruta:** `/libros`
- **Headers:** `Content-Type: application/json`
- **Cuerpo de la Petición:**
```json
{
  "titulo": "El túnel",
  "autor": "Ernesto Sabato",
  "anioPublicacion": 1948,
  "genero": "Novela psicológica"
}
```
- **Respuesta Exitosa:** `200 OK` / `201 Created` (devuelve el libro con su `id` generado por Oracle).

---

### 4. Actualizar un libro existente
- **Método:** `PUT`
- **Ruta:** `/libros/{id}`
- **Ejemplo:** `PUT /libros/1`
- **Cuerpo de la Petición:**
```json
{
  "titulo": "El túnel (Edición Especial)",
  "autor": "Ernesto Sabato",
  "anioPublicacion": 1950,
  "genero": "Novela psicológica"
}
```
- **Respuesta Exitosa:** `200 OK`

---

### 5. Eliminar un libro por ID
- **Método:** `DELETE`
- **Ruta:** `/libros/{id}`
- **Ejemplo:** `DELETE /libros/1`
- **Respuesta Exitosa:** `200 OK` / `204 No Content`

---

## ⚙️ Configuración de la Base de Datos (Oracle Cloud)

El archivo `src/main/resources/application.properties` contiene la configuración de conexión:

```properties
spring.application.name=gestor-biblioteca

# Conexión Oracle con Wallet
spring.datasource.url=jdbc:oracle:thin:@<alias_high>?TNS_ADMIN=./src/main/resources/wallet/<carpeta_wallet>
spring.datasource.username=ADMIN
spring.datasource.password=<tu_password>
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# Configuración JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

> **Nota sobre el Wallet:** Los archivos del Wallet (`tnsnames.ora`, `cwallet.sso`, etc.) deben encontrarse descomprimidos en la ruta indicada por `TNS_ADMIN`.

---

## ▶️ Cómo Ejecutar el Proyecto

1. Clonar el repositorio.
2. Asegurarse de tener instalado **Java 17 o superior**.
3. En la raíz del proyecto, ejecutar:

```bash
# Con el Maven Wrapper
./mvnw spring-boot:run

# O con Maven instalado en el sistema
mvn spring-boot:run
```

La aplicación quedará disponible en el puerto **`8080`**.
