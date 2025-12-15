# Franchises API - Gestión Reactiva de Franquicias

## Tabla de contenidos
1. [Descripción general](#descripción-general)
2. [Tecnologías utilizadas](#tecnologías-utilizadas)
3. [Arquitectura del proyecto](#arquitectura-del-proyecto)
4. [Modelo de datos](#modelo-de-datos)
5. [Configuración del proyecto](#configuración-del-proyecto)
6. [Cómo ejecutar el proyecto](#cómo-ejecutar-el-proyecto)
7. [Endpoints principales](#endpoints-principales)
8. [Manejo de errores](#manejo-de-errores)
9. [Buenas prácticas aplicadas](#buenas-prácticas-aplicadas)
10. [Posibles mejoras](#posibles-mejoras)
11. [Autor](#autor)

## Descripción general
Franchises API es una prueba técnica backend diseñada para administrar franquicias, sus sucursales y los productos disponibles en cada punto de venta. El servicio expone endpoints REST totalmente reactivos utilizando Spring WebFlux y R2DBC, lo que permite manejar concurrencia elevada con un consumo óptimo de recursos. Toda la persistencia se realiza sobre PostgreSQL, incluyendo compatibilidad con proveedores administrados como Neon.

## Tecnologías utilizadas
- **Java 21+** (compatible con 17)
- **Spring Boot 3.5.8**
- **Spring WebFlux** (controladores reactivos con `Mono` y `Flux`)
- **Spring Data R2DBC** (acceso no bloqueante a PostgreSQL)
- **PostgreSQL / Neon** (motor de base de datos relacional)
- **Flyway** (migraciones y datos semilla)
- **Maven** (construcción y gestión de dependencias)
- **IntelliJ IDEA** (opcional como IDE principal)

## Arquitectura del proyecto
El código se organiza bajo `src/main/java/com/yennyfer/franchises_api` siguiendo una arquitectura por capas:
- **controller**: expone los endpoints REST y valida entradas (`FranchisesController`, `BranchesController`, `ProductsController`).
- **service**: contiene la lógica de negocio, validaciones adicionales y manejo de errores mediante `ResponseStatusException`.
- **repository**: interfaces `ReactiveCrudRepository` y consultas personalizadas con `@Query`.
- **model**: entidades simples anotadas con `@Table` y agregados (`BranchAggregate`, `FranchiseAggregate`) utilizados para respuestas compuestas.
- **dto**: contratos de entrada/salida (`UpdateBranchRequest`, `UpdateFranchiseRequest`, `UpdateProductRequest`, `ProductMaxStockResponse`).
- **config**: configuraciones adicionales como `OpenApiConfig` para habilitar la documentación con Swagger UI.

## Modelo de datos
Las relaciones se resuelven manualmente usando claves foráneas y consultas reactivas, sin anotaciones JPA.

```text
Franchise (1) ── (N) Branch ── (N) Product
```
- **Franquicia → Sucursales**: `branch.franchise_id` enlaza cada sucursal con su franquicia.
- **Sucursal → Productos**: `product.branch_id` determina a qué sucursal pertenece cada producto.
- Los servicios combinan resultados (`FranchiseAggregate`, `BranchAggregate`) para construir respuestas jerárquicas.
- Flyway crea las tablas (`V1__init.sql`) y añade evoluciones (`V2__add_product_sku.sql`, `V3__add_branch_address.sql`). El script `V4__seed_db.sql` carga 2 franquicias, 3 sucursales y 15 productos (5 por sucursal).

## Configuración del proyecto
Ejemplo completo de `application.yml` (equivalente a `application.properties` actual):

```yaml
spring:
  application:
    name: franchises-api
  r2dbc:
    url: ${SPRING_R2DBC_URL:r2dbc:postgresql://localhost:5432/mydatabase}
    username: ${SPRING_R2DBC_USERNAME:myuser}
    password: ${SPRING_R2DBC_PASSWORD:secret}
  flyway:
    enabled: true
    url: ${SPRING_FLYWAY_URL:jdbc:postgresql://localhost:5432/mydatabase}
    user: ${SPRING_FLYWAY_USER:${SPRING_R2DBC_USERNAME:myuser}}
    password: ${SPRING_FLYWAY_PASSWORD:${SPRING_R2DBC_PASSWORD:secret}}
    locations: classpath:db/migration
server:
  port: ${SERVER_PORT:8080}
```

Para Neon basta con reemplazar las URLs por las provistas y añadir `?sslmode=require` si la instancia lo exige.

## Cómo ejecutar el proyecto
```bash
# Clonar y acceder
 git clone https://github.com/Yennyferjp/franchises-api.git
 cd franchises-api

# Construir sin tests
env SPRING_R2DBC_URL=r2dbc:postgresql://localhost:5432/mydatabase \
    SPRING_R2DBC_USERNAME=myuser \
    SPRING_R2DBC_PASSWORD=secret \
    ./mvnw -DskipTests package

# Ejecutar en modo desarrollo
./mvnw spring-boot:run
```
En IntelliJ IDEA: importar como proyecto Maven, configurar las variables anteriores en la configuración de ejecución y lanzar `FranchisesApiApplication`.

### Ejecución con Docker Compose (opcional)
```bash
cp .env.example .env   # Definir credenciales
APP_PORT=8081 docker compose -f compose.prod.yaml up -d --build
```
Esto crea PostgreSQL + API, ejecuta Flyway dentro del contenedor y expone la API en `http://localhost:8081`.

## Endpoints principales
| Recurso | Método | Ruta | Descripción |
|---------|--------|------|-------------|
| Franquicias | POST | `/api/franchises` | Crear franquicia |
| Franquicias | GET | `/api/franchises` | Listar franquicias |
| Franquicias | GET | `/api/franchises/details` | Listar con sucursales y productos |
| Franquicias | PATCH | `/api/franchises/{franchiseId}` | Actualizar franquicia |
| Franquicias | DELETE | `/api/franchises/{franchiseId}` | Eliminar franquicia |
| Franquicias | GET | `/api/franchises/{franchiseId}/products/max-stock` | Producto con mayor stock por sucursal |
| Sucursales | POST | `/api/branches` | Crear sucursal |
| Sucursales | GET | `/api/branches` | Listar sucursales |
| Sucursales | GET | `/api/branches/details` | Sucursales + productos |
| Sucursales | PATCH | `/api/branches/{branchId}` | Actualizar sucursal |
| Sucursales | DELETE | `/api/branches/{branchId}` | Eliminar sucursal |
| Productos | POST | `/api/products` | Crear producto |
| Productos | GET | `/api/products` | Listar productos |
| Productos | PATCH | `/api/products/{productId}` | Actualizar datos |
| Productos | PATCH | `/api/products/{productId}/stock` | Actualizar stock |
| Productos | DELETE | `/api/products/{productId}` | Eliminar producto |

**Ejemplo de request/response (crear sucursal)**
```http
POST /api/branches
Content-Type: application/json

{
  "name": "Sucursal Pacífico",
  "address": "Av. Costera 101",
  "franchiseId": 2
}
```
```json
{
  "id": 4,
  "name": "Sucursal Pacífico",
  "address": "Av. Costera 101",
  "franchiseId": 2
}
```

**Respuesta agregada:**
```json
[
  {
    "franchise": {"id": 1, "name": "Franquicia Norte"},
    "branches": [
      {
        "branch": {"id": 1, "name": "Sucursal Centro", "address": "Av. Principal 123, Ciudad Central", "franchiseId": 1},
        "products": [
          {"id": 1, "name": "Combo Básico", "description": "Paquete estándar para clientes nuevos", "stock": 50, "sku": 1001, "branchId": 1},
          {"id": 2, "name": "Combo Premium", "description": "Versión ampliada", "stock": 30, "sku": 1002, "branchId": 1}
        ]
      }
    ]
  }
]
```

## Manejo de errores
Los servicios utilizan `ResponseStatusException` para responder con códigos consistentes:
- **400 Bad Request**: payload nulo/incorrecto.
- **404 Not Found**: franquicia, sucursal o producto inexistente.
- **409 Conflict**: intentos de crear recursos duplicados.
- **500 Internal Server Error**: fallos inesperados en la capa de datos.

Formato estándar (Spring Boot WebFlux):
```json
{
  "timestamp": "2025-12-15T04:25:10.124Z",
  "path": "/api/products/999",
  "status": 404,
  "error": "Not Found",
  "message": "Producto no encontrado"
}
```

## Buenas prácticas aplicadas
- **Programación reactiva**: `Mono`/`Flux`, operadores no bloqueantes y acceso R2DBC.
- **Separación de responsabilidades**: capas claramente identificadas y DTOs para contratos de entrada/salida.
- **Manejo de errores**: mensajes específicos por recurso y validaciones con `jakarta.validation` (`@Valid`).
- **Migraciones versionadas**: Flyway gestiona cambios de esquema y datos semilla.
- **Configuración externa**: URLs y credenciales provienen de variables de entorno.

## Posibles mejoras
1. **Autenticación y autorización** (Spring Security + JWT o OAuth2).
2. **Dockerización completa**: publicar la imagen y automatizar despliegues (ya existe `Dockerfile` + `compose.prod.yaml`).
3. **Documentación OpenAPI ampliada**: añadir anotaciones por endpoint y ejemplos.
4. **Pruebas adicionales**: unitarias para servicios, integración para repositorios y contratos API.
5. **Observabilidad**: métricas con Micrometer, trazas distribuidas y alertas.

## Autor
- **Yennyfer Jarava** – Backend Developer
- 2025
