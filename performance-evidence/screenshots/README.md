# Screenshot Evidence

Folder ini digunakan untuk menyimpan bukti screenshot profiling, observability, dan monitoring modul Order Notification.

## Daftar Screenshot yang Disarankan

| File | Isi Screenshot |
| --- | --- |
| `performance-profiling-test.png` | Halaman `PerformanceProfilingTest` dengan status `100% successful / passed` |
| `prometheus-target-up.png` | Halaman `http://localhost:9090/targets` dengan target `bidmart-order-notification-service` berstatus `UP` |
| `grafana-dashboard.png` | Dashboard Grafana `BidMart Order Notification Observability` |
| `jacoco-coverage.png` | Halaman JaCoCo coverage report |

## Cara Referensi di README Utama

Setelah screenshot disimpan, gambar bisa direferensikan dari README utama seperti berikut:

```md
![Performance Profiling Test](performance-evidence/screenshots/performance-profiling-test.png)
![Prometheus Target Up](performance-evidence/screenshots/prometheus-target-up.png)
![Grafana Dashboard](performance-evidence/screenshots/grafana-dashboard.png)
```
