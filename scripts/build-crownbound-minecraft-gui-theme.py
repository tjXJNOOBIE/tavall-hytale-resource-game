from __future__ import annotations

import os
import zipfile
from pathlib import Path

from PIL import Image, ImageColor, ImageDraw


REPO_ROOT = Path(__file__).resolve().parent.parent
VANILLA_JAR = Path(os.path.expandvars(r"%APPDATA%\.minecraft\versions\1.21.4\1.21.4.jar"))
RESOURCE_PACK_ROOT = REPO_ROOT / "resource-pack"
OUT_CONTAINER_ROOT = RESOURCE_PACK_ROOT / "assets" / "minecraft" / "textures" / "gui" / "container"
OUT_SPRITE_ROOT = RESOURCE_PACK_ROOT / "assets" / "minecraft" / "textures" / "gui" / "sprites" / "container"
PREVIEW_PATH = REPO_ROOT / "temp" / "crownbound-kd-gui-preview.png"

GENERATED_ICON = RESOURCE_PACK_ROOT / "assets" / "crownbound" / "textures" / "item" / "ui" / "button_icon.png"
GENERATED_TAB = RESOURCE_PACK_ROOT / "assets" / "crownbound" / "textures" / "item" / "ui" / "button_tab.png"
GENERATED_PRIMARY = RESOURCE_PACK_ROOT / "assets" / "crownbound" / "textures" / "item" / "ui" / "button_primary.png"


def load_vanilla_texture(texture_name: str) -> Image.Image:
    with zipfile.ZipFile(VANILLA_JAR) as jar:
        with jar.open(texture_name) as handle:
            return Image.open(handle).convert("RGBA")


def lerp_channel(left: int, right: int, factor: float) -> int:
    return int(round(left + ((right - left) * factor)))


def lerp_color(left: tuple[int, int, int], right: tuple[int, int, int], factor: float) -> tuple[int, int, int]:
    return tuple(lerp_channel(left[index], right[index], factor) for index in range(3))


def tri_gradient(value: int, dark: tuple[int, int, int], middle: tuple[int, int, int], light: tuple[int, int, int]) -> tuple[int, int, int]:
    normalized = max(0.0, min(1.0, value / 255.0))
    if normalized < 0.5:
        return lerp_color(dark, middle, normalized * 2.0)
    return lerp_color(middle, light, (normalized - 0.5) * 2.0)


def recolor_grayscale(texture: Image.Image, dark: str, middle: str, light: str) -> Image.Image:
    dark_rgb = ImageColor.getrgb(dark)
    middle_rgb = ImageColor.getrgb(middle)
    light_rgb = ImageColor.getrgb(light)
    output = Image.new("RGBA", texture.size)
    pixels = output.load()
    for x in range(texture.width):
        for y in range(texture.height):
            red, green, blue, alpha = texture.getpixel((x, y))
            if alpha == 0:
                pixels[x, y] = (0, 0, 0, 0)
                continue
            luminance = int(round((red + green + blue) / 3))
            color = tri_gradient(luminance, dark_rgb, middle_rgb, light_rgb)
            pixels[x, y] = (*color, alpha)
    return output


def stretch_icon(path: Path, size: tuple[int, int], alpha: int) -> Image.Image:
    icon = Image.open(path).convert("RGBA").resize(size, Image.Resampling.NEAREST)
    icon.putalpha(alpha)
    return icon


def glow(draw: ImageDraw.ImageDraw, box: tuple[int, int, int, int], outline: str, width: int) -> None:
    for step in range(width, 0, -1):
        alpha = max(18, 72 - (step * 12))
        draw.rounded_rectangle(
            (box[0] - step, box[1] - step, box[2] + step, box[3] + step),
            radius=6 + step,
            outline=ImageColor.getrgb(outline) + (alpha,),
            width=1,
        )


def style_generic_54() -> Image.Image:
    base = recolor_grayscale(
        load_vanilla_texture("assets/minecraft/textures/gui/container/generic_54.png"),
        "#081019",
        "#4f6070",
        "#d0b27a",
    )
    draw = ImageDraw.Draw(base, "RGBA")
    panel = (0, 0, 175, 221)
    draw.rounded_rectangle(panel, radius=8, fill=(12, 18, 27, 240), outline=(191, 156, 87, 255), width=2)
    draw.rounded_rectangle((4, 4, 171, 217), radius=7, outline=(51, 72, 92, 220), width=1)

    draw.rounded_rectangle((7, 6, 168, 24), radius=6, fill=(19, 31, 44, 235), outline=(208, 175, 106, 255), width=1)
    draw.rectangle((7, 28, 168, 126), fill=(17, 22, 30, 225))
    draw.rectangle((7, 130, 168, 220), fill=(13, 17, 24, 230))
    draw.line((7, 126, 168, 126), fill=(208, 175, 106, 190), width=1)
    draw.line((7, 129, 168, 129), fill=(47, 65, 85, 255), width=1)

    tab = stretch_icon(GENERATED_TAB, (22, 22), 170)
    primary = stretch_icon(GENERATED_PRIMARY, (16, 16), 120)
    crest = stretch_icon(GENERATED_ICON, (36, 36), 195)
    base.alpha_composite(tab, (12, 2))
    base.alpha_composite(tab, (141, 2))
    base.alpha_composite(crest, (70, 1))

    for x in range(15, 160, 26):
        base.alpha_composite(primary, (x, 33))

    for x in range(16, 160, 18):
        for y in range(34, 124, 18):
            draw.rectangle((x, y, x + 15, y + 15), outline=(110, 138, 159, 80), width=1)

    for x in range(16, 160, 18):
        for y in range(146, 218, 18):
            draw.rectangle((x, y, x + 15, y + 15), outline=(88, 110, 126, 72), width=1)

    return base


def style_inventory() -> Image.Image:
    base = recolor_grayscale(
        load_vanilla_texture("assets/minecraft/textures/gui/container/inventory.png"),
        "#09111a",
        "#566777",
        "#d2b983",
    )
    draw = ImageDraw.Draw(base, "RGBA")
    panel = (0, 0, 175, 165)
    draw.rounded_rectangle(panel, radius=8, fill=(11, 17, 24, 242), outline=(191, 156, 87, 255), width=2)
    draw.rounded_rectangle((4, 4, 171, 161), radius=7, outline=(58, 77, 99, 210), width=1)
    draw.rounded_rectangle((7, 6, 168, 24), radius=6, fill=(18, 29, 42, 235), outline=(208, 175, 106, 255), width=1)
    draw.rectangle((7, 28, 168, 90), fill=(17, 22, 30, 225))
    draw.rectangle((7, 94, 168, 164), fill=(13, 17, 24, 230))
    draw.line((7, 90, 168, 90), fill=(208, 175, 106, 185), width=1)
    draw.line((7, 93, 168, 93), fill=(47, 65, 85, 255), width=1)

    crest = stretch_icon(GENERATED_ICON, (32, 32), 185)
    base.alpha_composite(crest, (72, 0))
    accent = stretch_icon(GENERATED_PRIMARY, (14, 14), 110)
    for x in range(15, 160, 20):
        base.alpha_composite(accent, (x, 35))

    return base


def style_slot() -> Image.Image:
    slot = Image.new("RGBA", (18, 18), (0, 0, 0, 0))
    draw = ImageDraw.Draw(slot, "RGBA")
    draw.rounded_rectangle((0, 0, 17, 17), radius=3, fill=(10, 16, 22, 235), outline=(201, 169, 101, 255), width=1)
    draw.rounded_rectangle((2, 2, 15, 15), radius=2, fill=(21, 29, 40, 230), outline=(77, 99, 118, 160), width=1)
    return slot


def style_slot_highlight_back() -> Image.Image:
    image = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image, "RGBA")
    glow(draw, (4, 4, 19, 19), "#e6cb92", 3)
    return image


def style_slot_highlight_front() -> Image.Image:
    image = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image, "RGBA")
    draw.rounded_rectangle((4, 4, 19, 19), radius=4, outline=(255, 227, 162, 210), width=2)
    draw.rounded_rectangle((6, 6, 17, 17), radius=3, outline=(129, 199, 220, 120), width=1)
    return image


def build_preview(generic_54: Image.Image, inventory: Image.Image, slot: Image.Image) -> None:
    preview = Image.new("RGBA", (600, 340), (10, 13, 18, 255))
    preview.alpha_composite(generic_54.resize((352, 444), Image.Resampling.NEAREST), (12, -52))
    preview.alpha_composite(inventory.resize((352, 332), Image.Resampling.NEAREST), (236, 4))
    slot_preview = slot.resize((144, 144), Image.Resampling.NEAREST)
    preview.alpha_composite(slot_preview, (228, 180))
    PREVIEW_PATH.parent.mkdir(parents=True, exist_ok=True)
    preview.save(PREVIEW_PATH)


def main() -> None:
    OUT_CONTAINER_ROOT.mkdir(parents=True, exist_ok=True)
    OUT_SPRITE_ROOT.mkdir(parents=True, exist_ok=True)

    generic_54 = style_generic_54()
    inventory = style_inventory()
    slot = style_slot()
    slot_highlight_back = style_slot_highlight_back()
    slot_highlight_front = style_slot_highlight_front()

    generic_54.save(OUT_CONTAINER_ROOT / "generic_54.png")
    inventory.save(OUT_CONTAINER_ROOT / "inventory.png")
    slot.save(OUT_SPRITE_ROOT / "slot.png")
    slot_highlight_back.save(OUT_SPRITE_ROOT / "slot_highlight_back.png")
    slot_highlight_front.save(OUT_SPRITE_ROOT / "slot_highlight_front.png")

    build_preview(generic_54, inventory, slot)

    print(f"wrote {(OUT_CONTAINER_ROOT / 'generic_54.png')}")
    print(f"wrote {(OUT_CONTAINER_ROOT / 'inventory.png')}")
    print(f"wrote {(OUT_SPRITE_ROOT / 'slot.png')}")
    print(f"wrote {(OUT_SPRITE_ROOT / 'slot_highlight_back.png')}")
    print(f"wrote {(OUT_SPRITE_ROOT / 'slot_highlight_front.png')}")
    print(f"wrote {PREVIEW_PATH}")


if __name__ == "__main__":
    main()
