# Ari AI

<img src="https://img.shields.io/badge/Platform-Android-34a853.svg?logo=android" alt="Android" /> <img src="https://img.shields.io/badge/Platform-iOS-lightgrey.svg?logo=apple" alt="iOS" /> <img src="https://img.shields.io/badge/Platform-Windows%2FmacOS%2FLinux-e10707.svg?logo=openjdk" alt="Platform JVM" /> <img src="https://img.shields.io/badge/PRoot-Debian%2012-A80030?logo=debian" alt="Debian 12" />

<div align="center">
<br>
<h2>📱 Ari AI — Personal AI Assistant & Mobile Debian Developer Terminal</h2>

An **open-source AI assistant with persistent memory** and a **full-featured Debian Linux mobile developer terminal** that runs on Android, iOS, Windows, Mac, and Linux.
</div>

---

## 🚀 Key Features

### 💻 Mobile Debian Terminal (Ari Build)
- **Embedded PRoot Debian 12**: Real Linux environment running on your phone without root.
- **In-Terminal Search & Highlighting**: Quick `Ctrl+F` search across the entire VT terminal buffer with instant match highlighting.
- **⚡ Quick Commands Palette**: One-tap execution for package updates, Git workflows (`git status`, `git diff`, `git log`), dev servers (`python3 -m http.server`), and monitoring (`htop`, `df -h`).
- **🔗 Slim Single-Line Hyperlink Bar**: Ultra-compact OSC 8 URL bar with instant Open, Copy, and Dismiss (`✕`) controls.
- **🎨 7 High-Contrast Themes**: Classic Dark, Dracula, Catppuccin Mocha, Nord, Monokai, Matrix Green, and Solarized Dark.
- **🔍 Font Scaling**: In-terminal zoom (`8sp` – `18sp`) with real-time PTY window resizing.
- **⌨️ Mobile Keycap Row**: Instant signal keys (`^C`, `^L`, `^D`, `^Z`), navigation keys (`Home`, `End`, `PgUp`, `PgDn`), and essential coding symbols (`|`, `~`, `/`, `-`, `_`, `$`, `>`, `&`, `` ` ``, `\`).
- **🤖 Antigravity & AI Agent Suite**: Built-in integration for Antigravity CLI (AGY), Grok CLI, OpenCode, Aider AI, GitHub CLI (`gh`), Astral UV, and Node.js.
- **⚙️ Personal Environment**: Automatic sourcing of `/root/.kai_env` for API keys and personalized mobile `.bashrc` aliases.

### 🧠 Intelligent Conversational AI
- **Interactive UI Engine**: Generates interactive cards, dashboards, forms, and games directly in chat.
- **Persistent Memory**: Retains knowledge and facts across multiple chat sessions.
- **Multi-Model Support**: Connects to OpenAI, Anthropic, Gemini, Grok, Ollama, and OpenAI-compatible endpoints.
- **Full Privacy & Local Execution**: Your data stays on your device.

---

## 📦 Download & Build APK

### Automatic GitHub Actions & Telegram Delivery
Every push automatically compiles the single standalone **`Ari-AI.apk`** and posts it directly to Telegram:
- **Telegram Channel:** [https://t.me/aaafreecloud](https://t.me/aaafreecloud)
- **GitHub Artifacts:** [GitHub Actions](https://github.com/aaa-ai-coder/Ari-AI/actions)

### Build Locally
```bash
git clone https://github.com/aaa-ai-coder/Ari-AI.git
cd Ari-AI
./gradlew :androidApp:assembleFossRelease
```
The APK will be generated at:
`build-apks/Ari-AI.apk`

---

## 📄 License

Apache License 2.0. See [LICENSE](LICENSE) for details.
