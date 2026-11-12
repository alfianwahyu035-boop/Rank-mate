# Cara Build APK RankMate V5.4 (Android Studio)

Paket ini sudah berisi **proyek Android Studio native lengkap**, bukan hanya `www/index.html`.

1. Ekstrak ZIP **RankMate_V5_4_Android_FULL_Source.zip**.
2. Buka **Android Studio → Open** dan pilih folder `RankMate_V5_4_Android_Full_Source` yang memiliki file `settings.gradle`.
3. Pastikan Android Studio telah memasang **Android SDK Platform 35** dan menggunakan **JDK 17 atau 21**.
4. Biarkan **Gradle Sync** berjalan. Koneksi internet diperlukan untuk mengunduh Gradle dan Android Gradle Plugin saat pertama kali build.
5. Pilih **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
6. File APK debug biasanya berada di `app/build/outputs/apk/debug/app-debug.apk`.
7. Pindahkan APK ke HP dan lakukan instalasi. Jika diminta, izinkan pemasangan dari sumber yang dipilih.

Terminal (opsional): `./gradlew assembleDebug` pada Linux/macOS atau `gradlew.bat assembleDebug` pada Windows. Jika `gradlew` tidak memiliki izin eksekusi: `chmod +x gradlew`.

## Apa yang ada di paket

- `app/src/main/assets/index.html`: dashboard RankMate V5.4 lengkap, termasuk fitur pilih AM.
- `app/src/main/assets/native-bridge.js`: penghubung ekspor gambar/Excel/CSV/JSON dari web ke Android.
- `app/src/main/java/com/rankmate/dashboard/MainActivity.java`: WebView, pemilih Excel, penyimpanan Downloads, dan Android Share.
- `app/src/main/AndroidManifest.xml`: deklarasi aplikasi.
- `app/src/main/res/`: nama, warna, ikon aplikasi.
- `build.gradle`, `settings.gradle`, `app/build.gradle`, `gradle/wrapper/`, `gradlew`, `gradlew.bat`: kebutuhan build.

## Catatan penting

- Minimal **Android 10**. Data disimpan di HP melalui localStorage aplikasi.
- File ekspor disimpan ke **Downloads/RankMate**; jika memilih *Bagikan gambar*, Android akan menampilkan pilihan aplikasi berbagi.
- Backup JSON sebelum mengganti aplikasi atau reset HP.
- Build APK **belum dapat diuji di lingkungan ini**, karena Android SDK/Gradle belum terpasang. Source Java, XML, dan skrip JS sudah divalidasi secara statis.
- Gradle wrapper pada paket berupa bootstrap yang menyertakan kode sumber dan mengunduh Gradle 8.9 saat pertama kali build. Setelah Gradle tersedia, jalankan `./gradlew wrapper --gradle-version 8.9` untuk menggantinya dengan wrapper Gradle resmi.
