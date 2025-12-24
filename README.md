# Portal 🚪

Real-time collaborative whiteboard and chat application for Android with WebSocket server.

![Android](https://img.shields.io/badge/Android-8.0%2B-green)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue)
![Ktor](https://img.shields.io/badge/Ktor-2.3.7-orange)

## Features

✨ **Real-time Whiteboard**
- Draw collaboratively with multiple users
- Automatic cleanup after 30 seconds
- Normalized coordinates for cross-device compatibility

💬 **Live Chat**
- Send text messages instantly
- Auto-cleanup after 15 seconds
- Simple, clean interface

🏠 **Home Screen Widget**
- Quick access from Android home screen
- One-tap to open app

🔄 **Robust Connection**
- Auto-reconnect on disconnect
- Connection status indicator
- Heartbeat mechanism

## Architecture

```
┌─────────────────┐
│  Android Client │
│   (Kotlin)      │
└────────┬────────┘
         │ WebSocket
         │ (JSON)
┌────────▼────────┐
│  Ktor Server    │  ◄──── Broadcast
│  (WebSocket)    │        to all
└────────┬────────┘
         │
    ┌────┴────┐
    │  Clients │
    └─────────┘
```

## Quick Start

### 1️⃣ Clone Repository

```bash
git clone https://github.com/alideni78/Portal.git
cd Portal
```

### 2️⃣ Setup Server

```bash
cd server
cp src/main/resources/application-example.conf src/main/resources/application.conf
./gradlew shadowJar
java -jar build/libs/portal-server-1.0-SNAPSHOT-all.jar
```

Server will start on `http://localhost:8080`

### 3️⃣ Setup Android Client

```bash
cd android-client
cp app/src/main/res/values/config-example.xml app/src/main/res/values/config.xml
```

Edit `config.xml` with your server URL:
- **Emulator**: `ws://10.0.2.2:8080/ws`
- **Physical Device**: `ws://YOUR_LOCAL_IP:8080/ws`

Open in Android Studio and run!

### 4️⃣ Test with Multiple Devices

Install on multiple devices/emulators to see real-time collaboration.

## Project Structure

```
Portal/
├── server/              # Ktor WebSocket server
│   ├── src/
│   │   └── main/kotlin/com/portal/server/
│   │       └── Application.kt
│   ├── build.gradle.kts
│   └── README.md
│
├── android-client/      # Android application
│   ├── app/
│   │   └── src/main/java/com/portal/app/
│   │       ├── model/          # Data models
│   │       ├── network/        # WebSocket client
│   │       ├── ui/             # Views and activities
│   │       ├── widget/         # Home screen widget
│   │       └── MainActivity.kt
│   ├── build.gradle
│   └── README.md
│
└── docs/                # Documentation
    ├── SERVER_SETUP.md         # Production deployment
    └── DEVELOPMENT_GUIDE.md    # Development guide
```

## Technologies

### Server
- **Ktor**: Lightweight Kotlin web framework
- **WebSockets**: Real-time bidirectional communication
- **Coroutines**: Non-blocking asynchronous operations

### Android Client
- **Kotlin**: Modern Android development language
- **OkHttp**: WebSocket client library
- **Custom Views**: Hand-drawn whiteboard implementation
- **Coroutines**: Async operations and timers
- **RecyclerView**: Efficient chat message display
- **AppWidget**: Home screen widget

## Message Protocol

All messages use this JSON format:

```json
{
  "type": "draw" | "message",
  "roomId": "demoRoom",
  "payload": "{...}",
  "timestamp": 1703000000000
}
```

**Draw Message Payload:**
```json
{
  "points": [{"x": 0.1, "y": 0.3}, ...],
  "color": "#000000",
  "strokeWidth": 4.0
}
```

**Text Message Payload:**
```json
{
  "text": "Hello world"
}
```

## Security ⚠️

**IMPORTANT**: This is an MVP for testing. DO NOT use in production without:

✅ SSL/TLS encryption (wss:// instead of ws://)
✅ User authentication
✅ Input validation
✅ Rate limiting
✅ Proper error handling

**Never commit:**
- Real server IPs or URLs
- Passwords or API keys
- SSL certificates
- Any credentials

All sensitive config files are in `.gitignore`.

## Documentation

- 📖 [Server Setup Guide](docs/SERVER_SETUP.md) - Production deployment on Ubuntu
- 🛠️ [Development Guide](docs/DEVELOPMENT_GUIDE.md) - Architecture and development tips
- 📱 [Android Client README](android-client/README.md) - Android-specific setup
- 🖥️ [Server README](server/README.md) - Server-specific details

## Requirements

### Server
- Java 17+
- Gradle 7.6+
- Ubuntu Server (2GB RAM, 1 vCPU) for deployment

### Android
- Android 8.0 (API 26) or higher
- Android Studio Hedgehog (2023.1.1) or newer

## Troubleshooting

### Server Issues
- **Port already in use**: `lsof -i :8080` to find and kill process
- **Can't connect**: Check firewall and ensure server is running

### Android Issues
- **WebSocket error**: Verify server URL in `config.xml`
- **Emulator can't connect**: Use `10.0.2.2` not `localhost`
- **Physical device can't connect**: Use your computer's network IP

### Widget Issues
- Rebuild app after widget changes
- Restart launcher if widget doesn't appear

## Future Enhancements

- [ ] Multiple rooms with room selection
- [ ] User authentication and profiles
- [ ] Color picker and drawing tools
- [ ] Undo/redo functionality
- [ ] Save and load drawings
- [ ] Image sharing in chat
- [ ] Typing indicators
- [ ] User presence (who's online)
- [ ] Voice/video chat via WebRTC

## Contributing

Contributions welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

**Remember**: Never commit sensitive data!

## License

MIT License - see LICENSE file for details

## Support

For issues and questions:
- Open an [issue](https://github.com/alideni78/Portal/issues)
- Check [documentation](docs/)

---

**Note**: This is an MVP for internal testing. Not recommended for production use without proper security measures.

Made with ❤️ using Kotlin