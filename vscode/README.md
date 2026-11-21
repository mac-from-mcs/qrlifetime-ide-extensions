# QRLifetime QR Code Generator

Turn a URL or any text into a QR code without leaving your editor. The QR code is generated on your machine with the open-source [`qrcode`](https://www.npmjs.com/package/qrcode) library: nothing is sent to a server, no account is needed, and the result is a plain SVG you can keep in your project.

## How to use it

1. Select a URL or some text in the editor, or leave the selection empty.
2. Open the Command Palette (`Ctrl+Shift+P`, or `Cmd+Shift+P` on macOS) and run **QR Lifetime: Generate QR code**. The command is also in the editor context menu when text is selected.
3. If nothing was selected, type the URL or text in the input box.
4. Choose what to do with the QR code:
   - **Insert SVG in the current file** puts the `<svg>` markup at the cursor, or in place of the selection. This is handy in HTML, Markdown, JSX or Vue files.
   - **Save as an SVG file in the project** writes the image to the `qr-codes/` folder of the workspace (or to a location you choose when no folder is open).

## Settings

| Setting | Default | Description |
| --- | --- | --- |
| `qrlifetime.errorCorrectionLevel` | `M` | Error correction level: `L`, `M`, `Q` or `H`. Higher levels survive more damage but make a denser code. |
| `qrlifetime.margin` | `4` | Quiet zone around the code, in modules. |
| `qrlifetime.outputFolder` | `qr-codes` | Folder, relative to the workspace root, where SVG files are saved. |

## Static and dynamic QR codes

The codes made by this extension are static: the URL is written into the pattern itself, so the code works forever but cannot be changed once printed. If you need a QR code whose destination you can edit later and that keeps working even if you stop paying, [QRLifetime](https://qrlifetime.com) makes dynamic QR codes that never expire.

## Privacy

The extension makes no network request and collects no data.

## License

MIT
