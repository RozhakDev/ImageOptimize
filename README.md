# ImageOptimize

> Asynchronous Image Optimization API & Native Android Client

ImageOptimize adalah proyek monorepo yang mencakup layanan backend optimasi gambar berkinerja tinggi beserta aplikasi klien Android.

## Features

**Backend API:**

- **High-Performance**: Pemrosesan gambar dengan *low memory footprint* menggunakan `libvips` (via `pyvips`).
- **Automated Storage Management**: *Garbage collection* asinkron untuk hasil optimasi menggunakan `APScheduler`.
- **Clean Architecture**: Memisahkan *routing* dan logika bisnis menggunakan pola *Thin Controller* dan *Orchestrator*.

**Mobile Client:**

- **Multi-Module Architecture**: Pemisahan struktur ke dalam `core:network`, `core:data`, `core:database`, dan `core:designsystem` untuk efisiensi *build time*.
- **Modern UI & Async**: Menggunakan Jetpack Compose (UI deklaratif) dan Kotlin Coroutines.
- **Dependency Injection**: Menggunakan Hilt.

## Quick Start

### 1. Backend API Setup

1. **Installation**: 
   
   ```bash
   cd API
   python -m venv venv
   
   # Windows: venv\Scripts\activate | Mac/Linux: source venv/bin/activate
   
   pip install -r requirements.txt
   ```

2. **Configuration**: Buat `.env` di dalam direktori `API` (contoh: pastikan `VIPS_DLL_PATH` disetel khusus untuk Windows).

3. **Run**: 
   
   ```bash
   uvicorn app.main:app --reload
   ```

### 2. Mobile Client Setup

1. **Installation**: Buka direktori `Mobile` menggunakan Android Studio (syarat minimum JDK 17 & SDK 34).
2. **Configuration**: Tambahkan *base URL* API backend ke dalam `Mobile/local.properties`: 
   
   ```properties
   API_BASE_URL="http://<ip-lokal-jaringan>:8000/api/v1/"
   ```
3. **Run**: Tunggu *Gradle sync* selesai, lalu jalankan target `app` di emulator atau *physical device*.

## API

| Endpoint           | Method | Deskripsi                                                              |
|:------------------ |:------:|:---------------------------------------------------------------------- |
| `/api/v1/images/`  | `POST` | Eksekusi *resize* dan kompresi gambar. Mengembalikan URL berkas hasil. |
| `/api/v1/analyze/` | `POST` | Menganalisis metadata gambar tanpa melakukan *decoding* piksel.        |
| `/health`          | `GET`  | Endpoint *health check* dan *liveness probe*.                          |

## Documentation

- **Software Requirements Specification (SRS)**: Lihat [Dokumentasi Wiki Resmi](https://github.com/RozhakDev/ImageOptimize/wiki) untuk spesifikasi teknis dan desain sistem.

## License

Didistribusikan di bawah lisensi **MIT License**. Lihat berkas `LICENSE` untuk detail penuh.
