# MyPass — Backend Setup (Node.js + PostgreSQL on your PC)

This is the **primary, recommended** backend (`server/`).
A legacy Kotlin/Ktor backend is still in `backend/` for reference.

**Stack**: Node.js 20+ · TypeScript · Fastify 5 · Prisma 5 · PostgreSQL · Zod

---

## 1. Install prerequisites on your PC

### Node.js 20+
<https://nodejs.org/> — install LTS. Verify:
```powershell
node --version    # should be v20+ (v22 recommended)
npm --version     # should be 10+
```

### PostgreSQL 14+
<https://www.postgresql.org/download/windows/> — during install:
- **User**: `postgres`
- **Password**: pick one (e.g. `postgres`)
- **Port**: `5432` (default)

> **macOS**: `brew install postgresql@16 && brew services start postgresql@16`
> **Linux**: `sudo apt install postgresql && sudo service postgresql start`

---

## 2. Create the database

Open `psql` (Start menu → "SQL Shell") or **pgAdmin 4** (bundled), and run:

```sql
CREATE DATABASE mypass;
```

That's all — Prisma will create every table on first run.

---

## 3. Configure the server

```powershell
cd server
copy .env.example .env
```

Edit `.env`:

```
DATABASE_URL="postgresql://postgres:YOUR_PASSWORD@localhost:5432/mypass?schema=public"
JWT_SECRET="any-long-random-string-for-jwt-signing"
PORT=8082
HOST=0.0.0.0
```

---

## 4. Install + initialize

```powershell
cd server
npm install
npm run setup    # → generate Prisma client, push schema, seed sample data
```

You should see:

```
🌱 Seeding database...
  ✓ User: namoune@mypass.dz / namoune123
  ✓ 5 flights with seat maps
✅ Seeding complete.
```

---

## 5. Start the server

### Development (hot reload)

```powershell
npm run dev
```

### Production

```powershell
npm run build
npm start
```

You should see:

```
[HH:MM:ss] INFO: ✓ PostgreSQL connection established.
[HH:MM:ss] INFO: ╔═════════════════════════════════════════════╗
[HH:MM:ss] INFO: ║  MyPass Backend ready on http://0.0.0.0:8082
[HH:MM:ss] INFO: ║  Local:   http://localhost:8082/api/health
[HH:MM:ss] INFO: ║  Network: http://<your-LAN-IP>:8082/api/health
[HH:MM:ss] INFO: ╚═════════════════════════════════════════════╝
```

Test in a browser: <http://localhost:8082/api/health>

---

## 6. Find your PC's LAN IP for the Android phone

Real Android devices cannot reach `localhost` on your PC. Find your IP:

```powershell
ipconfig
```

Look for **IPv4 Address** under your active WiFi adapter (e.g. `192.168.1.42`).

Open Windows Firewall for port 8082:

```powershell
New-NetFirewallRule -DisplayName "MyPass Backend" -Direction Inbound -Protocol TCP -LocalPort 8082 -Action Allow
```

Test from the phone's browser: `http://192.168.1.42:8082/api/health`

---

## 7. Configure the Android app

In the app: **Profile → Settings → Backend Server** → enter:

| Setup                              | URL                                |
|------------------------------------|------------------------------------|
| **Android emulator** on same PC    | `http://10.0.2.2:8082/api/`        |
| **Physical phone** on same WiFi    | `http://192.168.1.42:8082/api/`    |

Tap **Test Connection** → should show "✓ Connected".

---

## 8. Inspect / edit your database (Prisma Studio)

Prisma comes with a beautiful web DB browser:

```powershell
cd server
npm run db:studio
```

Opens <http://localhost:5555> — you can browse, query, and edit any row in any table.

---

## 9. Test the full flow

1. **Register** a new account in the app.
2. Run `npm run db:studio` and check the **users** table — your account is there.
3. **Find a booking** with `NM2025A` / `NAMOUNE` (seeded sample).
4. **Complete a check-in** through the app.
5. Refresh Prisma Studio and inspect:
   - **boarding_passes** — your new pass is linked to your `user_id`
   - **check_ins** — full passport + baggage record
   - **seats** — your selected seat is `occupied = true`
   - **flights** — `check_in_status` is `Checked-In`

---

## 10. Troubleshooting

| Problem                                          | Fix                                                            |
|--------------------------------------------------|----------------------------------------------------------------|
| `Cannot find module '@prisma/client'`            | Run `npm run db:generate`                                      |
| `Error: P1001: Can't reach database`             | Postgres not running? `services.msc` → start PostgreSQL service |
| `password authentication failed`                 | Wrong `DATABASE_URL` password — re-edit `.env`                 |
| App says "Cannot reach backend"                  | Check phone & PC on same WiFi + firewall + URL ends with `/api/` |
| Port 8082 already in use                         | Change `PORT=8083` in `.env`, restart                          |
| Prisma migration drift                           | `npm run db:push` (dev) — it'll sync without migrations        |

---

## 11. API reference

| Method | Path                              | Description                          |
|--------|-----------------------------------|--------------------------------------|
| `GET`  | `/api/health`                     | Health check                         |
| `POST` | `/api/auth/register`              | Create account                       |
| `POST` | `/api/auth/login`                 | Sign in                              |
| `POST` | `/api/auth/google`                | Google sign-in                       |
| `GET`  | `/api/flights/lookup`             | `?bookingRef=...&lastName=...`       |
| `GET`  | `/api/flights/{flightId}/seats`   | Seat map for a flight                |
| `POST` | `/api/checkin/complete`           | Complete check-in (returns pass)     |
| `GET`  | `/api/checkin/{passId}`           | Fetch a boarding pass                |
| `GET`  | `/api/user/{userId}`              | User profile                         |
| `GET`  | `/api/user/{userId}/boardingpasses`| All boarding passes for a user      |
| `POST` | `/api/sync`                       | Sync user data (passes + flights)    |

All endpoints accept:
- `Authorization: Bearer <jwt>` header (issued by `/auth/login`)
- `X-User-Id: <userId>` fallback header

---

## 12. Project structure

```
My_Pass/
├── app/                  → Android app (Jetpack Compose)
├── server/               → Node.js backend (PRIMARY) ★
│   ├── src/
│   │   ├── server.ts          Fastify entry
│   │   ├── lib/{prisma,auth}  DB client + JWT helpers
│   │   └── routes/            All REST endpoints
│   ├── prisma/
│   │   ├── schema.prisma      DB schema
│   │   └── seed.ts            Sample data
│   ├── package.json
│   └── README.md
├── backend/              → Kotlin/Ktor backend (legacy)
└── BACKEND_SETUP.md      → This file
```

---

## 13. Why Node.js + Fastify + Prisma?

| Feature              | Node.js (server/)        | Kotlin (backend/)       |
|----------------------|--------------------------|-------------------------|
| Cold start           | <1s                      | 8-15s (JVM warmup)      |
| Hot reload           | ✓ tsx watch              | Manual restart          |
| ORM                  | Prisma (type-safe + DB UI)| Exposed (manual)        |
| Validation           | Zod (declarative)        | Manual                  |
| Build time           | <2s (tsc)                | 30-60s (Gradle)         |
| Footprint            | ~80MB RAM                | ~250MB RAM (JVM)        |
| Production deploy    | `node dist/server.js`    | `gradle run` + JDK      |

The Kotlin backend remains in `backend/` for reference but the Node.js one is the primary.
