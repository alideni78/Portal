# Portal WebSocket Server

Ktor-based WebSocket server for real-time communication.

## Local Development

### Prerequisites

- Java 17+
- Gradle 7.6+

### Build

```bash
./gradlew build
```

### Run Locally

```bash
./gradlew run
```

Server will start on `http://localhost:8080`

WebSocket endpoint: `ws://localhost:8080/ws`

### Build Production JAR

```bash
./gradlew shadowJar
```

Output: `build/libs/portal-server-1.0-SNAPSHOT-all.jar`

### Test WebSocket

Using `websocat`:

```bash
websocat ws://localhost:8080/ws
```

Send test message:

```json
{"type":"message","roomId":"demoRoom","payload":"{\"text\":\"Hello\"}","timestamp":1703000000000}
```

## Configuration

**IMPORTANT**: Never commit `application.conf` with real server IPs or sensitive data!

1. Copy example config:
   ```bash
   cp src/main/resources/application-example.conf src/main/resources/application.conf
   ```

2. Edit `application.conf` with your settings (this file is gitignored)

## API

### WebSocket Endpoint: `/ws`

**Message Format:**

```json
{
  "type": "draw" | "message",
  "roomId": "demoRoom",
  "payload": "{...}",
  "timestamp": 1703000000000
}
```

**Draw Payload:**

```json
{
  "points": [{"x": 0.1, "y": 0.3}, ...],
  "color": "#000000",
  "strokeWidth": 4.0
}
```

**Message Payload:**

```json
{
  "text": "Hello"
}
```

### Health Check: `/health`

Returns `OK` if server is running.

## Deployment

See `../docs/SERVER_SETUP.md` for production deployment instructions.

## Security Notes

- All config files with sensitive data are gitignored
- Use SSL/TLS (wss://) in production
- Never hardcode IPs, passwords, or credentials
- Use environment variables or config files for sensitive settings