import * as vscode from 'vscode';
import * as path from 'path';
import * as os from 'os';
import QRCode from 'qrcode';

type Level = 'L' | 'M' | 'Q' | 'H';

const MAX_LENGTH = 2000;

/** Builds the QR code as an SVG string, entirely offline. */
export async function buildSvg(text: string, level: Level, margin: number): Promise<string> {
  const svg = await QRCode.toString(text, {
    type: 'svg',
    errorCorrectionLevel: level,
    margin,
    color: { dark: '#000000', light: '#ffffff' },
  });
  return svg.trim();
}

/** Turns a URL or text into a short, safe file name. */
export function fileNameFor(text: string): string {
  let base = text.replace(/^[a-z][a-z0-9+.-]*:\/\//i, '').replace(/[^a-z0-9]+/gi, '-');
  base = base.replace(/^-+|-+$/g, '').toLowerCase().slice(0, 60);
  return `qr-${base || 'code'}.svg`;
}

async function readInput(editor: vscode.TextEditor | undefined): Promise<string | undefined> {
  const selected = editor && !editor.selection.isEmpty ? editor.document.getText(editor.selection).trim() : '';
  if (selected) {
    return selected;
  }
  return vscode.window.showInputBox({
    title: 'QR Lifetime: Generate QR code',
    prompt: 'URL or text to encode in the QR code',
    placeHolder: 'https://example.com',
    ignoreFocusOut: true,
    validateInput: (value) => {
      if (!value.trim()) {
        return 'Enter a URL or some text.';
      }
      if (value.length > MAX_LENGTH) {
        return `The text is too long for a readable QR code (maximum ${MAX_LENGTH} characters).`;
      }
      return undefined;
    },
  });
}

async function saveSvg(svg: string, text: string, folder: string): Promise<vscode.Uri | undefined> {
  const root = vscode.workspace.workspaceFolders?.[0]?.uri;
  const name = fileNameFor(text);
  let target: vscode.Uri | undefined;
  if (root) {
    target = vscode.Uri.joinPath(root, folder, name);
  } else {
    target = await vscode.window.showSaveDialog({
      defaultUri: vscode.Uri.file(path.join(os.homedir(), name)),
      filters: { 'SVG image': ['svg'] },
    });
  }
  if (!target) {
    return undefined;
  }
  await vscode.workspace.fs.createDirectory(vscode.Uri.joinPath(target, '..'));
  await vscode.workspace.fs.writeFile(target, Buffer.from(svg + '\n', 'utf8'));
  return target;
}

/** Optional arguments, so the command can be bound to a key with a fixed text or action. */
interface CommandArgs {
  text?: string;
  action?: 'insert' | 'save';
}

async function generateQrCode(args?: CommandArgs): Promise<void> {
  const editor = vscode.window.activeTextEditor;
  const text = args?.text?.trim() || (await readInput(editor));
  if (!text) {
    return;
  }

  const config = vscode.workspace.getConfiguration('qrlifetime');
  const level = config.get<Level>('errorCorrectionLevel', 'M');
  const margin = config.get<number>('margin', 4);
  const folder = config.get<string>('outputFolder', 'qr-codes');

  let svg: string;
  try {
    svg = await buildSvg(text, level, margin);
  } catch (error) {
    vscode.window.showErrorMessage(`QR Lifetime: could not generate the QR code (${(error as Error).message}).`);
    return;
  }

  type Choice = vscode.QuickPickItem & { action: 'insert' | 'save' };
  const choices: Choice[] = [];
  if (editor) {
    choices.push({
      label: '$(insert) Insert SVG in the current file',
      description: editor.selection.isEmpty ? 'at the cursor' : 'replaces the selection',
      action: 'insert',
    });
  }
  choices.push({
    label: '$(save) Save as an SVG file in the project',
    description: vscode.workspace.workspaceFolders ? `${folder}/${fileNameFor(text)}` : 'choose a location',
    action: 'save',
  });

  const preset = choices.find((c) => c.action === args?.action);
  const choice = preset ?? (choices.length === 1 ? choices[0] : await vscode.window.showQuickPick(choices, {
    title: 'QR Lifetime: what should be done with the QR code?',
  }));
  if (!choice) {
    return;
  }

  if (choice.action === 'insert' && editor) {
    await editor.edit((builder) => builder.replace(editor.selection, svg));
    return;
  }

  const saved = await saveSvg(svg, text, folder);
  if (saved) {
    const open = 'Open file';
    const answer = await vscode.window.showInformationMessage(
      `QR code saved to ${vscode.workspace.asRelativePath(saved)}.`,
      open,
    );
    if (answer === open) {
      await vscode.commands.executeCommand('vscode.open', saved);
    }
  }
}

export function activate(context: vscode.ExtensionContext): void {
  context.subscriptions.push(vscode.commands.registerCommand('qrlifetime.generateQrCode', generateQrCode));
}

export function deactivate(): void {}
