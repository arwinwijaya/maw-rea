# Base Application: Catatan Harian (Clean-Room MVP)

## Ikhtisar

`Catatan Harian` adalah aplikasi pencatatan keuangan pribadi Android native murni (Java, Android SDK, SQLite) yang dibuat dengan metode **clean-room**. Aplikasi ini tidak menggunakan kode sumber, aset, merek, atau nama paket dari aplikasi pihak ketiga manapun.

- **Application ID / Namespace**: `id.maw.catatan`
- **Minimum SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 35 (Android 15)
- **Kompatibilitas Java**: Java 17

## Arsitektur & Prinsip Desain

1. **Zero External UI Dependencies** — Seluruh antarmuka dibuat secara terprogram menggunakan `LinearLayout`, `TextView`, `Button`, dan `EditText` tanpa pustaka UI eksternal (seperti AndroidX AppCompat, Material Components, atau Compose) untuk memastikan build dapat diselesaikan tanpa hambatan unduhan dependensi maven yang lambat/gagal.
2. **Deriving Balances (Single Source of Truth)** — Saldo akun tidak pernah disimpan secara mutlak sebagai satu angka yang rentan tidak sinkron; saldo dihitung secara dinamis dari:
   $$\text{Saldo Akhir} = \text{Saldo Awal} + \sum (\text{Pemasukan}) - \sum (\text{Pengeluaran})$$
3. **Integer Minor Units** — Nilai uang disimpan dan dihitung sebagai integer dalam satuan sen/subunit terkecil (misalnya `125000` untuk Rp 1.250,00) untuk menghindari masalah presisi floating-point (`double`/`float`).
4. **Local-Only SQLite** — Menggunakan `SQLiteOpenHelper` standar Android dengan foreign key diaktifkan (`PRAGMA foreign_keys = ON`) dan transaksi atomik untuk penambahan entri.

## Struktur Proyek

```
android-app/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradle/wrapper/            # wrapper jar + properties (di-track)
├── gradlew, gradlew.bat
├── local.properties          # lokal, tidak di-track
└── app/
    ├── build.gradle
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/id/maw/catatan/
        │   │   ├── CatatanApplication.java
        │   │   ├── data/
        │   │   │   ├── Account.java
        │   │   │   ├── CatatanDbHelper.java
        │   │   │   ├── CatatanRepository.java
        │   │   │   └── Transaction.java
        │   │   ├── ui/
        │   │   │   ├── account/AccountsActivity.java
        │   │   │   ├── main/MainActivity.java
        │   │   │   ├── settings/SettingsActivity.java
        │   │   │   ├── summary/SummaryActivity.java
        │   │   │   └── transaction/TransactionActivity.java
        │   │   └── util/
        │   │       └── Money.java
        │   └── res/
        │       ├── drawable/ic_launcher.xml
        │       └── values/
        │           ├── strings.xml
        │           └── styles.xml
        └── test/
            └── java/id/maw/catatan/
                └── util/
                    └── MoneyTest.java
```

## Fitur Utama

1. **Dashboard (MainActivity)**
   - Menampilkan saldo akun utama beserta total pemasukan dan pengeluaran sepanjang waktu.
   - Tombol cepat untuk menambah Pemasukan (`INCOME`) atau Pengeluaran (`EXPENSE`).
   - Daftar transaksi terbaru (6 entri terakhir).
   - Navigasi bawah ke menu Akun, Ringkasan, dan Pengaturan.

2. **Form Transaksi (TransactionActivity)**
   - Input jumlah nominal dengan aturan parsing locale Indonesia (`1.250` atau `1.250,50`). Input belum diformat otomatis saat mengetik.
   - Input catatan opsional.
   - Penyimpanan dalam transaksi database atomik.

3. **Manajemen Akun (AccountsActivity)**
   - Daftar akun kas dengan saldo real-time.
   - Dialog tambah akun baru dengan nama unik dan saldo awal.

4. **Ringkasan Keuangan (SummaryActivity)**
   - Akumulasi total pemasukan, pengeluaran, dan saldo bersih per akun.

5. **Pengaturan (SettingsActivity)**
   - Informasi mata uang (IDR) dan versi aplikasi.

## Format Mata Uang & Parsing (`Money.java`)

Parser memakai konvensi locale Indonesia (`id_ID`): titik (`.`) sebagai pemisah ribuan dan koma (`,`) sebagai pemisah desimal. Nilai internal memakai dua digit subunit untuk semua mata uang, termasuk IDR—ini pilihan teknis MVP, bukan klaim bahwa Rupiah memiliki pecahan sen yang lazim dipakai. Contoh uji:
- `"1.250"` → `125000` (Rp 1.250 dalam representasi nominal tanpa pecahan)
- `"1.250,50"` → `125050` (Rp 1.250,50)
- `"1250"` → `125000` (Rp 1.250 dalam representasi nominal tanpa pecahan)
- Parser juga mendukung `"1250.50"` dari keyboard numerik sebagai Rp 1.250,50, jika titik tunggal diikuti satu atau dua angka.

Parser membedakan `1.250` (titik ribuan, tiga digit sesudah titik) dari `1250.50` (titik desimal, satu/dua digit sesudah titik). Ia belum memvalidasi tata bahasa pemisah secara ketat atau melakukan pembulatan lebih dari dua digit pecahan.

## Hasil Validasi Build & Tes

Diverifikasi lokal pada 2026-10-15 dengan Gradle wrapper 8.11.1 (JDK 21, Android SDK platform 35):

| Pemeriksaan | Hasil |
|-------------|-------|
| `./gradlew clean test assembleDebug` | **BUILD SUCCESSFUL** |
| Unit test `MoneyTest` (debug & release) | **5 tes, 0 gagal, 0 error** |
| APK debug | `android-app/app/build/outputs/apk/debug/app-debug.apk` |
| SHA-256 wrapper JAR | `2db75c40782f5e8ba1fc278a5574bab070adccb2d21ca5a6e5ed840888448046` |

Catatan: selain validasi build-time dan unit test, startup aplikasi serta tampilan dashboard juga diverifikasi di perangkat fisik. Interaksi UI dan alur transaksi/database belum diuji end-to-end.

### Uji Runtime di Perangkat Fisik

Diverifikasi pada perangkat Xiaomi M2103K19G (Android 13 / API 33, MIUI V14, ARM64):

| Pemeriksaan | Hasil |
|-------------|-------|
| `adb install -r app-debug.apk` | **Success** |
| Peluncuran `MainActivity` | **Berhasil** (`topResumedActivity=id.maw.catatan/.ui.main.MainActivity`) |
| Struktur UI dashboard (via `uiautomator dump`) | Sesuai: judul, saldo `Rp 0,00`, tombol Pemasukan/Pengeluaran, navigasi Akun/Ringkasan/Pengaturan |
| Crash saat peluncuran (`AndroidRuntime`) | Tidak ada di logcat |
| Interaksi UI via `adb shell input tap` | **Terblokir kebijakan MIUI** (`SecurityException: INJECT_EVENTS`) |

**Keterbatasan:** alur transaksi (tambah akun, simpan pemasukan/pengeluaran, verifikasi saldo) **belum diuji end-to-end** karena perangkat uji memblokir injeksi input ADB. Pengujian interaksi memerlukan instrumented test (AndroidJUnitRunner) atau verifikasi manual oleh pengguna.

## Batasan yang Diketahui (Out of Scope MVP)

- **Transfer antar akun** belum diimplementasikan.
- **Ringkasan per periode** (harian/bulanan) belum ada; `SummaryActivity` menampilkan total seumur hidup per akun.
- **Mata uang** masih terpaku IDR; belum ada pemilih mata uang.
- **Backup/pulihkan, sinkronisasi, kategori berlabel, dan lampiran** belum tersedia.
- **Edit/hapus transaksi** belum tersedia (hanya tambah).
- **Migrasi skema** belum ada (`onUpgrade` sengaja melempar error agar perubahan skema tidak senyap).

## Petunjuk Build & Test

Prasyarat:
- Java JDK 17 atau 21 (`JAVA_HOME` diset)
- Android SDK dengan platform-35 dan build-tools terpasang (`ANDROID_HOME` diset)

Perintah Build (Debug APK):
```bash
cd android-app
./gradlew assembleDebug
```
Output APK tersedia di:
`android-app/app/build/outputs/apk/debug/app-debug.apk`

Perintah Unit Test:
```bash
./gradlew test
```

## Perbedaan dari Aplikasi Asal (Clean-Room Differentiation)

| Aspek | Aplikasi Asal (`cashbook.cashbook`) | Clean-Room MVP (`id.maw.catatan`) |
|-------|--------------------------------------|-----------------------------------|
| **Paket & Nama** | `cashbook.cashbook` / terobfuscasi | `id.maw.catatan` / `Catatan Harian` |
| **Framework UI** | Detail internal tidak sepenuhnya terverifikasi melalui dekompilasi | Murni Android Views (`LinearLayout`, `TextView`) tanpa dependensi UI eksternal |
| **SDK & Target** | Target SDK 36 | Target SDK 35 (kompatibel penuh Android 15) |
| **Kompleksitas** | 15.185 kelas terdeteksi JADX | Kumpulan kelas Java kecil untuk pencatatan lokal dasar |
| **Privasi** | Permission jaringan teramati; penggunaan aktual tidak disimpulkan | Manifest MVP tanpa permission jaringan dan backup Android dinonaktifkan; tidak ada SDK iklan/billing dalam dependensi proyek |
