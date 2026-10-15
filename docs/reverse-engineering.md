# Reverse-Engineering Report: Cash Book Daily Expenses (v238)

## Provenance

| Item | Value |
|------|-------|
| **Sumber file** | `target/Cash-Book-daily-expenses_238_apkcube.apks` |
| **SHA-256 base.apk** | `8d6d66ae20b88e19091680ecabbc24a072418a52e7e593068b6b688de1bc86d5` |
| **SHA-256 JAR JADX** | `6e5eacf500b64292bfb73c49797c1958f6ee44646e43e868039ae7feb573ff75` |
| **Paket aplikasi asli** | `cashbook.cashbook` |
| **Version code / name** | 238 / — |
| **minSdk / targetSdk** | 24 / 36 |
| **Jumlah kelas (JADX)** | 15.185 |
| **Tanggal analisis** | 2025-10-15 |

## Metode Analisis

- **Alat**: REA CLI v6.4.0 (engine JADX headless-mcp 0.7.1)
- **Perintah utama**: `npx -y rea-agents@latest inspect-android-class "<base.apk>" "<FQCN>" --format json`
- **Kelas yang diinspeksi**: `CashEntryActivity`, `EditActivity`, `AccountsActivity`, `TransferActivity`, `SummaryActivity`, `SettingsActivity`, `BackupRestoreActivity`, `CashBookRoomDatabase` (dan metode `v`, `w`, `x`, `y`, `z`)
- **Bukti disimpan**: `work/evidence/core/*.json`, `work/evidence/search_cashbook.json`

## Temuan Teramati (Observed)

1. **Struktur paket** — Aplikasi asli menggunakan paket `cashbook.cashbook` dengan 15.185 kelas terdeteksi.

2. **Komponen UI (Activity)** — REA mengidentifikasi minimal 7 Activity utama:
   - `CashEntryActivity` — input transaksi (pemasukan/pengeluaran)
   - `EditActivity` — edit entri
   - `AccountsActivity` — daftar akun kas
   - `TransferActivity` — transfer antar akun
   - `SummaryActivity` — ringkasan keuangan
   - `SettingsActivity` — pengaturan
   - `BackupRestoreActivity` — cadangkan/pulihkan data

3. **Database lokal** — `CashBookRoomDatabase` (Room) dengan metode singleton `y(Context)` dan DAO abstrak `z()`. Nama DAO/entity/field terobfuscasi (`p1.*`), sehingga **kontrak fungsional tidak dapat disimpulkan dari nama saja**.

4. **Arsitektur** — Menggunakan Room (SQLite), singleton database, pola repository/DAO standar Android.

5. **Manifes** — `minSdk=24`, `targetSdk=36`, permission internet & akses jaringan terdeteksi.

## Asumsi & Keterbatasan (Assumptions / Limitations)

| Asumsi | Status |
|--------|--------|
| Transfer antar akun diimplementasikan sama seperti aplikasi asli | **Belum diverifikasi** — nama `TransferActivity` ada, tapi detail perilaku (validasi saldo, double-entry) tidak diketahui dari dekompilasi statis. |
| Nama field DAO (`p1.w0`, `p1.x0`, `p1.j2`, `p1.y`, `p1.a1`, `p1.x1`) mencerminkan entitas transaksi/akun | **Tidak dapat disimpulkan** — obfuscasi menghapus makna semantik. |
| Ringkasan per periode (mingguan/bulanan) tersedia di aplikasi asli | **Tidak diverifikasi** — `SummaryActivity` ada, tapi granularitasnya tidak diketahui. |
| Mata uang/multi-currency didukung | **Tidak diverifikasi** — tidak ada evidence mata uang dari REA. |
| Backup/restore ke cloud/Google Drive | **Tidak diverifikasi** — `BackupRestoreActivity` ada tapi implementasinya tidak terbaca. |
| Enkripsi database / keamanan data | **Tidak diverifikasi** — Room default tidak mengenkripsi; tidak ada evidence SQLCipher. |

## Implikasi untuk Clean-Room Rebuild

- **Jangan menyalin** nama paket, nama class obfuscated, string resources, atau asset visual dari APK asli.
- **Implementasikan ulang** skema database dari prinsip akuntansi (double-entry ledger: saldo = saldo awal + Σ signed amount), bukan dari nama kolom obfuscated.
- **Transfer** dianggap *out of scope* untuk MVP sampai ada spesifikasi perilaku yang jelas.
- **Ringkasan** MVP hanya total seumur hidup per akun (bukan per periode).
- **Mata uang** MVP hardcoded IDR (Rupiah), formatting mengikuti locale `id_ID`.

## Evidence Files

```
work/evidence/core/
├── CashEntryActivity.json
├── EditActivity.json
├── AccountsActivity.json
├── TransferActivity.json
├── SummaryActivity.json
├── SettingsActivity.json
├── CashBookRoomDatabase.json
├── db_v.json
├── db_w.json
├── db_x.json
├── db_y.json
├── db_z.json
├── p1_w0.json
├── p1_x0.json
├── q1_*.json
└── i3_a.json
```

## Audit Summary (Final State)

| Item | Status |
|------|--------|
| `target/Cash-Book-daily-expenses_238_apkcube.apks` | Ignored (root `.gitignore`), present locally |
| `work/extracted/base.apk` + splits | Ignored (`work/` in `.gitignore`), present locally |
| `work/evidence/` (JSON hasil REA) | Ignored, present locally |
| `rea-source/` (clone REA) | Ignored, present locally |
| `android-app/app/build/outputs/` (APK hasil build) | Ignored via `build/` in `android-app/.gitignore` |
| `android-app/local.properties` | Ignored, regenerasi via `ANDROID_HOME` |
| `android-app/*.log` | Ignored, tidak masuk commit |
| `android-app/gradle/wrapper/gradle-wrapper.jar` | **Tracked** (dengan negasi di `.gitignore`) |
| `docs/reverse-engineering.md`, `docs/base-app.md` | Dokumentasi sumber; status commit mengikuti repository |

*Dokumen ini membedakan temuan teramati (berdasarkan output REA/JADX) dari asumsi yang **belum terbukti**. Asumsi di atas **tidak** digunakan sebagai dasar implementasi MVP.*