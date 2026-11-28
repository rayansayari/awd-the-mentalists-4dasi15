# Review Service

Notes, commentaires et moyenne des professionnels.

| | |
|---|---|
| **Responsable** | Fares Ben Kheder |
| **Port** | 8086 |
| **Base de données** | review_db |

La conception détaillée (données, endpoints, événements) est décrite dans [le rapport technique](../../docs/rapport/Rapport_Technique.md).

## Fonctionnement

- À la réception de `booking.completed` (exchange `awd.events`), le service enregistre une `ReviewableBooking` : c'est elle qui autorise l'avis.
- Un client ne peut publier qu'**un seul avis par réservation**, avec une note de 1 à 5, uniquement sur **sa** réservation terminée.
- À chaque avis publié, modifié, masqué ou supprimé, la note moyenne du professionnel (`ProviderRatingSummary`, avis `PUBLISHED` uniquement) est recalculée et publiée via `review.rating-updated`.
- Aucune communication directe avec le User Service, le Service Catalogue et le Payment Service.

## Endpoints

| Méthode | URL | Description | Codes |
|---|---|---|---|
| POST | `/api/reviews` | Publier un avis | 201, 400, 401, 403, 409 |
| GET | `/api/reviews/{id}` | Consulter un avis | 200, 404 |
| GET | `/api/reviews/provider/{providerId}` | Avis d'un professionnel | 200, 404 |
| GET | `/api/reviews/provider/{providerId}/summary` | Note moyenne et nombre d'avis | 200, 404 |
| GET | `/api/reviews/client/{clientId}` | Avis publiés par un client | 200, 401, 403 |
| GET | `/api/reviews/booking/{bookingId}/eligibility` | Une réservation peut-elle être évaluée ? | 200, 401, 404 |
| PUT | `/api/reviews/{id}` | Modifier son avis | 200, 400, 401, 403, 404 |
| DELETE | `/api/reviews/{id}` | Supprimer un avis (statut DELETED) | 204, 401, 403, 404 |
| PATCH | `/api/reviews/{id}/hide` | Masquer un avis (Admin, statut HIDDEN) | 200, 401, 403, 404 |

### Identité de l'utilisateur

Le JWT est validé par l'API Gateway. Le Review Service lit l'identité dans les en-têtes transmis par la Gateway :

- `X-User-Id` : identifiant de l'utilisateur (le `clientId` d'un avis vient de là, jamais du corps de la requête) ;
- `X-User-Role` : `CLIENT`, `PROVIDER` ou `ADMIN`.

Pour tester avec Postman avant que la Gateway ne les ajoute, envoyer ces deux en-têtes à la main.

## Événements RabbitMQ (exchange `awd.events`, topic)

| Routing key | Sens | Détail |
|---|---|---|
| `booking.completed` | Consommé (file `review.booking-completed`) | `{bookingId, clientId, providerId, serviceId, status}` ; idempotent grâce à `eventId` |
| `review.rating-updated` | Publié | `{providerId, averageRating, ratingCount}`, `eventType = ReviewRatingUpdated` |

## Lancer le service

Prérequis : MariaDB (`localhost:3306`, utilisateur `root`), RabbitMQ (`localhost:5672`) et Eureka (`localhost:8761`) démarrés.

```bash
cd backend/review-service
mvn spring-boot:run
```

## Route à ajouter dans l'API Gateway

```properties
# Route review-service
spring.cloud.gateway.routes[3].id=review-service
spring.cloud.gateway.routes[3].uri=http://localhost:8086
spring.cloud.gateway.routes[3].predicates[0]=Path=/api/reviews/**
```
