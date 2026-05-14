# GeoMeasure Pro

**Precision Land Measurement — Offline, Private, Free. Forever.**

[![Platform](https://img.shields.io/badge/platform-Android%209%E2%80%9315+-brightgreen)]()
[![Language](https://img.shields.io/badge/language-Kotlin-purple)]()
[![Architecture](https://img.shields.io/badge/architecture-MVI%20%2B%20Clean%20Architecture-blue)]()
[![License](https://img.shields.io/badge/license-Apache%202.0-green)]()
[![Release](https://img.shields.io/github/v/release/SecretArrow/GeoMeasure)](https://github.com/SecretArrow/GeoMeasure/releases/latest)

GeoMeasure Pro is a **professional-grade land measurement application** built for surveyors, farmers, land owners, construction engineers, and hikers — anyone who needs accurate polygon-based land measurement without paying recurring cloud fees.

---

## 📸 Screenshots

```
┌─────────────────────────────┐
│  GeoMeasure Pro             │
├─────────────────────────────┤
│                             │
│     🗺️ OSMDroid Map         │
│        with My Location     │
│                             │
│   [Vertex markers]          │
│   [Polygon overlay]         │
├─────────────────────────────┤
│  GPS: 3.2m  8/12 sats      │
├─────────────────────────────┤
│  Area: 2,450.3 m²  0.245 ha│
│  Perimeter: 198.4 m         │
└─────────────────────────────┘
```

---

## ✨ Features

### 🗺️ Map
- **OSMDroid offline engine** — OpenStreetMap tiles, no API key required
- **My Location** — Real-time blue dot, follow-me toggle, GPS accuracy display
- **Multi-touch** — Pinch zoom, pan, rotate
- **Tile caching** — Automatic local tile cache for offline use
- **Tile download** — Download regions by bounding box for offline use
- **MBTiles / .map import** — Pre-built offline map support
- **Satellite / hybrid overlay** (configurable tile source)
- **Scale bar** — Visual distance reference overlay
- **My Location** — Real-time blue dot with follow-me mode
- **Point labels** — P1, P2, P3... markers with info windows
- **Vertex markers** — Draggable, with order labels

### 📏 Measurement
- **Tap-to-Measure** — Tap map to place vertices, auto-close polygon
- **Walk-to-Measure** — GPS recording via foreground service (screen-off safe)
- **Line measurement** — Distance between two points
- **Point capture** — Single coordinate capture
- **Real-time area & perimeter** — Calculated using spherical excess formula on WGS-84
- **Geometry editing** — Drag vertices, undo/redo, delete points
- **Snap-to-grid** (1/10/100m)

### 🔢 Unit Conversion & Coordinate Formats
- **7 area units**: m², hectare, are, acre, km², ft², yd²
- **5 distance units**: m, km, mile, yard, foot
- Real-time conversion with formatted output
- **DMS format** (Degrees° Minutes' Seconds") — standard BPN Indonesia
- **Decimal Degrees (DD)** — standard digital format
- **UTM projection** — WGS-84 → UTM easting/northing/zone
- **TM-3 projection** — Indonesian local projection (BPN standard)
- **Bearing & Azimuth** — Direction between vertices (N 45° E, etc.)
- **Interior angles** — Left/Right turn at each vertex

### 🗄️ Data & Privacy
- **SQLCipher encrypted database** — All data encrypted at rest
- **Android KeyStore** — AES-256 GCM key management
- **Projects & folders** — Organize measurements hierarchically
- **Metadata** — Notes, timestamps, colors, measurement types
- **100% offline** — No account, no telemetry, no analytics

### 📤 Export / Import
| Format | Export | Import |
|--------|--------|--------|
| **GeoJSON** | ✅ FeatureCollection with Polygon | ✅ |
| **KML** | ✅ Placemark with LinearRing | ✅ |
| **GPX** | ✅ Waypoints + Track | ✅ |
| **CSV** | ✅ Vertex table + Summary | ❌ |
| **PDF** | ✅ iTextG report with map screenshot | ❌ |
| **SHP** | ✅ ESRI Shapefile (.shp+.shx+.dbf) | ❌ |
| **Screenshot** | ✅ Map image with coordinate overlay (PNG) | ❌ |

### ☁️ Google Drive Sync (Optional)
- Sign in with your Google account
- Auto-upload to "GeoMeasure Pro Backups" folder
- Download/restore from Drive
- Per-file sync tracking

### 📡 GPS & Sensors
- **Foreground service** — Recording continues with screen off
- **Accuracy filter** — Configurable threshold (default ≤ 10m)
- **Min distance filter** — Skip redundant points (default 1m)
- **HDOP / Satellite info** — Real-time GPS status panel
- **Persistent notification** — Shows point count, stop action

### 🎨 UI / UX
- **Material You** — Dynamic color on Android 12+
- **Dark mode** — Full dark theme support
- **4-screen navigation** — Map, Projects, Tools, Settings
- **Bottom sheet** — Quick access to measurements, coordinates, export
- **GPS status panel** — Collapsible overlay

### ⚙️ Settings
- Default area/distance units
- Decimal precision (0–8)
- GPS accuracy threshold, min distance, update interval
- Map cache management
- Dark mode, dynamic colors, GPS panel toggle
- Encrypted backup & restore
- Delete all data

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin 1.9.22 |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | MVI (Model-View-Intent) + Clean Architecture |
| **DI** | Dagger Hilt 2.50 + KSP |
| **Database** | Room 2.6.1 + SQLCipher 4.5.4 |
| **Map** | OSMDroid 6.1.18 |
| **Preferences** | DataStore Preferences |
| **Navigation** | Navigation Compose 2.7.6 |
| **PDF** | iTextG 5.5.10 |
| **Drive API** | Google Drive v3 REST API |
| **Build** | Gradle 8.5 + AGP 8.2.2 |
| **Min SDK / Target** | API 28 → API 35 |

### Project Structure

```
app/src/main/kotlin/com/geomeasure/pro/
├── core/
│   ├── geometry/          GeometryCalculator, GeoPoint
│   ├── gps/               GpsRecordingService, GpsStatusProvider
│   ├── map/               TileDownloadManager, OfflineMapManager
│   ├── security/          KeystoreManager
│   └── util/              UnitConverter, Extensions, FileUtils
├── data/
│   ├── local/
│   │   ├── db/            AppDatabase, entities, DAOs, migrations
│   │   └── prefs/         AppPreferences (DataStore)
│   ├── remote/drive/      DriveManager
│   ├── export/            GeoJson, KML, GPX, CSV, PDF exporters
│   ├── import/            FileImporter (GeoJSON/KML/GPX)
│   └── repository/        MeasurementRepositoryImpl
├── domain/
│   ├── model/             Project, Vertex, GpsStatus
│   ├── repository/        Interfaces
│   └── usecase/           CalculateArea, Export, Import, Sync
├── presentation/
│   ├── theme/             Theme, Typography, Color
│   ├── navigation/        AppNavigation (5 tabs)
│   ├── screens/
│   │   ├── map/           MapScreen, MapViewModel, MapUiState
│   │   ├── projects/      ProjectListScreen, ProjectViewModel
│   │   ├── tools/         ToolsScreen, ExportViewModel
│   │   ├── settings/      SettingsScreen, SettingsViewModel
│   │   └── drive/         DriveSettingsScreen, DriveViewModel
│   └── components/        GpsStatusPanel, MeasurementBottomSheet
└── di/                    AppModule (Hilt)
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 35

### Quick Start (Linux/macOS)
```bash
# 1. Clone the repo
git clone https://github.com/SecretArrow/GeoMeasure.git
cd GeoMeasure

# 2. Install dependencies (Java, Android SDK, Gradle)
./scripts/install.sh

# 3. Generate signing keystore
./scripts/sign.sh

# 4. Build signed release APKs
./scripts/build.sh

# 5. Run unit tests (82 tests)
./scripts/test.sh

# 6. Create GitHub release
./scripts/release.sh v1.2.0 "GeoMeasure Pro v1.2.0"
```

### Quick Start (Windows)
```cmd
scripts\install.bat
scripts\sign.bat
scripts\build.bat
scripts\test.bat
scripts\release.bat v1.2.0 "GeoMeasure Pro v1.2.0"
```

### One-shot CI/CD Pipeline
```bash
./scripts/all.sh --tag v1.2.0
```

---

## 📦 Build Output

### APK Variants
| Architecture | File | Size |
|-------------|------|------|
| ARM 64-bit | `GeoMeasure-Pro-arm64-v8a.apk` | ~53 MB |
| ARM 32-bit | `GeoMeasure-Pro-armeabi-v7a.apk` | ~51 MB |
| x86 64-bit | `GeoMeasure-Pro-x86_64.apk` | ~53 MB |
| x86 32-bit | `GeoMeasure-Pro-x86.apk` | ~53 MB |

### Signing
- **Signed**: `GeoMeasure-Pro-<arch>.apk` (with release keystore)
- **Unsigned**: `GeoMeasure-Pro-unsigned-<arch>.apk` (no signing config)

Credentials stored in `.env` file (gitignored). Auto-generated by `sign.sh`.

---

## 🧪 Testing

| Test Type | Count | Run Command |
|-----------|-------|-------------|
| **Unit tests** | 82 ✅ | `./gradlew testReleaseUnitTest` |
| **Instrumentation tests** | 8 | `./gradlew connectedDebugAndroidTest` |
| **UI tests (Compose)** | 13 | `./gradlew connectedDebugAndroidTest` |

### Test Coverage
- `GeometryCalculator` — area, perimeter, haversine (10 tests)
- `UnitConverter` — 7 area + 5 distance units (13 tests)
- `Extensions` — format, escape (9 tests)
- `GeoJsonExporter` — JSON structure, coordinates (7 tests)
- `KmlExporter` — XML, coordinates, escape (9 tests)
- `GpxExporter` — waypoints, track (9 tests)
- `CsvExporter` — header, rows, summary (9 tests)
- `CalculateAreaUseCase` — edge cases (4 tests)
- `MapViewModel` — state, modes, undo/redo (12 tests)
- `ProjectDao` — CRUD, search, sync (5 tests)
- `VertexDao` — insert, ordering, cascade (3 tests)

---

## 📜 Scripts

| Script | Purpose | Linux | Windows |
|--------|---------|-------|---------|
| Install | Setup Java + Android SDK + Gradle | `install.sh` | `install.bat` |
| Sign | Generate keystore + .env credentials | `sign.sh` | `sign.bat` |
| Build | Build signed/unsigned APKs | `build.sh` | `build.bat` |
| Test | Run unit + instrumentation tests | `test.sh` | `test.bat` |
| Release | Create GitHub release with APKs | `release.sh` | `release.bat` |
| Pipeline | Full CI/CD (install → sign → build → test → release) | `all.sh` | `all.bat` |

---

## 🔒 Security

- **Database**: 256-bit AES encrypted via SQLCipher
- **Key Storage**: Android Keystore (AES/GCM/NoPadding)
- **Backup**: Encrypted `.gmbackup` files
- **Drive Sync**: OAuth2 with minimal `drive.file` scope
- **Network**: HTTPS only, cleartext limited to OSM tile servers
- **Permissions**: Minimal — location, notifications, storage (pre-Q)

---

## 📄 License

```
Copyright 2026 GeoMeasure Pro

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repo
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 🙏 Credits

- [OSMDroid](https://github.com/osmdroid/osmdroid) — Map engine
- [SQLCipher](https://www.zetetic.net/sqlcipher/) — Encrypted database
- [iText](https://itextpdf.com/) — PDF generation
- OpenStreetMap contributors — Map data
- [Jetpack Compose](https://developer.android.com/jetpack/compose) — UI toolkit

---

## 📬 Contact

**GeoMeasure Pro** — Precision Land Measurement, Free for Everyone.
