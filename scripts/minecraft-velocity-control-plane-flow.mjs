import mineflayer from 'mineflayer'
import fs from 'node:fs/promises'
import path from 'node:path'

const host = process.argv[2] || process.env.MINECRAFT_PROXY_HOST || '127.0.0.1'
const port = Number(process.argv[3] || process.env.MINECRAFT_PROXY_PORT || 25565)
const username = process.argv[4] || process.env.MINECRAFT_PROXY_BOT_USERNAME || 'ResourceProxyBot'
const version = process.argv[5] || process.env.MINECRAFT_PROXY_VERSION || '1.21.4'
const outputDir = process.argv[6] || process.env.MINECRAFT_PROXY_OUTPUT_DIR || '/tmp/resource-game-minecraft-velocity-flow'
const commands = (process.env.MINECRAFT_PROXY_COMMANDS || '/kd clock state kingdom-1|/kd citizens summary kingdom-1')
  .split('|')
  .map(command => command.trim())
  .filter(Boolean)

const transcript = []

function log(message) {
  const line = `[minecraft-velocity-flow] ${message}`
  transcript.push(line)
  console.log(line)
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

function normalizeMessage(message) {
  if (typeof message === 'string') {
    return message
  }
  if (message?.toString) {
    return message.toString()
  }
  return JSON.stringify(message)
}

async function waitForCommandResponse(messages, command, timeoutMs = 15000) {
  const startedLength = messages.length
  const startedAt = Date.now()
  while (Date.now() - startedAt < timeoutMs) {
    const newMessages = messages.slice(startedLength)
    const resultMessage = newMessages.find(message =>
      message.includes('COMPLETED:') ||
      message.includes('FAILED:') ||
      message.includes('REJECTED:') ||
      message.includes('DISPATCHED:') ||
      message.includes('Missing permission') ||
      message.includes('Unknown')
    )
    if (resultMessage) {
      return resultMessage
    }
    await sleep(250)
  }
  throw new Error(`Timed out waiting for response to ${command}`)
}

await fs.mkdir(outputDir, { recursive: true })
const messages = []
const bot = mineflayer.createBot({
  host,
  port,
  username,
  version,
  auth: 'offline'
})

bot.on('messagestr', message => {
  messages.push(message)
  log(`chat ${message}`)
})

bot.on('message', message => {
  const normalized = normalizeMessage(message)
  if (!messages.includes(normalized)) {
    messages.push(normalized)
    log(`message ${normalized}`)
  }
})

try {
  await new Promise((resolve, reject) => {
    const timeout = setTimeout(() => reject(new Error('Timed out waiting for Minecraft proxy login')), 30000)
    bot.once('login', () => {
      clearTimeout(timeout)
      log(`logged in as ${username}`)
      resolve()
    })
    bot.once('kicked', reason => {
      clearTimeout(timeout)
      reject(new Error(`Kicked before verification: ${normalizeMessage(reason)}`))
    })
    bot.once('error', error => {
      clearTimeout(timeout)
      reject(error)
    })
  })

  await sleep(2000)
  const commandResults = []
  for (const command of commands) {
    log(`send ${command}`)
    bot.chat(command)
    const response = await waitForCommandResponse(messages, command)
    commandResults.push({ command, response })
  }

  const failed = commandResults.filter(result =>
    result.response.includes('FAILED:') ||
    result.response.includes('REJECTED:') ||
    result.response.includes('Missing permission') ||
    result.response.includes('Unknown')
  )
  const summary = {
    success: failed.length === 0,
    host,
    port,
    username,
    version,
    commandResults
  }
  await fs.writeFile(path.join(outputDir, 'scenario-result.json'), JSON.stringify(summary, null, 2))
  await fs.writeFile(path.join(outputDir, 'transcript.txt'), transcript.join('\n') + '\n')
  if (failed.length > 0) {
    throw new Error(`Velocity command verification failed: ${JSON.stringify(failed)}`)
  }
  log('success')
} finally {
  bot.quit()
}
