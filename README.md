# Xionco Chatbot - AI Assistant with Spring Boot & Ollama

A production-ready chatbot application featuring Spring Boot 3.5.3 backend with Spring AI 2.0.0 integration to Ollama's Qwen2.5:7b LLM, complete with Thymeleaf frontend and full containerization.

## Features

- **Local LLM Integration**: Qwen2.5:7b running in Podman via Ollama
- **Multi-turn Conversations**: Full conversation history per session
- **Real-time UI**: Thymeleaf + Tailwind CSS with AJAX chat
- **Bahasa Indonesia**: Complete Indonesian language UI and system prompts
- **Fully Containerized**: Both Ollama and Spring Boot app run in Podman
- **Comprehensive Tests**: 12 unit tests covering service and API layers
- **XSS Protection**: Secure input handling throughout

## Quick Start (Podman)

### Prerequisites
- Podman (v6.0.0+)
- podman-compose (v1.6.0+)
- At least 8GB available for model

### Run Everything in Podman

```bash
# 1. Clone/navigate to project directory
cd xionco_chatbot

# 2. Build and start all services
podman-compose up -d

# 3. Wait for Ollama to be ready, then pull the model
sleep 10
podman exec -it ollama ollama pull qwen2.5:7b

# 4. Wait for chatbot to start (healthcheck will verify)
sleep 30

# 5. Open browser
http://localhost:8080/chat
```

### Stop Everything

```bash
podman-compose down
```

### View Logs

```bash
# All services
podman-compose logs -f

# Just chatbot
podman-compose logs -f chatbot

# Just Ollama
podman-compose logs -f ollama
```

## Local Development (Without Podman)

### Prerequisites
- Java 25+
- Maven 3.8+
- Podman (for Ollama only)

### Development Setup

```bash
# 1. Start Ollama in Podman
podman-compose up -d ollama

# 2. Pull model
podman exec -it ollama ollama pull qwen2.5:7b

# 3. Run Spring Boot application
mvn spring-boot:run

# 4. Open browser
http://localhost:8080/chat
```

## Architecture

### Services (in compose.yaml)

| Service | Container | Port | Role |
|---------|-----------|------|------|
| ollama | `docker.io/ollama/ollama:latest` | 11434 | LLM backend |
| chatbot | Built from Dockerfile | 8080 | Spring Boot app |

### Networking
- Both services connected via `xionco-network` bridge network
- Chatbot communicates with Ollama via internal hostname `ollama:11434`
- Exposed to host at `localhost:8080` and `localhost:11434`

### Build & Deploy

**Dockerfile stages:**
1. **Builder**: Maven + JDK 25 → compiles and packages JAR
2. **Runtime**: JRE 25 → runs the lightweight JAR

```dockerfile
# Build stage: Maven compile
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /build
COPY pom.xml .
COPY src src/
RUN mvn clean package -DskipTests

# Runtime stage: JRE only
FROM eclipse-temurin:25-jre
COPY --from=builder /build/app.jar .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## API Endpoints

### Web Routes (Thymeleaf MVC)
- `GET /` → Redirects to `/chat`
- `GET /chat` → Renders chat page with conversation history
- `GET /chat/clear` → Clears history and redirects to chat

### REST API (JSON)
- `POST /api/chat` - Send message
  ```json
  {
    "message": "Halo, siapa nama Anda?",
    "sessionId": "user-123"
  }
  ```
  Response:
  ```json
  {
    "status": "sukses",
    "role": "assistant",
    "content": "Saya Xionco, asisten AI Anda.",
    "timestamp": "14:30:45"
  }
  ```

- `DELETE /api/chat/clear` - Clear conversation history
  ```
  Query params: ?sessionId=user-123
  ```

## Testing

```bash
# Run all tests
mvn test

# Service tests only
mvn test -Dtest=ChatServiceTest

# API controller tests only
mvn test -Dtest=ChatApiControllerTest
```

**Test Coverage:**
- **ChatServiceTest** (7 tests)
  - Assistant message responses
  - System prompt initialization
  - Default session handling
  - Multi-turn conversations
  - Error handling
  - History clearing
  - System message filtering

- **ChatApiControllerTest** (5 tests)
  - POST /api/chat success (200)
  - POST /api/chat error (500)
  - Empty message validation (4xx)
  - Session forwarding
  - DELETE /api/chat/clear

## Configuration

### application.yaml
```yaml
server:
  port: 8080

spring:
  ai:
    ollama:
      base-url: ${SPRING_AI_OLLAMA_BASE_URL:http://localhost:11434}
      chat:
        model: qwen2.5:7b
        options:
          temperature: 0.7
          num-ctx: 4096
          top-k: 40
          top-p: 0.9
```

### Environment Variables
- `SPRING_AI_OLLAMA_BASE_URL` - Ollama API endpoint (default: `http://localhost:11434`)

## Project Structure

```
xionco_chatbot/
├── pom.xml                           # Maven configuration
├── Dockerfile                        # Multi-stage build
├── compose.yaml                      # Podman Compose (Ollama + Chatbot)
├── README.md                         # This file
├── src/
│   ├── main/
│   │   ├── java/com/xionco/chatbot/
│   │   │   ├── XioncoChatbotApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── ChatController.java       (MVC)
│   │   │   │   └── ChatApiController.java    (REST)
│   │   │   ├── service/
│   │   │   │   └── ChatService.java
│   │   │   └── dto/
│   │   │       ├── ChatMessage.java
│   │   │       └── ChatRequest.java
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── templates/
│   │           └── chat.html
│   └── test/java/com/xionco/chatbot/
│       ├── service/ChatServiceTest.java
│       └── controller/ChatApiControllerTest.java
└── target/
    └── xionco-chatbot-1.0.0.jar
```

## Technology Stack

- **Backend**: Spring Boot 3.5.3
- **LLM Framework**: Spring AI 2.0.0
- **LLM Model**: Ollama (Qwen2.5:7b)
- **Frontend**: Thymeleaf + Tailwind CSS (CDN)
- **Container**: Podman + Podman Compose
- **Java**: Version 25
- **Build**: Maven 3.8+
- **Testing**: JUnit 5 + Mockito
- **Validation**: Jakarta Bean Validation

## Performance Notes

- First startup pulls ~4GB Qwen model into Podman
- Ollama container has persistent volume for model caching
- Spring Boot startup ~5-10 seconds (excluding Ollama)
- Chatbot healthcheck verifies readiness after 40 seconds
- Conversation history stored in-memory (ConcurrentHashMap)

## Troubleshooting

### Ollama not connecting
```bash
# Check if Ollama is running
podman ps | grep ollama

# View Ollama logs
podman-compose logs ollama

# Verify model is pulled
podman exec ollama ollama list
```

### Chatbot not starting
```bash
# Check logs
podman-compose logs chatbot

# Verify Ollama is accessible from chatbot
podman exec xionco-chatbot curl http://ollama:11434/api/tags
```

### Port conflicts
```bash
# Check which service is using port
lsof -i :8080
lsof -i :11434

# Modify ports in compose.yaml if needed
```

### Container build failures
```bash
# Force rebuild without cache
podman-compose build --no-cache

# View build logs in detail
podman-compose build --verbose
```

## Development Notes

- **Session Management**: Uses default session ID "default" if not specified
- **Conversation History**: Stored per-session in `ConcurrentHashMap`
- **System Prompt**: Injected as first message for each session in Bahasa Indonesia
- **Error Handling**: RuntimeException wraps Ollama errors with context
- **XSS Protection**: Input escaped in HTML, validated in API layer

## License & Attribution

Built with Spring Boot, Spring AI, and powered by Ollama's local LLM capabilities.

Co-Authored-By: Claude Haiku 4.5 <noreply@anthropic.com>
