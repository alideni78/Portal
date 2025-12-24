# Portal Development Guide

## Project Overview

Portal is a real-time collaborative whiteboard and chat application consisting of:
- **Android Client**: Kotlin-based Android app with custom drawing view
- **WebSocket Server**: Ktor-based server for real-time communication

## Architecture

### Communication Flow

```
Android Client 1 ──┐
                   │
Android Client 2 ──┼──> WebSocket Server ──> Broadcast to all clients
                   │
Android Client 3 ──┘
```

### Message Format

All messages follow this JSON structure:

```json
{
  "type": "draw" | "message",
  "roomId": "demoRoom",
  "payload": "{...}",
  "timestamp": 1703000000000
}
```

### Auto-Cleanup

- **Whiteboard strokes**: Removed after 30 seconds
- **Chat messages**: Removed after 15 seconds
- Cleanup happens on client side only

## Development Setup

### 1. Server Development

```bash
cd server

# Create config file
cp src/main/resources/application-example.conf src/main/resources/application.conf

# Run locally
./gradlew run

# Or build JAR
./gradlew shadowJar
java -jar build/libs/portal-server-1.0-SNAPSHOT-all.jar
```

Server runs on: `http://localhost:8080`

### 2. Android Development

```bash
cd android-client

# Create config file
cp app/src/main/res/values/config-example.xml app/src/main/res/values/config.xml

# Edit config.xml with your server URL
# For emulator: ws://10.0.2.2:8080/ws
# For device: ws://YOUR_LOCAL_IP:8080/ws
```

Open in Android Studio and run.

### 3. Testing Multi-User

**Option A: Multiple Emulators**
1. Start multiple Android emulators
2. Use `ws://10.0.2.2:8080/ws` for all
3. Test drawing and chat

**Option B: Physical Devices**
1. Find your computer's IP: `ipconfig` or `ifconfig`
2. Use `ws://YOUR_IP:8080/ws`
3. Ensure devices and computer are on same network

**Option C: Mixed**
- Emulators use `ws://10.0.2.2:8080/ws`
- Physical devices use `ws://YOUR_IP:8080/ws`

## Code Structure

### Android Client

```
app/
├── model/
│   └── PortalMessage.kt        # Data models
├── network/
│   ├── WebSocketClient.kt      # WebSocket connection
│   └── MessageBuilder.kt       # Message creation helpers
├── ui/
│   ├── whiteboard/
│   │   └── WhiteboardView.kt   # Custom drawing view
│   └── chat/
│       └── ChatAdapter.kt      # Chat RecyclerView adapter
├── widget/
│   └── PortalWidget.kt         # Home screen widget
└── MainActivity.kt             # Main activity
```

### Server

```
server/src/main/kotlin/com/portal/server/
└── Application.kt              # Main server logic
```

## Key Features Implementation

### Whiteboard Drawing

1. **Touch Events**: WhiteboardView captures touch events
2. **Normalization**: Coordinates normalized to 0.0-1.0 range
3. **Broadcasting**: Stroke sent as JSON via WebSocket
4. **Rendering**: Received strokes denormalized and drawn
5. **Cleanup**: Timer removes strokes older than 30s

### Chat System

1. **Input**: EditText with send button
2. **Display**: RecyclerView with custom adapter
3. **Broadcasting**: Text sent as JSON via WebSocket
4. **Cleanup**: Timer removes messages older than 15s

### WebSocket Connection

1. **Auto-connect**: Connects on app start
2. **Auto-reconnect**: Reconnects after 3s on failure
3. **Heartbeat**: 20s ping interval
4. **State tracking**: Shows connection status in UI

## Troubleshooting

### Server won't start
- Check port 8080 is not in use: `lsof -i :8080`
- Check Java version: `java -version` (need 17+)

### Client can't connect
- Verify server is running: `curl http://localhost:8080/health`
- Check firewall settings
- For emulator, use `10.0.2.2` not `localhost`
- For physical device, use computer's network IP

### Drawing not syncing
- Check WebSocket connection status in app
- Check server logs: `tail -f logs/portal.log`
- Verify message format in logcat

### Widget not appearing
- Rebuild app after widget changes
- Clear home screen cache (restart launcher)

## Security Considerations

### DO NOT COMMIT:
- `config.xml` with real server URLs
- `application.conf` with production settings
- Any IP addresses, passwords, or credentials
- SSL certificates or keys

### For Production:
- Use SSL/TLS (wss:// instead of ws://)
- Add authentication
- Implement rate limiting
- Add input validation
- Use proper error handling
- Set up monitoring and logging

## Performance Tips

### Android
- Drawing strokes are lightweight (just points)
- Auto-cleanup prevents memory growth
- RecyclerView efficiently handles chat messages

### Server
- Stateless design (no data persistence)
- Concurrent HashMap for thread-safe connections
- Coroutines for non-blocking I/O

## Next Steps (Beyond MVP)

1. **Multiple Rooms**: Room selection UI
2. **Authentication**: User accounts and login
3. **Drawing Tools**: Color picker, eraser, undo
4. **Persistence**: Save and load drawings
5. **Image Sharing**: Send images in chat
6. **Voice Chat**: WebRTC integration
7. **Presence**: Show active users
8. **Typing Indicators**: Show when others are typing

## Resources

- [Ktor Documentation](https://ktor.io/docs/)
- [Android Custom Views](https://developer.android.com/develop/ui/views/layout/custom-views)
- [OkHttp WebSocket](https://square.github.io/okhttp/features/websockets/)
- [App Widgets](https://developer.android.com/develop/ui/views/appwidgets)

## Contributing

Remember:
1. Never commit sensitive data
2. Test on multiple devices
3. Follow Kotlin coding conventions
4. Add comments for complex logic
5. Update documentation for new features