# Xionco Chatbot

Aplikasi chatbot AI cerdas berbasis Spring Boot dengan LLM lokal menggunakan Ollama dan Podman. Antarmuka web responsif dengan Thymeleaf dan Tailwind CSS.

**Status**: ✅ Fully Operational - Running with TinyLlama 1.1B Model

## 📋 Prasyarat

Sebelum menjalankan aplikasi, pastikan Anda sudah menginstal:

- **Podman** (v5.0+) - Container runtime alternatif untuk Docker
  - macOS: `brew install podman`
  - Linux: Ikuti https://podman.io/docs/installation
  - Windows: Download dari https://podman.io/docs/installation

- **Podman Compose** (v1.0+)
  ```bash
  pip install podman-compose
  ```

- **Java 25** (JDK)
  - macOS: `brew install openjdk@25`
  - Linux/Windows: Download dari https://www.oracle.com/java/technologies/downloads/

- **Maven 3.8+** (untuk build - opsional jika hanya menjalankan)
  - macOS: `brew install maven`

## 🚀 Cara Menjalankan Aplikasi

### Metode 1: Menggunakan Script (Rekomendasi)

Script otomatis akan mengelola semua proses:

```bash
# Clone repository
git clone https://github.com/sinagajunior/xionco_chatbot.git
cd xionco_chatbot

# Jalankan script
chmod +x start.sh
./start.sh
```

Script akan otomatis:
- ✅ Memverifikasi instalasi Podman
- ✅ Membangun dan menjalankan container
- ✅ Menunggu Ollama siap
- ✅ Memuat model TinyLlama (~600MB)
- ✅ Menunggu Spring Boot startup
- ✅ Menampilkan status container

### Metode 2: Manual (Langkah demi Langkah)

```bash
# 1. Clone repository
git clone https://github.com/sinagajunior/xionco_chatbot.git
cd xionco_chatbot

# 2. Bangun dan jalankan container
podman-compose up -d

# 3. Tunggu Ollama siap (tunggu ~15 detik)
sleep 15

# 4. Load model TinyLlama (pertama kali akan download ~600MB)
podman exec ollama ollama pull tinyllama:latest

# 5. Tunggu Spring Boot startup (tunggu ~20 detik)
sleep 20

# 6. Verifikasi semua container berjalan
podman-compose ps
```

### Metode 3: Development (Tanpa Full Containerization)

Jika ingin develop di local machine:

```bash
# 1. Jalankan Ollama container saja
podman-compose up -d ollama

# 2. Tunggu dan pull model
sleep 10
podman exec ollama ollama pull tinyllama:latest

# 3. Jalankan Spring Boot di local
mvn spring-boot:run
```

## 🌐 Akses Aplikasi

Setelah semua container berjalan, buka browser:

```
http://localhost:8080/chat
```

Anda akan melihat:
- 💬 Interface chat yang elegan dengan Tailwind CSS
- 📝 Kolom input untuk mengetik pesan
- ⚡ Real-time responses dari AI
- 🧹 Tombol untuk menghapus riwayat percakapan
- ⏳ Loading indicator saat model memproses

## ✅ Verifikasi Instalasi

### Cek Container Berjalan

```bash
podman-compose ps
```

Output yang benar:
```
CONTAINER ID  IMAGE                                  STATUS            PORTS
xxxxx         docker.io/ollama/ollama                Up (healthy)      0.0.0.0:11434->11434/tcp
xxxxx         localhost/xionco_chatbot_chatbot       Up (healthy)      0.0.0.0:8080->8080/tcp
```

### Cek Model Tersedia

```bash
curl -s http://localhost:11434/api/tags | jq '.models[].name'
```

Output yang benar:
```
"tinyllama:latest"
```

### Test Chat API

```bash
curl -s -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Halo!","sessionId":"test"}' | jq '.'
```

Output yang benar:
```json
{
  "status": "sukses",
  "role": "assistant",
  "content": "...",
  "timestamp": "14:30:45"
}
```

## 📦 Arsitektur Aplikasi

```
xionco_chatbot/
├── compose.yaml                      # Konfigurasi Podman Compose
├── Dockerfile                        # Multi-stage build
├── pom.xml                          # Dependensi Maven
├── start.sh                         # Script otomatis
├── README.md                        # Dokumentasi
├── src/
│   ├── main/
│   │   ├── java/com/xionco/chatbot/
│   │   │   ├── XioncoChatbotApplication.java      # Main class
│   │   │   ├── controller/
│   │   │   │   ├── ChatController.java            # MVC Thymeleaf
│   │   │   │   └── ChatApiController.java         # REST API
│   │   │   ├── service/
│   │   │   │   └── ChatService.java               # Logika chat
│   │   │   ├── config/
│   │   │   │   └── RestConfig.java                # Spring config
│   │   │   └── dto/
│   │   │       ├── ChatMessage.java
│   │   │       └── ChatRequest.java
│   │   └── resources/
│   │       ├── application.yaml                   # Config
│   │       └── templates/
│   │           └── chat.html                      # UI Thymeleaf
│   └── test/java/com/xionco/chatbot/
│       ├── service/ChatServiceTest.java
│       └── controller/ChatApiControllerTest.java
└── target/
    └── xionco-chatbot-1.0.0.jar
```

## 🛠️ Mengelola Container

### Lihat Log Real-time

```bash
# Semua service
podman-compose logs -f

# Hanya Ollama
podman-compose logs -f ollama

# Hanya Spring Boot
podman-compose logs -f chatbot
```

### Stop Container

```bash
podman-compose down
```

### Restart Container

```bash
podman-compose restart
```

### Rebuild Image

```bash
# Rebuild tanpa cache
podman-compose build --no-cache

# Jalankan ulang
podman-compose up -d
```

### Hapus Semua Data

```bash
# Hapus container, network, dan volume
podman-compose down -v
```

## 📝 Testing

### Jalankan Unit Tests

```bash
mvn test
```

### Jalankan Test Tertentu

```bash
# Test ChatService saja
mvn test -Dtest=ChatServiceTest

# Test ChatApiController saja
mvn test -Dtest=ChatApiControllerTest
```

## 🔧 Troubleshooting

### ❌ "site can't be reached" - http://localhost:8080

**Penyebab**: Spring Boot atau container tidak jalan

**Solusi**:
```bash
# Cek status container
podman-compose ps

# Lihat log aplikasi
podman logs xionco-chatbot

# Tunggu lebih lama (~30 detik) dan coba lagi
sleep 30

# Jika masih error, restart
podman-compose restart
```

### ❌ "insufficient memory" error

**Penyebab**: Model membutuhkan lebih banyak RAM

**Solusi**:
- Aplikasi sudah menggunakan TinyLlama (608MB) - sangat hemat
- Jika masih error, tingkatkan alokasi di `compose.yaml`:
  ```yaml
  deploy:
    resources:
      limits:
        memory: 12G    # Naikkan dari 8G
      reservations:
        memory: 10G    # Naikkan dari 6G
  ```

### ❌ "unable to start container"

**Solusi**:
```bash
# Stop semua
podman-compose down

# Rebuild tanpa cache
podman-compose build --no-cache

# Jalankan lagi
podman-compose up -d
```

### ❌ Port 8080 atau 11434 sudah terpakai

**Solusi**: Edit `compose.yaml` dan ubah port:
```yaml
services:
  ollama:
    ports:
      - "11435:11434"  # Ganti port

  chatbot:
    ports:
      - "8081:8080"    # Ganti port
```

Kemudian akses: `http://localhost:8081/chat`

### ❌ Model tidak terload

**Solusi**:
```bash
# Cek model yang tersedia
curl http://localhost:11434/api/tags

# Pull model manual
podman exec ollama ollama pull tinyllama:latest

# Verifikasi
curl http://localhost:11434/api/tags
```

## 📊 Spesifikasi Sistem

| Komponen | Minimum | Rekomendasi |
|----------|---------|------------|
| **RAM** | 2GB | 4GB+ |
| **CPU** | 2 core | 4 core+ |
| **Storage** | 1.5GB | 5GB+ |
| **OS** | Linux/macOS | macOS/Linux |

## 🎯 Fitur Aplikasi

- ✅ Chat real-time dengan AI
- ✅ Riwayat percakapan per session
- ✅ Hapus riwayat chat dengan 1 klik
- ✅ Responsif di desktop dan mobile
- ✅ UI lengkap Bahasa Indonesia
- ✅ Model LLM lokal (tidak butuh internet)
- ✅ Fully containerized dengan Podman
- ✅ Health checks otomatis
- ✅ Multi-session support
- ✅ XSS protection built-in

## 🧠 Model AI: TinyLlama

**TinyLlama** - Model bahasa 1.1B parameter
- 📦 Ukuran: 608 MB
- ⚡ Kecepatan: Sangat cepat
- 💾 Memory: Minimal (~500MB saat running)
- 📝 Context: 2048 tokens
- 🎯 Performa: Optimal untuk chatbot umum

Cocok untuk:
- General purpose chatbot
- Development & testing
- Embedded systems
- Learning purposes

## 📡 API Endpoints

### GET /chat
Render halaman chat dengan Thymeleaf
- Response: HTML page

### POST /api/chat
Kirim pesan ke chatbot

**Request**:
```json
{
  "message": "Halo, apa kabar?",
  "sessionId": "user-123"
}
```

**Response**:
```json
{
  "status": "sukses",
  "role": "assistant",
  "content": "Halo! Saya baik-baik saja. Ada yang bisa saya bantu?",
  "timestamp": "14:30:45"
}
```

### DELETE /api/chat/clear
Hapus riwayat percakapan

**Request**:
```json
{
  "sessionId": "user-123"
}
```

**Response**: 200 OK

## ⚙️ Konfigurasi

### application.yaml

```yaml
server:
  port: 8080

spring:
  application:
    name: xionco-chatbot
  thymeleaf:
    cache: false
    encoding: UTF-8

app:
  name: "Xionco Chatbot"
  description: "Asisten AI Cerdas Berbasis TinyLlama"
  greeting: "Halo! Saya Xionco, asisten AI Anda. Ada yang bisa saya bantu?"
  placeholder: "Ketik pesan Anda di sini..."
  send-button-text: "Kirim"
```

### compose.yaml

```yaml
services:
  ollama:
    image: docker.io/ollama/ollama:latest
    container_name: ollama
    ports:
      - "11434:11434"
    volumes:
      - ollama_data:/root/.ollama
    deploy:
      resources:
        limits:
          memory: 8G
        reservations:
          memory: 6G

  chatbot:
    build: .
    container_name: xionco-chatbot
    ports:
      - "8080:8080"
    environment:
      - SPRING_AI_OLLAMA_BASE_URL=http://ollama:11434
    depends_on:
      - ollama
```

## 🔐 Keamanan

- ✅ Input di-escape untuk mencegah XSS
- ✅ CSRF protection via Spring Security
- ✅ Tidak ada data yang dikirim ke internet
- ✅ Model berjalan 100% lokal
- ✅ No external API dependencies

## 📚 Stack Teknologi

| Layer | Teknologi |
|-------|-----------|
| **Backend** | Spring Boot 3.5.3, Spring Web |
| **Frontend** | Thymeleaf, Tailwind CSS |
| **Database** | In-memory (ConcurrentHashMap) |
| **LLM** | Ollama + TinyLlama 1.1B |
| **Container** | Podman + Podman Compose |
| **Build** | Maven, Docker multi-stage |
| **Testing** | JUnit 5, Mockito |
| **Java** | Version 25 |

## 📖 Dokumentasi Lengkap

- Spring Boot: https://spring.io/projects/spring-boot
- Thymeleaf: https://www.thymeleaf.org/
- Ollama: https://ollama.ai/
- Podman: https://podman.io/
- TinyLlama: https://github.com/jzhang38/TinyLlama

## 🚀 Deploy ke Production

Untuk production deployment:

1. Update `application.yaml` dengan production config
2. Setup reverse proxy (Nginx/Apache)
3. Configure SSL/TLS certificates
4. Increase resource limits sesuai kebutuhan
5. Setup monitoring & logging
6. Consider Kubernetes untuk scaling

## 🤝 Contributing

Fork repository dan submit pull requests untuk improvements!

## 📄 Lisensi

MIT License - Bebas digunakan untuk komersial maupun personal

## 👥 Tim

- **Developer**: Roy


## 📞 Support & Issues

Jika menemukan bug atau ada pertanyaan:
1. Cek bagian Troubleshooting di atas
2. Lihat logs container: `podman-compose logs`
3. Buat issue di GitHub

---

**Terima kasih sudah menggunakan Xionco Chatbot! 🎉**

Jika dokumentasi ini membantu, berikan ⭐ di GitHub!

**Last Updated**: August 2026
**Status**: ✅ Fully Operational
**Model**: TinyLlama 1.1B
**Version**: 1.0.0

screen shoot

<img width="1910" height="1070" alt="xionco_bot_chat" src="https://github.com/user-attachments/assets/309c6d5e-f5b5-4aa4-ba9a-050fa5278c40" />

