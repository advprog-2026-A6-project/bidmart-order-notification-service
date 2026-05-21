# Setup Observability dan Monitoring Performa

Direktori ini berisi konfigurasi observability lokal untuk memantau **performa aplikasi** (latensi API, error, JVM, APDEX) dan **performa database** (connection pool Hikari, latensi SQL).

---

## 1. Metrik Observability yang Dipantau

Spring Boot Actuator mengekspos metrik utama yang diambil oleh Prometheus dan ditampilkan di Grafana.

### A. Performa Aplikasi dan APDEX

* **Kesehatan JVM**: penggunaan CPU (`system_cpu_usage`, `process_cpu_usage`), penggunaan heap memory (`jvm_memory_used_bytes`), dan siklus garbage collection (`jvm_gc_pause_seconds`).
* **Latensi HTTP**: durasi request (`http_server_requests_seconds_bucket`) yang dikelompokkan berdasarkan URI dan status code.
* **Skor APDEX**: dihitung menggunakan query Prometheus dengan threshold latensi $T = 500\text{ms}$:

  ```promql
  (sum(rate(http_server_requests_seconds_bucket{le="0.5"}[5m])) + sum(rate(http_server_requests_seconds_bucket{le="2.0"}[5m])) / 2) / sum(rate(http_server_requests_seconds_count[5m]))
  ```

### B. Performa Database (HikariCP)

Karena PostgreSQL/H2 diakses melalui Hikari Connection Pool, metrik kesehatan database berikut ikut dipantau:

* **Active Connections**: jumlah transaksi SQL aktif yang sedang berjalan (`hikaricp_connections_active`).
* **Idle Connections**: koneksi yang tersedia di pool (`hikaricp_connections_idle`).
* **Connection Acquisition Latency**: waktu yang dibutuhkan untuk memperoleh koneksi database (`hikaricp_connections_acquire_seconds`).
* **Pending Threads**: jumlah thread yang menunggu koneksi (`hikaricp_connections_pending`), berguna untuk mendeteksi connection pool exhaustion.

---

## 2. Cara Menjalankan Stack Observability

1. **Jalankan container Prometheus dan Grafana**:

   ```bash
   cd monitoring
   docker compose up -d
   ```

2. **Buka Prometheus**:

   Buka `http://localhost:9090` untuk memeriksa status scraping target. Pada menu **Status** -> **Targets**, endpoint `bidmart-order-notification-service` seharusnya tampil `UP`.

3. **Buka Grafana**:

   Buka `http://localhost:3001` di browser.

   - **Username**: `admin`
   - **Password**: `admin` (biasanya diminta mengganti password saat login pertama).

4. **Buka dashboard yang sudah diprovisioning**:

   - Grafana otomatis menyiapkan datasource Prometheus dan dashboard `BidMart Order Notification Observability` dari repository ini.
   - Ambil screenshot panel APDEX, HTTP latency, JVM, dan HikariCP ke `performance-evidence/monitoring/`.
