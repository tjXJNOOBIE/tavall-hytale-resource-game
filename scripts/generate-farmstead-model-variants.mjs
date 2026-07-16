import { mkdir, readFile, writeFile } from "node:fs/promises";
import path from "node:path";

const resourceRoot = path.resolve("src/main/resources");
const baseModelPath = path.join(resourceRoot, "Common", "Items", "ResourceGame", "Farmstead", "Model.blockymodel");
const itemRoot = path.join(resourceRoot, "Common", "Items", "ResourceGame", "Farmstead");
const serverModelRoot = path.join(resourceRoot, "Server", "Models", "ResourceGame", "Farmstead");
const baseServerModelPath = path.join(resourceRoot, "Server", "Models", "ResourceGame", "Farmstead.json");

const constructionVariants = [
  {
    name: "Foundation",
    visible: [
      "grass_",
      "path",
      "foundation",
      "stone_base",
      "soil",
      "ridge"
    ]
  },
  {
    name: "Scaffolding",
    visible: [
      "grass_",
      "path",
      "foundation",
      "stone_base",
      "soil",
      "ridge",
      "trim",
      "beam",
      "fence",
      "support",
      "ladder",
      "well_stone"
    ]
  },
  {
    name: "Shell",
    visible: [
      "grass_",
      "path",
      "foundation",
      "stone_base",
      "soil",
      "ridge",
      "trim",
      "beam",
      "fence",
      "support",
      "ladder",
      "well_",
      "farmhouse_",
      "barn_",
      "silo_core",
      "silo_front",
      "silo_back",
      "silo_left",
      "silo_right"
    ],
    hidden: ["roof", "chimney", "cap", "wagon", "chicken", "wildflower", "tree_leaf"]
  },
  {
    name: "Complete",
    visible: ["*"]
  }
];

const levelVariants = [
  {
    name: "Level_01",
    visible: ["grass_", "path", "farmhouse_", "crop_", "well_path"]
  },
  {
    name: "Level_02",
    visible: ["grass_", "path", "farmhouse_", "crop_", "well_", "barn_"]
  },
  {
    name: "Level_03",
    visible: ["grass_", "path", "farmhouse_", "crop_", "well_", "barn_", "silo_"]
  },
  {
    name: "Level_04",
    visible: ["grass_", "path", "farmhouse_", "crop_", "well_", "barn_", "silo_", "wagon_", "hay_", "tree_trunk"]
  },
  {
    name: "Level_05",
    visible: ["*"]
  }
];

const baseModel = JSON.parse(await readFile(baseModelPath, "utf8"));
const baseServerModel = JSON.parse(await readFile(baseServerModelPath, "utf8"));

await generateCollection(levelVariants, itemRoot, serverModelRoot);
await generateCollection(constructionVariants, path.join(itemRoot, "Construction"), path.join(serverModelRoot, "Construction"));

async function generateCollection(variants, itemDirectory, serverDirectory) {
  for (const variant of variants) {
    const model = clone(baseModel);
    model.nodes = model.nodes.map((node) => applyVisibility(node, variant));
    model.variant = {
      source: "Items/ResourceGame/Farmstead/Model.blockymodel",
      name: variant.name
    };

    const variantItemDirectory = path.join(itemDirectory, variant.name);
    await mkdir(variantItemDirectory, { recursive: true });
    await writeFile(path.join(variantItemDirectory, "Model.blockymodel"), `${JSON.stringify(model, null, 2)}\n`, "utf8");

    await mkdir(serverDirectory, { recursive: true });
    const serverModel = {
      ...baseServerModel,
      Model: relativeModelPath(variantItemDirectory)
    };
    await writeFile(path.join(serverDirectory, `${variant.name}.json`), `${JSON.stringify(serverModel, null, 2)}\n`, "utf8");
  }
}

function applyVisibility(node, variant) {
  const next = { ...node };
  if (next.shape) {
    next.shape = {
      ...next.shape,
      visible: isVisible(next.name, variant)
    };
  }
  if (Array.isArray(next.children)) {
    next.children = next.children.map((child) => applyVisibility(child, variant));
  }
  return next;
}

function isVisible(nodeName, variant) {
  const name = String(nodeName ?? "").toLowerCase();
  if (name === "farmstead_prefab_root") {
    return true;
  }
  if (variant.hidden?.some((token) => name.includes(token))) {
    return false;
  }
  if (variant.visible.includes("*")) {
    return true;
  }
  return variant.visible.some((token) => name.includes(token));
}

function relativeModelPath(variantItemDirectory) {
  return path.posix.join(
    "Items",
    "ResourceGame",
    "Farmstead",
    ...path.relative(itemRoot, variantItemDirectory).split(path.sep),
    "Model.blockymodel"
  );
}

function clone(value) {
  return JSON.parse(JSON.stringify(value));
}
