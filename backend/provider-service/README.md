# AWD – Provider Service

Microservice de gestion des **prestataires** (profils, compétences, zones d'intervention, disponibilités) de la plateforme AWD.

| Élément | Valeur |
|---|---|
| Nom | `provider-service` |
| Port | `8083` |
| Base MySQL | `provider_db` (indépendante) |
| Stack | Java 17, Spring Boot 3.5.6, Spring Data JPA, MySQL 8, Eureka Client |

## Architecture

`Controller → Service → Repository → MySQL`, avec DTO + Mappers (les entités ne sont jamais exposées).

```
com.awd.provider
├── controller   (REST)
├── service      (logique métier)
├── repository   (Spring Data JPA)
├── entity       (Provider, Skill, ProviderSkill, InterventionZone, Availability)
├── dto / mapper
├── exception    (GlobalExceptionHandler)
├── config
└── enums        (ProviderStatus)
```

`userId` référence un utilisateur du **User Service** (simple `Long`, aucune entité User ici).

## Prérequis

- JDK 17, Maven (ou celui d'IntelliJ), MySQL 8 démarré sur `localhost:3306`.
- La base `provider_db` est créée automatiquement (`createDatabaseIfNotExist=true`).

## Lancer

1. Ouvrir le dossier dans IntelliJ (*File → Open* → `pom.xml` → *Open as Project*).
2. Si le mot de passe MySQL n'est pas vide : variable d'environnement `DB_PASSWORD` (et `DB_USERNAME` si différent de `root`). Ne jamais commiter de mot de passe.
3. Lancer `ProviderServiceApplication`.

### Profils

| Profil | Usage | Eureka |
|---|---|---|
| `local` (défaut) | développement autonome / Postman | désactivé |
| `integration` | intégration équipe | enregistrement sur `http://localhost:8761/eureka/` |

Activer l'intégration : `-Dspring.profiles.active=integration` (ou variable `SPRING_PROFILES_ACTIVE=integration`).
Le routage vers ce service est fait par l'API Gateway (routes statiques, configurées dans le projet Gateway).

## Endpoints

**Providers** : `POST/GET /api/providers`, `GET/PUT/DELETE /api/providers/{id}`, `PATCH /api/providers/{id}/activate|suspend`

**Skills** : `POST/GET /api/skills`, `GET/PUT/DELETE /api/skills/{id}`

**ProviderSkill** : `POST/GET /api/providers/{providerId}/skills`, `DELETE /api/providers/{providerId}/skills/{skillId}`

**Zones** : `POST/GET /api/providers/{providerId}/zones`, `PUT/DELETE .../zones/{zoneId}`

**Disponibilités** : `POST/GET /api/providers/{providerId}/availabilities`, `PUT/DELETE .../availabilities/{availabilityId}`

## Règles métier

- Provider créé en `PENDING` ; `activate` → `ACTIVE`, `suspend` → `SUSPENDED` (409 si déjà dans cet état).
- Email Provider unique (409). Nom de Skill unique (409).
- Un Provider ne peut avoir la même Skill qu'une fois (409).
- Supprimer un Provider supprime en cascade ses skills, zones et disponibilités.
- Supprimer une Skill utilisée par un Provider → 409.
- `startTime` doit être < `endTime` (400).

## Format des erreurs

```json
{ "timestamp": "...", "status": 400, "error": "Bad Request", "message": "Donnees invalides",
  "path": "/api/providers", "validationErrors": { "email": "email invalide" } }
```

## Tests Postman

Importer `postman/Provider-Service.postman_collection.json`. Exécuter les requêtes dans l'ordre : les IDs sont mémorisés automatiquement dans les variables de collection (`providerId`, `skillId`, `zoneId`, `availabilityId`).
Headers : `Content-Type: application/json` pour POST/PUT.
