# Android CPU Monitor Pro - Master Development Plan

## 1. Product Vision

Build a professional Android CPU monitoring platform focused on:
- Real-time system monitoring
- Performance diagnostics
- Thermal analysis
- Device benchmarking
- Historical analytics
- Professional reporting

Target competitors:
- CPU-Z
- DevCheck
- Device Info HW
- Resource Monitor

Design goal:
A modern, scalable, maintainable Android application that can grow for years without major architectural refactoring.

---

# 2. Core Principles

## Architecture First

All development decisions must prioritize:

- Scalability
- Maintainability
- Testability
- Performance
- Feature isolation
- Modularization

Never sacrifice architecture quality for short-term speed.

---

# 3. Technology Stack

## Language

- Kotlin

## UI

- Jetpack Compose
- Material 3

## Architecture

- Clean Architecture
- MVVM
- Repository Pattern
- Use Cases

## Dependency Injection

- Hilt

## Concurrency

- Kotlin Coroutines
- Flow
- StateFlow
- SharedFlow

## Navigation

- Navigation Compose

## Persistence

- Room Database
- DataStore

## Charts

- Vico Charts

## Background Tasks

- Foreground Service
- WorkManager

## Logging

- Timber

## Analytics

- Firebase Analytics

## Crash Monitoring

- Firebase Crashlytics

## CI/CD

- GitHub Actions

---

# 4. Project Structure

feature-based modular architecture

app/

core/
- common
- designsystem
- ui
- database
- datastore
- monitoring
- charts
- network
- logging
- analytics
- testing

domain/
- model
- repository
- usecase

data/
- repository
- datasource

feature-dashboard
feature-cpu
feature-memory
feature-battery
feature-thermal
feature-storage
feature-process
feature-deviceinfo
feature-benchmark
feature-history
feature-export
feature-overlay
feature-alerts
feature-settings

service-monitoring
service-overlay

---

# 5. Data Layer Design

Monitoring sources:

- /proc/stat
- /proc/cpuinfo
- /sys/devices/system/cpu
- ActivityManager
- BatteryManager
- StorageManager
- Thermal APIs
- UsageStatsManager

Create abstraction layer:

SystemMonitorProvider

All feature modules must use repositories only.

No UI should directly access Android APIs.

---

# 6. Feature Set

## Dashboard

Purpose:
System overview.

Metrics:

- CPU Usage
- CPU Frequency
- CPU Temperature
- RAM Usage
- Battery Status
- Storage Usage
- Device Health Score

Realtime refresh.

Customizable widgets.

---

## CPU Module

### Usage

- Total Usage
- Per-Core Usage
- Big Core Usage
- Little Core Usage

### Frequency

- Current
- Minimum
- Maximum

### Architecture

- ARM Version
- ABI
- Core Count

### History

- 1 minute
- 5 minutes
- 15 minutes
- 1 hour
- 24 hours

### Charts

Realtime charts.

---

## Thermal Module

### Monitoring

- CPU Temperature
- Battery Temperature
- Thermal Zones

### Detection

- Overheating
- Thermal Throttling

### Analytics

- Temperature trends
- Heat maps

---

## RAM Module

### Memory Metrics

- Used
- Available
- Free
- Cached

### Process Memory

Top memory consumers.

### History

Memory usage timeline.

---

## Battery Module

### Information

- Percentage
- Voltage
- Current
- Capacity
- Temperature
- Health

### Charging

- Charge speed
- Remaining time estimation

### Battery Analytics

- Consumption trends
- Charging history

---

## Storage Module

### Internal Storage

- Used
- Free

### Categories

- Applications
- Images
- Videos
- Audio
- Documents
- Downloads

---

## Process Monitor

### Running Processes

Display:

- Process Name
- PID
- CPU Usage
- Memory Usage

### Sorting

- CPU
- RAM
- Name

### Search

Realtime filtering.

---

## Device Information

### Hardware

- CPU
- GPU
- RAM
- Storage

### Software

- Android Version
- Kernel
- Security Patch

### Screen

- Resolution
- Density
- Refresh Rate

### Network

- WiFi
- Mobile Data
- IP Information

---

## Benchmark Module

### CPU Benchmark

- Single Core
- Multi Core

### Stress Test

- 5 minutes
- 10 minutes
- 30 minutes

### Results

- Score
- Temperature
- Frequency Stability

---

## Overlay Module

Floating monitor.

Display:

- CPU
- RAM
- Temperature
- FPS

Gaming mode support.

---

## History Module

Store historical metrics.

Retention options:

- 24 Hours
- 7 Days
- 30 Days
- Unlimited

---

## Alerts Module

User-defined alerts.

Examples:

- CPU > 90%
- Temperature > 45°C
- RAM > 85%
- Battery < 20%

Notification support.

---

## Export Module

Formats:

- CSV
- JSON
- PDF

Generate diagnostic reports.

---

# 7. Database Design

Tables:

- cpu_metrics
- memory_metrics
- thermal_metrics
- battery_metrics
- storage_metrics
- benchmark_results
- alert_history

Retention policies configurable.

---

# 8. UI/UX Design

Material 3.

Themes:

- Light
- Dark
- AMOLED

Responsive layouts.

Support:

- Phones
- Tablets
- Foldables

Consistent card system.

Realtime animations.

---

# 9. Performance Requirements

Dashboard refresh:
- 1 second

Background refresh:
- 5 to 60 seconds configurable

CPU overhead:
- Below 2%

Battery impact:
- Minimal

Memory footprint:
- Below 150 MB

---

# 10. Testing Strategy

Unit Tests

Coverage target:
- 80%+

Integration Tests

Repository validation.

UI Tests

Compose testing.

Stress Testing

Long-running monitoring sessions.

---

# 11. Security & Privacy

No root required.

Local-first architecture.

No personal data collection.

Export only on user request.

Clear privacy policy.

---

# 12. CI/CD

GitHub Actions

Automated:

- Build
- Lint
- Unit Tests
- Release Generation

Versioning:

Semantic Versioning

---

# 13. Release Roadmap

## MVP

Dashboard
CPU
RAM
Battery
Thermal

## V1.0

Storage
History
Export

## V1.5

Process Monitor
Alerts

## V2.0

Overlay
Benchmark
Stress Test

## V3.0

Advanced Analytics
Performance Scoring
Device Health Engine

---

# 14. Cursor AI Development Rules

Always follow:

1. Clean Architecture.
2. Feature-first modularization.
3. No business logic inside UI.
4. Repository pattern everywhere.
5. Use immutable UI state.
6. One responsibility per class.
7. Use Kotlin Flow for realtime data.
8. Full documentation for public APIs.
9. No duplicated code.
10. Prefer composition over inheritance.

Every generated code change must preserve architecture consistency.

---

# 15. Long-Term Expansion

Future modules:

- GPU Monitor
- FPS Monitor
- Network Monitor
- AI Diagnostics
- Device Health Prediction
- Cloud Sync
- Wear OS Companion
- Desktop Dashboard
- Remote Monitoring

This architecture must support future expansion without requiring major rewrites.

---

## Tiến độ

### Phase 1: Foundation Setup

| Bước | Status | Notes |
|------|--------|-------|
| Multi-module structure (Section 4) | completed | 2026-06-08 — `app`, `core/*` (11 modules), `domain`, `data`, 14 `feature-*`, 2 `service-*` |
| Gradle dependencies (Coroutines, Hilt, Navigation Compose, Room, Timber) | completed | 2026-06-08 — `gradle/libs.versions.toml` + per-module `build.gradle.kts` |
| Domain Clean Architecture base (`UseCase`, `FlowUseCase`, `Repository`, `Result`) | completed | 2026-06-08 — `domain/src/main/kotlin/com/cpumonitor/domain/` |
| Data layer repository pattern (`BaseRepository`, `DataSource` contracts) | completed | 2026-06-08 — `data/src/main/kotlin/com/cpumonitor/data/` |
| UI skeleton (no business logic in Composables/ViewModels) | completed | 2026-06-08 — Feature screens delegate to immutable `UiState`; `BaseViewModel` in `core:ui` |
| Compile verification (`assembleDebug`) | completed | 2026-06-08 — BUILD SUCCESSFUL |
| UI/UX Design System — Section 8 (`core:designsystem`) | completed | 2026-06-08 — M3 Light/Dark/AMOLED themes, card system, immutable UI models, Vico charts in `core:charts`, dummy StateFlow in `DashboardViewModel` |

### Phase 2: Overlay Module (Sections 4 & 6)

| Bước | Status | Notes |
|------|--------|-------|
| Domain overlay models & use cases | completed | 2026-06-08 — `OverlayMetrics`, `OverlayConfig`, 5 use cases, `OverlayServiceController` gateway |
| Data overlay repository (mock metrics + permission) | completed | 2026-06-08 — `OverlayRepositoryImpl`, `MockOverlayMetricsDataSource`, `OverlayPermissionDataSource` |
| `service-overlay` Foreground Service + floating Compose window | completed | 2026-06-08 — `OverlayMonitoringService`, `OverlayWindowManager`, gaming-mode optimizations |
| `feature-overlay` permission UI + controls | completed | 2026-06-08 — `OverlayPermissionEffect`, `OverlayScreen`, Hilt `OverlayViewModel` |
| Overlay modules compile verification | completed | 2026-06-08 — `:service-overlay:compileDebugKotlin` + `:feature-overlay:compileDebugKotlin` BUILD SUCCESSFUL |
| Real overlay metrics via SystemMonitorProvider + native FPS | completed | 2026-06-08 — `SystemOverlayMetricsDataSource`, thermal sysfs, `FpsMonitorProviderImpl` (Choreographer) |

### Phase 2: Persistence Layer (Section 7)

| Bước | Status | Notes |
|------|--------|-------|
| Domain metric models (`CpuMetric`, `MemoryMetric`, `ThermalMetric`, `BatteryMetric`) | completed | 2026-06-08 — `domain/model/metric/` |
| Domain settings models (`AppSettings`, `RetentionPolicy`, `RetentionPeriod`, `AppTheme`) | completed | 2026-06-08 — `domain/model/settings/` |
| Domain repository contracts (`MetricsRepository`, `SettingsRepository`) | completed | 2026-06-08 — chỉ expose interface ra domain layer |
| Room entities + DAOs (Coroutines/Flow) | completed | 2026-06-08 — `core:database` — 4 bảng: `cpu_metrics`, `memory_metrics`, `thermal_metrics`, `battery_metrics` |
| Room `CPUMonitorDatabase` + Hilt `DatabaseModule` | completed | 2026-06-08 — `MetricTypeConverters`, index `timestamp_millis` |
| DataStore app settings + retention policy | completed | 2026-06-08 — `core:datastore` — `AppPreferencesDataStore`, `DataStoreModule` |
| Data layer: local data sources, mappers, repository impls | completed | 2026-06-08 — `data/datasource/local/`, `data/mapper/`, `data/repository/` |
| Hilt bindings (`DataModule`) | completed | 2026-06-08 — bind `MetricsRepository`, `SettingsRepository` |
| Persistence compile verification | completed | 2026-06-08 — `:data:assembleDebug` BUILD SUCCESSFUL |
| Retention WorkManager job (`ApplyRetentionPolicyUseCase` + `MetricRetentionWorker`) | completed | 2026-06-08 — chạy mỗi 24h, gọi `delete*MetricsBefore` theo `RetentionPolicy` |
| Background metric persistence (`PersistPolledMetricsUseCase` + `BackgroundMonitoringService`) | completed | 2026-06-08 — poll CPU/RAM, ghi Room; interval từ `AppSettings.backgroundRefreshIntervalSeconds` |
| `service-monitoring` compile verification | completed | 2026-06-08 — `:service-monitoring:compileDebugKotlin` BUILD SUCCESSFUL |

### Phase 2: Data Layer Isolation (Section 5)

| Bước | Status | Notes |
|------|--------|-------|
| `SystemMonitorProvider` abstraction | completed | 2026-06-08 — `core:monitoring/SystemMonitorProvider.kt` + `SystemMonitorProviderImpl` |
| Data sources (`/proc/stat`, `/proc/cpuinfo`, `ActivityManager`) | completed | 2026-06-08 — `ProcStatDataSource`, `ProcCpuInfoDataSource`, `ActivityManagerMemoryDataSource` |
| Domain models + repository contracts (CPU, RAM) | completed | 2026-06-08 — `CpuUsageMetrics`, `MemoryMetrics`, `CpuRepository`, `MemoryRepository` |
| Repository impls `Flow<Result<T>>` + Hilt DI | completed | 2026-06-08 — `CpuRepositoryImpl`, `MemoryRepositoryImpl`, `MonitoringModule` |
| Parser unit tests + performance tuning | completed | 2026-06-08 — delta sampling, min 500ms interval, `distinctUntilChanged`, IO dispatcher |

### Phase 3: MVP Feature UI (Section 13 MVP)

| Bước | Status | Notes |
|------|--------|-------|
| `ThermalRepository` + `BatteryRepository` (domain + data + Hilt) | completed | 2026-06-08 — `BatteryMetrics`, `BatteryManagerDataSource`, `ThermalRepositoryImpl`, `BatteryRepositoryImpl`, `observeBattery()` on `SystemMonitorProvider` |
| Monitoring use cases (`ObserveCpuUsage`, `ObserveMemory`, `ObserveThermal`, `ObserveBattery`, `ObserveCpuArchitecture`) | completed | 2026-06-08 — `domain/usecase/monitoring/`, `MonitoringUseCaseUtils.unwrap` |
| Dashboard wire real data | completed | 2026-06-08 — `DashboardViewModel` xóa dummy feed, combine 5 streams, thêm battery widget |
| Feature CPU module UI | completed | 2026-06-08 — usage per-core, big/little, frequency, architecture, chart, history 1 min từ Room |
| Feature Memory module UI | completed | 2026-06-08 — used/available/free/cached, % chart, `formatBytes` helper |
| Feature Thermal module UI | completed | 2026-06-08 — CPU/battery temp, thermal zones, overheating flag, chart |
| Feature Battery module UI | completed | 2026-06-08 — %, voltage, current, temp, health, charging, chart |
| Extend `PersistPolledMetricsUseCase` (thermal + battery) | completed | 2026-06-08 — persist `ThermalMetric` + `BatteryMetric` với zones và overheating |
| Unit tests (`BatteryStatusParser`, `ThermalTemperatureNormalizer`) | completed | 2026-06-08 — `data/src/test/.../system/` |
| Compile verification (`assembleDebug`) | completed | 2026-06-08 — BUILD SUCCESSFUL |

### Phase 4: V1.0 Feature UI (Section 13 V1.0)

| Bước | Status | Notes |
|------|--------|-------|
| Storage domain + data (`StorageRepository`, `StorageDataSource`, StatFs/MediaStore) | completed | 2026-06-08 — `StorageMetrics`, `StorageCategory`, `ObserveStorageUsageUseCase` |
| Feature Storage UI | completed | 2026-06-08 — used/free/total, category breakdown cards |
| History models + `ObserveMetricHistoryUseCase` | completed | 2026-06-08 — `HistoryMetricType`, `HistoryTimeRange` (1m–24h), chart từ Room |
| Feature History UI | completed | 2026-06-08 — FilterChips metric/time range, `RealtimeLineChart` |
| Export domain + data (`ExportRepository`, CSV/JSON/PDF) | completed | 2026-06-08 — `ExportMetricsReportUseCase`, `MetricsReportBuilder`, `PdfReportWriter` |
| Feature Export UI + FileProvider share | completed | 2026-06-08 — format/range picker, share intent, `app/res/xml/file_paths.xml` |
| `MetricsRepository.get*Metrics` one-shot queries | completed | 2026-06-08 — hỗ trợ export snapshot |
| Dashboard quick nav (Storage/History/Export) | completed | 2026-06-08 — Quick Access cards + `CPUMonitorNavHost` wiring |
| Unit tests (`MetricsReportBuilder`) | completed | 2026-06-08 — CSV/JSON builder tests |
| Compile verification (`assembleDebug`) | completed | 2026-06-08 — BUILD SUCCESSFUL |

### Phase 5: V1.5 Feature UI (Section 13 V1.5)

| Bước | Status | Notes |
|------|--------|-------|
| Process domain + data (`ProcessRepository`, `ActivityManagerProcessDataSource`, `ProcPidStatParser`) | completed | 2026-06-08 — `RunningProcess`, per-PID CPU từ `/proc/[pid]/stat`, PSS memory |
| Feature Process UI | completed | 2026-06-08 — sort CPU/RAM/Name, search, process list |
| Alerts domain + data (`AlertRepository`, `AlertRulesCodec`, Room `alert_history` v2) | completed | 2026-06-08 — `AlertRule`, `AlertHistoryEntry`, DataStore rules |
| Alert evaluation + notifications (`MonitorAlertsUseCase`, `BackgroundMonitoringService`) | completed | 2026-06-08 — `AlertEvaluator`, cooldown 5 phút, `AlertNotificationControllerImpl` |
| Feature Alerts UI | completed | 2026-06-08 — toggle rules, history list, default presets |
| Dashboard quick nav (Process/Alerts) | completed | 2026-06-08 — Quick Access cards + `CPUMonitorNavHost` wiring |
| Unit tests (`AlertEvaluator`, `ProcPidStatParser`, `AlertRulesCodec`) | completed | 2026-06-08 — domain + data tests pass |
| Compile verification (`assembleDebug`) | completed | 2026-06-08 — BUILD SUCCESSFUL |

### Phase 6: V2.0 Feature UI (Section 13 V2.0)

| Bước | Status | Notes |
|------|--------|-------|
| Overlay module (Phase 2 foundation) | completed | 2026-06-08 — `feature-overlay` + `service-overlay` đã có từ Phase 2 |
| Dashboard quick nav → Overlay | completed | 2026-06-08 — Quick Access card + `CPUMonitorNavHost` wiring |
| Benchmark domain + data (`BenchmarkRepository`, `CpuBenchmarkEngine`) | completed | 2026-06-08 — single/multi core, stress 5/10/30 phút, thermal + frequency sampling |
| Benchmark use cases (`RunCpuBenchmark`, `RunStressTest`, `ObserveProgress`, `Cancel`) | completed | 2026-06-08 — `domain/usecase/benchmark/` |
| Feature Benchmark UI | completed | 2026-06-08 — mode/duration picker, progress, results (score, temp, stability) |
| Dashboard quick nav → Benchmark | completed | 2026-06-08 — Quick Access card V2.0 |
| Unit tests (`BenchmarkCalculators`) | completed | 2026-06-08 — score + frequency stability tests |
| Compile verification (`assembleDebug`) | completed | 2026-06-08 — BUILD SUCCESSFUL |

### Phase 7: V3.0 Feature UI (Section 13 V3.0)

| Bước | Status | Notes |
|------|--------|-------|
| Analytics domain engines (`DeviceHealthEngine`, `PerformanceScoreEngine`, `AdvancedAnalyticsEngine`) | completed | 2026-06-08 — pure scoring/trends từ metric history |
| `AnalyticsRepository` + `AnalyticsRepositoryImpl` | completed | 2026-06-08 — aggregate Room metrics, poll Flow |
| Analytics use cases (`ObserveAnalyticsDashboard`, `ObserveDeviceHealth`, `GetAnalyticsDashboard`) | completed | 2026-06-08 — `domain/usecase/analytics/` |
| Feature Analytics UI (`feature-analytics`) | completed | 2026-06-08 — health, performance score, trends, insights |
| Dashboard Device Health Score widget | completed | 2026-06-08 — widget `/100` + status từ `ObserveDeviceHealthUseCase` |
| Dashboard quick nav → Analytics | completed | 2026-06-08 — Quick Access card V3.0 |
| Unit tests (`DeviceHealthEngine`, `PerformanceScoreEngine`) | completed | 2026-06-08 — domain tests pass |
| Compile verification (`assembleDebug`) | completed | 2026-06-08 — BUILD SUCCESSFUL |

### Phase 8: UI/UX Design (Section 8)

| Bước | Status | Notes |
|------|--------|-------|
| Responsive layout (`MonitorWindowSize`, breakpoints phones/tablets/foldables) | completed | 2026-06-08 — `core:designsystem/layout/MonitorWindowSize.kt`, `MonitorWindowSizeProvider` |
| Adaptive dashboard grid + content padding | completed | 2026-06-08 — `DashboardScreen` dùng `adaptiveGridMinSize()` + `horizontalContentPadding()` |
| Realtime metric animations (`AnimatedContent` + `LiveIndicatorBadge`) | completed | 2026-06-08 — `MonitorMetricValue`, `MetricMonitorCard` |
| Settings UI (theme Light/Dark/AMOLED, background refresh, retention) | completed | 2026-06-08 — `feature-settings` full screen + use cases |
| Theme wiring từ DataStore → `CPUMonitorTheme` | completed | 2026-06-08 — `AppThemeViewModel`, `CPUMonitorApp`, `AppThemeMapper` |
| Dashboard quick nav → Settings | completed | 2026-06-08 — Quick Access card + `CPUMonitorNavHost` wiring |
| Compile verification (`assembleDebug`) | completed | 2026-06-08 — BUILD SUCCESSFUL |

### Phase 9: Performance Requirements (Section 9)

| Bước | Status | Notes |
|------|--------|-------|
| `PerformanceBudget` domain constants (1s dashboard, 5–60s background, 2% CPU, 150 MB RAM) | completed | 2026-06-08 — `domain/performance/PerformanceBudget.kt` |
| `MonitoringOverheadTracker` + `AppMemorySnapshot` | completed | 2026-06-08 — `core:monitoring`, tích hợp `monitoringPollFlow` |
| Settings performance diagnostics UI | completed | 2026-06-08 — CPU overhead % + memory footprint trong Settings |
| Dashboard refresh 1s (`MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS = 1000`) | completed | 2026-06-08 — xác nhận giữ nguyên |
| Background refresh configurable 5–60s (`AppSettings` + `BackgroundMonitoringService`) | completed | 2026-06-08 — Settings slider wired |
| Compile verification (`assembleDebug`) | completed | 2026-06-08 — BUILD SUCCESSFUL |

### Phase 10: Testing Strategy (Section 10)

| Bước | Status | Notes |
|------|--------|-------|
| `core:testing` utilities (`CoroutineTestRule`, `TestDispatchersProvider`) | completed | 2026-06-08 — shared test helpers |
| Unit tests mở rộng (`PerformanceBudget`, `AdvancedAnalyticsEngine`, `MonitoringOverheadTracker`, `MonitorWindowSize`) | completed | 2026-06-08 — domain + core modules |
| Integration tests repository (`SettingsRepositoryImplTest` + fake data source) | completed | 2026-06-08 — `data/src/test/.../SettingsRepositoryImplTest.kt` |
| Stress test long-running poll (`MonitoringPollFlowStressTest` 120 samples) | completed | 2026-06-08 — xác nhận CPU overhead trong budget |
| Compose UI test (`DashboardScreenTest`) | completed | 2026-06-08 — `feature-dashboard/src/androidTest` |
| Test suite + `assembleDebug` verification | completed | 2026-06-08 — `:domain:test`, `:data:testDebugUnitTest`, `:core:monitoring:test`, `:core:designsystem:testDebugUnitTest` BUILD SUCCESSFUL |

### Phase 11: Device Information & Authenticity (Section 6 — Device Information)

| Bước | Status | Notes |
|------|--------|-------|
| Domain models (`DeviceSpec`, `AuthenticityReport`) | completed | 2026-06-08 — `domain/model/device/` |
| `DeviceAuthenticityValidator` cross-check heuristics | completed | 2026-06-08 — emulator, hardware mismatch, core count, ROM restrictions |
| Data sources (`BuildInfo`, `CpuSysfs`, `DisplayInfo`, `GpuInfo`) | completed | 2026-06-08 — `data/datasource/system/` |
| `DeviceRepository` + use cases + Hilt binding | completed | 2026-06-08 — `DeviceRepositoryImpl`, `GetDeviceSpecUseCase`, `ValidateDeviceAuthenticityUseCase` |
| Feature Device Info UI (specs + per-core MHz + authenticity score) | completed | 2026-06-08 — `feature-deviceinfo` full screen |
| Settings entry → Device Info | completed | 2026-06-08 — `settingsScreen(onNavigateToDeviceInfo)` |
| Unit tests (`DeviceAuthenticityValidatorTest`) | completed | 2026-06-08 — trusted/emulator/mismatch cases |
| Compile verification (`assembleDebug` + `installDebug`) | completed | 2026-06-08 — BUILD SUCCESSFUL, đã cài lên Honor AAK-AN00 |

### Phase 12: App Update via GitHub Releases

| Bước | Status | Notes |
|------|--------|-------|
| Domain models (`AppRelease`, `AppUpdateStatus`) + `VersionComparator` | completed | 2026-06-08 — `domain/model/update/`, `domain/update/VersionComparator.kt` |
| `AppUpdateRepository` + use cases (`CheckForAppUpdate`, `DownloadAppUpdate`) | completed | 2026-06-08 — `domain/repository/AppUpdateRepository.kt`, `domain/usecase/update/` |
| `core:network` OkHttp + `GitHubReleaseDataSource` | completed | 2026-06-08 — GitHub API `/releases/latest`, parse APK asset |
| `AppUpdateRepositoryImpl` + Hilt binding | completed | 2026-06-08 — download APK vào cache, so sánh version |
| Settings UI — App update card (check/download/install) | completed | 2026-06-08 — `feature-settings`, `AppUpdateInstaller`, FileProvider |
| BuildConfig `GITHUB_REPO_OWNER/NAME` + permissions | completed | 2026-06-08 — `TaoLaoVN/MaHUD`, INTERNET, REQUEST_INSTALL_PACKAGES |
| Git init + push `main` → GitHub | completed | 2026-06-08 — https://github.com/TaoLaoVN/MaHUD |
| Compile verification (`assembleDebug` + `domain:test`) | completed | 2026-06-08 — BUILD SUCCESSFUL |
