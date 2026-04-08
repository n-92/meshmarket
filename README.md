# MeshMarket

Decentralized mesh market and messaging based on [bitchat](https://github.com/permissionlesstech/bitchat) technology. Buy, sell, and chat over Bluetooth mesh — no internet, no accounts, encrypted.

[<img alt="Get it on Google Play" height="60" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png"/>](https://play.google.com/store/apps/details?id=com.uniktek.meshmarket)

## Features

- **🛒 Mesh Marketplace**: Create and browse listings with photos and prices directly over the mesh
- **💬 Encrypted Messaging**: X25519 key exchange + AES-256-GCM end-to-end encryption
- **📡 Bluetooth Mesh Network**: Automatic peer discovery and multi-hop relay over BLE
- **🔒 Privacy First**: No accounts, no servers, no tracking — cryptographic identity only
- **📴 Works Offline**: No internet, no Wi-Fi, no cellular needed
- **💬 Channel-Based Chats**: Topic-based group messaging with optional password protection
- **📦 Store & Forward**: Messages cached for offline peers and delivered on reconnect
- **🔋 Battery Optimized**: Adaptive scanning and power management
- **🎨 Modern UI**: Jetpack Compose with Material Design 3

## Install

### Google Play

Download from the [Google Play Store](https://play.google.com/store/apps/details?id=com.uniktek.meshmarket).

### GitHub Releases

Download the latest APK from the [Releases page](../../releases).

## Getting Started

1. Install MeshMarket on your Android device (requires Android 8.0+)
2. Grant Bluetooth and location permissions when prompted
3. The app automatically discovers nearby MeshMarket users
4. Browse the marketplace or start chatting

### Chat Commands

| Command | Description |
|---------|-------------|
| `/j #channel` | Join or create a channel |
| `/m @name message` | Send a private message |
| `/w` | List online users |
| `/channels` | Show all discovered channels |
| `/block @name` | Block a peer |
| `/unblock @name` | Unblock a peer |
| `/clear` | Clear chat messages |
| `/pass [password]` | Set channel password (owner only) |
| `/transfer @name` | Transfer channel ownership |
| `/save` | Toggle message retention (owner only) |

## Build from Source

### Prerequisites

- Android Studio Arctic Fox or newer
- Android SDK API level 26+
- JDK 8+

### Build

```bash
git clone https://github.com/n-92/meshmarket.git
cd meshmarket
./gradlew assembleDebug
```

### Release Bundle

```bash
# Requires keystore.properties in project root
./gradlew bundleRelease
```

## Security & Privacy

- **End-to-End Encryption**: X25519 + AES-256-GCM for all private messages
- **Digital Signatures**: Ed25519 for message authenticity
- **Forward Secrecy**: New key pairs generated each session
- **No Registration**: No accounts, emails, or phone numbers
- **Ephemeral by Default**: Messages exist only in device memory
- **Emergency Wipe**: Triple-tap logo to instantly clear all data

## Permissions

| Permission | Reason |
|------------|--------|
| Bluetooth | BLE mesh networking |
| Location | Required by Android for BLE scanning |
| Camera | QR code scanning |
| Media | Photo attachments for listings |
| Notifications | Message alerts |

## Architecture

Built with Kotlin and Jetpack Compose. Key components:

- **BluetoothMeshService**: BLE mesh networking (central + peripheral roles)
- **EncryptionService**: Cryptographic operations via BouncyCastle
- **BinaryProtocol**: Efficient binary packet format optimized for BLE
- **Nordic BLE Library**: Reliable Bluetooth LE operations
- **Foreground Service**: Background mesh connectivity

Cross-platform compatible with [bitchat for iOS](https://github.com/permissionlesstech/bitchat).

## Contributing

Contributions are welcome! Open an [issue](../../issues) for bugs or feature requests.

## License

This project is released into the public domain. See [LICENSE](LICENSE.md) for details.
