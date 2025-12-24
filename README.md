# Portal

Real-time collaborative whiteboard and chat application for Android with WebSocket server.

## Project Structure

```
Portal/
├── server/           # Ktor WebSocket server
│   ├── src/
│   └── build.gradle.kts
├── android-client/   # Android Kotlin application
│   ├── app/
│   └── build.gradle
└── docs/            # Documentation and setup guides
```

## Features (MVP)

- Real-time whiteboard with auto-cleanup (30 seconds)
- Text chat with auto-cleanup (15 seconds)
- Home screen widget for quick access
- Single shared room for all users
- No authentication required

## Security Notice

⚠️ **NEVER commit sensitive information:**
- Server IP addresses
- Passwords or API keys
- SSL certificates
- Any credentials

All configuration files with sensitive data are gitignored.

## Quick Start

See individual README files in `server/` and `android-client/` directories for detailed setup instructions.

## License

MIT