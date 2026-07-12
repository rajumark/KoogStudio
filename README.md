# KoogStudio

A local-first desktop AI chat app powered by [Ollama](https://ollama.com). Built with Kotlin Multiplatform and Compose Multiplatform.

<p align="center">
  <img src="screenshots/koogstudio-screenshot.png" alt="KoogStudio" width="800"/>
</p>

## About

KoogStudio is a native desktop client for Ollama that lets you run AI conversations entirely on your machine. No cloud. No API keys. No data leaving your laptop.



## Features

- **Fully Local** — Runs on your machine via Ollama. Your conversations stay private.
- **Multi-Thread Chats** — Organize conversations by topic, client, or deal.
- **Model Picker** — Switch between any installed Ollama model on the fly.
- **Markdown Support** — AI responses render with rich formatting (code blocks, lists, bold, etc.).
- **Persistent History** — Threads save automatically to disk. Pick up where you left off.
- **Material Design 3** — Clean, modern UI with sidebar navigation and chat bubbles.
- **Typing Indicator** — Animated indicator while the AI is generating a response.

## Tech Stack

| | |
|---|---|
| **Language** | Kotlin 2.4 |
| **UI** | Compose Multiplatform 1.11 + Material 3 |
| **AI Backend** | Ollama (via Koog Agents) |
| **Architecture** | MVVM with AndroidX ViewModel |
| **Persistence** | Kotlinx Serialization (JSON) |
| **Build** | Gradle 9.1 with Kotlin DSL |

## Prerequisites

- JDK 17+
- [Ollama](https://ollama.com) installed and running
- At least one model pulled:
  ```bash
  ollama pull gemma3:4b
  ```

## Getting Started

```bash
# Clone the repo
git clone https://github.com/rajumark/KoogStudio.git
cd KoogStudio

# Run the desktop app
./gradlew :desktopApp:run

# Or with hot reload
./gradlew :desktopApp:hotRun --auto
```

## Project Structure

```
KoogStudio/
├── desktopApp/          # Desktop entry point
├── shared/              # All core logic (shared module)
│   └── src/commonMain/kotlin/com/koog/studio/
│       ├── ChatViewModel.kt    # Ollama integration & state
│       ├── ChatScreen.kt       # Main UI layout
│       ├── ChatBubble.kt       # Message rendering
│       ├── Sidebar.kt          # Thread navigation
│       └── ...
├── screenshots/
└── build.gradle.kts
```

## Running Tests

```bash
./gradlew :shared:jvmTest
```

## License

Open source. See repository for details.
