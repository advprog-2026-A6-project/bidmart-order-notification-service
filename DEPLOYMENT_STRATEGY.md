# Strategi Deployment: Feature Flags

## Justifikasi Pemilihan Feature Flags

Dari beberapa strategi deployment yang tersedia (Blue/Green, Canary, Feature Flags), kami memilih **Feature Flags** berdasarkan pertimbangan berikut:

### 1. Keterbatasan Infrastruktur

Proyek ini berjalan di **single EC2 instance**. Strategi Blue/Green membutuhkan dua lingkungan identik (dua instance), sedangkan Canary membutuhkan load balancer untuk membagi traffic. Feature Flags tidak membutuhkan infrastruktur tambahan karena bekerja di level aplikasi.

### 2. Kontrol Granular Tanpa Redeployment

Feature Flags memungkinkan **mengaktifkan atau menonaktifkan fitur secara independen** tanpa harus melakukan deployment ulang. Cukup mengubah environment variable di `/etc/bidmart/order-notification.env`, lalu restart service:

```bash
sudo systemctl restart bidmart-order-notification-service
```

### 3. Kesesuaian dengan Domain Notification Service

Notification service memiliki beberapa channel pengiriman (email, push notification, WebSocket) yang sering perlu di-toggle secara independen. Misalnya:

- **Disable email** saat SMTP provider sedang maintenance, tanpa mengganggu push notification.
- **Disable push notification** saat melakukan migrasi WebSocket, tanpa mengganggu email.
- **Disable WebSocket live updates** saat debugging koneksi real-time.

---

## Implementasi Feature Flags

### Konfigurasi

Feature flags didefinisikan di `application.properties` dan dapat di-override melalui environment variables:

```properties
# Feature Flags
feature.email-notification-enabled=${FEATURE_EMAIL_NOTIFICATION_ENABLED:true}
feature.push-notification-enabled=${FEATURE_PUSH_NOTIFICATION_ENABLED:true}
feature.websocket-live-updates=${FEATURE_WEBSOCKET_LIVE_UPDATES:true}
```

### Komponen

| Komponen | File | Fungsi |
|---|---|---|
| Config | `FeatureFlagProperties.java` | `@ConfigurationProperties` yang membaca flag dari config |
| Service | `NotificationServiceImpl.java` | Menggunakan flag untuk gate delivery email dan push |
| API | `FeatureFlagController.java` | `GET /api/feature-flags` untuk monitoring status flag |

### Alur Kerja Feature Flag

```text
1. Request masuk ke NotificationServiceImpl
2. Service memeriksa FeatureFlagProperties:
   - featureFlags.isPushNotificationEnabled() → jika false, push notification dilewati
   - featureFlags.isEmailNotificationEnabled() → jika false, email dilewati
3. Jika flag aktif DAN user preference aktif → notifikasi dikirim
4. Jika flag nonaktif → notifikasi dilewati (tanpa error)
```

### Cara Mengubah Feature Flag di Production

```bash
# 1. Edit environment file di EC2
sudo nano /etc/bidmart/order-notification.env

# 2. Tambahkan/ubah flag (contoh: disable email)
FEATURE_EMAIL_NOTIFICATION_ENABLED=false

# 3. Restart service (tanpa redeployment)
sudo systemctl restart bidmart-order-notification-service

# 4. Verifikasi status flag via API
curl http://localhost:8085/api/feature-flags
```

### Contoh Response API Feature Flags

```json
{
  "emailNotificationEnabled": true,
  "pushNotificationEnabled": true,
  "websocketLiveUpdates": true
}
```

---

## Prosedur Deployment Lanjutan: Rollback Otomatis

Selain Feature Flags, proyek ini juga menerapkan **rollback deployment otomatis** di `deployment/deploy.sh`:

### Alur Rollback

```text
1. Backup JAR yang sedang berjalan → app.jar.bak
2. Deploy JAR baru → app.jar
3. Restart service via systemd
4. Health check via /actuator/health (12 attempts × 5 detik)
5. Jika health check GAGAL:
   a. Restore app.jar.bak → app.jar
   b. Restart service dengan versi lama
   c. Verifikasi health check versi lama
   d. Exit dengan error code 1
6. Jika health check BERHASIL → deployment selesai
```

### Komponen Pendukung

| Komponen | Detail |
|---|---|
| Backup | `cp app.jar app.jar.bak` sebelum deploy |
| Health check | `curl /actuator/health` dengan 12 attempts × 5s interval |
| Auto-rollback | Restore backup JAR jika health check gagal |
| Systemd restart policy | `Restart=on-failure`, `RestartSec=10` |
