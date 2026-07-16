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
| `game-api` | Shared | Java | Adapter/projection contracts only |
| `control-server` | Backend/control plane | Java | Canonical gameplay, cloud/control console, panel, persistence, and transport |
| `minecraft-proxy` | Minecraft | Java plugin/native adapter | Adapter only |
| `discord-frontend` | Discord | Java bot/buttons | Adapter only |
| `android-app` | Android future | Kotlin | Adapter/app shell only |

On `minecraft-main`, non-Minecraft platform modules are intentionally omitted from the branch tree. Canonical gameplay state stays in the Java control server; frontend modules consume projections and dispatch command/input events back to middleware.

The `game-api` module is the first extraction boundary. Java platform modules depend on it for frontend adapter descriptors, platform action descriptors, and shared action catalogs. Future projection/command DTOs should move there before platform-specific adapters consume them.
