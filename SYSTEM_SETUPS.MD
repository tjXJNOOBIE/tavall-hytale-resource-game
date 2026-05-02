# System Languages
This section is to determine which system belongs to what language.

| Layer                       |       Shared? |  Home                             |
| --------------------------- | ------------: | ------------------------------------- |
| Auth/account state          |           Yes | Java control server                   |
| Guild/castle/resource state |           Yes | Java control server                   |
| Healing/trade/recon logic   |           Yes | Java control server                   |
| Asset IDs/projections       |           Yes | Java control server + shared DTO spec |
| Minecraft rendering/input   |            No | Java plugin/native adapter            |
| Hytale rendering/input      |            No | Hytale-native                         |
| Roblox rendering/input      |            No | Luau                                  |
| Discord UI                  |            No | Java bot/buttons                      |
| Android app UI              | Mostly unique | Kotlin                                |
| PC app UI/gameplay shell    | Mostly unique | C#                                    |

## Resource Game Frontend Modules

These modules live under the current `tavall-hytale-resource-game` folder and are registered from the `tavall-java-hytale-games` Maven parent.

| Module | Platform | Runtime | State ownership |
| --- | --- | --- | --- |
| `tavall-resource-game-shared-contracts` | Shared | Java | Adapter/projection contracts only |
| `tavall-resource-game-hytale-frontend` | Hytale | Java/Hytale-native | Adapter only |
| `tavall-resource-game-minecraft-frontend` | Minecraft | Java plugin/native adapter | Adapter only |
| `tavall-resource-game-roblox-frontend` | Roblox | Luau | Adapter only |
| `tavall-resource-game-discord-frontend` | Discord | Java bot/buttons | Adapter only |
| `tavall-resource-game-android-app` | Android future | Kotlin | Adapter/app shell only |
| `tavall-resource-game-pc-app` | PC app future | C# | Adapter/app shell only |

The existing root `tavall-hytale-resource-game` module remains the current middleware/control-server and Hytale plugin home until Hytale-specific runtime code can be moved safely. Canonical gameplay state stays in the Java control server; frontend modules consume projections and dispatch command/input events back to middleware.

The `tavall-resource-game-shared-contracts` module is the first extraction boundary. Java platform modules depend on it for frontend adapter descriptors, platform action descriptors, and shared action catalogs. Future projection/command DTOs should move there before platform-specific adapters consume them.
