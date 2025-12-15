# Franchises API - Gestión Reactiva de Franquicias

## Tabla de contenidos
1. [Descripción general](#descripción-general)
2. [Tecnologías utilizadas](#tecnologías-utilizadas)
3. [Arquitectura del proyecto](#arquitectura-del-proyecto)
4. [Modelo de datos](#modelo-de-datos)
5. [Configuración del proyecto](#configuración-del-proyecto)
6. [Cómo ejecutar el proyecto](#cómo-ejecutar-el-proyecto)
7. [Endpoints principales](#endpoints-principales)
8. [Pruebas Unitarias](#pruebas-unitarias)
9. [Manejo de errores](#manejo-de-errores)
10. [Buenas prácticas aplicadas](#buenas-prácticas-aplicadas)
11. [Posibles mejoras](#posibles-mejoras)
12. [Autor](#autor)

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

## Pruebas Unitarias

El proyecto cuenta con una suite completa de **64 pruebas unitarias** que garantizan la calidad y el correcto funcionamiento de todos los componentes. Se utilizan **JUnit 5**, **Mockito** y **WebTestClient** para validar tanto la capa de controladores como la capa de servicios.

### Tecnologías de Testing
- **JUnit 5** (`@Test`, `@BeforeEach`, `@DisplayName`)
- **Mockito** (`@MockitoBean`, `@Mock`, `@InjectMocks`)
- **WebTestClient** (para pruebas de controladores reactivos)
- **StepVerifier** (para verificar flujos reactivos)
- **Spring Boot Test** (`@WebFluxTest`)

### Ejecutar las Pruebas
```bash
# Ejecutar todas las pruebas
./mvnw test

# Ejecutar con cobertura
./mvnw verify
```

### Estructura de las Pruebas

#### 1. Pruebas de Controladores (Controller Tests)
Ubicadas en `src/test/java/com/yennyfer/franchises_api/controller/`

##### FranchisesControllerTest (12 tests)
Valida los endpoints relacionados con franquicias:
- ✅ **createFranchise_success**: Crear franquicia con datos válidos retorna recurso creado
- ✅ **createFranchise_conflict**: Crear franquicia duplicada retorna 409 Conflict
- ✅ **getAllFranchises_success**: Listar franquicias retorna colección
- ✅ **getAllFranchises_serverError**: Listar franquicias con error en servicio retorna 500
- ✅ **getFranchisesWithDetails_success**: Obtener franquicias con detalles de sucursales y productos
- ✅ **updateFranchise_success**: Actualizar franquicia retorna entidad actualizada
- ✅ **updateFranchise_notFound**: Actualizar franquicia inexistente retorna 404
- ✅ **deleteFranchise_success**: Eliminar franquicia existente retorna cuerpo vacío
- ✅ **deleteFranchise_notFound**: Eliminar franquicia inexistente retorna 404
- ✅ **getProductsWithMaxStock_success**: Consultar productos con mayor stock retorna payload
- ✅ **getProductsWithMaxStock_notFound**: Consulta sin franquicia retorna 404

##### BranchesControllerTest (9 tests)
Valida los endpoints relacionados con sucursales:
- ✅ **createBranch_success**: Crear sucursal con datos válidos retorna recurso
- ✅ **createBranch_conflict**: Crear sucursal duplicada retorna 409
- ✅ **getAllBranches_success**: Listar sucursales retorna colección
- ✅ **getAllBranches_error**: Listar sucursales con error retorna 500
- ✅ **getAllBranchesWithDetails_success**: Obtener sucursales con productos
- ✅ **updateBranch_success**: Actualizar sucursal retorna recurso actualizado
- ✅ **updateBranch_notFound**: Actualizar sucursal inexistente retorna 404
- ✅ **deleteBranch_success**: Eliminar sucursal retorna cuerpo vacío
- ✅ **deleteBranch_notFound**: Eliminar sucursal inexistente retorna 404

##### ProductsControllerTest (11 tests)
Valida los endpoints relacionados con productos:
- ✅ **createProduct_success**: Crear producto con datos válidos retorna recurso
- ✅ **createProduct_conflict**: Crear producto duplicado retorna 409
- ✅ **getAllProducts_success**: Listar productos retorna payload
- ✅ **getAllProducts_error**: Listar productos con error retorna 500
- ✅ **updateProduct_success**: Actualizar producto retorna recurso actualizado
- ✅ **updateProduct_notFound**: Actualizar producto inexistente retorna 404
- ✅ **updateProductStock_success**: Actualizar stock retorna producto actualizado
- ✅ **updateProductStock_notFound**: Actualizar stock de producto inexistente retorna 404
- ✅ **deleteProduct_success**: Eliminar producto retorna cuerpo vacío
- ✅ **deleteProduct_notFound**: Eliminar producto inexistente retorna 404

#### 2. Pruebas de Servicios (Service Tests)
Ubicadas en `src/test/java/com/yennyfer/franchises_api/service/`

##### FranchisesServiceTest (8 tests)
Valida la lógica de negocio de franquicias:
- ✅ **createFranchise_success**: Crear franquicia con nombre único guarda entidad
- ✅ **createFranchise_conflict**: Crear franquicia con nombre duplicado emite error
- ✅ **getAllFranchises**: Listar franquicias retorna flujo reactivo
- ✅ **getFranchisesWithDetails**: Obtener agregados construye respuesta anidada
- ✅ **updateFranchise_success**: Actualizar franquicia persiste cambios
- ✅ **updateFranchise_notFound**: Actualizar franquicia desconocida retorna not found
- ✅ **deleteFranchise_success**: Eliminar franquicia existente completa correctamente
- ✅ **deleteFranchise_notFound**: Eliminar franquicia inexistente emite not found

##### BranchesServiceTest (8 tests)
Valida la lógica de negocio de sucursales:
- ✅ **createBranch_success**: Crear sucursal con nombre único tiene éxito
- ✅ **createBranch_conflict**: Crear sucursal con nombre duplicado emite error
- ✅ **getAllBranches_success**: Listar sucursales retorna flujo reactivo
- ✅ **getAllBranchesWithDetails**: Obtener detalles retorna agregados con productos
- ✅ **updateBranch_success**: Actualizar sucursal persiste cambios
- ✅ **updateBranch_notFound**: Actualizar sucursal desconocida emite not found
- ✅ **deleteBranch_success**: Eliminar sucursal existente completa correctamente
- ✅ **deleteBranch_notFound**: Eliminar sucursal inexistente emite not found

##### ProductsServiceTest (10 tests)
Valida la lógica de negocio de productos:
- ✅ **createProduct_success**: Crear producto único guarda entidad
- ✅ **createProduct_conflict**: Crear producto con nombre duplicado emite error
- ✅ **getAllProducts**: Listar productos retorna flujo reactivo
- ✅ **deleteProduct_success**: Eliminar producto existente completa correctamente
- ✅ **deleteProduct_notFound**: Eliminar producto inexistente retorna not found
- ✅ **updateProduct_success**: Actualizar producto persiste cambios
- ✅ **updateProduct_notFound**: Actualizar producto desconocido emite not found
- ✅ **updateProductStock_success**: Actualizar stock guarda valor
- ✅ **updateProductStock_notFound**: Actualizar stock de producto inexistente emite not found
- ✅ **getProductsWithMaxStockPerFranchise**: Consultar mayor stock delega al repositorio

### Cobertura de Pruebas
Las pruebas cubren los siguientes aspectos:
- ✅ **Casos exitosos**: Operaciones CRUD estándar con datos válidos
- ✅ **Validación de errores**: Recursos no encontrados (404), conflictos (409), errores de servidor (500)
- ✅ **Flujos reactivos**: Uso de `Mono` y `Flux` con `StepVerifier`
- ✅ **Mocking completo**: Repositorios y servicios mockeados con Mockito
- ✅ **Respuestas HTTP**: Validación de códigos de estado y estructura JSON
- ✅ **Agregados complejos**: Respuestas anidadas con franquicias, sucursales y productos

### Patrones de Testing Utilizados
- **Arrange-Act-Assert**: Estructura clara en cada test
- **Given-When-Then**: Nomenclatura descriptiva con `@DisplayName`
- **Mocks y Stubs**: Aislamiento de dependencias externas
- **Test Fixtures**: Datos de prueba inicializados en `@BeforeEach`
- **Verificación reactiva**: `StepVerifier` para validar flujos asíncronos

## Buenas prácticas aplicadas
- **Programación reactiva**: `Mono`/`Flux`, operadores no bloqueantes y acceso R2DBC.
- **Separación de responsabilidades**: capas claramente identificadas y DTOs para contratos de entrada/salida.
- **Manejo de errores**: mensajes específicos por recurso y validaciones con `jakarta.validation` (`@Valid`).
- **Migraciones versionadas**: Flyway gestiona cambios de esquema y datos semilla.
- **Configuración externa**: URLs y credenciales provienen de variables de entorno.
- **Cobertura de pruebas**: 64 tests unitarios que validan controladores y servicios con programación reactiva.

## Posibles mejoras
1. **Autenticación y autorización** (Spring Security + JWT o OAuth2).
2. **Dockerización completa**: publicar la imagen y automatizar despliegues (ya existe `Dockerfile` + `compose.prod.yaml`).
3. **Documentación OpenAPI ampliada**: añadir anotaciones por endpoint y ejemplos.
4. **Pruebas de integración**: tests end-to-end con TestContainers y base de datos real.
5. **Observabilidad**: métricas con Micrometer, trazas distribuidas y alertas.
6. **Cobertura de código**: integrar JaCoCo para reportes de cobertura automáticos.

## Autor
- **Yennyfer Jarava** – Backend Developer
- 2025
