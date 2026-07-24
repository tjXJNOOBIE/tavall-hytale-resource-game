plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "tavall-hytale-resource-game"

include(
    "game-api",
    "minecraft-framework",
    "minecraft-framework:minecraft-backend-api",
    "control-server",
    "minecraft-game-server",
    "minecraft-proxy",
)
