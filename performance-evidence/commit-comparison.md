# Perbandingan Commit Sebelum dan Sesudah

Isi file ini setelah commit optimasi final sudah di-push.

| Bukti | Commit |
| --- | --- |
| Sebelum optimasi | `<before_commit_hash>` |
| Sesudah optimasi | `<after_commit_hash>` |

## Ringkasan Optimasi

| Area | Sebelum | Sesudah |
| --- | --- | --- |
| Pencarian notifikasi | Memuat semua notifikasi lalu filter di memori | Query berdasarkan `userId` yang sudah diindeks melalui repository |
| Komunikasi auction/order | Jalur HTTP langsung saja | Listener RabbitMQ ditambah fallback HTTP |
| Feedback pengguna | Pengguna perlu refresh manual untuk melihat update | Alur notifikasi push melalui WebSocket |
| Deployment | Bergantung pada asumsi service manual | Artifact diuji CI, provisioning systemd, health check, dan rollback |
| Observability | Pengecekan lokal ad-hoc | Dashboard Prometheus, Grafana, APDEX, dan metrik database HikariCP |

## Tautan

- Run CI:
- Run deployment:
- Bukti dashboard Grafana:
- Lighthouse sebelum:
- Lighthouse sesudah:
- Clarity sebelum:
- Clarity sesudah:
