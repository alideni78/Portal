# Portal Android Client

Real-time collaborative whiteboard and chat Android application.

## Features

- **Whiteboard**: Draw in real-time with automatic cleanup after 30 seconds
- **Chat**: Send text messages with automatic cleanup after 15 seconds
- **Widget**: Quick access from home screen
- **No Authentication**: Simple shared room for testing

## Requirements

- Android 8.0 (API 26) or higher
- Android Studio Hedgehog (2023.1.1) or newer

## Configuration

### Server URL Setup

**IMPORTANT**: Never commit your server IP or URL to Git!

1. Create `app/src/main/res/values/config.xml` (this file is gitignored):

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Replace with your server URL -->
    <!-- Development: ws://10.0.2.2:8080/ws (Android Emulator) -->
    <!-- Development: ws://YOUR_LOCAL_IP:8080/ws (Physical device) -->
    <!-- Production: wss://your-domain.com/ws -->
    <string name="websocket_url">ws://10.0.2.2:8080/ws</string>
</resources>
```

2. For reference, see `app/src/main/res/values/config-example.xml`

## Build and Run

### Using Android Studio

1. Open `android-client` folder in Android Studio
2. Create `config.xml` as described above
3. Sync Gradle
4. Run on emulator or device

### Using Command Line

```bash
cd android-client
./gradlew assembleDebug
```

APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

## Testing

### Emulator

Use `ws://10.0.2.2:8080/ws` to connect to localhost on your development machine.

### Physical Device

1. Find your computer's local IP: `ipconfig` (Windows) or `ifconfig` (Linux/Mac)
2. Use `ws://YOUR_LOCAL_IP:8080/ws`
3. Make sure device and computer are on same network

### Multiple Devices

Install on multiple devices/emulators to test real-time collaboration.

## Widget Installation

1. Install and open the app
2. Long-press on home screen
3. Select "Widgets"
4. Find "Portal" widget
5. Drag to home screen
6. Tap widget to open app

## Architecture

```
app/
├── model/          # Data models
├── network/        # WebSocket client
├── ui/             # Activities and Views
│   ├── main/       # MainActivity
│   ├── whiteboard/ # WhiteboardView
│   └── chat/       # Chat components
└── widget/        # AppWidget
```

## Security Notes

- Never commit `config.xml` with real server URLs
- Use `wss://` (SSL) for production
- Keep credentials out of code
- The `.gitignore` protects sensitive files automatically