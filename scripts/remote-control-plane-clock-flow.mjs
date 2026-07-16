import path from "node:path";
import {
  delay,
  ensureBotBaseline,
  resolveBotClientModuleUrl,
  writeJson,
  printStructured,
  captureWorldSnapshot,
  waitForServerMessage
} from "./bot-flow-helpers.mjs";

async function sendCommandAndWait(bot, command, predicate, timeoutMs, label) {
  bot.chat(command);
  const message = await waitForServerMessage(bot, predicate, timeoutMs, label);
  return {
    command,
    message
  };
}

function includesAll(message, fragments) {
  const normalized = `${message}`.toLowerCase();
  return fragments.every((fragment) => normalized.includes(fragment.toLowerCase()));
}

async function main() {
  const clientModuleUrl = resolveBotClientModuleUrl();
  const { createBot } = await import(clientModuleUrl);

  const host = process.argv[2] ?? "127.0.0.1";
  const port = Number.parseInt(process.argv[3] ?? "5520", 10);
  const username = process.argv[4] ?? "ClockBot";
  const uuid = process.argv[5] ?? "523e4567-e89b-12d3-a456-426614174001";
  const outputDir = process.argv[6] ?? path.resolve(process.cwd(), ".runs", "control-plane-clock-flow");
  const resultPath = path.join(outputDir, "scenario-result.txt");
  const startedAt = new Date().toISOString();
  const assertions = [];
  const commandResults = [];

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

    commandResults.push(await sendCommandAndWait(
      bot,
      "/kd clock override kingdom-1 22:00",
      (message) => includesAll(message, ["Control command dispatched", "Time override set", "phase=NIGHT"]),
      12_000,
      "clock override routed through Hytale command"
    ));
    assertions.push("kd-clock-override-night-dispatched");

    commandResults.push(await sendCommandAndWait(
      bot,
      "/kd clock state kingdom-1",
      (message) => includesAll(message, ["Control command dispatched", "Clock state", "phase=NIGHT"]),
      12_000,
      "clock state routed through Hytale command"
    ));
    assertions.push("kd-clock-state-night-read");

    commandResults.push(await sendCommandAndWait(
      bot,
      "/kd clock projection kingdom-1 HYTALE",
      (message) => includesAll(message, ["Control command dispatched", "Clock projection refreshed for HYTALE"]),
      12_000,
      "clock projection routed through Hytale command"
    ));
    assertions.push("kd-clock-projection-hytale-refreshed");

    commandResults.push(await sendCommandAndWait(
      bot,
      "/kd schedule active kingdom-1",
      (message) => includesAll(message, ["Control command dispatched", "Active schedule rules"]),
      12_000,
      "schedule active routed through Hytale command"
    ));
    assertions.push("kd-schedule-active-read");

    commandResults.push(await sendCommandAndWait(
      bot,
      "/kd aging debug kingdom-1",
      (message) => includesAll(message, ["Control command dispatched", "Aging tick evaluated"]),
      12_000,
      "aging debug routed through Hytale command"
    ));
    assertions.push("kd-aging-debug-evaluated");

    commandResults.push(await sendCommandAndWait(
      bot,
      "/kd clock clear-override kingdom-1",
      (message) => includesAll(message, ["Control command dispatched", "Time override cleared"]),
      12_000,
      "clock clear override routed through Hytale command"
    ));
    assertions.push("kd-clock-clear-override-dispatched");

    const result = {
      name: "remote-control-plane-clock-flow",
      success: true,
      startedAt,
      endedAt: new Date().toISOString(),
      assertions,
      commandResults,
      clientSnapshot: {
        baseline: baseline.snapshot,
        final: captureWorldSnapshot(bot, 14)
      },
      finalServerMessage: bot.getServerMessages().at(-1) ?? null
    };
    await bot.trace.flush(outputDir);
    await writeJson(resultPath, result);
    printStructured(result);
  } catch (error) {
    const result = {
      name: "remote-control-plane-clock-flow",
      success: false,
      startedAt,
      endedAt: new Date().toISOString(),
      assertions,
      commandResults,
      error: error instanceof Error ? error.message : String(error),
      clientSnapshot: {
        final: captureWorldSnapshot(bot, 14)
      },
      finalServerMessage: bot.getServerMessages().at(-1) ?? null
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
