# JobBoard API

API REST réactive construite avec **Spring WebFlux** permettant de gérer des offres d'emploi et des candidatures.

---

## Description

JobBoard est une API qui permet de :
- Créer, consulter, modifier et supprimer des offres d'emploi
- Soumettre et gérer des candidatures
- Rechercher des offres par titre, société ou localisation (insensible à la casse et aux accents)
- Paginer et trier les résultats

---

## Prérequis

- Java 21
- Maven 3.8+

## Installation et démarrage

```bash
# Cloner le projet
git clone https://github.com/poncemathieu/JobBoard.git
cd JobBoard

# Lancer l'application
./mvnw spring-boot:run
```

L'API est accessible sur `http://localhost:8080`.

---

## Endpoints API

Une collection Postman est disponible à la racine du projet : `JobBoard.postman_collection.json`

### Jobs

| Méthode | Endpoint | Description |
|---|---|---|
| `GET` | `/jobs` | Liste paginée des offres |
| `GET` | `/jobs/all` | Liste complète des offres |
| `GET` | `/jobs/{id}` | Offre par ID |
| `POST` | `/jobs` | Créer une offre |
| `PUT` | `/jobs/{id}` | Modifier une offre |
| `DELETE` | `/jobs/{id}` | Supprimer une offre |

#### Paramètres de pagination — `GET /jobs`

| Paramètre | Défaut | Description |
|---|---|---|
| `limit` | `20` | Nombre de résultats (1-100) |
| `offset` | `0` | Décalage |
| `sortBy` | `id` | Champ de tri |
| `direction` | `asc` | `asc` ou `desc` |
| `query` | - | Recherche par titre, société ou localisation |

#### Body — `POST /jobs` et `PUT /jobs/{id}`

```json
{
  "title": "Développeur Angular",
  "company": "TechCorp",
  "location": "Montréal",
  "salaryMin": 60000,
  "salaryMax": 80000
}
```

---

### Applications

| Méthode | Endpoint | Description |
|---|---|---|
| `GET` | `/applications` | Liste toutes les candidatures |
| `GET` | `/applications?jobId={id}` | Candidatures filtrées par offre |
| `GET` | `/applications/{id}` | Candidature par ID |
| `POST` | `/applications` | Soumettre une candidature |
| `PATCH` | `/applications/{id}/status` | Modifier le statut |

#### Body — `POST /applications`

```json
{
  "jobId": "1",
  "candidateName": "Mathieu Ponce",
  "candidateEmail": "mathieu@example.com",
  "message": "Je suis très motivé pour ce poste."
}
```

#### Body — `PATCH /applications/{id}/status`

```json
{
  "status": "ACCEPTED"
}
```

Valeurs possibles pour `status` : `PENDING`, `ACCEPTED`, `REJECTED`

---

## Convention de logging et traceId

Chaque requête est tracée via un identifiant unique — le `traceId`.

### Header

| Header | Description |
|---|---|
| `X-Request-Id` | Header entrant et sortant |

- Si le client envoie un `X-Request-Id` → il est réutilisé
- Sinon → un UUID court de 8 caractères est généré automatiquement (ex: `a1b2c3d4`)

### Présence du traceId

Le `traceId` est présent dans :
- Les **logs serveur** — chaque ligne de log inclut le traceId entre crochets
- Le **header de réponse** — `X-Request-Id`
- Le **body des erreurs** — champ `traceId`

### Format des logs

```
[a1b2c3d4] >>> GET /jobs/1
[a1b2c3d4] getJobById called - id=1
[a1b2c3d4] getJobById success - id=1
[a1b2c3d4] <<< GET /jobs/1 - 200
```

### Format des erreurs

```json
{
  "traceId": "a1b2c3d4",
  "status": 404,
  "message": "Job Not Found 1",
  "jobId": "1"
}
```

---

## Lancer les tests

```bash
./mvnw test
```

Les tests couvrent :
- `JobControllerTest` — tous les endpoints `/jobs`
- `ApplicationControllerTest` — tous les endpoints `/applications`

Cas couverts : 200, 201, 204, 400, 404, 409.