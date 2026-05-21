# Checklist Bukti Performa Milestone 4

Gunakan direktori ini untuk menyimpan bukti publik yang dibutuhkan pada rubrik nilai 4. File bukti dapat ikut di-commit jika berasal dari proses yang dapat direproduksi, atau dilampirkan sebagai artifact GitHub Actions.

Workflow GitHub Actions `Performance Evidence` dapat dijalankan secara manual dari tab Actions untuk menghasilkan artifact test, load test, Lighthouse, dan metrik actuator dalam satu proses yang dapat direproduksi.

## Bukti yang Dibutuhkan

| Poin Rubrik | File yang Dibutuhkan |
| --- | --- |
| Perbandingan commit sebelum/sesudah | `commit-comparison.md` |
| Profiling kode | `profiling/before-after-profile.txt` atau log CI dari `PerformanceProfilingTest` |
| APDEX | `apdex/apdex-before.png`, `apdex/apdex-after.png`, dan/atau export query Prometheus |
| Lighthouse | `lighthouse/lighthouse-before.html`, `lighthouse/lighthouse-after.html` |
| Usability Clarity | `clarity/clarity-before.png`, `clarity/clarity-after.png` |
| Observability aplikasi | `monitoring/grafana-app-dashboard.png` |
| Observability database | `monitoring/grafana-db-dashboard.png` |

## Perintah

Membuat bukti test dan profiling:

```bash
./gradlew clean test jacocoTestReport
```

Menjalankan stack monitoring:

```bash
cd monitoring
docker compose up -d
```

Menjalankan load test:

```bash
locust -f load-testing/locustfile.py --host=http://localhost:8085 --headless -u 100 -r 10 -t 2m --html performance-evidence/apdex/locust-after.html
```

Menjalankan Lighthouse setelah aplikasi aktif:

```bash
npx lighthouse http://localhost:8085/orders --output=html --output-path=performance-evidence/lighthouse/lighthouse-after.html
```

Untuk Microsoft Clarity, set `CLARITY_PROJECT_ID` pada environment hasil deploy, jalankan alur `/orders` dan `/notifications`, lalu export atau screenshot dashboard ke `performance-evidence/clarity/`.
