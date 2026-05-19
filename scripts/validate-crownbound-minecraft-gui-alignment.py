from __future__ import annotations

import argparse
import json
from pathlib import Path

from PIL import Image

VISIBLE_REGION = (0, 0, 176, 222)


def measure_gold_frame(texture_path: Path) -> dict[str, object]:
    image = Image.open(texture_path).convert("RGBA")
    points: list[tuple[int, int]] = []
    for y in range(image.height):
        for x in range(image.width):
            red, green, blue, alpha = image.getpixel((x, y))
            if alpha > 200 and red > 120 and green > 90 and blue < 120 and (red - blue) > 40 and (green - blue) > 20:
                points.append((x, y))
    if not points:
        raise ValueError(f"no gold frame pixels found in {texture_path}")
    xs = [point[0] for point in points]
    ys = [point[1] for point in points]
    bbox = (min(xs), min(ys), max(xs), max(ys))
    center_x = (bbox[0] + bbox[2]) / 2.0
    center_y = (bbox[1] + bbox[3]) / 2.0
    visible_left, visible_top, visible_right, visible_bottom = VISIBLE_REGION
    visible_center_x = (visible_left + visible_right - 1) / 2.0
    visible_center_y = (visible_top + visible_bottom - 1) / 2.0
    return {
        "path": str(texture_path),
        "size": [image.width, image.height],
        "visibleRegion": [visible_left, visible_top, visible_right, visible_bottom],
        "bbox": list(bbox),
        "center": [center_x, center_y],
        "delta": [center_x - visible_center_x, center_y - visible_center_y],
        "overflow": {
            "left": max(0, visible_left - bbox[0]),
            "top": max(0, visible_top - bbox[1]),
            "right": max(0, bbox[2] - (visible_right - 1)),
            "bottom": max(0, bbox[3] - (visible_bottom - 1)),
        },
    }


def main() -> int:
    parser = argparse.ArgumentParser(description="Validate that the Crownbound generic_54 GUI frame stays centered.")
    parser.add_argument("--texture", required=True, type=Path)
    parser.add_argument("--tolerance", type=float, default=1.5)
    parser.add_argument("--report", type=Path, default=None)
    args = parser.parse_args()

    report = measure_gold_frame(args.texture)
    if args.report is not None:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")

    delta_x, delta_y = report["delta"]
    overflow = report["overflow"]
    print(json.dumps(report, indent=2))
    if any(value > 0 for value in overflow.values()):
        raise SystemExit(
            "GUI texture spills outside the visible 54-slot chest region: "
            f"overflow={overflow}"
        )
    if abs(delta_x) > args.tolerance or abs(delta_y) > args.tolerance:
        raise SystemExit(
            f"GUI alignment drift detected for {args.texture}: delta=({delta_x:.2f},{delta_y:.2f}) tolerance={args.tolerance}"
        )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
