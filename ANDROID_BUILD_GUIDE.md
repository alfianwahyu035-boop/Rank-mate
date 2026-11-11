# Build APK Android — RankMate V3.8

Ini **source aplikasi**, bukan APK siap instal. Perlu Node.js dan Android Studio dengan Android SDK.

1. Ekstrak ZIP ke folder kerja.
2. Buka terminal di folder tersebut.
3. Jalankan `npm install`.
4. Jalankan `npx cap add android` (cukup saat pertama kali).
5. Jalankan `npx cap sync android`.
6. Jalankan `npx cap open android` dan gunakan Build > Build Bundle(s) / APK(s) > Build APK(s).

`www/index.html` adalah aplikasi terbaru yang mendukung impor langsung 3 file monitoring.
Pastikan izin akses file Android dan ekspor Excel/PNG diuji pada perangkat sebelum digunakan secara operasional.
