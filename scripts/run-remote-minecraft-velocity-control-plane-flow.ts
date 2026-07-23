import { createWriteStream, existsSync } from "node:fs";
import { mkdir, rename, writeFile } from "node:fs/promises";
import path from "node:path";
import { spawn } from "node:child_process";
import { fileURLToPath } from "node:url";

type Options = {
  sshAlias: string;
  sshConfigPath: string;
  remoteProxyDir: string;
  remoteBackendDir: string;
  remoteSwitchBackendDir: string;
  switchBackendPort: number;
  remoteKingdomServersDir: string;
  remoteKingdomBackendName: string;
  kingdomBackendPort: number;
  kingdomVelocityServerName: string;
  kingdomMinecraftVersion: string;
  kingdomServerJarName: string;
  kingdomServerJarLocalPath: string;
  remoteControlDir: string;
  remoteHeadlessDir: string;
  controlServerJarPath: string;
  pluginJarPath: string;
  serverPluginJarPath: string;
  scenarioScriptPath: string;
  controlPort: number;
  botUsername: string;
  minecraftVersion: string;
  instanceServerMap: string;
  commandList: string;
  serverCommandList: string;
  logDir: string;
};

type ArgSpec = {
  key: keyof Options;
  type: "string" | "number";
};

const scriptPath = fileURLToPath(import.meta.url);
const scriptsDir = path.dirname(scriptPath);
const repoRoot = path.dirname(scriptsDir);

const defaults: Options = {
  sshAlias: "novus-remote",
  sshConfigPath: "C:\\Users\\TJ\\.ssh\\config",
  remoteProxyDir: "/srv/proxy",
  remoteBackendDir: "/srv/ffa",
  remoteSwitchBackendDir: "/srv/ffa-switch",
  switchBackendPort: 25567,
  remoteKingdomServersDir: "/srv/mc-kingdom-servers",
  remoteKingdomBackendName: "mc-kingdom-server-1",
  kingdomBackendPort: 25568,
  kingdomVelocityServerName: "kingdom",
  kingdomMinecraftVersion: "1.21.4",
  kingdomServerJarName: "paper-1.21.4.jar",
  kingdomServerJarLocalPath: "",
  remoteControlDir: "/srv/resource-game-control",
  remoteHeadlessDir: "/srv/headless",
  controlServerJarPath: path.join(
    repoRoot,
    "distribution",
    "control-server",
    "application.jar"
  ),
  pluginJarPath: path.join(
    repoRoot,
    "distribution",
    "minecraft-proxy",
    "plugins",
    "minecraft-proxy.jar"
  ),
  serverPluginJarPath: path.join(
    repoRoot,
    "distribution",
    "minecraft-game-server",
    "plugins",
    "minecraft-game-server.jar"
  ),
  scenarioScriptPath: path.join(repoRoot, "scripts", "minecraft-velocity-control-plane-flow.mjs"),
  controlPort: 19081,
  botUsername: "ResourceProxyBot",
  minecraftVersion: "1.21.4",
  instanceServerMap: "kingdom-1-minecraft-primary=kingdom,kingdom-2-minecraft-primary=ffa",
  commandList:
    "/server kingdom|/kd help|/kd ui|/kd place castle|/kd place confirm|/kd buildings stage farmstead|/kd hologram spawn kingdom-debug|/kd entity spawn farmer|/kd resources add food 3|/kd companion give ARCANE|/kd scene refresh|/rank list|/rank inspect miner-1|/rank set miner-1 moderator|/rank inspect miner-1",
  serverCommandList:
    "/kd help|/kd ui|/kd place castle|/kd place confirm|/kd buildings stage farmstead|/kd hologram spawn kingdom-debug|/kd entity spawn farmer|/kd interior add|/kd resources add food 3|/kd companion give ARCANE|/kd scene refresh|!/rank inspect miner-1",
  logDir: "bot-logs"
};

const argSpecs = new Map<string, ArgSpec>(
  Object.entries({
    "--ssh-alias": { key: "sshAlias", type: "string" },
    "--ssh-config-path": { key: "sshConfigPath", type: "string" },
    "--remote-proxy-dir": { key: "remoteProxyDir", type: "string" },
    "--remote-backend-dir": { key: "remoteBackendDir", type: "string" },
    "--remote-switch-backend-dir": { key: "remoteSwitchBackendDir", type: "string" },
    "--switch-backend-port": { key: "switchBackendPort", type: "number" },
    "--remote-kingdom-servers-dir": { key: "remoteKingdomServersDir", type: "string" },
    "--remote-kingdom-backend-name": { key: "remoteKingdomBackendName", type: "string" },
    "--kingdom-backend-port": { key: "kingdomBackendPort", type: "number" },
    "--kingdom-velocity-server-name": { key: "kingdomVelocityServerName", type: "string" },
    "--kingdom-minecraft-version": { key: "kingdomMinecraftVersion", type: "string" },
    "--kingdom-server-jar-name": { key: "kingdomServerJarName", type: "string" },
    "--kingdom-server-jar-local-path": { key: "kingdomServerJarLocalPath", type: "string" },
    "--remote-control-dir": { key: "remoteControlDir", type: "string" },
    "--remote-headless-dir": { key: "remoteHeadlessDir", type: "string" },
    "--control-server-jar-path": { key: "controlServerJarPath", type: "string" },
    "--plugin-jar-path": { key: "pluginJarPath", type: "string" },
    "--server-plugin-jar-path": { key: "serverPluginJarPath", type: "string" },
    "--scenario-script-path": { key: "scenarioScriptPath", type: "string" },
    "--control-port": { key: "controlPort", type: "number" },
    "--bot-username": { key: "botUsername", type: "string" },
    "--minecraft-version": { key: "minecraftVersion", type: "string" },
    "--instance-server-map": { key: "instanceServerMap", type: "string" },
    "--command-list": { key: "commandList", type: "string" },
    "--server-command-list": { key: "serverCommandList", type: "string" },
    "--log-dir": { key: "logDir", type: "string" }
  } satisfies Record<string, ArgSpec>)
);

function parseOptions(argv: string[]): Options {
  const options = { ...defaults };
  for (let index = 0; index < argv.length; index += 1) {
    const arg = argv[index];
    const [flag, inlineValue] = arg.includes("=") ? arg.split(/=(.*)/s, 2) : [arg, undefined];
    const spec = argSpecs.get(flag);
    if (!spec) {
      throw new Error(`Unknown argument ${arg}`);
    }
    const rawValue = inlineValue ?? argv[++index];
    if (rawValue == null || rawValue.startsWith("--")) {
      throw new Error(`Missing value for ${flag}`);
    }
    if (spec.type === "number") {
      const parsed = Number(rawValue);
      if (!Number.isInteger(parsed)) {
        throw new Error(`Expected integer for ${flag}, got ${rawValue}`);
      }
      (options[spec.key] as number) = parsed;
    } else {
      (options[spec.key] as string) = rawValue;
    }
  }
  return options;
}

function timestampForFile(date = new Date()): string {
  const pad = (value: number) => String(value).padStart(2, "0");
  return [
    date.getFullYear(),
    pad(date.getMonth() + 1),
    pad(date.getDate()),
    "-",
    pad(date.getHours()),
    pad(date.getMinutes()),
    pad(date.getSeconds())
  ].join("");
}

function isoNow(): string {
  return new Date().toISOString();
}

function remoteQuote(value: string): string {
  return `'${value.replace(/'/g, "'\\''")}'`;
}

async function appendLine(filePath: string, message: string): Promise<void> {
  await writeFile(filePath, `${message}\n`, { flag: "a", encoding: "utf8" });
  console.log(message);
}

async function invokeChecked(
  runLogPath: string,
  filePath: string,
  args: string[],
  failureMessage: string
): Promise<void> {
  await new Promise<void>((resolve, reject) => {
    const logStream = createWriteStream(runLogPath, { flags: "a", encoding: "utf8" });
    const child = spawn(filePath, args, { stdio: ["ignore", "pipe", "pipe"] });

    child.stdout.on("data", (chunk: Buffer) => {
      process.stdout.write(chunk);
      logStream.write(chunk);
    });
    child.stderr.on("data", (chunk: Buffer) => {
      process.stderr.write(chunk);
      logStream.write(chunk);
    });
    child.on("error", (error) => {
      logStream.end();
      reject(error);
    });
    child.on("close", (code) => {
      logStream.end();
      if (code === 0) {
        resolve();
      } else {
        reject(new Error(`${failureMessage} (${filePath} exited ${code})`));
      }
    });
  });
}

async function downloadPaperIfNeeded(options: Options, logRoot: string, runLogPath: string): Promise<string> {
  let kingdomServerJarLocalPath = options.kingdomServerJarLocalPath;
  if (!kingdomServerJarLocalPath.trim()) {
    const paperCacheDir = path.join(logRoot, "paper");
    await mkdir(paperCacheDir, { recursive: true });
    kingdomServerJarLocalPath = path.join(paperCacheDir, options.kingdomServerJarName);
  }

  if (existsSync(kingdomServerJarLocalPath)) {
    return kingdomServerJarLocalPath;
  }

  await appendLine(runLogPath, `[${isoNow()}] Downloading Paper ${options.kingdomMinecraftVersion} for kingdom backend.`);
  const buildsResponse = await fetch(
    `https://api.papermc.io/v2/projects/paper/versions/${options.kingdomMinecraftVersion}/builds`
  );
  if (!buildsResponse.ok) {
    throw new Error(`Failed to list Paper builds: HTTP ${buildsResponse.status}`);
  }
  const builds = (await buildsResponse.json()) as { builds: Array<{ build: number }> };
  const latestBuild = builds.builds.at(-1)?.build;
  if (latestBuild == null) {
    throw new Error(`No Paper builds found for ${options.kingdomMinecraftVersion}`);
  }

  const buildResponse = await fetch(
    `https://api.papermc.io/v2/projects/paper/versions/${options.kingdomMinecraftVersion}/builds/${latestBuild}`
  );
  if (!buildResponse.ok) {
    throw new Error(`Failed to inspect Paper build ${latestBuild}: HTTP ${buildResponse.status}`);
  }
  const build = (await buildResponse.json()) as { downloads: { application: { name: string } } };
  const downloadName = build.downloads.application.name;
  const downloadResponse = await fetch(
    `https://api.papermc.io/v2/projects/paper/versions/${options.kingdomMinecraftVersion}/builds/${latestBuild}/downloads/${downloadName}`
  );
  if (!downloadResponse.ok || !downloadResponse.body) {
    throw new Error(`Failed to download Paper ${downloadName}: HTTP ${downloadResponse.status}`);
  }

  const tempPath = `${kingdomServerJarLocalPath}.new`;
  const buffer = Buffer.from(await downloadResponse.arrayBuffer());
  await writeFile(tempPath, buffer);
  await rename(tempPath, kingdomServerJarLocalPath);
  return kingdomServerJarLocalPath;
}

function assertLocalFile(filePath: string, label: string): void {
  if (!existsSync(filePath)) {
    throw new Error(`${label} not found at ${filePath}`);
  }
}

async function main(): Promise<void> {
  const options = parseOptions(process.argv.slice(2));
  const logRoot = path.resolve(repoRoot, options.logDir);
  await mkdir(logRoot, { recursive: true });

  const timestamp = timestampForFile();
  const baseName = `remote-minecraft-velocity-control-plane-${timestamp}`;
  const runLogPath = path.join(logRoot, `${baseName}-run.txt`);
  const resultPath = path.join(logRoot, `${baseName}-result.json`);
  const transcriptPath = path.join(logRoot, `${baseName}-transcript.txt`);
  const serverResultPath = path.join(logRoot, `${baseName}-server-result.json`);
  const serverTranscriptPath = path.join(logRoot, `${baseName}-server-transcript.txt`);
  const remotePluginPath = `${options.remoteProxyDir}/plugins/minecraft-proxy.jar`;
  const remoteBackendPluginPath = `${options.remoteBackendDir}/plugins/minecraft-game-server.jar`;
  const remoteSwitchBackendPluginPath = `${options.remoteSwitchBackendDir}/plugins/minecraft-game-server.jar`;
  const remoteKingdomBackendDir = `${options.remoteKingdomServersDir}/${options.remoteKingdomBackendName}`;
  const remoteKingdomBackendPluginPath = `${remoteKingdomBackendDir}/plugins/minecraft-game-server.jar`;
  const remoteKingdomServerJarPath = `${remoteKingdomBackendDir}/${options.kingdomServerJarName}`;
  const remoteScriptPath = `${options.remoteHeadlessDir}/${baseName}.mjs`;
  const remoteOutputDir = `/tmp/${baseName}`;

  assertLocalFile(options.pluginJarPath, "Minecraft Velocity plugin jar");
  assertLocalFile(options.serverPluginJarPath, "Minecraft Bukkit server plugin jar");
  assertLocalFile(options.controlServerJarPath, "Control server jar");
  const controlServerLibDirectory = path.join(path.dirname(options.controlServerJarPath), "libs");
  assertLocalFile(controlServerLibDirectory, "Control server dependency directory");
  assertLocalFile(options.scenarioScriptPath, "Scenario script");
  const kingdomServerJarLocalPath = await downloadPaperIfNeeded(options, logRoot, runLogPath);

  const sshArgs = (...args: string[]) => ["-F", options.sshConfigPath, options.sshAlias, ...args];
  const scpArgs = (...args: string[]) => ["-F", options.sshConfigPath, ...args];

  await appendLine(runLogPath, `[${isoNow()}] Deploying resource-game control bridge.`);
  await invokeChecked(
    runLogPath,
    "ssh.exe",
    sshArgs(`mkdir -p ${remoteQuote(`${options.remoteControlDir}/logs`)} && rm -rf ${remoteQuote(`${options.remoteControlDir}/libs.new`)}`),
    "Failed to prepare remote control directory."
  );
  await invokeChecked(
    runLogPath,
    "scp.exe",
    scpArgs(options.controlServerJarPath, `${options.sshAlias}:${options.remoteControlDir}/control-server.jar.new`),
    "Failed to copy control server jar."
  );
  await invokeChecked(
    runLogPath,
    "scp.exe",
    scpArgs("-r", controlServerLibDirectory, `${options.sshAlias}:${options.remoteControlDir}/libs.new`),
    "Failed to copy control server dependencies."
  );

  const remoteControl = `
set -euo pipefail
cd ${remoteQuote(options.remoteControlDir)}
mv control-server.jar.new control-server.jar
rm -rf libs.previous
if [ -d libs ]; then mv libs libs.previous; fi
mv libs.new libs
if command -v fuser >/dev/null 2>&1; then
  fuser -k ${options.controlPort}/tcp 2>/dev/null || true
fi
for i in $(seq 1 30); do
  if ! ss -ltn | grep -q ':${options.controlPort} '; then
    break
  fi
  sleep 1
done
if ss -ltn | grep -q ':${options.controlPort} '; then
  lsof -ti tcp:${options.controlPort} | xargs -r kill -9 || true
fi
for i in $(seq 1 10); do
  if ! ss -ltn | grep -q ':${options.controlPort} '; then
    break
  fi
  sleep 1
done
if ss -ltn | grep -q ':${options.controlPort} '; then
  echo 'Resource-game control bridge port is still occupied after shutdown.' >&2
  ss -ltnp | grep ':${options.controlPort} ' >&2 || true
  exit 1
fi
tmux kill-session -t control 2>/dev/null || true
tmux new-session -d -s control -c ${remoteQuote(options.remoteControlDir)} "env TAVALL_CONTROL_BRIDGE_HOST=127.0.0.1 TAVALL_CONTROL_BRIDGE_PORT=${options.controlPort} java --enable-preview -cp 'control-server.jar:libs/*' org.tavall.control.cli.ControlConsoleApplication 2>&1 | tee -a logs/control-bridge.out.log"
for i in $(seq 1 60); do
  if ss -ltn | grep -q ':${options.controlPort} '; then
    sleep 2
    exit 0
  fi
  sleep 1
done
echo 'Resource-game control bridge did not become reachable.' >&2
tail -n 120 logs/control-bridge.err.log >&2 || true
tail -n 120 logs/control-bridge.out.log >&2 || true
exit 1
`.trim();
  await invokeChecked(runLogPath, "ssh.exe", sshArgs(remoteControl), "Failed to start remote resource-game control bridge.");

  await appendLine(runLogPath, `[${isoNow()}] Deploying Minecraft Velocity control-plane plugin.`);
  await invokeChecked(
    runLogPath,
    "ssh.exe",
    sshArgs(
      [
        "mkdir -p",
        remoteQuote(`${options.remoteProxyDir}/plugins`),
        remoteQuote(`${options.remoteProxyDir}/logs`),
        remoteQuote(`${options.remoteBackendDir}/plugins`),
        remoteQuote(`${options.remoteBackendDir}/logs`),
        remoteQuote(`${options.remoteSwitchBackendDir}/plugins`),
        remoteQuote(`${options.remoteSwitchBackendDir}/logs`),
        remoteQuote(`${remoteKingdomBackendDir}/plugins`),
        remoteQuote(`${remoteKingdomBackendDir}/logs`),
        remoteQuote(options.remoteHeadlessDir)
      ].join(" ")
    ),
    "Failed to prepare remote Minecraft directories."
  );
  await invokeChecked(runLogPath, "scp.exe", scpArgs(options.pluginJarPath, `${options.sshAlias}:${remotePluginPath}.new`), "Failed to copy Velocity plugin jar.");
  await invokeChecked(runLogPath, "scp.exe", scpArgs(options.serverPluginJarPath, `${options.sshAlias}:${remoteBackendPluginPath}.new`), "Failed to copy Bukkit server plugin jar to backend.");
  await invokeChecked(runLogPath, "scp.exe", scpArgs(options.serverPluginJarPath, `${options.sshAlias}:${remoteSwitchBackendPluginPath}.new`), "Failed to copy Bukkit server plugin jar to switch backend.");
  await invokeChecked(runLogPath, "scp.exe", scpArgs(options.serverPluginJarPath, `${options.sshAlias}:${remoteKingdomBackendPluginPath}.new`), "Failed to copy Bukkit server plugin jar to kingdom backend.");
  await invokeChecked(runLogPath, "scp.exe", scpArgs(kingdomServerJarLocalPath, `${options.sshAlias}:${remoteKingdomServerJarPath}.new`), "Failed to copy Paper kingdom backend jar.");
  await invokeChecked(runLogPath, "scp.exe", scpArgs(options.scenarioScriptPath, `${options.sshAlias}:${remoteScriptPath}`), "Failed to copy Minecraft Velocity scenario script.");

  const remoteDeploy = `
set -euo pipefail
if [ -f ${remoteQuote(remotePluginPath)} ]; then
  cp ${remoteQuote(remotePluginPath)} ${remoteQuote(`${remotePluginPath}.bak-${timestamp}`)}
fi
mv ${remoteQuote(`${remotePluginPath}.new`)} ${remoteQuote(remotePluginPath)}
if [ -f ${remoteQuote(`${options.remoteProxyDir}/plugins/Speedrun.jar`)} ]; then
  mv ${remoteQuote(`${options.remoteProxyDir}/plugins/Speedrun.jar`)} ${remoteQuote(`${options.remoteProxyDir}/plugins/Speedrun.jar.disabled-${timestamp}`)}
fi
if [ -e ${remoteQuote(`${options.remoteProxyDir}/plugins/velocitycore`)} ]; then
  mv ${remoteQuote(`${options.remoteProxyDir}/plugins/velocitycore`)} ${remoteQuote(`${options.remoteProxyDir}/plugins/velocitycore.disabled-${timestamp}`)}
fi
if [ -f ${remoteQuote(remoteBackendPluginPath)} ]; then
  cp ${remoteQuote(remoteBackendPluginPath)} ${remoteQuote(`${remoteBackendPluginPath}.bak-${timestamp}`)}
fi
mv ${remoteQuote(`${remoteBackendPluginPath}.new`)} ${remoteQuote(remoteBackendPluginPath)}
if [ -f ${remoteQuote(remoteSwitchBackendPluginPath)} ]; then
  cp ${remoteQuote(remoteSwitchBackendPluginPath)} ${remoteQuote(`${remoteSwitchBackendPluginPath}.bak-${timestamp}`)}
fi
mv ${remoteQuote(`${remoteSwitchBackendPluginPath}.new`)} ${remoteQuote(remoteSwitchBackendPluginPath)}
if [ -f ${remoteQuote(remoteKingdomBackendPluginPath)} ]; then
  cp ${remoteQuote(remoteKingdomBackendPluginPath)} ${remoteQuote(`${remoteKingdomBackendPluginPath}.bak-${timestamp}`)}
fi
mv ${remoteQuote(`${remoteKingdomBackendPluginPath}.new`)} ${remoteQuote(remoteKingdomBackendPluginPath)}
if [ -f ${remoteQuote(remoteKingdomServerJarPath)} ]; then
  cp ${remoteQuote(remoteKingdomServerJarPath)} ${remoteQuote(`${remoteKingdomServerJarPath}.bak-${timestamp}`)}
fi
mv ${remoteQuote(`${remoteKingdomServerJarPath}.new`)} ${remoteQuote(remoteKingdomServerJarPath)}
chmod +x ${remoteQuote(`${options.remoteProxyDir}/start.sh`)} || true
python3 - <<'PY'
from pathlib import Path
path = Path('${options.remoteProxyDir}') / 'velocity.toml'
text = path.read_text()
quote = chr(34)
text = '\\n'.join(
    'player-info-forwarding-mode = ' + quote + 'none' + quote if line.strip().startswith('player-info-forwarding-mode') else line
    for line in text.splitlines()
) + '\\n'
lines = []
in_servers = False
server_written = False
skip_try = False
for line in text.splitlines():
    stripped = line.strip()
    if skip_try:
        if stripped == ']':
            lines.append('try = [')
            lines.append('    ' + chr(34) + '${options.kingdomVelocityServerName}' + chr(34) + ',')
            lines.append('    ' + chr(34) + 'lobby' + chr(34))
            lines.append(']')
            skip_try = False
        continue
    if stripped == '[servers]':
        in_servers = True
        lines.append(line)
        continue
    if in_servers and stripped.startswith('['):
        if not server_written:
            lines.append('${options.kingdomVelocityServerName} = ' + chr(34) + '127.0.0.1:${options.kingdomBackendPort}' + chr(34))
            server_written = True
        in_servers = False
    if in_servers and stripped.startswith('try ='):
        if not server_written:
            lines.append('${options.kingdomVelocityServerName} = ' + chr(34) + '127.0.0.1:${options.kingdomBackendPort}' + chr(34))
            server_written = True
        skip_try = True
        continue
    if line.strip().startswith('ffa ='):
        lines.append('ffa = ' + chr(34) + '127.0.0.1:${options.switchBackendPort}' + chr(34))
    elif line.strip().startswith('${options.kingdomVelocityServerName} ='):
        if not server_written:
            lines.append('${options.kingdomVelocityServerName} = ' + chr(34) + '127.0.0.1:${options.kingdomBackendPort}' + chr(34))
            server_written = True
    else:
        lines.append(line)
if in_servers and not server_written:
    lines.append('${options.kingdomVelocityServerName} = ' + chr(34) + '127.0.0.1:${options.kingdomBackendPort}' + chr(34))
path.write_text('\\n'.join(lines) + '\\n')
PY
`.trim();
  await invokeChecked(runLogPath, "ssh.exe", sshArgs(remoteDeploy), "Failed to install remote Minecraft proxy/server plugins.");

  await appendLine(runLogPath, `[${isoNow()}] Ensuring configured remote Minecraft backend is listening on 25566.`);
  const remoteBackend = `
set -euo pipefail
if [ ! -d ${remoteQuote(options.remoteBackendDir)} ]; then
  echo 'Remote Minecraft backend directory does not exist: ${options.remoteBackendDir}' >&2
  exit 1
fi
cd ${remoteQuote(options.remoteBackendDir)}
mkdir -p logs plugins
python3 - <<'PY'
from pathlib import Path
path = Path('spigot.yml')
if path.exists():
    path.write_text(path.read_text().replace('  bungeecord: true', '  bungeecord: false'))
PY
if command -v fuser >/dev/null 2>&1; then
  fuser -k 25566/tcp 2>/dev/null || true
fi
for i in $(seq 1 30); do
  if ! ss -ltn | grep -q ':25566 '; then
    break
  fi
  sleep 1
done
if ss -ltn | grep -q ':25566 '; then
  echo 'Minecraft backend port 25566 is still occupied after shutdown.' >&2
  ss -ltnp | grep ':25566 ' >&2 || true
  exit 1
fi
tmux kill-session -t minecraft-ffa 2>/dev/null || true
tmux new-session -d -s minecraft-ffa -c ${remoteQuote(options.remoteBackendDir)} "env RESOURCE_GAME_MINECRAFT_SERVER_ID='minecraft-backend-ffa' RESOURCE_GAME_MINECRAFT_PROXY_ID='velocity-proxy' RESOURCE_GAME_MINECRAFT_CONTROL_INGRESS_URL='tcp://127.0.0.1:${options.controlPort}' RESOURCE_GAME_CONTROL_INGRESS_URL='tcp://127.0.0.1:${options.controlPort}' java -Xss1650k -Xmx1536M -jar spigot.jar nogui 2>&1 | tee -a logs/resource-game-backend.out.log"
for i in $(seq 1 60); do
  if ss -ltn | grep -q ':25566 '; then
    break
  fi
  sleep 1
done
if ! ss -ltn | grep -q ':25566 '; then
  echo 'Minecraft backend did not open port 25566 in time.' >&2
  tail -n 120 logs/resource-game-backend.err.log >&2 || true
  tail -n 120 logs/resource-game-backend.out.log >&2 || true
  exit 1
fi
for i in $(seq 1 30); do
  if grep -h 'Tavall Resource Game Bukkit server frontend enabled. serverId=minecraft-backend-ffa proxyId=velocity-proxy' logs/latest.log logs/resource-game-backend.out.log logs/resource-game-backend.err.log 2>/dev/null; then
    exit 0
  fi
  sleep 1
done
echo 'Minecraft backend started, but Tavall Bukkit server plugin did not report enabled.' >&2
tail -n 160 logs/latest.log >&2 || true
tail -n 120 logs/resource-game-backend.err.log >&2 || true
tail -n 120 logs/resource-game-backend.out.log >&2 || true
exit 1
`.trim();
  await invokeChecked(runLogPath, "ssh.exe", sshArgs(remoteBackend), "Failed to ensure remote Minecraft backend.");

  await appendLine(runLogPath, `[${isoNow()}] Ensuring remote Minecraft switch backend is listening on ${options.switchBackendPort}.`);
  const remoteSwitchBackend = `
set -euo pipefail
if [ ! -f ${remoteQuote(`${options.remoteBackendDir}/spigot.jar`)} ]; then
  echo 'Remote Minecraft source backend jar does not exist: ${options.remoteBackendDir}/spigot.jar' >&2
  exit 1
fi
mkdir -p ${remoteQuote(options.remoteSwitchBackendDir)} ${remoteQuote(`${options.remoteSwitchBackendDir}/logs`)}
cp ${remoteQuote(`${options.remoteBackendDir}/spigot.jar`)} ${remoteQuote(`${options.remoteSwitchBackendDir}/spigot.jar`)}
cp ${remoteQuote(`${options.remoteBackendDir}/spigot.yml`)} ${remoteQuote(`${options.remoteSwitchBackendDir}/spigot.yml`)} 2>/dev/null || true
cp ${remoteQuote(`${options.remoteBackendDir}/bukkit.yml`)} ${remoteQuote(`${options.remoteSwitchBackendDir}/bukkit.yml`)} 2>/dev/null || true
cat > ${remoteQuote(`${options.remoteSwitchBackendDir}/eula.txt`)} <<'EOF'
eula=true
EOF
cat > ${remoteQuote(`${options.remoteSwitchBackendDir}/server.properties`)} <<'EOF'
server-port=${options.switchBackendPort}
server-ip=127.0.0.1
online-mode=false
motd=Resource Game Switch Backend
enable-command-block=true
white-list=false
spawn-protection=0
EOF
cd ${remoteQuote(options.remoteSwitchBackendDir)}
mkdir -p plugins logs
python3 - <<'PY'
from pathlib import Path
path = Path('spigot.yml')
if path.exists():
    path.write_text(path.read_text().replace('  bungeecord: true', '  bungeecord: false'))
PY
if command -v fuser >/dev/null 2>&1; then
  fuser -k ${options.switchBackendPort}/tcp 2>/dev/null || true
fi
for i in $(seq 1 30); do
  if ! ss -ltn | grep -q ':${options.switchBackendPort} '; then
    break
  fi
  sleep 1
done
if ss -ltn | grep -q ':${options.switchBackendPort} '; then
  echo 'Minecraft switch backend port ${options.switchBackendPort} is still occupied after shutdown.' >&2
  ss -ltnp | grep ':${options.switchBackendPort} ' >&2 || true
  exit 1
fi
tmux kill-session -t minecraft-switch 2>/dev/null || true
tmux new-session -d -s minecraft-switch -c ${remoteQuote(options.remoteSwitchBackendDir)} "env RESOURCE_GAME_MINECRAFT_SERVER_ID='minecraft-backend-switch' RESOURCE_GAME_MINECRAFT_PROXY_ID='velocity-proxy' RESOURCE_GAME_MINECRAFT_CONTROL_INGRESS_URL='tcp://127.0.0.1:${options.controlPort}' RESOURCE_GAME_CONTROL_INGRESS_URL='tcp://127.0.0.1:${options.controlPort}' java -Xss1650k -Xmx1024M -jar spigot.jar nogui 2>&1 | tee -a logs/resource-game-switch-backend.out.log"
for i in $(seq 1 60); do
  if ss -ltn | grep -q ':${options.switchBackendPort} '; then
    break
  fi
  sleep 1
done
if ! ss -ltn | grep -q ':${options.switchBackendPort} '; then
  echo 'Minecraft switch backend did not open port ${options.switchBackendPort} in time.' >&2
  tail -n 120 logs/resource-game-switch-backend.err.log >&2 || true
  tail -n 120 logs/resource-game-switch-backend.out.log >&2 || true
  exit 1
fi
for i in $(seq 1 30); do
  if grep -h 'Tavall Resource Game Bukkit server frontend enabled. serverId=minecraft-backend-switch proxyId=velocity-proxy' logs/latest.log logs/resource-game-switch-backend.out.log logs/resource-game-switch-backend.err.log 2>/dev/null; then
    exit 0
  fi
  sleep 1
done
echo 'Minecraft switch backend started, but Tavall Bukkit server plugin did not report enabled.' >&2
tail -n 160 logs/latest.log >&2 || true
tail -n 120 logs/resource-game-switch-backend.err.log >&2 || true
tail -n 120 logs/resource-game-switch-backend.out.log >&2 || true
exit 1
`.trim();
  await invokeChecked(runLogPath, "ssh.exe", sshArgs(remoteSwitchBackend), "Failed to ensure remote Minecraft switch backend.");

  await appendLine(runLogPath, `[${isoNow()}] Ensuring remote Minecraft kingdom backend is listening on ${options.kingdomBackendPort}.`);
  const remoteKingdomBackend = `
set -euo pipefail
mkdir -p ${remoteQuote(options.remoteKingdomServersDir)} ${remoteQuote(remoteKingdomBackendDir)} ${remoteQuote(`${remoteKingdomBackendDir}/logs`)} ${remoteQuote(`${remoteKingdomBackendDir}/plugins`)}
cp ${remoteQuote(`${options.remoteBackendDir}/spigot.yml`)} ${remoteQuote(`${remoteKingdomBackendDir}/spigot.yml`)} 2>/dev/null || true
cp ${remoteQuote(`${options.remoteBackendDir}/bukkit.yml`)} ${remoteQuote(`${remoteKingdomBackendDir}/bukkit.yml`)} 2>/dev/null || true
if [ ! -f ${remoteQuote(`${remoteKingdomBackendDir}/${options.kingdomServerJarName}`)} ]; then
  echo 'Kingdom Paper jar missing after deploy: ${remoteKingdomBackendDir}/${options.kingdomServerJarName}' >&2
  exit 1
fi
ln -sfn ${remoteQuote(options.kingdomServerJarName)} ${remoteQuote(`${remoteKingdomBackendDir}/server.jar`)}
cat > ${remoteQuote(`${remoteKingdomBackendDir}/eula.txt`)} <<'EOF'
eula=true
EOF
cat > ${remoteQuote(`${remoteKingdomBackendDir}/server.properties`)} <<'EOF'
server-port=${options.kingdomBackendPort}
server-ip=127.0.0.1
online-mode=false
enforce-secure-profile=false
motd=Resource Game Kingdom Server 1
enable-command-block=true
white-list=false
spawn-protection=0
EOF
cd ${remoteQuote(remoteKingdomBackendDir)}
python3 - <<'PY'
from pathlib import Path
path = Path('spigot.yml')
if path.exists():
    path.write_text(path.read_text().replace('  bungeecord: true', '  bungeecord: false'))
PY
if command -v fuser >/dev/null 2>&1; then
  fuser -k ${options.kingdomBackendPort}/tcp 2>/dev/null || true
fi
for i in $(seq 1 30); do
  if ! ss -ltn | grep -q ':${options.kingdomBackendPort} '; then
    break
  fi
  sleep 1
done
if ss -ltn | grep -q ':${options.kingdomBackendPort} '; then
  echo 'Minecraft kingdom backend port ${options.kingdomBackendPort} is still occupied after shutdown.' >&2
  ss -ltnp | grep ':${options.kingdomBackendPort} ' >&2 || true
  exit 1
fi
tmux kill-session -t minecraft-kingdom 2>/dev/null || true
tmux new-session -d -s minecraft-kingdom -c ${remoteQuote(remoteKingdomBackendDir)} "env RESOURCE_GAME_MINECRAFT_SERVER_ID='${options.remoteKingdomBackendName}' RESOURCE_GAME_MINECRAFT_PROXY_ID='velocity-proxy' RESOURCE_GAME_MINECRAFT_CONTROL_INGRESS_URL='tcp://127.0.0.1:${options.controlPort}' RESOURCE_GAME_CONTROL_INGRESS_URL='tcp://127.0.0.1:${options.controlPort}' java -Xmx1536M -jar server.jar nogui 2>&1 | tee -a logs/resource-game-kingdom-backend.out.log"
for i in $(seq 1 60); do
  if ss -ltn | grep -q ':${options.kingdomBackendPort} '; then
    break
  fi
  sleep 1
done
if ! ss -ltn | grep -q ':${options.kingdomBackendPort} '; then
  echo 'Minecraft kingdom backend did not open port ${options.kingdomBackendPort} in time.' >&2
  tail -n 120 logs/resource-game-kingdom-backend.err.log >&2 || true
  tail -n 120 logs/resource-game-kingdom-backend.out.log >&2 || true
  exit 1
fi
for i in $(seq 1 120); do
  if grep -h 'Tavall Resource Game Bukkit server frontend enabled. serverId=${options.remoteKingdomBackendName} proxyId=velocity-proxy' logs/latest.log logs/resource-game-kingdom-backend.out.log logs/resource-game-kingdom-backend.err.log 2>/dev/null; then
    if grep -h 'Minecraft server version: ${options.kingdomMinecraftVersion}' logs/latest.log logs/resource-game-kingdom-backend.out.log logs/resource-game-kingdom-backend.err.log 2>/dev/null || grep -h 'Starting minecraft server version ${options.kingdomMinecraftVersion}' logs/latest.log logs/resource-game-kingdom-backend.out.log logs/resource-game-kingdom-backend.err.log 2>/dev/null; then
      exit 0
    fi
  fi
  sleep 1
done
echo 'Minecraft kingdom backend started, but Tavall Bukkit server plugin or Paper ${options.kingdomMinecraftVersion} did not report enabled.' >&2
tail -n 160 logs/latest.log >&2 || true
tail -n 120 logs/resource-game-kingdom-backend.err.log >&2 || true
tail -n 120 logs/resource-game-kingdom-backend.out.log >&2 || true
exit 1
`.trim();
  await invokeChecked(runLogPath, "ssh.exe", sshArgs(remoteKingdomBackend), "Failed to ensure remote Minecraft kingdom backend.");

  await appendLine(runLogPath, `[${isoNow()}] Restarting remote Velocity proxy.`);
  const remoteRestart = `
set -euo pipefail
test -f ${remoteQuote(remotePluginPath)}
test -f ${remoteQuote(remoteBackendPluginPath)}
test -f ${remoteQuote(remoteSwitchBackendPluginPath)}
test -f ${remoteQuote(remoteKingdomBackendPluginPath)}
pkill -f '[v]elocity.jar' || true
for i in $(seq 1 30); do
  if ! ss -ltn | grep -q ':25565 '; then
    break
  fi
  sleep 1
done
cd ${remoteQuote(options.remoteProxyDir)}
tmux kill-session -t minecraft-proxy 2>/dev/null || true
tmux new-session -d -s minecraft-proxy -c ${remoteQuote(options.remoteProxyDir)} "env RESOURCE_GAME_MINECRAFT_OWNER_USERNAMES='${options.botUsername}' RESOURCE_GAME_MINECRAFT_SERVER_ID='velocity-proxy' RESOURCE_GAME_MINECRAFT_INSTANCE_SERVER_MAP='${options.instanceServerMap}' RESOURCE_GAME_MINECRAFT_CONTROL_INGRESS_URL='tcp://127.0.0.1:${options.controlPort}' RESOURCE_GAME_CONTROL_INGRESS_URL='tcp://127.0.0.1:${options.controlPort}' bash ./start.sh 2>&1 | tee -a logs/resource-game-proxy.out.log"
for i in $(seq 1 40); do
  if ss -ltn | grep -q ':25565 '; then
    break
  fi
  sleep 1
done
if ! ss -ltn | grep -q ':25565 '; then
  echo 'Velocity proxy did not open port 25565 in time.' >&2
  tail -n 80 logs/resource-game-proxy.err.log >&2 || true
  tail -n 80 logs/resource-game-proxy.out.log >&2 || true
  exit 1
fi
for i in $(seq 1 30); do
  if grep -h 'Registered Tavall Resource Game Velocity routing adapter. serverId=velocity-proxy' logs/latest.log logs/resource-game-proxy.out.log logs/resource-game-proxy.err.log 2>/dev/null; then
    exit 0
  fi
  sleep 1
done
echo 'Velocity proxy opened 25565, but updated Tavall proxy plugin did not report registered.' >&2
tail -n 120 logs/latest.log >&2 || true
tail -n 80 logs/resource-game-proxy.err.log >&2 || true
tail -n 80 logs/resource-game-proxy.out.log >&2 || true
exit 1
`.trim();
  await invokeChecked(runLogPath, "ssh.exe", sshArgs(remoteRestart), "Failed to restart remote Velocity proxy.");

  await appendLine(runLogPath, `[${isoNow()}] Whitelisting bot if remote whitelist helper exists.`);
  const remoteWhitelist = `if [ -x ${remoteQuote(`${options.remoteProxyDir}/ensure-proxy-whitelist-users.sh`)} ]; then ${remoteQuote(`${options.remoteProxyDir}/ensure-proxy-whitelist-users.sh`)} ${remoteQuote(options.botUsername)}; fi`;
  await invokeChecked(runLogPath, "ssh.exe", sshArgs(remoteWhitelist), "Failed to whitelist Minecraft verification bot.");

  await appendLine(runLogPath, `[${isoNow()}] Running Minecraft Velocity command flow through the remote proxy.`);
  const remoteRun = [
    `cd ${remoteQuote(options.remoteHeadlessDir)}`,
    `mkdir -p ${remoteQuote(remoteOutputDir)}`,
    `MINECRAFT_PROXY_COMMANDS=${remoteQuote(options.commandList)} MINECRAFT_PROXY_COMMAND_DELAY_MS=1000 node ${remoteQuote(remoteScriptPath)} 127.0.0.1 25565 ${remoteQuote(options.botUsername)} ${remoteQuote(options.minecraftVersion)} ${remoteQuote(remoteOutputDir)}`
  ].join(" && ");
  await invokeChecked(runLogPath, "ssh.exe", sshArgs(remoteRun), "Remote Minecraft Velocity command flow failed.");
  await invokeChecked(runLogPath, "scp.exe", scpArgs(`${options.sshAlias}:${remoteOutputDir}/scenario-result.json`, resultPath), "Failed to copy remote Minecraft Velocity result.");
  await invokeChecked(runLogPath, "scp.exe", scpArgs(`${options.sshAlias}:${remoteOutputDir}/transcript.txt`, transcriptPath), "Failed to copy remote Minecraft Velocity transcript.");

  await appendLine(runLogPath, `[${isoNow()}] Running Minecraft server command flow directly against kingdom backend.`);
  const remoteServerOutputDir = `${remoteOutputDir}-server`;
  const remoteServerRun = [
    `cd ${remoteQuote(options.remoteHeadlessDir)}`,
    `mkdir -p ${remoteQuote(remoteServerOutputDir)}`,
    `MINECRAFT_PROXY_COMMANDS=${remoteQuote(options.serverCommandList)} MINECRAFT_PROXY_COMMAND_DELAY_MS=1000 node ${remoteQuote(remoteScriptPath)} 127.0.0.1 ${options.kingdomBackendPort} 'DirectKingdomBot' ${remoteQuote(options.minecraftVersion)} ${remoteQuote(remoteServerOutputDir)}`
  ].join(" && ");
  await invokeChecked(runLogPath, "ssh.exe", sshArgs(remoteServerRun), "Remote Minecraft server command flow failed.");
  await invokeChecked(runLogPath, "scp.exe", scpArgs(`${options.sshAlias}:${remoteServerOutputDir}/scenario-result.json`, serverResultPath), "Failed to copy remote Minecraft server result.");
  await invokeChecked(runLogPath, "scp.exe", scpArgs(`${options.sshAlias}:${remoteServerOutputDir}/transcript.txt`, serverTranscriptPath), "Failed to copy remote Minecraft server transcript.");

  await appendLine(runLogPath, `[${isoNow()}] ResultFile=${resultPath}`);
  await appendLine(runLogPath, `[${isoNow()}] TranscriptFile=${transcriptPath}`);
  await appendLine(runLogPath, `[${isoNow()}] ServerResultFile=${serverResultPath}`);
  await appendLine(runLogPath, `[${isoNow()}] ServerTranscriptFile=${serverTranscriptPath}`);
}

main().catch((error: unknown) => {
  console.error(error instanceof Error ? error.stack ?? error.message : String(error));
  process.exitCode = 1;
});
