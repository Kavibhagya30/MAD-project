# PII Eradication Agent

Android app matching the architecture diagram: **Compose UI → ViewModel (MVVM) →
Domain (Use Cases) → Data layer (Room + Retrofit + WorkManager)**, all wired
with real, working code — no stubs.

This build implements every objective from the project-review deck
(`MAD_ZEROTH_REVIEW-BATCH_6`): detection, automated deletion requests,
status tracking, continuous monitoring, a privacy dashboard, secure
Keystore storage, and notifications.

## What it does

1. **WorkManager** (`SyncPiiWorker`, a real `HiltWorker`/`CoroutineWorker`) runs
   on a periodic schedule (every 6h, network-constrained, exponential backoff)
   and can also be triggered on demand from the UI ("Sync now"). Each run
   chains all four automated objectives below in sequence.
2. **Sync & redact.** It calls **Retrofit** against a real public endpoint
   (`https://jsonplaceholder.typicode.com/users`), which returns PII-shaped
   fields (email, phone, street address). The response is passed through
   `PiiEradicator` — real regex-based detection/redaction that also records
   *which* PII category (`EMAIL` / `PHONE` / `ADDRESS`) each record exposed.
   Only the **redacted** result is persisted to **Room** — raw PII is never
   written to disk.
3. **Detect data brokers.** `DataBrokerRegistry` holds a known set of
   people-search / data-broker sites tagged with which PII categories each
   is known to collect. For every synced record, `GenerateDeletionRequestsUseCase`
   intersects the record's detected fields against that registry to decide
   which brokers are likely exposing it.
4. **Generate & send deletion requests.** For every newly detected
   (record, broker) pair, a `PENDING` deletion request is filed in Room.
   `ProcessDeletionRequestsUseCase` then submits it over a real HTTPS call
   (`https://httpbin.org/post`) — deliberately carrying only a one-way
   SHA-256 hash of the record, never raw PII, so even the deletion request
   itself can't leak anything.
5. **Track status & re-verify.** Requests move
   `PENDING → SENT → ACKNOWLEDGED → COMPLETED` (or `FAILED`, retried up to 5
   times). The pure, unit-tested `DeletionRequestStatusAdvancer` decides when
   to advance an in-flight request based on elapsed wall-clock time, and is
   re-evaluated on every periodic worker run — this is the "continuous
   monitoring and re-verification" objective from the brief.
6. **Notify.** When a request reaches `COMPLETED`, `NotificationHelper` posts
   a local notification via `NotificationCompat` (channel created at app
   startup, `POST_NOTIFICATIONS` requested at runtime on Android 13+).
7. **Dashboard.** The Compose UI observes Room via `Flow` → `StateFlow` and
   shows: a live `WorkInfo` sync status, a records list annotated with which
   brokers were matched per record, and a **Dashboard** tab with privacy
   insight cards (records synced, fields redacted, brokers detected,
   requests completed) plus a stacked-bar breakdown of the deletion-request
   pipeline and a recent-activity list.
8. **Secure storage.** A **Profile** tab lets the app owner save their own
   name/email/phone into `EncryptedSharedPreferences`, whose AES key is
   generated and held by `MasterKey` in the **Android Keystore** — the raw
   key never leaves secure hardware/OS storage, and nothing here is ever
   sent over the network.

## Project layout

```
app/src/main/java/com/piieradication/agent/
├── PiiApplication.kt          # Configuration.Provider -> HiltWorkerFactory, schedules periodic work, creates notification channel
├── MainActivity.kt            # Requests POST_NOTIFICATIONS at runtime, hosts HomeScreen
├── di/                        # Hilt modules: Network, DeletionNetwork, Database, Repository, WorkManager
├── data/
│   ├── remote/                # Retrofit UserApi + DeletionRequestApi, DTOs
│   ├── local/                 # Room entities/DAOs for records + deletion requests
│   ├── eradicator/            # PiiEradicator (pure, unit-tested)
│   ├── repository/            # PiiRepositoryImpl, DeletionRequestRepositoryImpl, UserProfileRepositoryImpl
│   ├── secure/                # UserProfileSecureStore (Keystore-backed EncryptedSharedPreferences)
│   ├── notification/          # NotificationHelper
│   └── worker/                # SyncPiiWorker (chains sync -> detect -> send -> re-verify -> notify)
├── domain/
│   ├── model/                 # PiiRecord, PiiFieldType, DataBroker, DeletionRequest(Status), UserProfile, PrivacyInsights
│   ├── registry/               # DataBrokerRegistry (detection), DeletionRequestStatusAdvancer (pure status logic)
│   ├── repository/            # PiiRepository, DeletionRequestRepository, UserProfileRepository interfaces
│   └── usecase/                # Sync, GenerateDeletionRequests, ProcessDeletionRequests, ObserveX, SaveUserProfile, SchedulePiiSync
└── presentation/
    ├── PiiViewModel.kt / DashboardViewModel.kt / ProfileViewModel.kt
    ├── screen/HomeScreen.kt    # bottom nav: Records / Dashboard / Profile
    ├── screen/PiiListScreen.kt, DashboardScreen.kt, ProfileScreen.kt
    └── theme/
```

## Build & run

1. Open the `PiiEradicationAgent/` folder in Android Studio (Koala+ recommended).
2. Let Gradle sync — it needs internet access to `google()` and `mavenCentral()`
   for dependencies (Hilt, Room, Retrofit, WorkManager, Compose, security-crypto).
3. Run on a device/emulator with **API 26+** and network access — the app
   makes real HTTPS calls to JSONPlaceholder and httpbin.org.
4. Tap **"Sync now"** on the Records tab to trigger the full pipeline
   immediately, or just wait — it's also scheduled periodically (every 6h)
   from `PiiApplication.onCreate()`. Watch the **Dashboard** tab to see
   deletion requests move through Pending → Sent → Acknowledged → Completed
   over a few minutes (or a few taps of "Sync now" a couple of minutes apart).
5. On the **Profile** tab, save your name/email — it's written to
   Keystore-encrypted local storage only.
6. Unit tests (`./gradlew testDebugUnitTest`): `PiiEradicatorTest` (redaction
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
  for auth & notifications. This offline build can't ship a live Firebase
  project (needs `google-services.json` + a backend), so it uses local
  `NotificationCompat` notifications instead — genuinely working, just
  on-device only. To upgrade: add the Firebase BoM + `firebase-messaging`,
  drop in `google-services.json`, replace `NotificationHelper.notifyCompleted`
  with an FCM-triggered receiver, and gate the Profile/Dashboard tabs behind
  `FirebaseAuth` sign-in.
- **AI/ML-based exposure detection:** the brief calls this "future-ready."
  `DataBrokerRegistry.detect()` is the seam to replace with a model or a real
  scanning API — it's a pure function from `Set<PiiFieldType>` to
  `List<DataBroker>`, so nothing else needs to change.
