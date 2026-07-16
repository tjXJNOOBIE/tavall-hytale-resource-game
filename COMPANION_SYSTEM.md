# Companion System

The companion system is a Java control-plane-owned RPG unit layer. Frontends can render, command, and project companions, but canonical companion state lives in the backend command pipeline.

Implemented first slice:
- Companion types: `HEALER`, `BRAWLER`, `BRUTE`, `ARCANE`.
- Skill affinities: `EARTH` for `BRUTE`/`BRAWLER`, `ARCANE` for `ARCANE`/`HEALER`.
- Max level 70 with milestone-driven slot unlocks.
- Companion Camp training with proportional XP on early cancel.
- Wisdom Well ability upgrades for eligible skills.
- Behavior states: `IDLE`, `FOLLOWING`, `AGGRESSIVE`, `FLEEING`, `DUELING`.
- Morale states: `HIGH`, `MEDIUM`, `LOW`, `POOR`.
- Wall assignment and defensive bonus calculation.
- Postgres tables for companions, attributes, skill slots/upgrades, training sessions, and wall assignments.
- `/kd companion ...` commands route through the shared control command pipeline.

Debug command examples:
- `/kd companion give <playerId> BRUTE`
- `/kd companion list <playerId>`
- `/kd companion setlevel <companionId> 30`
- `/kd companion xp <companionId> 1000`
- `/kd companion morale <companionId> HIGH`
- `/kd companion behavior <companionId> FOLLOWING`
- `/kd companion train <playerId> <companionId>`
- `/kd companion claim <playerId> <companionId>`
- `/kd companion skill unlock <playerId> <companionId> arcane-mend`
- `/kd companion skill upgrade <playerId> <companionId> arcane-mend`
- `/kd companion wall assign <playerId> <companionId> north`

Design rule: companions are personal tactical units and battle modifiers. They should not become a second army system.
