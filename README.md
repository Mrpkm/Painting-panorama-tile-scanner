# Photo Mosaic Scanner (Stage 1)

**Photo Mosaic Scanner** is a native Android application that serves as a guided capture-grid assistant for photographing large flat artwork or paintings in overlapping tiles. The resulting high-resolution photographs are named deterministically and saved alongside session metadata, making them ready for image stitching software such as **Hugin**.

---

## 📱 Features

- 📐 **Grid & Session Setup**: Input painting dimensions (cm/mm/in), target overlap percentage (with built-in validation warnings), and grid rows/columns with intelligent grid estimation.
- 📷 **Live CameraX Preview**: Real-time camera preview overlaid with translucent serpentine grid tiles, highlighted current target, and plain-language movement directions (*"Move right to tile r01_c02"*).
- 📸 **High-Resolution Capture**: Uses CameraX (`ResolutionSelector.HIGHEST_AVAILABLE_STRATEGY`, `CAPTURE_MODE_MAXIMIZE_QUALITY`) to capture full-resolution stills.
- 🏷️ **Deterministic & Retake-Safe Naming**: Photos are automatically saved as `r01_c01.jpg`, `r01_c02.jpg`, etc. Retakes automatically append version suffixes (`r01_c01_v2.jpg`) without overwriting previous attempts.
- 🔄 **Session Persistence & Recovery**: Scan state and photo metadata are continually saved to `session.json`. Re-opening the app automatically resumes the scan session exactly where you left off.
- 📊 **Overview & Jump Navigation**: Visual tile overview grid allowing quick tap-to-jump navigation to retake any specific tile.

---

## 📁 Repository Structure

```
Painting-panorama-tile-scanner/
├── .github/
│   └── workflows/
│       └── build-apk.yml              # GitHub Actions CI workflow to build debug APK
├── app/
│   ├── build.gradle.kts               # App module build configuration & dependencies
│   └── src/main/
│       ├── AndroidManifest.xml        # Application manifest & permissions
│       ├── java/com/example/photomosaicscanner/
│       │   ├── MainActivity.kt        # Main entry point & simple screen router
│       │   ├── capture/               # Camera & file naming logic
│       │   │   ├── CaptureFileNaming.kt    # Deterministic file & versioned naming
│       │   │   └── PhotoCaptureController.kt # CameraX preview & image capture manager
│       │   ├── grid/                  # Grid math & geometry
│       │   │   └── GridCalculator.kt  # Serpentine ordering & grid estimation
│       │   ├── model/                 # Core domain data models
│       │   │   ├── PaintingUnits.kt   # Unit definitions (CM, MM, IN)
│       │   │   ├── ScanSession.kt     # Scan session state & completion tracking
│       │   │   ├── TileCoordinate.kt  # Row/Column tile coordinate model
│       │   │   └── TileRecord.kt      # Captured photo metadata model
│       │   ├── session/               # State management & repository
│       │   │   ├── MovementHint.kt    # Qualitative directional movement hints
│       │   │   ├── SessionRepository.kt # JSON file persistence (session.json)
│       │   │   └── SessionViewModel.kt# ViewModel managing UI state & transitions
│       │   └── ui/                    # Jetpack Compose UI layer
│       │       ├── camera/            # Live camera capture screen
│       │       ├── components/        # Shared UI components (GridDiagram)
│       │       ├── overview/          # Session overview & progress grid
│       │       ├── setup/             # Initial scan configuration screen
│       │       └── theme/             # Material3 theme definitions
│       └── res/                       # App icons, strings, and XML resources
├── build.gradle.kts                   # Root build script
├── settings.gradle.kts                # Gradle settings & plugin management
├── gradlew / gradlew.bat              # Gradle wrapper scripts
└── README.md                          # Project documentation
```

---

## 🏗️ Architecture & Component Overview

The application follows clean architecture principles with Jetpack Compose and MVVM:

1. **Domain Models (`model/`)**: Pure Kotlin data classes representing unit conversions, grid coordinates, and scan progress with zero Android dependencies.
2. **Grid Geometry (`grid/`)**: `GridCalculator` determines the optimal serpentine path (boustrophedon pattern) for minimal camera movement during scanning.
3. **Capture Controller (`capture/`)**: Decouples CameraX lifecycle and hardware interactions from the UI layer. Handles high-resolution still image acquisition.
4. **Session Persistence (`session/`)**: `SessionRepository` serializes session state into `session.json` directly inside the session image folder. `SessionViewModel` exposes reactive state via Kotlin `StateFlow`.
5. **UI Layer (`ui/`)**: Declarative Jetpack Compose UI built with Material3.

---

## 🛠️ How to Build & Run

### Prerequisites
- **Android Studio**: Koala (2024.1.1) or newer recommended.
- **JDK**: Java 17.
- **Target Device**: Android 8.0 (API Level 26) or higher with camera support.

### Building with Android Studio
1. Clone the repository:
   ```bash
   git clone https://github.com/Mrpkm/Painting-panorama-tile-scanner.git
   ```
2. Open Android Studio and select **Open** -> Select the cloned repository directory.
3. Let Gradle sync complete.
4. Select your connected Android device or emulator and click **Run (Shift + F10)**.

### Building from Command Line
You can build the debug APK using the included Gradle wrapper:

```bash
# On Linux / macOS
./gradlew assembleDebug

# On Windows (PowerShell / Command Prompt)
.\gradlew.bat assembleDebug
```

The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

To install directly onto a connected device via ADB:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## ☁️ Continuous Integration (GitHub Actions)

This repository includes a GitHub Actions workflow ([`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml)) that automatically compiles the debug APK on every commit pushed to `main`.

To download the compiled APK from GitHub:
1. Go to the **Actions** tab in the GitHub repository.
2. Select the latest workflow run.
3. Scroll down to **Artifacts** and download `PhotoMosaicScanner-debug-apk`.

---

## 💾 Storage & Photo Location

Captured photographs and session metadata are stored in app-specific external storage:

```
Android/data/com.example.photomosaicscanner/files/Pictures/PhotoMosaicScanner/
└── Painting_<YYYY-MM-DD>_<SESSION_ID>/
    ├── session.json
    ├── r01_c01.jpg
    ├── r01_c02.jpg
    └── r01_c01_v2.jpg (retake)
```

### Exporting Photos to PC
To copy your session photos to a Windows PC for stitching in Hugin:
- Connect your phone via USB cable in **File Transfer (MTP)** mode and browse to the path above.
- Alternatively, pull the files via ADB:
  ```bash
  adb pull /sdcard/Android/data/com.example.photomosaicscanner/files/Pictures/PhotoMosaicScanner/ .
  ```

---

## 🚀 Roadmap (Future Stages)

- **Stage 2**: Integration of feature matching (OpenCV/MediaPipe), automatic field-of-view (FOV) calibration, and visual overlap calculation.
- **Stage 3**: Real-time motion detection, automatic shutter trigger on target alignment, and blur detection.
- **Stage 4**: Automated Hugin project file (`.pto`) generation and desktop sync.

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
