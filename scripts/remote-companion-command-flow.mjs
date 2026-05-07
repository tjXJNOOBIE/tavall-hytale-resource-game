import path from "node:path";
import {
  delay,
  ensureBotBaseline,
  resolveBotClientModuleUrl,
  writeJson,
  printStructured,
  captureWorldSnapshot
} from "./bot-flow-helpers.mjs";

function isBotConnected(bot) {
  return typeof bot.isConnected === "function" ? bot.isConnected() : true;
}

function latestServerMessage(bot) {
  return bot.getServerMessages().at(-1) ?? null;
}

async function chatUntilMessage(bot, command, predicate, timeoutMs, label) {
  const startedAt = Date.now();
  let lastSendError = null;
  while ((Date.now() - startedAt) < timeoutMs) {
    if (!isBotConnected(bot)) {
      break;
    }
    try {
      bot.chat(command);
    } catch (error) {
      lastSendError = error instanceof Error ? error.message : String(error);
      break;
    }

    const retryUntil = Date.now() + 1_500;
    while ((Date.now() - startedAt) < timeoutMs && Date.now() < retryUntil) {
      const message = latestServerMessage(bot);
      if (message && predicate(message)) {
        return message;
      }
      await delay(150);
    }
  }

  throw new Error(`Timed out waiting for ${label}; finalServerMessage=${latestServerMessage(bot)}; lastSendError=${lastSendError}`);
}

function extractCompanionId(message) {
  const match = /Companion created:\s*([0-9a-fA-F-]{36})/.exec(message);
  if (!match) {
    throw new Error(`Unable to extract companion id from message: ${message}`);
  }
  return match[1];
}

async function main() {
  const clientModuleUrl = resolveBotClientModuleUrl();
  const { createBot } = await import(clientModuleUrl);

  const host = process.argv[2] ?? "127.0.0.1";
  const port = Number.parseInt(process.argv[3] ?? "5520", 10);
  const username = process.argv[4] ?? "CompanionBot";
  const uuid = process.argv[5] ?? "723e4567-e89b-12d3-a456-426614174000";
  const outputDir = process.argv[6] ?? path.resolve(process.cwd(), ".runs", "companion-command-flow");
  const resultPath = path.join(outputDir, "scenario-result.txt");
  const startedAt = new Date().toISOString();
  const assertions = [];
  const messages = [];

  const bot = await createBot({
    host,
    port,
    username,
    uuid,
    autoConnect: true,
    autoAcknowledgePages: true
  });

  try {
    await bot.trace.enable({ outputDir });
    const baseline = await ensureBotBaseline(bot, assertions, {
      username,
      nearbyRadius: 14
    });
    await delay(1_500);

    const createMessage = await chatUntilMessage(
      bot,
      `/kd companion give ${uuid} BRUTE`,
      (message) => message.includes("Companion created:") && message.includes("type=BRUTE"),
      18_000,
      "companion creation response"
    );
    messages.push(createMessage);
    assertions.push("companion-created-through-hytale-kd");

    const companionId = extractCompanionId(createMessage);

    const listMessage = await chatUntilMessage(
      bot,
      `/kd companion list ${uuid}`,
      (message) => message.includes("Companions: 1"),
      12_000,
      "companion list response"
    );
    messages.push(listMessage);
    assertions.push("companion-listed-through-hytale-kd");

    const levelMessage = await chatUntilMessage(
      bot,
      `/kd companion setlevel ${companionId} 10`,
      (message) => message.includes("Companion level set: 10"),
      12_000,
      "companion level response"
    );
    messages.push(levelMessage);
    assertions.push("companion-level-set");

    const moraleMessage = await chatUntilMessage(
      bot,
      `/kd companion morale ${companionId} HIGH`,
      (message) => message.includes("Companion morale updated: HIGH"),
      12_000,
      "companion morale response"
    );
    messages.push(moraleMessage);
    assertions.push("companion-morale-set");

    const behaviorMessage = await chatUntilMessage(
      bot,
      `/kd companion behavior ${companionId} FOLLOWING`,
      (message) => message.includes("Companion behavior updated: FOLLOWING"),
      12_000,
      "companion behavior response"
    );
    messages.push(behaviorMessage);
    assertions.push("companion-behavior-set");

    const skillMessage = await chatUntilMessage(
      bot,
      `/kd companion skill upgrade ${uuid} ${companionId} stone-guard`,
      (message) => message.includes("Companion skill upgraded: Stone Guard"),
      12_000,
      "companion skill upgrade response"
    );
    messages.push(skillMessage);
    assertions.push("companion-wisdom-upgrade");

    const wallMessage = await chatUntilMessage(
      bot,
      `/kd companion wall assign ${uuid} ${companionId} north`,
      (message) => message.includes("Companion assigned to wall: north"),
      12_000,
      "companion wall assignment response"
    );
    messages.push(wallMessage);
    assertions.push("companion-wall-assigned");

    const result = {
      name: "remote-companion-command-flow",
      success: true,
      startedAt,
      endedAt: new Date().toISOString(),
      assertions,
      messages,
      companionId,
      clientSnapshot: {
        baseline: baseline.snapshot,
        final: captureWorldSnapshot(bot, 14)
      },
      finalServerMessage: latestServerMessage(bot)
    };
    await bot.trace.flush(outputDir);
    await writeJson(resultPath, result);
    printStructured(result);
  } catch (error) {
    const result = {
      name: "remote-companion-command-flow",
      success: false,
      startedAt,
      endedAt: new Date().toISOString(),
      assertions,
      messages,
      error: error instanceof Error ? error.message : String(error),
      clientSnapshot: {
        final: captureWorldSnapshot(bot, 14)
      },
      finalServerMessage: latestServerMessage(bot)
    };
    try {
      await bot.trace.flush(outputDir);
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
