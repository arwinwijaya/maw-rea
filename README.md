# Catatan Harian — Android Clean-Room MVP

Aplikasi Android native untuk pencatatan pemasukan, pengeluaran, akun kas, dan ringkasan dasar secara lokal.

- **Application ID:** `id.maw.catatan`
- **Framework:** Android SDK / Java 17 / SQLite
- **Minimum Android:** API 24
- **Dokumentasi:** [temuan reverse-engineering](docs/reverse-engineering.md) · [aplikasi dan build](docs/base-app.md)

## Build

Dengan JDK 17/21 dan Android SDK platform 35 terpasang:

```bash
cd android-app
./gradlew test assembleDebug
```

APK debug: `android-app/app/build/outputs/apk/debug/app-debug.apk`

Hasil reverse-engineering lokal dan APK sumber tidak disertakan di Git. Lihat `.gitignore`.
