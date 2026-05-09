# MyPass Server

**Stack**: Node.js 20+ · TypeScript · Fastify 5 · Prisma 5 · PostgreSQL · Zod

## Quick start

```bash
cd server

# 1. Install dependencies
npm install

# 2. Configure environment
cp .env.example .env
# Edit .env with your PostgreSQL credentials

# 3. Set up DB schema + seed sample data
npm run setup

# 4. Start (development with hot reload)
npm run dev
```

Server: <http://localhost:8082> · Health: <http://localhost:8082/api/health>

## Production

```bash
npm run build
npm start
```

## Scripts

| Command              | What it does                             |
|----------------------|------------------------------------------|
| `npm run dev`        | tsx watch (hot reload)                   |
| `npm run build`      | Compile TS to `dist/`                    |
| `npm start`          | Run compiled server                      |
| `npm run db:push`    | Sync schema to DB (no migrations)        |
| `npm run db:migrate` | Create + apply named migration           |
| `npm run db:studio`  | Open Prisma Studio (web DB browser)      |
| `npm run db:seed`    | Insert sample users + flights + seats    |
| `npm run setup`      | generate + push + seed (one-shot)        |

## Architecture

```
server/
├── src/
│   ├── server.ts            Fastify entry point
│   ├── lib/
│   │   ├── prisma.ts        Prisma client singleton
│   │   └── auth.ts          bcrypt + JWT helpers
│   └── routes/
│       ├── auth.ts          /api/auth/{register,login,google}
│       ├── flights.ts       /api/flights/{lookup,:id/seats}
│       ├── checkin.ts       /api/checkin/{complete,:passId}
│       ├── user.ts          /api/user/:id/{boardingpasses,profile}
│       └── sync.ts          /api/sync
├── prisma/
│   ├── schema.prisma        Database schema (5 models)
│   └── seed.ts              Sample data
└── package.json
```

## Endpoints

| Method | Path                                | Description                    |
|--------|-------------------------------------|--------------------------------|
| `GET`  | `/api/health`                       | Health check                   |
| `POST` | `/api/auth/register`                | Create account                 |
| `POST` | `/api/auth/login`                   | Sign in                        |
| `POST` | `/api/auth/google`                  | Google sign-in                 |
| `GET`  | `/api/flights/lookup`               | `?bookingRef=...&lastName=...` |
| `GET`  | `/api/flights/:id/seats`            | Seat map                       |
| `POST` | `/api/checkin/complete`             | Complete check-in              |
| `GET`  | `/api/checkin/:passId`              | Fetch a boarding pass          |
| `GET`  | `/api/user/:id/boardingpasses`      | All passes for a user          |
| `GET`  | `/api/user/:id`                     | User profile                   |
| `POST` | `/api/sync`                         | Sync user data                 |

All endpoints accept `Authorization: Bearer <jwt>` and `X-User-Id: <id>` headers.

## Sample data (seeded)

User: `namoune@mypass.dz` / `namoune123`

| Booking  | Last name | Route                  |
|----------|-----------|------------------------|
| NM2025A  | NAMOUNE   | ALG → CDG (Paris)      |
| NM2025B  | NAMOUNE   | ALG → DXB (Dubai)      |
| NM2025C  | NAMOUNE   | ALG → ORN (Oran)       |
| KL45PQ   | BENALI    | ALG → IST (Istanbul)   |
| QW34TY   | MARTINEZ  | ALG → JFK (New York)   |

## Database inspection

```bash
npm run db:studio
```

Opens a web UI at <http://localhost:5555> where you can browse, edit, and query all tables visually.
