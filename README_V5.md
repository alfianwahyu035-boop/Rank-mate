# RankMate V5 · Source Android (Capacitor)

Paket ini adalah **SOURCE UNTUK BUILD APK**, bukan file `.apk` siap instalasi.

## Prasyarat
- Node.js dan npm
- Android Studio + Android SDK + Java sesuai kebutuhan Android Gradle Plugin

## Build
```sh
npm install
npx cap add android        # jalankan pertama kali jika folder android/ belum tersedia
npx cap sync android
npx cap open android
```
Gunakan Android Studio > Build > Build Bundle(s) / APK(s) > Build APK(s).

Aplikasi memuat web statis dari `www/index.html`. Uji langsung di perangkat Android terutama **download/share file PNG/XLSX** dari dalam WebView; perilaku unduhan dapat memerlukan konfigurasi native tambahan. Jangan menganggap paket ini sudah menghasilkan APK final.

Tema UI 3D Folder, tetap memiliki impor 3 Excel monitoring, dashboard mingguan, overall bulanan, ekspor PNG dan Excel.
