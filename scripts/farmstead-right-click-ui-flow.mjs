import path from "node:path";
import {
  aimAtPosition,
  captureWorldSnapshot,
  createTraceSession,
  delay,
  ensureBotBaseline,
  resolveBotClientModuleUrl,
  waitForWorldSnapshot,
  walkToApproxPosition,
  writeJson,
  printStructured
} from "./bot-flow-helpers.mjs";

const FARMSTEAD_PAGE = "com.tavall.resourcegame.ui.FarmsteadMenuPage";
const EXPECTED_OPTIONS = ["Crops", "Storage", "Workers", "Upgrade", "Close"];

function readSelectorValue(snapshot, selector) {
  const command = snapshot?.commands?.slice().reverse().find((entry) => entry.type === "Set" && entry.selector === selector);
  if (!command) {
    return null;
  }
  if (command.text != null) {
    return command.text;
  }
  try {
    const parsed = JSON.parse(command.data);
    return parsed?.[0] ?? null;
  } catch {
    return null;
  }
}

function visibleTextValues(snapshot) {
  const values = [];
  for (const command of snapshot?.commands ?? []) {
    if (command.type !== "Set") {
      continue;
    }
    if (typeof command.text === "string") {
      values.push(command.text);
      continue;
    }
    if (typeof command.data !== "string") {
      continue;
    }
    try {
      const parsed = JSON.parse(command.data);
      if (typeof parsed?.[0] === "string") {
        values.push(parsed[0]);
      }
    } catch {
    }
  }
  return values;
}

function nearestNewEntity(snapshot, beforeEntityIds) {
  return (snapshot?.nearbyEntities ?? [])
    .filter((entity) => entity?.id != null && !beforeEntityIds.has(entity.id) && entity.position)
    .sort((left, right) => left.distance - right.distance)[0] ?? null;
}

function parseServerPosition(message) {
  const match = message?.match(/(?:\|\s*pos| at)\s+(-?\d+(?:\.\d+)?)\s+(-?\d+(?:\.\d+)?)\s+(-?\d+(?:\.\d+)?)/);
  if (!match) {
    return null;
  }
  return {
    x: Number.parseFloat(match[1]),
    y: Number.parseFloat(match[2]),
    z: Number.parseFloat(match[3])
  };
}

function parseServerUuid(message) {
  const match = message?.match(/\buuid\s+([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})\b/);
  return match?.[1]?.toLowerCase() ?? null;
}

async function waitForNewServerMessage(bot, previousCount, predicate, timeoutMs, label) {
  const startedAt = Date.now();
  while ((Date.now() - startedAt) < timeoutMs) {
    const messages = bot.getServerMessages();
    for (const message of messages.slice(previousCount)) {
      if (predicate(message)) {
        return message;
      }
    }
    await delay(100);
  }
  throw new Error(`Timed out waiting for ${label}`);
}

async function scanPlayerPosition(bot) {
  const previousCount = bot.getServerMessages().length;
  bot.chat("/kd scan");
  const message = await waitForNewServerMessage(
    bot,
    previousCount,
    (entry) => entry.includes("World:") && entry.includes("| pos "),
    8_000,
    "player scan position"
  );
  const position = parseServerPosition(message);
  if (!position) {
    throw new Error(`Could not parse player position from scan message: ${message}`);
  }
  return position;
}

function nonSelfEntities(bot) {
  const clientId = typeof bot.getClientId === "function" ? bot.getClientId() : bot.clientId;
  return [...(bot.world?.entities?.values?.() ?? [])].filter((entity) => entity?.id != null && entity.id !== clientId);
}

async function waitForNewEntity(bot, beforeEntityIds, timeoutMs) {
  const startedAt = Date.now();
  while ((Date.now() - startedAt) < timeoutMs) {
    const entity = nonSelfEntities(bot).find((entry) => !beforeEntityIds.has(entry.id));
    if (entity) {
      return entity;
    }
    await delay(100);
  }
  throw new Error(`Farmstead Steward entity was not received by the bot. knownEntities=${JSON.stringify(nonSelfEntities(bot).map((entry) => entry.id))}`);
}

function positionNearTarget(playerPosition, targetPosition, distance = 1.35) {
  const dx = playerPosition.x - targetPosition.x;
  const dz = playerPosition.z - targetPosition.z;
  const horizontalDistance = Math.sqrt((dx * dx) + (dz * dz));
  if (horizontalDistance < 0.001) {
    return { x: targetPosition.x, y: targetPosition.y, z: targetPosition.z - distance };
  }
  return {
    x: targetPosition.x + (dx / horizontalDistance) * distance,
    y: playerPosition.y,
    z: targetPosition.z + (dz / horizontalDistance) * distance
  };
}

function summarizePage(snapshot) {
  return {
    key: snapshot?.key ?? null,
    title: readSelectorValue(snapshot, "#FarmsteadTitle.Text"),
    visibleText: visibleTextValues(snapshot),
    selectors: snapshot?.selectors ?? []
  };
}

async function main() {
  const clientModuleUrl = resolveBotClientModuleUrl();
  const { createBot } = await import(clientModuleUrl);

  const host = process.argv[2] ?? "127.0.0.1";
  const port = Number.parseInt(process.argv[3] ?? "5520", 10);
  const username = process.argv[4] ?? "FarmsteadBot";
  const uuid = process.argv[5] ?? "b23e4567-e89b-12d3-a456-426614174000";
  const outputDir = process.argv[6] ?? path.resolve(process.cwd(), "build", "test-results", "farmstead-right-click-ui");
  const resultPath = path.join(outputDir, "scenario-result.txt");
  const startedAt = new Date().toISOString();
  const assertions = [];
  const diagnostics = {};

  const bot = await createBot({
    host,
    port,
    username,
    uuid,
    autoConnect: true,
    autoAcknowledgePages: true
  });
  const trace = createTraceSession(bot, outputDir);

  try {
    await trace.enable();
    await ensureBotBaseline(bot, assertions, { username, nearbyRadius: 16, settleDelayMs: 1500 });
    const scannedPosition = await scanPlayerPosition(bot);
    if (typeof bot.assumePosition !== "function") {
      throw new Error("Bot client does not expose assumePosition; cannot seed decoded player position from /kd scan.");
    }
    bot.assumePosition(scannedPosition);
    await delay(250);
    const baselineSnapshot = captureWorldSnapshot(bot, 16);
    diagnostics.baselineSnapshot = baselineSnapshot;
    diagnostics.scannedPosition = scannedPosition;
    assertions.push("bot-joined");

    bot.chat("/kd entity clear");
    await delay(500);
    bot.look(0, 0, 0);
    await delay(500);
    const beforeEntityIds = new Set(nonSelfEntities(bot).map((entity) => entity.id));

    const previousMessageCount = bot.getServerMessages().length;
    bot.chat("/kd entity spawn steward");
    assertions.push("spawn-command-sent");
    const spawnMessage = await waitForNewServerMessage(
      bot,
      previousMessageCount,
      (message) => message.includes("Spawned Farmstead Steward"),
      8_000,
      "Farmstead Steward spawn confirmation"
    );
    const stewardPosition = parseServerPosition(spawnMessage);
    if (!stewardPosition) {
      throw new Error(`Could not parse Farmstead Steward position from spawn message: ${spawnMessage}`);
    }
    const stewardUuid = parseServerUuid(spawnMessage);
    if (!stewardUuid) {
      throw new Error(`Could not parse Farmstead Steward UUID from spawn message: ${spawnMessage}`);
    }
    diagnostics.spawnMessage = spawnMessage;
    diagnostics.stewardPosition = stewardPosition;
    diagnostics.stewardUuid = stewardUuid;
    assertions.push("farmstead-steward-spawned");

    const stewardEntity = await waitForNewEntity(bot, beforeEntityIds, 10_000);
    const steward = {
      id: stewardEntity.id,
      position: stewardEntity.position ?? stewardPosition
    };
    if (!steward.id || !steward.position) {
      throw new Error(`Farmstead Steward entity was not found after spawn. entity=${JSON.stringify(stewardEntity)}`);
    }
    diagnostics.steward = steward;

    const currentPosition = captureWorldSnapshot(bot, 8).position ?? scannedPosition;
    if (!currentPosition) {
      throw new Error("Bot cannot move near Farmstead Steward because current position is missing.");
    }
    const targetPosition = positionNearTarget(currentPosition, steward.position);
    const movedSnapshot = await walkToApproxPosition(bot, targetPosition, { stepSize: 0.75, maxSteps: 12, settleDelayMs: 120 });
    if (!movedSnapshot?.position) {
      throw new Error("Bot movement toward Farmstead Steward did not produce a position update.");
    }
    assertions.push("bot-moved-into-range");

    await aimAtPosition(bot, { x: steward.position.x, y: steward.position.y + 1.25, z: steward.position.z }, 350);
    assertions.push("bot-aimed-at-steward");

    if (typeof bot.rightClickEntity !== "function") {
      throw new Error("Bot client does not expose rightClickEntity; MouseInteraction Secondary input cannot be sent.");
    }
    bot.rightClickEntity(steward.id, {
      targetEntityUuid: stewardUuid,
      hitLocation: { x: steward.position.x, y: steward.position.y + 1.0, z: steward.position.z }
    });
    assertions.push("bot-sent-secondary-input");

    const page = await bot.waitForPage(FARMSTEAD_PAGE, 12_000);
    if (page.key !== FARMSTEAD_PAGE) {
      throw new Error(`Wrong UI opened. expected=${FARMSTEAD_PAGE} actual=${page.key}`);
    }
    const snapshot = bot.snapshotPage();
    diagnostics.pageSnapshot = summarizePage(snapshot);
    const title = readSelectorValue(snapshot, "#FarmsteadTitle.Text");
    if (title !== "Farmstead") {
      throw new Error(`Farmstead UI opened with wrong title. expected=Farmstead actual=${title}`);
    }
    assertions.push("farmstead-ui-title-verified");

    const visibleText = visibleTextValues(snapshot);
    for (const option of EXPECTED_OPTIONS) {
      if (!visibleText.includes(option)) {
        throw new Error(`Farmstead UI missing visible option ${option}. visibleText=${JSON.stringify(visibleText)} selectors=${JSON.stringify(snapshot?.selectors ?? [])}`);
      }
    }
    assertions.push("farmstead-ui-options-verified");

    const result = {
      name: "FarmsteadStewardRightClickMenuBotTest",
      success: true,
      startedAt,
      endedAt: new Date().toISOString(),
      assertions,
      diagnostics,
      finalServerMessage: bot.getServerMessages().at(-1) ?? null,
      outputDir
    };
    await trace.flush();
    await writeJson(resultPath, result);
    printStructured(result);
  } catch (error) {
    const result = {
      name: "FarmsteadStewardRightClickMenuBotTest",
      success: false,
      startedAt,
      endedAt: new Date().toISOString(),
      assertions,
      diagnostics,
      error: error instanceof Error ? error.message : String(error),
      finalPageSnapshot: summarizePage(bot.snapshotPage()),
      finalWorldSnapshot: captureWorldSnapshot(bot, 12),
      finalServerMessage: bot.getServerMessages().at(-1) ?? null,
      outputDir
    };
    try {
      await trace.flush();
      await writeJson(resultPath, result);
    } catch {
    }
    printStructured(result, true);
    process.exitCode = 1;
  } finally {
    await bot.disconnect();
  }
}

await main();
