#!/bin/bash

set -e

echo "════════════════════════════════════════════════════════"
echo "  XIONCO CHATBOT - FULL PODMAN CONTAINERIZATION"
echo "════════════════════════════════════════════════════════"
echo ""
echo "Starting:"
echo "  • Ollama Service (LLM in container on port 11434)"
echo "  • Chatbot Service (Spring Boot in container on port 8080)"
echo ""

# Check prerequisites
echo "✓ Checking prerequisites..."
command -v podman >/dev/null 2>&1 || { echo "✗ Podman not found. Please install Podman."; exit 1; }
command -v podman-compose >/dev/null 2>&1 || { echo "✗ podman-compose not found. Please install podman-compose."; exit 1; }

echo "  • Podman: $(podman --version)"
echo "  • Podman Compose: $(podman-compose --version)"
echo ""

# Build and start services
echo "✓ Building and starting containers..."
podman-compose down 2>/dev/null || true
podman-compose up -d --build

echo "  • Waiting for container startup (15 seconds)..."
sleep 15

# Verify Ollama container is running
echo ""
echo "✓ Verifying Ollama container..."
if podman ps | grep -q ollama; then
    echo "  ✓ Ollama container is running (port 11434)"
else
    echo "  ✗ Ollama container failed to start"
    podman-compose logs ollama
    exit 1
fi

# Wait for Ollama to be healthy
echo "  • Waiting for Ollama API to be ready..."
max_attempts=30
attempt=0
while [ $attempt -lt $max_attempts ]; do
    if curl -sf http://localhost:11434/api/tags >/dev/null 2>&1; then
        echo "  ✓ Ollama API is healthy"
        break
    fi
    attempt=$((attempt + 1))
    if [ $((attempt % 10)) -eq 0 ]; then
        echo "    Still waiting... ($attempt/$max_attempts)"
    fi
    sleep 1
done

if [ $attempt -eq $max_attempts ]; then
    echo "  ✗ Ollama API failed to respond"
    exit 1
fi

# Check and pull model
echo ""
echo "✓ Setting up Qwen model in Ollama container..."
if podman exec ollama ollama list | grep -q "qwen2.5:7b"; then
    echo "  ✓ Model qwen2.5:7b already available"
else
    echo "  • Pulling qwen2.5:7b model (~4GB)..."
    echo "  • This may take a few minutes..."
    podman exec ollama ollama pull qwen2.5:7b
    echo "  ✓ Model download complete"
fi

# Verify Chatbot container is running
echo ""
echo "✓ Verifying Chatbot container..."
if podman ps | grep -q xionco-chatbot; then
    echo "  ✓ Chatbot container is running (port 8080)"
else
    echo "  ✗ Chatbot container failed to start"
    podman-compose logs chatbot
    exit 1
fi

# Wait for chatbot health check
echo "  • Waiting for Spring Boot application to start..."
max_attempts=40
attempt=0
while [ $attempt -lt $max_attempts ]; do
    if curl -sf http://localhost:8080/chat >/dev/null 2>&1; then
        echo "  ✓ Chatbot application is ready"
        break
    fi
    attempt=$((attempt + 1))
    if [ $((attempt % 10)) -eq 0 ]; then
        echo "    Still waiting... ($attempt/$max_attempts)"
    fi
    sleep 1
done

if [ $attempt -eq $max_attempts ]; then
    echo "  ✗ Chatbot failed to start after $max_attempts seconds"
    echo "  Check logs: podman-compose logs chatbot"
    exit 1
fi

# Display final status
echo ""
echo "════════════════════════════════════════════════════════"
echo "  ✓ ALL SERVICES RUNNING IN PODMAN"
echo "════════════════════════════════════════════════════════"
echo ""
echo "📊 Container Status:"
podman ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "ollama|chatbot"
echo ""
echo "🌐 Access the application:"
echo "  • Web UI: http://localhost:8080/chat"
echo ""
echo "🔧 Useful commands:"
echo "  • View all logs:       podman-compose logs -f"
echo "  • View Ollama logs:    podman-compose logs -f ollama"
echo "  • View Chatbot logs:   podman-compose logs -f chatbot"
echo "  • Stop containers:     podman-compose down"
echo "  • Restart services:    podman-compose restart"
echo "  • Check status:        podman-compose ps"
echo ""
echo "🤖 Ollama model management (inside container):"
echo "  • List models:         podman exec ollama ollama list"
echo "  • Pull new model:      podman exec ollama ollama pull <model>"
echo "  • Delete model:        podman exec ollama ollama rm <model>"
echo ""
echo "════════════════════════════════════════════════════════"
echo ""
