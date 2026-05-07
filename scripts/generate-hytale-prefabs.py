from __future__ import annotations

import json
import math
import sys
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
PREFAB_ROOT = REPO_ROOT / "src" / "main" / "resources" / "Common" / "Prefabs" / "ResourceGame"
SERVER_PREFAB_ROOT = REPO_ROOT / "src" / "main" / "resources" / "Server" / "Prefabs" / "ResourceGame"
MODEL_ROOT = REPO_ROOT / "src" / "main" / "resources" / "Common" / "Models" / "ResourceGame"
FARMSTEAD_MODEL = MODEL_ROOT / "farmstead.bbmodel"
BLOCKBENCH_UNITS_PER_BLOCK = 4


def is_farmstead_runtime_prefab(recipe_path: Path) -> bool:
    relative_recipe = recipe_path.relative_to(PREFAB_ROOT).as_posix().lower()
    return relative_recipe == "farmstead.resource-prefab.json" or relative_recipe == "buildings/farmstead/level_01.resource-prefab.json"


def block_for_element(element_name: str) -> str:
    normalized_name = element_name.lower()
    if any(token in normalized_name for token in ("crop", "wheat", "green", "grass")):
        return "Rock_Shale"
    if any(token in normalized_name for token in ("roof", "red shell", "barn")):
        return "Rock_Stone_Brick"
    if any(token in normalized_name for token in ("stone", "silo", "well", "foundation", "chimney")):
        return "Rock_Quartzite"
    if any(token in normalized_name for token in ("window", "white", "smoke")):
        return "Rock_Stone"
    return "Rock_Shale"


def scaled_range(start_value: float, end_value: float) -> range:
    minimum_value = min(start_value, end_value)
    maximum_value = max(start_value, end_value)
    start_block = math.floor(minimum_value / BLOCKBENCH_UNITS_PER_BLOCK)
    end_block = math.ceil(maximum_value / BLOCKBENCH_UNITS_PER_BLOCK) - 1
    if end_block < start_block:
        end_block = start_block
    return range(start_block, end_block + 1)


def farmstead_model_blocks() -> list[dict[str, int | str]]:
    model = json.loads(FARMSTEAD_MODEL.read_text(encoding="utf-8-sig"))
    elements = [
        element for element in model.get("elements", [])
        if isinstance(element.get("from"), list) and isinstance(element.get("to"), list)
    ]
    if not elements:
        raise ValueError(f"No Blockbench cube elements found in {FARMSTEAD_MODEL}")

    block_map: dict[tuple[int, int, int], str] = {}
    for element in elements:
        block_name = block_for_element(str(element.get("name", "")))
        from_values = element["from"]
        to_values = element["to"]
        for block_x in scaled_range(float(from_values[0]), float(to_values[0])):
            for block_y in scaled_range(float(from_values[1]), float(to_values[1])):
                for block_z in scaled_range(float(from_values[2]), float(to_values[2])):
                    block_map[(block_x, block_y, block_z)] = block_name

    return [
        {
            "x": block_x,
            "y": block_y,
            "z": block_z,
            "name": block_name,
        }
        for (block_x, block_y, block_z), block_name in sorted(block_map.items())
    ]


def hytale_prefab_path(recipe_path: Path) -> Path:
    relative_recipe = recipe_path.relative_to(PREFAB_ROOT)
    relative_prefab = relative_recipe.with_name(relative_recipe.name.replace(".resource-prefab.json", ".prefab.json"))
    return SERVER_PREFAB_ROOT / relative_prefab


def convert_recipe(recipe_path: Path) -> Path:
    recipe = json.loads(recipe_path.read_text(encoding="utf-8-sig"))
    anchor = recipe.get("anchor", {})
    if is_farmstead_runtime_prefab(recipe_path):
        blocks = farmstead_model_blocks()
    else:
        blocks = [
            {
                "x": int(block["x"]),
                "y": int(block["y"]),
                "z": int(block["z"]),
                "name": str(block["block"]),
            }
            for block in recipe.get("blocks", [])
        ]
    prefab = {
        "version": 8,
        "blockIdVersion": 1,
        "anchorX": int(anchor.get("x", 0)),
        "anchorY": int(anchor.get("y", 0)),
        "anchorZ": int(anchor.get("z", 0)),
        "blocks": blocks,
    }
    output_path = hytale_prefab_path(recipe_path)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(json.dumps(prefab, indent=2) + "\n", encoding="utf-8")
    return output_path


def main() -> None:
    generate_all = "--all" in sys.argv
    recipes = sorted(PREFAB_ROOT.rglob("*.resource-prefab.json"))
    if not generate_all:
        recipes = [recipe for recipe in recipes if "farmstead" in recipe.as_posix().lower()]
    outputs = [convert_recipe(recipe) for recipe in recipes]
    farmstead_outputs = [
        path for path in outputs
        if "farmstead" in path.as_posix().lower()
    ]
    print(f"Converted {len(outputs)} Resource Game prefab recipes into Hytale .prefab.json files.")
    print(f"Farmstead prefabs: {len(farmstead_outputs)}")
    for path in farmstead_outputs[:8]:
        print(path.relative_to(REPO_ROOT).as_posix())


if __name__ == "__main__":
    main()
