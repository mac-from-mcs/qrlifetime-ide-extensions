# QRLifetime QR Code Generator for code editors

Source code of the QRLifetime QR Code Generator extension for VS Code (also published on Open VSX) and of the plugin for JetBrains IDEs.

Both add a **QR Lifetime: Generate QR code** command that turns the selected URL or text into a QR code, generated locally, and inserts it as SVG in the current file or saves it in the project.

- `vscode/`: VS Code extension in TypeScript, built with `npm install` then `npm run package`.
- `jetbrains/`: IntelliJ Platform plugin in Kotlin, built with `./gradlew buildPlugin` (Java 21).

For dynamic QR codes that you can edit after printing and that never expire, see [qrlifetime.com](https://qrlifetime.com).

MIT licence.
