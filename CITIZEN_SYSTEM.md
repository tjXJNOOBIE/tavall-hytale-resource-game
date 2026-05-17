# Citizen System

## Architecture
The canonical Citizen System lives in the plain Java control plane. Spring remains an optional admin surface, and game frontends consume projections or submit command envelopes.

- Canonical records live under `com.tavall.resourcegame.middleware.citizen`.
- The existing project job list is reused exactly from `CitizenJobType`: `IDLE`, `GATHERER`, `HUNTER`, `COOK`, `MINER`, `BLACKSMITH`, `ARCHITECT`, `GRUNT_BUILDER`, legacy `BUILDER`, `TRAINEE`, and `SOLDIER`.
- Hytale's existing `PopulationService` remains the current runtime aggregate adapter for live UI/count commands; the new control-plane citizen records are the canonical per-citizen model to bridge into that runtime deliberately.
- Frontends must not calculate age, job eligibility, troop state, or productivity locally.

## Data Model
`CitizenData` stores individual citizen identity, owner player, kingdom, birth time, lifecycle state, existing job type, health, morale, housing, nutrition, training state, troop-link state, stats, timestamps, and metadata.

Citizens and troops are the same person record:

`ACTIVE_CITIZEN -> IN_TRAINING -> ACTIVE_TROOP`

Demotion returns the same `citizenId` to `ACTIVE_CITIZEN`.

## Aging
Aging is derived from `bornAtEpochMillis` using `CitizenAgingConfig`.

Prototype balance:
- 1 real day = 1 in-game month
- 12 real days = 1 citizen year

Age stages are recalculated on read and maintenance. Age is not stored as the canonical field.

## Jobs, Training, And Troops
Job assignment validates lifecycle stage, status, health, morale, nutrition, and the existing `CitizenJobType` enum. Unknown jobs are rejected instead of expanding the job taxonomy in command parsing.

Training and troop conversion keep the same person record:

- `CitizenTrainingStartHandler` moves eligible citizens into `IN_TRAINING`.
- `CitizenTroopPromotionHandler` promotes the same `citizenId` to `ACTIVE_TROOP`.
- `CitizenTroopDemotionHandler` returns the same `citizenId` to `ACTIVE_CITIZEN` when policy allows.

## Conditions And Clock Hooks
Food, morale, housing, nutrition, health, and night-rest handlers are first-pass control-plane hooks. They update canonical citizen state and summary caches; frontends only receive projected results.

When `KingdomClockControlSystem` is present, `CitizenClockIntegrationHandler` reads canonical phase state to apply productivity and rest modifiers. Citizen code does not calculate day/night independently.

## Cache And Persistence
The control-plane repository abstraction is `CitizenRepository`.

- L1 memory and L2 Redis-style summary behavior are represented by `CitizenSummaryCacheRepository`.
- Missing or dirty cache summaries rebuild from durable citizen records.
- `PostgresCitizenRepository` and `schema/postgres/005_citizen_control_plane.sql` define the durable record shape for production Postgres.
- Redis remains optional through the existing cache architecture; the first control-plane slice uses an in-memory Redis-like cache adapter for deterministic tests.

## Commands
Canonical commands are parsed by the existing control command pipeline:

- `citizens spawn <ownerPlayerId> <amount> [kingdomId]`
- `citizens list <ownerPlayerId|kingdomId>`
- `citizens summary <ownerPlayerId|kingdomId>`
- `citizens debug <citizenId>`
- `citizens age <citizenId> <years>`
- `citizens setstage <citizenId> <stage>`
- `citizens setjob <citizenId> <job>`
- `citizens clearjob <citizenId>`
- `citizens train <citizenId>`
- `citizens promote <citizenId>`
- `citizens demote <citizenId>`
- `citizens health|morale|nutrition|housing <citizenId> <state>`
- `citizens maintenance [kingdomId]`
- `citizens refresh-cache <ownerPlayerId|kingdomId>`
- `citizens refresh-displays <ownerPlayerId|kingdomId>`

`/kd citizens spawn ...` and related canonical citizen subcommands route through `FrontendCommandIngressHandler`.

## Projections
Citizen projections use aggregate data and representative anchors:

- `CitizenPopulationProjection`
- `CitizenDisplayAnchorProjection`
- anchor text such as `Citizens: X` and `Troops: Y`

Minecraft, Hytale, Roblox, and Discord wrappers are thin platform selectors around `FrontendProjectionHandler`.

## Verification
The focused JVM verification is:

`.\.codex-temp\apache-maven-3.9.6\bin\mvn.cmd -q -Dtest=CitizenControlSystemIntegrationTest test`

Live Hytale player-bot verification remains routed through the existing HytaleDevServer mirror and player-like bot harness. The current live aggregate `/kd citizens add|set` path is intentionally left compatible; canonical `/kd citizens spawn|summary|debug|setjob|train|promote|demote|refresh-cache|refresh-displays` coverage is verified through frontend command ingress tests until the live UI bridge is promoted to canonical records.

## Optional Spring Panel
No Spring dependency is required for the citizen control plane. Optional citizen dashboard/list/detail/cache pages should call direct Java query handlers and `WebControlCommandSubmissionHandler`; controllers must not mutate citizen state or call internal HTTP loopback.

## TODO
- Bridge Hytale's existing `/kd citizens add|set` aggregate debug commands into the canonical citizen command model without breaking current live UI test flows.
- Add Spring citizen pages that call Java query handlers and `WebControlCommandSubmissionHandler` directly.
- Add production Redis-backed citizen summary cache adapter.
- Install/update the shared frontend contracts artifact before root projection wrappers reference the new citizen action constants directly.
- Expand Postgres repository tests with a live/Testcontainers database once the repo has durable test database infrastructure.
- Tune age/job/training eligibility using gameplay balancing data.
- Add natural death, retirement, family/lineage, moving representative entities, and full citizen AI later.
