import mineflayer from 'mineflayer'
import { createHash } from 'node:crypto'
import fs from 'node:fs/promises'
import path from 'node:path'
import util from 'node:util'
import { Vec3 } from 'vec3'

const host = process.argv[2] || process.env.MINECRAFT_PROXY_HOST || '127.0.0.1'
const port = Number(process.argv[3] || process.env.MINECRAFT_PROXY_PORT || 25565)
const username = process.argv[4] || process.env.MINECRAFT_PROXY_BOT_USERNAME || 'ResourceProxyBot'
const version = process.argv[5] || process.env.MINECRAFT_PROXY_VERSION || '1.21.4'
const outputDir = process.argv[6] || process.env.MINECRAFT_PROXY_OUTPUT_DIR || '/tmp/resource-game-minecraft-velocity-flow'
const commandDelayMs = Number(process.env.MINECRAFT_PROXY_COMMAND_DELAY_MS || 1000)
const commands = (process.env.MINECRAFT_PROXY_COMMANDS || '/kd clock state kingdom-1|/kd citizens summary kingdom-1')
  .split('|')
  .map(command => command.trim())
  .filter(Boolean)
const texturePackResults = {
  SUCCESSFULLY_LOADED: 0,
  DECLINED: 1,
  FAILED_DOWNLOAD: 2,
  ACCEPTED: 3
}

const transcript = []

function log(message) {
  const line = `[minecraft-velocity-flow] ${message}`
  transcript.push(line)
  console.log(line)
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

async function downloadResourcePack(url, outputDir) {
  const response = await fetch(url, { redirect: 'follow' })
  if (!response.ok) {
    throw new Error(`Resource pack download failed: status=${response.status} url=${url}`)
  }
  const bytes = Buffer.from(await response.arrayBuffer())
  if (bytes.length < 4 || bytes[0] !== 0x50 || bytes[1] !== 0x4b) {
    throw new Error(`Resource pack download did not return a zip archive: url=${url} bytes=${bytes.length}`)
  }
  const sha256 = createHash('sha256').update(bytes).digest('hex')
  const expectedEntries = [
    'assets/crownbound/items/ui/button_tab.json',
    'assets/crownbound/items/ui/button_icon.json',
    'assets/crownbound/models/item/ui/button_tab.json',
    'assets/crownbound/textures/item/ui/button_tab.png',
    'assets/crownbound/font/gui.json',
    'assets/crownbound/textures/font/kd_command_center.png',
    'assets/minecraft/textures/gui/container/generic_54.png',
    'assets/minecraft/textures/gui/container/inventory.png',
    'assets/minecraft/textures/gui/sprites/container/slot.png'
  ]
  for (const entry of expectedEntries) {
    if (!bytes.includes(Buffer.from(entry, 'utf8'))) {
      throw new Error(`Resource pack archive is missing expected entry marker: ${entry}`)
    }
  }
  const filePath = path.join(outputDir, 'downloaded-resource-pack.zip')
  await fs.writeFile(filePath, bytes)
  return {
    url,
    status: response.status,
    bytes: bytes.length,
    sha256,
    filePath
  }
}

function resolveResourcePackEvent(resourcePackFirstArg, resourcePackSecondArg, resourcePackThirdArg) {
  const firstText = normalizeMessage(resourcePackFirstArg).trim()
  const secondText = normalizeMessage(resourcePackSecondArg).trim()
  const thirdText = normalizeMessage(resourcePackThirdArg).trim()
  const firstLooksLikeUrl = /^https?:\/\//i.test(firstText)
  const secondLooksLikeUrl = /^https?:\/\//i.test(secondText)
  const uuidLikePattern = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i
  const secondLooksLikeUuid = uuidLikePattern.test(secondText)
  const firstLooksLikeUuid = uuidLikePattern.test(firstText)
  if (firstLooksLikeUrl) {
    return {
      url: firstText,
      token: secondText || thirdText || '[none]',
      tokenKind: secondLooksLikeUuid || firstLooksLikeUuid ? 'uuid' : 'hash'
    }
  }
  if (secondLooksLikeUrl) {
    return {
      url: secondText,
      token: firstText || thirdText || '[none]',
      tokenKind: firstLooksLikeUuid || secondLooksLikeUuid ? 'uuid' : 'hash'
    }
  }
  return {
    url: firstText || secondText || '[unknown]',
    token: secondText || thirdText || '[none]',
    tokenKind: 'hash'
  }
}

function writeResourcePackReceive(bot, tokenKind, token, result) {
  if (tokenKind === 'uuid' && bot.supportFeature('resourcePackUsesUUID')) {
    bot._client.write('resource_pack_receive', {
      uuid: token,
      result
    })
    return
  }
  if (tokenKind === 'hash' && bot.supportFeature('resourcePackUsesHash')) {
    bot._client.write('resource_pack_receive', {
      hash: token,
      result
    })
    return
  }
  bot._client.write('resource_pack_receive', {
    result
  })
}

function normalizeMessage(message) {
  if (typeof message === 'string') {
    return message
  }
  if (message == null) {
    return ''
  }
  if (Array.isArray(message)) {
    return message.map(entry => normalizeMessage(entry)).join('')
  }
  if (typeof message === 'number' || typeof message === 'boolean' || typeof message === 'bigint') {
    return String(message)
  }
  if (typeof message === 'object') {
    if (Object.prototype.hasOwnProperty.call(message, 'text')) {
      const nestedText = normalizeMessage(message.text)
      if (nestedText && nestedText !== '[object Object]') {
        return nestedText
      }
    }
    if (Object.prototype.hasOwnProperty.call(message, 'value')) {
      const nestedValue = normalizeMessage(message.value)
      if (nestedValue && nestedValue !== '[object Object]') {
        return nestedValue
      }
    }
    if (Object.prototype.hasOwnProperty.call(message, 'content')) {
      const nestedContent = normalizeMessage(message.content)
      if (nestedContent && nestedContent !== '[object Object]') {
        return nestedContent
      }
    }
    if (typeof message.translate === 'string') {
      return message.translate
    }
    if (Array.isArray(message.extra)) {
      const extraText = message.extra.map(entry => normalizeMessage(entry)).join('')
      if (extraText.trim()) {
        return extraText
      }
    }
  }
  if (message?.toString) {
    const text = message.toString()
    if (text && text !== '[object Object]') {
      return text
    }
  }
  try {
    const json = JSON.stringify(message)
    if (typeof json === 'string') {
      return json
    }
  } catch {
  }
  return String(message)
}

function normalizeWindowTitle(titleValue) {
  const title = normalizeMessage(titleValue).trim()
  return title || '[unknown window title]'
}

function summarizeWindowItem(item) {
  if (!item) {
    return ''
  }
  const itemModel = item.componentMap?.get?.('item_model')?.data ?? null
  const componentLore = Array.isArray(item.componentMap?.get?.('lore')?.data)
    ? item.componentMap.get('lore').data.map(entry => normalizeMessage(entry))
    : []
  const parts = [
    item.displayName ?? null,
    item.name ?? null,
    Array.isArray(item.lore) ? item.lore.join('\n') : null,
    componentLore.length > 0 ? componentLore.join('\n') : null,
    itemModel ? `item_model=${itemModel}` : null,
    item.nbt ? normalizeMessage(item.nbt) : null
  ]
  return parts.filter(Boolean).join('\n')
}

function assertWindowItemModel(item, expectedModel, label) {
  const summary = summarizeWindowItem(item)
  if (!summary.includes(expectedModel)) {
    throw new Error(`${label} missing expected item_model ${expectedModel}: ${summary || '[empty]'}`)
  }
  log(`${label} item_model ${expectedModel}`)
}

async function waitForWindowSlot(bot, slot, timeoutMs = 4000) {
  const startedAt = Date.now()
  while (Date.now() - startedAt < timeoutMs) {
    const item = bot.currentWindow?.slots?.[slot]
    if (item) {
      return item
    }
    await sleep(100)
  }
  return bot.currentWindow?.slots?.[slot] ?? null
}

async function waitForCurrentWindow(bot, timeoutMs = 4000) {
  const startedAt = Date.now()
  while (Date.now() - startedAt < timeoutMs) {
    if (bot.currentWindow) {
      return bot.currentWindow
    }
    await sleep(100)
  }
  return bot.currentWindow ?? null
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
      message.includes('Rank commands:') ||
        message.includes('Loaded ') ||
        message.includes('Updated ') ||
        message.includes('You have banned ') ||
        message.includes('You have warned ') ||
        message.includes('You have muted ') ||
        message.includes('You have unbanned ') ||
        message.includes('You have kicked ') ||
        message.includes('You were banned by ') ||
        message.includes('Simulation tooling is not enabled on this proxy.') ||
        message.includes('Rank command failed') ||
        message.includes('command.failed') ||
        message.includes('An unexpected error occurred trying to execute that command') ||
        message.includes('Connected to') ||
        message.includes('already connected') ||
      message.includes('Kingdom Commands') ||
      message.includes('Usage:') ||
      message.includes('Tavall Resource Game server surface') ||
      message.includes('Tavall Resource Game visual rendered') ||
      message.includes('Tavall Resource Game server interaction') ||
      message.includes('Resource-game server snapshot') ||
      message.includes('Kingdom:') ||
      message.includes('Kingdom command center') ||
      message.includes('NPC interaction') ||
      message.includes('Building interaction') ||
      message.includes('Companion') ||
      message.includes('Missing permission') ||
      message.includes('No permission.') ||
      message.includes('Unknown')
    )
    if (resultMessage) {
      return resultMessage
    }
    await sleep(250)
  }
  throw new Error(`Timed out waiting for response to ${command}`)
}

function isFailureMessage(message) {
  return message.includes('FAILED:') ||
    message.includes('REJECTED:') ||
    message.includes('Missing permission') ||
    message.includes('Unknown');
}

async function waitForWindowOpen(bot, command, titleFragment, timeoutMs = 15000) {
  const startedAt = Date.now()
  return await new Promise((resolve, reject) => {
    let resolved = false
    const timeout = setTimeout(() => {
      bot.removeListener('windowOpen', handler)
      clearInterval(poller)
      reject(new Error(`Timed out waiting for inventory window for ${command}`))
    }, timeoutMs)

    function handler(window) {
      if (resolved) {
        return
      }
      const title = normalizeWindowTitle(window?.title)
      const resolvedTitle = title === '[unknown window title]' && titleFragment ? titleFragment : title
      if (titleFragment && !resolvedTitle.toLowerCase().includes(titleFragment.toLowerCase())) {
        log(`window title mismatch ${title}`)
      }
      resolved = true
      clearTimeout(timeout)
      clearInterval(poller)
      bot.removeListener('windowOpen', handler)
      log(`window ${resolvedTitle}`)
      resolve(resolvedTitle)
    }

    const poller = setInterval(() => {
      if (resolved) {
        clearInterval(poller)
        return
      }
      const currentWindow = bot.currentWindow
      if (!currentWindow) {
        return
      }
      const title = normalizeWindowTitle(currentWindow.title)
      const resolvedTitle = title === '[unknown window title]' && titleFragment ? titleFragment : title
      if (titleFragment && !resolvedTitle.toLowerCase().includes(titleFragment.toLowerCase())) {
        log(`window title mismatch ${resolvedTitle}`)
      }
      resolved = true
      clearTimeout(timeout)
      clearInterval(poller)
      bot.removeListener('windowOpen', handler)
      log(`window ${resolvedTitle}`)
      resolve(resolvedTitle)
    }, 200)

    handler(bot.currentWindow)
    bot.on('windowOpen', handler)
  })
}

async function clickWindowSlot(bot, slot) {
  await new Promise((resolve) => {
    let settled = false
    const settle = () => {
      if (settled) {
        return
      }
      settled = true
      clearTimeout(timeout)
      resolve()
    }
    const timeout = setTimeout(() => {
      log(`click timeout slot ${slot}`)
      settle()
    }, 1000)

    const tryClick = () => {
      if (!bot.currentWindow) {
        return false
      }
      try {
        bot.clickWindow(slot, 0, 0, error => {
          if (error) {
            log(`click callback error slot ${slot}: ${error instanceof Error ? error.message : normalizeMessage(error)}`)
            settle()
            return
          }
          settle()
        })
        return true
      } catch (error) {
        log(`click throw slot ${slot}: ${error instanceof Error ? error.message : normalizeMessage(error)}`)
        settle()
        return true
      }
    }

    if (tryClick()) {
      return
    }

    const startedAt = Date.now()
    const poller = setInterval(() => {
      if (settled) {
        clearInterval(poller)
        return
      }
      if (tryClick()) {
        clearInterval(poller)
        return
      }
      if (Date.now() - startedAt > 1500) {
        clearInterval(poller)
        log(`click skipped no-open-window slot ${slot}`)
        settle()
      }
    }, 100)
  })
}

function captureCompanionId(messages) {
  for (let index = messages.length - 1; index >= 0; index -= 1) {
    const message = messages[index]
    const match = message.match(/(?:Companion created:|ref=|companion:)([0-9a-fA-F-]{36})/i)
    if (match && match[1]) {
      return match[1]
    }
  }
  return null
}

function captureInventoryItems(bot) {
  const slots = bot.inventory?.slots ?? []
  return slots
    .map((item, index) => item ? {
      slot: index,
      name: item.name,
      count: item.count,
      displayName: item.displayName ?? item.name
    } : null)
    .filter(Boolean)
}

function captureHotbarItems(bot) {
  const slots = bot.inventory?.slots ?? []
  return slots.slice(36, 45)
    .map((item, offset) => item ? {
      slot: 36 + offset,
      name: item.name,
      count: item.count,
      displayName: item.displayName ?? item.name
    } : null)
    .filter(Boolean)
}

function captureNearbyEntities(bot, radius = 16) {
  const origin = bot.entity?.position
  if (!origin) {
    return []
  }
  return Object.values(bot.entities ?? {})
    .filter(entity => entity && entity.position && entity.position.distanceTo(origin) <= radius)
    .map(entity => ({
      id: entity.id,
      name: String(entity.name ?? entity.mobType ?? entity.type ?? '').toLowerCase(),
      displayName: entity.displayName ?? null,
      position: entity.position
    }))
}

function captureBotState(bot) {
  return {
    position: bot.entity?.position ? bot.entity.position.clone() : null,
    inventory: captureInventoryItems(bot),
    hotbar: captureHotbarItems(bot),
    entities: captureNearbyEntities(bot, 24)
  }
}

function countItems(items, names) {
  const wanted = new Set(names.map(name => name.toLowerCase()))
  return items
    .filter(item => wanted.has(String(item.name).toLowerCase()))
    .reduce((sum, item) => sum + Number(item.count || 0), 0)
}

function countBlocksNear(bot, names, radius = 8) {
  const origin = bot.entity?.position?.floored()
  if (!origin) {
    return 0
  }
  const wanted = new Set(names.map(name => name.toLowerCase()))
  let count = 0
  for (let x = origin.x - radius; x <= origin.x + radius; x += 1) {
    for (let y = origin.y - 4; y <= origin.y + 8; y += 1) {
      for (let z = origin.z - radius; z <= origin.z + radius; z += 1) {
        const block = bot.blockAt(new Vec3(x, y, z))
        if (block && wanted.has(block.name.toLowerCase())) {
          count += 1
        }
      }
    }
  }
  return count
}

function hasNearbyEntity(bot, names, radius = 16) {
  const wanted = new Set(names.map(name => name.toLowerCase()))
  return captureNearbyEntities(bot, radius).some(entity => wanted.has(entity.name))
}

function distanceBetweenPositions(left, right) {
  if (!left || !right) {
    return Number.POSITIVE_INFINITY
  }
  const dx = left.x - right.x
  const dy = left.y - right.y
  const dz = left.z - right.z
  return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz))
}

async function waitForWorldEffect(bot, command, beforeState) {
  const normalized = command.toLowerCase()
  const relevant =
    normalized.startsWith('/kd ui') ||
    normalized.startsWith('/kd npc') ||
    normalized.startsWith('/kd building') ||
    normalized.includes('/kd place castle') ||
    normalized.includes('/kd buildings stage') ||
    normalized.includes('/kd buildings place') ||
    normalized.includes('/kd hologram spawn') ||
    normalized.includes('/kd entity spawn') ||
    normalized.includes('/kd companion give') ||
    normalized.includes('/kd companion summon') ||
    normalized.includes('/kd interior') ||
    normalized.includes('/kd resources ') ||
    normalized.includes('/kd citizens ') ||
    normalized.includes('/kd troops ')
  if (!relevant) {
    return null
  }
  const startedAt = Date.now()
  const timeoutMs = 12000
  while (Date.now() - startedAt < timeoutMs) {
    const current = captureBotState(bot)
    if (normalized.startsWith('/kd ui') || normalized.startsWith('/kd npc') || normalized.startsWith('/kd building')) {
      if (bot.currentWindow) {
        return 'ui-open'
      }
    } else if (normalized.includes('/kd place castle')) {
      if (countBlocksNear(bot, ['beacon', 'stone_bricks', 'cobblestone', 'glowstone'], 10) > 0) {
        return 'castle-placed'
      }
    } else if (normalized.includes('/kd buildings stage') || normalized.includes('/kd buildings place')) {
      if (countBlocksNear(bot, ['crafting_table', 'bricks', 'iron_block', 'campfire', 'loom', 'hay_block', 'stone_bricks'], 10) > 0) {
        return 'building-staged'
      }
    } else if (normalized.includes('/kd hologram spawn') || normalized.includes('/kd entity spawn') || normalized.includes('/kd companion give') || normalized.includes('/kd companion summon')) {
      if (hasNearbyEntity(bot, ['armor_stand', 'villager', 'allay'], 24) || current.entities.length > beforeState.entities.length) {
        return 'entity-spawned'
      }
    } else if (normalized.includes('/kd interior')) {
      if (distanceBetweenPositions(current.position, beforeState.position) > 2.5) {
        return 'teleported'
      }
      if (countBlocksNear(bot, ['smooth_stone', 'sea_lantern', 'oak_door'], 8) > 0) {
        return 'interior-built'
      }
    } else if (normalized.includes('/kd resources ')) {
      const currentItems = countItems(current.inventory, ['apple', 'oak_log', 'iron_ingot', 'emerald', 'gold_ingot'])
      const beforeItems = countItems(beforeState.inventory, ['apple', 'oak_log', 'iron_ingot', 'emerald', 'gold_ingot'])
      if (currentItems > beforeItems) {
        return 'inventory-updated'
      }
      if (current.hotbar.length > 0) {
        return 'hotbar-updated'
      }
    } else if (normalized.includes('/kd citizens ') || normalized.includes('/kd troops ')) {
      if (hasNearbyEntity(bot, ['villager', 'iron_golem'], 24)) {
        return 'citizen-entity-updated'
      }
    }
    await sleep(250)
  }
  throw new Error(`Timed out waiting for world effect after ${command}`)
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

bot.on('kicked', reason => {
  log(`kicked ${normalizeMessage(reason)}`)
})

bot.on('end', reason => {
  log(`ended ${normalizeMessage(reason)}`)
})

bot.on('error', error => {
  log(`error ${error.message}`)
})

bot.on('death', () => {
  log('death event received')
  setTimeout(() => {
    try {
      if (bot._client?.write) {
        bot._client.write('client_command', { actionId: 0 })
        log('respawn requested')
      }
    } catch (error) {
      log(`respawn error ${error instanceof Error ? error.message : normalizeMessage(error)}`)
    }
  }, 750)
})

bot.on('spawn', () => {
  log('spawn event received')
})

const resourcePackPromise = new Promise((resolve, reject) => {
  const timeout = setTimeout(() => reject(new Error('Timed out waiting for Minecraft resource pack request')), 30000)
  bot.once('resourcePack', (url, hashOrUuid, uuidMaybe) => {
    ;(async () => {
      clearTimeout(timeout)
      const resourcePack = resolveResourcePackEvent(url, hashOrUuid, uuidMaybe)
      log(`resource-pack requested url=${resourcePack.url} token=${resourcePack.token} tokenKind=${resourcePack.tokenKind}`)
      try {
        writeResourcePackReceive(bot, resourcePack.tokenKind, resourcePack.token, texturePackResults.ACCEPTED)
        log('resource-pack accepted')
      } catch (error) {
        reject(error)
        return
      }
      const download = await downloadResourcePack(resourcePack.url, outputDir)
      try {
        writeResourcePackReceive(bot, resourcePack.tokenKind, resourcePack.token, texturePackResults.SUCCESSFULLY_LOADED)
      } catch (error) {
        reject(error)
        return
      }
      log(`resource-pack downloaded status=${download.status} bytes=${download.bytes} sha256=${download.sha256}`)
      resolve({ url, hashOrUuid, uuidMaybe, ...download })
    })().catch(reject)
  })
  bot.once('kicked', reason => {
    clearTimeout(timeout)
    reject(new Error(`Kicked before resource pack request: ${normalizeMessage(reason)}`))
  })
  bot.once('error', error => {
    clearTimeout(timeout)
    reject(error)
  })
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
  await resourcePackPromise

  await sleep(2000)
  try {
    bot.chat('/gamemode creative')
    await sleep(1500)
  } catch (error) {
    log(`safety gamemode command failed ${error instanceof Error ? error.message : normalizeMessage(error)}`)
  }
  const commandResults = []
  let companionId = null
  for (const command of commands) {
    const expectFailure = command.trim().startsWith('!')
    const prepared = command.replaceAll('{companionId}', companionId ?? '{companionId}').replace(/^!+/, '')
    log(`send ${prepared}`)
    const beforeState = captureBotState(bot)
    const normalizedPrepared = prepared.toLowerCase()
    const uiCommand = normalizedPrepared.startsWith('/kd ui')
      || normalizedPrepared.startsWith('/kd npc')
      || normalizedPrepared.startsWith('/kd building')
    const uiWindowPromise = normalizedPrepared.startsWith('/kd ui') || normalizedPrepared.startsWith('/kd npc') || normalizedPrepared.startsWith('/kd building')
      ? waitForWindowOpen(
          bot,
          prepared,
          normalizedPrepared.startsWith('/kd npc') ? 'NPC Interaction' : normalizedPrepared.startsWith('/kd building') ? 'Building Detail' : 'Kingdom Command Center'
        ).catch(error => {
        log(`ui window error ${error instanceof Error ? error.message : normalizeMessage(error)}`)
        return null
      })
      : null
    const commandResponsePromise = waitForCommandResponse(messages, prepared)
    bot.chat(prepared)
    let response = ''
    if (uiWindowPromise) {
      const safeCommandResponsePromise = commandResponsePromise.catch(error => {
        log(`command response error ${error instanceof Error ? error.message : normalizeMessage(error)}`)
        return ''
      })
      response = await Promise.race([uiWindowPromise, safeCommandResponsePromise])
      if (normalizedPrepared.startsWith('/kd ui')) {
        const currentWindow = await waitForCurrentWindow(bot)
        if (!currentWindow) {
          throw new Error('Resource-pack-backed UI window did not stay open long enough for inspection.')
        }
        log(`ui window shape inventoryStart=${currentWindow.inventoryStart ?? '[unknown]'} slots=${currentWindow.slots?.length ?? '[unknown]'} type=${currentWindow.type ?? '[unknown]'}`)
        if (currentWindow.inventoryStart !== 54) {
          throw new Error(`Resource-pack-backed UI window is not a 54-slot chest layout: inventoryStart=${currentWindow.inventoryStart ?? '[unknown]'} slots=${currentWindow.slots?.length ?? '[unknown]'}`)
        }
        const headerItem = await waitForWindowSlot(bot, 4)
        const accountItem = await waitForWindowSlot(bot, 10)
        log(`ui slot4 raw ${util.inspect(headerItem, { depth: 5, breakLength: 160 })}`)
        log(`ui slot10 raw ${util.inspect(accountItem, { depth: 5, breakLength: 160 })}`)
        const headerSummary = summarizeWindowItem(headerItem)
        if (!headerSummary.includes('Command Center') && !headerSummary.includes('Nether Star')) {
          throw new Error(`Resource-pack-backed UI header looked wrong: ${headerSummary || '[empty]'}`)
        }
        log(`resource-pack header ${headerSummary.replace(/\s+/g, ' ').trim()}`)
        const accountSummary = summarizeWindowItem(accountItem)
        log(`ui account summary ${accountSummary.replace(/\s+/g, ' ').trim()}`)
        assertWindowItemModel(headerItem, 'crownbound:ui/button_icon', 'ui header')
        assertWindowItemModel(accountItem, 'crownbound:ui/button_tab', 'ui account button')
      }
      try {
        const clickSlot = normalizedPrepared.startsWith('/kd npc') ? 12 : normalizedPrepared.startsWith('/kd building') ? 13 : 10
        await clickWindowSlot(bot, clickSlot)
        log(`clicked slot ${clickSlot}`)
        await sleep(750)
        if (bot.currentWindow) {
          const currentTitle = normalizeWindowTitle(bot.currentWindow.title)
          log(`current window after click ${currentTitle}`)
          try {
            bot.closeWindow(bot.currentWindow)
          } catch (error) {
            log(`close-window-after-click error ${error instanceof Error ? error.message : normalizeMessage(error)}`)
          }
        }
      } catch (error) {
        log(`ui click error ${error instanceof Error ? error.message : normalizeMessage(error)}`)
      }
    } else {
      response = await commandResponsePromise
    }
    if (!uiCommand) {
      try {
        const effect = await waitForWorldEffect(bot, prepared, beforeState)
        if (effect) {
          log(`world-effect ${effect}`)
        }
      } catch (error) {
        log(`world-effect error ${error instanceof Error ? error.message : normalizeMessage(error)}`)
        throw error
      }
    }
    commandResults.push({ command: prepared, response, expectFailure })
    if (companionId == null) {
      companionId = captureCompanionId(messages)
      if (companionId) {
        log(`captured companionId ${companionId}`)
      }
    }
    await sleep(commandDelayMs)
  }

  const failed = commandResults.filter(result => {
    const failureMessage = isFailureMessage(result.response) || result.response.includes('submitted=false')
    if (result.expectFailure) {
      return !failureMessage
    }
    return failureMessage
  })
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
} catch (error) {
  const failure = {
    success: false,
    host,
    port,
    username,
    version,
    error: error instanceof Error ? error.message : normalizeMessage(error),
    messages
  }
  await fs.writeFile(path.join(outputDir, 'scenario-result.json'), JSON.stringify(failure, null, 2))
  await fs.writeFile(path.join(outputDir, 'transcript.txt'), transcript.join('\n') + '\n')
  throw error
} finally {
  bot.quit()
}
