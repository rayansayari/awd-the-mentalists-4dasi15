# Service Catalogue (`catalogue-service`)

Catégories, prestations et tarifs de la plateforme RE'SERV.

| Élément | Valeur |
|---|---|
| **Responsable** | Khadija Bessais |
| **Port** | `8082` |
| **Base de données** | `catalogue_db` (MariaDB) |
| **Stack** | Java 17, Spring Boot 3.3.5, Spring Data JPA, Spring Security (JWT), MariaDB, Eureka Client |

La conception détaillée (données, endpoints, événements) est décrite dans [le rapport technique](../../docs/rapport/Rapport_Technique.md).

## Architecture

```text
com.reserv.catalogue
├── config       (CompatMariaDBDialect)
├── controller   (CategoryController, ServiceController)
├── dto          (CategoryRequest/Response, ServiceRequest/Response, ServiceStatusUpdateRequest, ErrorResponse)
├── entity       (Category, Service, ServiceStatus)
├── event        (CatalogueEvent, CatalogueEventPublisher, LoggingCatalogueEventPublisher)
├── exception    (BadRequestException, ConflictException, ResourceNotFoundException, GlobalExceptionHandler)
├── repository   (CategoryRepository, ServiceRepository)
├── security     (JwtAuthFilter, JwtService, Role, SecurityConfig, UserPrincipal)
└── service      (CategoryService, CatalogueService + impl)
```

## Configuration MariaDB locale

Copier `.env.example` vers `.env` (ignoré par Git) et adapter `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, et `JWT_SECRET` (identique au User Service).

## Profils

- `local` (défaut) : Eureka désactivé (`eureka.client.enabled=false`).
- `integration` : enregistrement sur `http://localhost:8761/eureka/`.

## Endpoints

### Catégories (`/api/categories`)
- `POST /api/categories` (ADMIN)
- `GET /api/categories` (Public)
- `GET /api/categories/{id}` (Public)
- `PUT /api/categories/{id}` (ADMIN)
- `DELETE /api/categories/{id}` (ADMIN)

### Prestations (`/api/services`)
- `POST /api/services` (ADMIN)
- `GET /api/services` (Public — supporte `minPrice`, `maxPrice`, `includeUnavailable`)
- `GET /api/services/{id}` (Public)
- `GET /api/services/category/{categoryId}` (Public)
- `GET /api/services/search?name={name}` (Public)
- `PUT /api/services/{id}` (ADMIN)
- `PATCH /api/services/{id}/status` (ADMIN)
- `DELETE /api/services/{id}` (ADMIN)

## Tests Postman

Importer `postman/Catalogue-Service.postman_collection.json` dans Postman et renseigner `adminToken` avec un JWT `ADMIN` émis par le User Service.
