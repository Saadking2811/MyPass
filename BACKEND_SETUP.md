# MyPass — Backend Setup (PostgreSQL on your PC)

This guide walks you through setting up the **MyPass backend** with PostgreSQL on your PC and connecting the Android app to it.

---

## 1. Install PostgreSQL on Windows

1. Download the installer from <https://www.postgresql.org/download/windows/>.
2. Run the installer. When asked, set:
   - **Username**: `postgres` (default)
   - **Password**: choose one and remember it (e.g. `postgres`)
   - **Port**: `5432` (default)
3. Finish the install. PostgreSQL service should auto-start.

> **macOS / Linux**: install via Homebrew (`brew install postgresql@16`) or apt (`sudo apt install postgresql`). The rest of the guide is identical.

---

## 2. Create the `mypass` database

Open `psql` (or **pgAdmin 4** which is bundled with the installer):

```sql
CREATE DATABASE mypass;
```

That's it — the backend will create the schema and seed sample data on first run.

---

## 3. Configure environment variables (optional)

Defaults are fine for local development. If your PostgreSQL setup differs, set these env vars before starting the server:

| Variable      | Default                                    | Purpose                       |
|---------------|--------------------------------------------|-------------------------------|
| `DB_MODE`     | `postgres`                                 | `postgres` or `h2` (in-memory)|
| `DB_URL`      | `jdbc:postgresql://localhost:5432/mypass`  | JDBC URL                      |
| `DB_USER`     | `postgres`                                 | DB user                       |
| `DB_PASSWORD` | `postgres`                                 | DB password                   |
| `PORT`        | `8082`                                     | Server port                   |

### Windows PowerShell (current session)

```powershell
$env:DB_USER     = "postgres"
$env:DB_PASSWORD = "yourpassword"
```

### Permanently (Windows)

Settings → System → About → Advanced System Settings → Environment Variables.

---

## 4. Start the backend

From the project root:

```powershell
.\gradlew.bat :backend:run
```

You should see:

```
============================================================
 MyPass Backend — connecting to PostgreSQL
   URL:  jdbc:postgresql://localhost:5432/mypass
   User: postgres
============================================================
 ✅ PostgreSQL connection established.
MyPass backend running on port 8082
```

Health check: open <http://localhost:8082/api/health> in your browser → `{"status":"ok",...}`

### Quick fallback (no PostgreSQL needed)

```powershell
$env:DB_MODE = "h2"
.\gradlew.bat :backend:run
```

Uses in-memory H2 — data does **not** persist. Use only for development.

---

## 5. Find your PC's IP for the Android phone

A real Android device cannot reach `localhost`. Find your LAN IP:

```powershell
ipconfig
```

Look for **IPv4 Address** under your active adapter (e.g. `192.168.1.42`).

Test from the phone's browser: `http://192.168.1.42:8082/api/health`

If the phone can't connect:
- The phone and PC must be on the **same WiFi network**.
- Allow port 8082 through the Windows Firewall:

```powershell
New-NetFirewallRule -DisplayName "MyPass Backend" -Direction Inbound -Protocol TCP -LocalPort 8082 -Action Allow
```

---

## 6. Configure the Android app

Open the app → **Profile → Settings → Backend Server**.

Enter the URL based on your setup:

| Setup                                  | URL to enter                       |
|----------------------------------------|------------------------------------|
| **Android emulator** on the same PC    | `http://10.0.2.2:8082/api/`        |
| **Physical phone** on the same WiFi    | `http://192.168.1.42:8082/api/` (replace with your PC IP) |

Tap **Test Connection**. You should see "✓ Connected".

The app stores this in SharedPreferences and uses it for all subsequent requests.

---

## 7. Test the full flow

1. **Register** a new account in the app (`Register` screen).
2. Run `psql` or pgAdmin and verify:
   ```sql
   SELECT id, full_name, email FROM users;
   ```
   You should see your new user.
3. **Find a booking** with `NM2025A` / `NAMOUNE` (seeded sample data).
4. **Complete a check-in** through the app.
5. Verify in PostgreSQL:
   ```sql
   SELECT booking_reference, passenger_name, seat, status FROM boarding_passes;
   SELECT user_id, selected_seat, checked_in_at FROM check_ins;
   ```

---

## 8. Pre-seeded test data

The backend seeds these on first start (only if `users` table is empty):

| Email                  | Password    | User                |
|------------------------|-------------|---------------------|
| `namoune@mypass.dz`    | `namoune123`| Saad Namoune        |

| Booking Ref | Last Name | Route             |
|-------------|-----------|-------------------|
| `NM2025A`   | `NAMOUNE` | ALG → CDG (Paris) |
| `NM2025B`   | `NAMOUNE` | ALG → DXB (Dubai) |
| `NM2025C`   | `NAMOUNE` | ALG → ORN (Oran)  |
| `KL45PQ`    | `BENALI`  | ALG → IST (Istanbul) |
| `QW34TY`    | `MARTINEZ`| ALG → JFK (New York) |

---

## 9. API endpoints

Base URL: `http://localhost:8082/api`

| Method | Path                              | Description                          |
|--------|-----------------------------------|--------------------------------------|
| `GET`  | `/health`                         | Health check                         |
| `POST` | `/auth/register`                  | Create account                       |
| `POST` | `/auth/login`                     | Sign in                              |
| `POST` | `/auth/google`                    | Google sign-in                       |
| `GET`  | `/flights/lookup`                 | `?bookingRef=...&lastName=...`       |
| `GET`  | `/flights/{flightId}/seats`       | Seat map for a flight                |
| `POST` | `/checkin/complete`               | Complete check-in (returns BP)       |
| `GET`  | `/checkin/{passId}`               | Fetch a boarding pass                |
| `GET`  | `/user/{userId}/boardingpasses`   | All boarding passes for a user       |
| `POST` | `/sync`                           | Sync user data (passes + flights)    |

---

## 10. Troubleshooting

**"PostgreSQL not reachable"** when starting the server:
- Is the PostgreSQL service running? Open `services.msc` → look for `postgresql-x64-16` (or similar) → start it.
- Check the port: `netstat -an | findstr 5432` — should show `LISTENING`.
- Is your password correct? Set `$env:DB_PASSWORD` accordingly.

**Phone shows "Cannot reach backend"**:
- Same WiFi check.
- Firewall: see step 5.
- Server URL: must end with `/api/` (trailing slash).

**Compile errors in IDE but `gradlew` works**:
- This is normal. Run **File → Sync Project with Gradle Files** in Android Studio.

---

## 11. Project structure

```
My_Pass/
├── app/                 → Android app (Jetpack Compose)
├── backend/             → Ktor server (PostgreSQL + Exposed)
│   └── src/main/kotlin/com/mypass/backend/
│       ├── Application.kt    Entry point + Ktor setup
│       ├── Database.kt       Tables + connection (PG / H2)
│       └── Routes.kt         All REST endpoints
├── BACKEND_SETUP.md     → This file
└── gradle/              → Shared dependency catalog
```

The frontend's network layer is in `app/src/main/java/com/example/myapplication/network/` — `RetrofitClient.kt` handles URL config and auth interceptor.
