# Perbaikan Build APK RankMate di GitHub Actions

Penyebab gagal: `.github/workflows/main.yml` masih menjalankan `npm run android:init`, sedangkan `package.json` tidak memiliki script tersebut. Repository yang sekarang adalah proyek **Android native Java + Gradle**, bukan proyek Capacitor yang harus diinisialisasi setiap kali build.

## Cara termudah dari HP (hanya 1 file)
1. Buka repo `alfianwahyu035-boop/Rank-mate` di GitHub.
2. Buka `.github/workflows/main.yml` -> ikon pensil (Edit).
3. Hapus seluruh isi file lalu salin isi `RankMate_GitHub_Actions_FIX.yml`.
4. Commit changes ke branch `main`.
5. Buka **Actions -> Build RankMate Android APK -> Run workflow** (atau tunggu run otomatis dari commit).
6. Jika build hijau, buka run -> **Artifacts** -> `RankMate-V5-4-debug-APK` dan unduh ZIP. Di dalam ZIP ada `app-debug.apk`.

Workflow mengganti langkah Capacitor dengan `gradle :app:assembleDebug`, menggunakan Gradle 8.9, JDK 17, SDK 35. Karena repo GitHub sebelumnya kehilangan `AndroidManifest.xml` dan ikon foreground, workflow juga membuat fallback resource tersebut *hanya jika belum ada*.

## Alternatif: Source lengkap
File `RankMate_Android_GitHub_FIXED_Full.zip` berisi source Android V5.4 lengkap, file manifest, PNG ikon asli, dan workflow pengganti. Ekstrak dulu sebelum mengunggah ke GitHub.

## Catatan
Belum berhasil menjalankan build Android sungguhan di lingkungan pembuatan file. Keberhasilan APK perlu dikonfirmasi dari GitHub Actions. Source / data aplikasi yang sudah ada tidak diubah kecuali workflow.
