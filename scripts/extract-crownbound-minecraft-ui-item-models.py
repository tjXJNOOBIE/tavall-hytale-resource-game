from __future__ import annotations

import io
import zipfile
from pathlib import Path

from PIL import Image


REPO_ROOT = Path(__file__).resolve().parents[1]
PACK_ARCHIVE = REPO_ROOT / "resource-pack" / "distribution" / "crownbound_minecraft_resource_pack.zip"
OUTPUT_ROOT = REPO_ROOT / "resource-pack" / "assets" / "crownbound" / "textures" / "item" / "ui"
PREVIEW_PATH = OUTPUT_ROOT / "kd_ui_preview.png"
BUTTON_FAMILIES = (
    "primary",
    "secondary",
    "danger",
    "success",
    "tab",
    "icon",
)
TARGET_SIZE = 128
PADDING = 10
CROP_BOXES = {
    "primary": (16, 20, 110, 116),
    "secondary": (18, 18, 136, 102),
    "danger": (18, 18, 136, 102),
    "success": (38, 30, 230, 126),
    "tab": (52, 82, 300, 172),
    "icon": (36, 1124, 258, 1242),
}


def build_item_texture_for_family(source_image: Image.Image, family: str) -> Image.Image:
    cropped = source_image.convert("RGBA").crop(CROP_BOXES[family])
    target = Image.new("RGBA", (TARGET_SIZE, TARGET_SIZE), (0, 0, 0, 0))

    inner_width = TARGET_SIZE - (PADDING * 2)
    inner_height = TARGET_SIZE - (PADDING * 2)
    scale = min(inner_width / cropped.width, inner_height / cropped.height)
    resized = cropped.resize(
        (
            max(1, int(round(cropped.width * scale))),
            max(1, int(round(cropped.height * scale))),
        ),
        Image.Resampling.LANCZOS,
    )
    offset_x = (TARGET_SIZE - resized.width) // 2
    offset_y = (TARGET_SIZE - resized.height) // 2
    target.paste(resized, (offset_x, offset_y), resized)
    return target


def main() -> None:
    OUTPUT_ROOT.mkdir(parents=True, exist_ok=True)
    preview_images: list[tuple[str, Image.Image]] = []

    with zipfile.ZipFile(PACK_ARCHIVE) as archive:
        for family in BUTTON_FAMILIES:
            source_entry = f"assets/crownbound/textures/gui/buttons/button_{family}.png"
            with archive.open(source_entry) as source_file:
                source_image = Image.open(io.BytesIO(source_file.read()))
                source_image.load()
            texture = build_item_texture_for_family(source_image, family)
            output_path = OUTPUT_ROOT / f"button_{family}.png"
            texture.save(output_path)
            preview_images.append((family, texture))

    build_preview_sheet(preview_images)


def build_preview_sheet(preview_images: list[tuple[str, Image.Image]]) -> None:
    columns = 3
    cell_size = TARGET_SIZE + 24
    rows = (len(preview_images) + columns - 1) // columns
    sheet = Image.new("RGBA", (columns * cell_size, rows * cell_size), (12, 12, 16, 255))

    for index, (_, texture) in enumerate(preview_images):
        column = index % columns
        row = index // columns
        x = column * cell_size + 12
        y = row * cell_size + 12
        sheet.paste(texture, (x, y), texture)

    PREVIEW_PATH.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(PREVIEW_PATH)


if __name__ == "__main__":
    main()
