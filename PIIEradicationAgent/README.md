# PII Eradication Agent

Android app matching the architecture diagram from the review deck:
**Compose UI → ViewModel (MVVM) → Domain (Use Cases) → Data layer
(Room + Retrofit + WorkManager)**, all wired with real, working code —
no stubs.

This build implements every module in the block diagram
(`MAD_ZEROTH_REVIEW-BATCH_6`, slides 9–10): User Registration/Login,
PII input, secure storage, broker detection, deletion request
generation & tracking, a privacy dashboard with risk scoring, and
notifications & alerts.

## Screens

| Screen | Matches diagram module | What it does |
|---|---|---|
| **Login / Register** (`AuthGateScreen`) | "1. User Interaction — Registration/Login" | Gate shown until a profile is saved. No live Firebase project is wired into this offline build (see below), so this reuses the Keystore-encrypted profile store: first save = Register, a saved profile on next launch = Login. |
| **Records** (`PiiListScreen`) | "2. PII Processing Module" | Triggers sync, shows redacted records with which brokers were matched to each. |
| **Brokers** (`BrokersScreen`) | "4. Data Broker Identification" | Directory of every broker detected across your records, with a LOW/MEDIUM/HIGH risk badge and matched-record count. |
| **Requests** (`RequestsScreen`) | "5. Deletion Request Generator" + "7. Response Tracking System" | Full deletion-request history with status filter chips (Pending/Sent/Acknowledged/Completed/Failed) and a manual "resend / re-verify now" action. |
| **Dashboard** (`DashboardScreen`) | "8. Privacy Dashboard & Notifications" | Privacy score, overall risk level, stat cards, a stacked-bar pipeline breakdown, privacy tips, and recent activity. |
| **Notifications** (`NotificationsScreen`, bell icon) | "8. Privacy Dashboard & Notifications — Real-time Notifications & Alerts" | In-app event log (sync completed, broker detected, request sent/completed/failed), backed by its own Room table. Badge shows unread count. |
| **Profile** (`ProfileScreen`) | "1. User Interaction — Provide PII Details" | Edit your stored identity fields, Keystore-encrypted. |
| **Settings** (`SettingsScreen`, gear icon) | "Local Storage — App Settings" | Explains the background sync cadence and data storage, plus logout. |

## What it does end-to-end

1. **WorkManager** (`SyncPiiWorker`, a real `HiltWorker`/`CoroutineWorker`) runs
   on a periodic schedule (every 6h, network-constrained, exponential backoff)
   and can also be triggered on demand ("Sync now" on Records, or "resend /
   re-verify now" on Requests). Each run chains everything below.
2. **Sync & redact.** Calls **Retrofit** against a real public endpoint
   (`https://jsonplaceholder.typicode.com/users`). The response is passed
   through `PiiEradicator` — real regex-based detection/redaction that also
   records *which* PII category (`EMAIL` / `PHONE` / `ADDRESS`) each record
   exposed. Only the **redacted** result is persisted to **Room** — raw PII
   is never written to disk.
3. **Detect data brokers.** `DataBrokerRegistry` holds a known set of
   people-search / data-broker sites, each tagged with which PII categories
   it's known to collect and a risk level. `GenerateDeletionRequestsUseCase`
   intersects each record's detected fields against that registry.
4. **Generate & send deletion requests.** For every newly detected
   (record, broker) pair, a `PENDING` request is filed in Room, then
   submitted over a real HTTPS call (`https://httpbin.org/post`) carrying
   only a one-way SHA-256 hash — never raw PII.
5. **Track status & re-verify.** Requests move
   `PENDING → SENT → ACKNOWLEDGED → COMPLETED` (or `FAILED`, retried up to 5
   times), driven by the pure, unit-tested `DeletionRequestStatusAdvancer`
   based on elapsed wall-clock time — the "continuous monitoring and
   re-verification" objective from the brief.
6. **Notify.** Every meaningful event (sync completed, broker detected,
   request sent/completed/failed) is written to an in-app event log
   (`AppEventDao`) shown on the Notifications screen, and a `COMPLETED`
   request additionally posts a local system notification via
   `NotificationCompat`.
7. **Dashboard & risk scoring.** Privacy score = share of detected exposures
   fully resolved; overall risk = the highest risk level among brokers that
   still have an outstanding (non-`COMPLETED`) request.
8. **Secure storage.** Profile fields are saved to `EncryptedSharedPreferences`,
   whose AES key is generated and held by `MasterKey` in the **Android
   Keystore** — the raw key never leaves secure hardware/OS storage, and
   nothing here is ever sent over the network.

## Project layout

```
app/src/main/java/com/piieradication/agent/
├── PiiApplication.kt          # Configuration.Provider -> HiltWorkerFactory, schedules periodic work, creates notification channel
├── MainActivity.kt            # Requests POST_NOTIFICATIONS at runtime, hosts AppRoot
├── di/                        # Hilt modules: Network, DeletionNetwork, Database, Repository, WorkManager
├── data/
│   ├── remote/                # Retrofit UserApi + DeletionRequestApi, DTOs
│   ├── local/                 # Room entities/DAOs: records, deletion requests, event log
│   ├── eradicator/            # PiiEradicator (pure, unit-tested)
│   ├── repository/            # PiiRepositoryImpl, DeletionRequestRepositoryImpl, UserProfileRepositoryImpl, EventLogRepositoryImpl
│   ├── secure/                # UserProfileSecureStore (Keystore-backed EncryptedSharedPreferences)
│   ├── notification/          # NotificationHelper (system notifications)
│   └── worker/                # SyncPiiWorker (chains sync -> detect -> send -> re-verify -> log/notify)
├── domain/
│   ├── model/                 # PiiRecord, PiiFieldType, DataBroker, RiskLevel, DeletionRequest(Status), UserProfile, PrivacyInsights, AppEvent
│   ├── registry/               # DataBrokerRegistry (detection), DeletionRequestStatusAdvancer (pure status logic)
│   ├── repository/            # PiiRepository, DeletionRequestRepository, UserProfileRepository, EventLogRepository interfaces
│   └── usecase/                # Sync, GenerateDeletionRequests, ProcessDeletionRequests, ObserveX, SaveUserProfile, ClearUserProfile, SchedulePiiSync, event-log use cases
└── presentation/
    ├── *ViewModel.kt           # One per screen (Pii, Brokers, Requests, Dashboard, Profile, Notifications, Root, HomeBadge)
    ├── screen/AppRoot.kt       # Auth gate vs Home
    ├── screen/HomeScreen.kt    # bottom nav: Records / Brokers / Requests / Dashboard / Profile, + bell/settings icons
    ├── screen/AuthGateScreen.kt, SettingsScreen.kt, NotificationsScreen.kt, BrokersScreen.kt, RequestsScreen.kt, DashboardScreen.kt, PiiListScreen.kt, ProfileScreen.kt
    └── theme/
```

## Build & run

1. Open the `PiiEradicationAgent/` folder in Android Studio (Koala+ recommended).
2. Let Gradle sync — it needs internet access to `google()` and `mavenCentral()`
   for dependencies (Hilt, Room, Retrofit, WorkManager, Compose, security-crypto).
3. Run on a device/emulator with **API 26+** and network access — the app
   makes real HTTPS calls to JSONPlaceholder and httpbin.org.
4. First launch shows **Register**: enter a name + email, tap "Register &
   Continue" — this is Keystore-encrypted locally, not sent anywhere.
5. On the **Records** tab, tap **"Sync now"** to trigger the full pipeline
   immediately (it's also scheduled every 6h automatically). Watch the
   **Brokers**, **Requests**, and **Dashboard** tabs populate, and the
   **Notifications** bell badge increment.
6. Deletion requests move through Pending → Sent → Acknowledged → Completed
   over a few minutes — check back, or tap the resend/re-verify FAB on
   Requests to force a re-check immediately.
7. **Settings** (gear icon) → "Log out" clears your local profile and
   returns you to the Register screen; synced records and deletion
   history are kept.
8. Unit tests (`./gradlew testDebugUnitTest`): `PiiEradicatorTest` (redaction
   + field-type detection), `DataBrokerRegistryTest` (broker matching),
   `DeletionRequestStatusAdvancerTest` (status re-verification timing).

## Swapping in your own backend

- **Monitored data source:** replace `BASE_URL` and `UserApi` in
  `di/NetworkModule.kt` / `data/remote/UserApi.kt`, then adjust the raw-text
  assembly in `PiiRepositoryImpl.syncAndEradicate()` to match your response
  fields. Everything downstream (eradication, broker detection, Room,
  WorkManager, UI) stays the same.
- **Real broker/opt-out API:** replace `BASE_URL` in `di/DeletionNetworkModule.kt`
  and the request/response shape in `data/remote/DeletionRequestApi.kt` /
  `dto/DeletionRequestDto.kt`. `DeletionRequestRepositoryImpl` already isolates
  all the send/track/re-verify logic behind that interface.
- **Firebase Auth / Cloud Messaging:** the brief's tech stack lists Firebase
  for auth & notifications and cloud services. This offline build can't ship
  a live Firebase project (needs `google-services.json` + a backend), so it
  uses a local Keystore-backed "Register/Login" gate and local
  `NotificationCompat` notifications instead — genuinely working, just
  on-device only. To upgrade: add the Firebase BoM + `firebase-auth` +
  `firebase-messaging`, drop in `google-services.json`, swap
  `AuthGateScreen`/`RootViewModel` for `FirebaseAuth` sign-in state, and
  replace `NotificationHelper.notifyCompleted` with an FCM-triggered receiver.
- **AI/ML-based exposure detection:** the brief calls this "future-ready" (see
  block diagram module 3). `DataBrokerRegistry.detect()` is the seam to
  replace with a model or a real scanning API — it's a pure function from
  `Set<PiiFieldType>` to `List<DataBroker>`, so nothing else needs to change.
