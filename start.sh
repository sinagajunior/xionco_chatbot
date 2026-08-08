#!/bin/bash

set -e

echo "================================================"
echo "  Xionco Chatbot - Podman Startup Script"
echo "================================================"
echo ""

# Check prerequisites
echo "✓ Checking prerequisites..."
command -v podman >/dev/null 2>&1 || { echo "✗ Podman not found. Please install Podman."; exit 1; }
command -v podman-compose >/dev/null 2>&1 || { echo "✗ podman-compose not found. Please install podman-compose."; exit 1; }

echo "  • Podman: $(podman --version)"
echo "  • Podman Compose: $(podman-compose --version)"
echo ""

# Build and start services
echo "✓ Starting services with podman-compose..."
podman-compose down 2>/dev/null || true
podman-compose up -d

echo "  • Waiting for services to start (10 seconds)..."
sleep 10

# Check Ollama
echo ""
echo "✓ Setting up Ollama..."
if podman exec -it ollama ollama list | grep -q "qwen2.5:7b"; then
    echo "  • Model qwen2.5:7b already pulled"
else
    echo "  • Pulling qwen2.5:7b model (~4GB)..."
    podman exec -it ollama ollama pull qwen2.5:7b
fi

# Wait for chatbot health check
echo ""
echo "✓ Waiting for chatbot service to be ready..."
max_attempts=30
attempt=0
while [ $attempt -lt $max_attempts ]; do
    if curl -sf http://localhost:8080/chat >/dev/null 2>&1; then
        echo "  • Chatbot is ready!"
        break
    fi
    attempt=$((attempt + 1))
    if [ $((attempt % 10)) -eq 0 ]; then
        echo "  • Still waiting... ($attempt/$max_attempts)"
    fi
    sleep 1
done

if [ $attempt -eq $max_attempts ]; then
    echo "  ✗ Chatbot failed to start after $max_attempts seconds"
    echo "  Check logs: podman-compose logs chatbot"
    exit 1
fi

# Display status
echo ""
echo "================================================"
echo "  ✓ Xionco Chatbot is running!"
echo "================================================"
echo ""
echo "Access the application:"
echo "  • Web UI:  http://localhost:8080/chat"
echo ""
echo "Useful commands:"
echo "  • View logs:      podman-compose logs -f"
echo "  • Stop services:  podman-compose down"
echo "  • Restart:        podman-compose restart"
echo "  • Check status:   podman-compose ps"
echo ""
echo "Model management:"
echo "  • List models:    podman exec ollama ollama list"
echo "  • Pull model:     podman exec ollama ollama pull <model>"
echo ""
