# MyPass

Application Android de check-in pour compagnie aérienne, développée dans le cadre du projet de fin d'année **ENSI 2CS SIL**.

L'application permet à un voyageur de retrouver sa réservation, scanner son passeport, choisir son siège, déclarer ses bagages, formuler des demandes spéciales puis recevoir sa carte d'embarquement avec QR code, le tout connecté à un backend Node.js/PostgreSQL.

---

## Fonctionnalités

- **Authentification** — inscription, connexion, et connexion Google via le sélecteur de comptes natif Android (AccountManager)
- **Recherche de réservation** par référence + nom
- **Scan de passeport** via CameraX + ML Kit, parser MRZ ICAO 9303 avec vote multi-frames pour la précision
- **Sélection de siège** sur plan interactif (Premium / Standard / Occupé)
- **Bagages et demandes spéciales** (repas, assistance, infant, animal)
- **Carte d'embarquement** avec QR code, génération **PDF** et bouton **Add to Wallet** (partage système)
- **Notifications** modernes au moment de la délivrance de la carte
- **Mode hors ligne** + **synchronisation automatique** à la reconnexion
- **Multilingue** (Français / English / العربية), thème clair/sombre

## Pile technique

### Frontend (Android)
- **Kotlin** + **Jetpack Compose** + **Material 3**
- **MVVM** avec `StateFlow` + `ViewModel`
- **Navigation Compose** pour le routage
- **Retrofit** + **Gson** pour le réseau
- **DataStore Preferences** pour le cache hors ligne et les paramètres
- **CameraX** + **ML Kit Text Recognition** pour le scan passeport
- **ZXing** pour la génération QR
- **Coil** pour les images distantes

### Backend (`server/`)
- **Node.js** + **TypeScript** + **Fastify 5**
- **Prisma 5** + **PostgreSQL**
- **JWT** (jsonwebtoken) + **bcrypt** pour l'auth
- **Zod** pour la validation

## Architecture du code

Le projet suit une architecture MVVM en couches, organisée par feature :

```
com.example.myapplication/
├── MainActivity.kt
├── model/              data classes (UserAccount, FlightItinerary, Seat, …)
├── network/            Retrofit ApiService + RetrofitClient
├── repository/         AuthRepository, FlightRepository, BoardingPassRepository,
│                       SyncRepository + AppRepository (facade)
├── viewmodel/          AppViewModel + AppUiState
├── data/               NetworkMonitor, OfflineCacheManager, PreferencesManager
├── ocr/                MrzParser (ICAO 9303 TD3)
├── util/               QrGenerator, BoardingPassPdfGenerator,
│                       BoardingPassSharer, CheckInNotifier
└── ui/
    ├── AirlineCheckInApp.kt    point d'entrée + NavHost
    ├── theme/                  Color, Theme, Type
    ├── components/             BrandLogo, Buttons, Inputs, Dialogs, ScreenScaffold, …
    ├── util/                   Img, Destinations
    ├── splash/                 SplashScreen
    ├── auth/                   SignInScreen, SignUpScreen, AuthComponents
    ├── main/                   MainScaffold (bottom nav)
    ├── home/                   HomeTab
    ├── trips/                  TripsTab
    ├── explore/                ExploreTab
    ├── profile/                ProfileTab
    ├── checkin/                BookingLookup, PassportScan, DetailsReview,
    │                           SeatSelection, Baggage, SpecialRequests, BoardingPass
    └── preferences/            PreferencesScreen
```

Chaque fichier a une responsabilité unique. Aucun fichier ne dépasse ~330 lignes.

## Démarrage

### Backend

```bash
cd server
cp .env.example .env       # ajuster DATABASE_URL et JWT_SECRET
npm install
npx prisma migrate dev
npm run dev                # écoute par défaut sur :8082
```

Voir [`BACKEND_SETUP.md`](BACKEND_SETUP.md) pour les détails.

### Application Android

1. Ouvrir le projet dans **Android Studio** (Hedgehog ou plus récent)
2. Régler l'URL du backend depuis l'écran **Settings** de l'app :
   - Émulateur : `http://10.0.2.2:8082/api/`
   - Téléphone réel : `http://<IP-LAN-du-PC>:8082/api/`
3. Lancer sur un device API 26+ (testé sur Samsung S24 Ultra, Android 15)

### Build APK depuis la ligne de commande

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug   # installe sur le device connecté
```

## Identifiants de démonstration

| Référence | Nom | Description |
|-----------|-----|-------------|
| `NM2025A` | `NAMOUNE` | Vol Alger → Paris, ouvert au check-in |

## Captures d'écran

Voir le dossier `screenshots/` (à venir).

## Auteur

**Seif el islam Namoune** — ENSI 2CS SIL, promotion 2026
