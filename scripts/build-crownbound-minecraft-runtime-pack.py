from __future__ import annotations

import argparse
import hashlib
import zipfile
from pathlib import Path


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", required=True, type=Path)
    parser.add_argument("--bundle", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--checksum", required=True, type=Path)
    return parser.parse_args()


def collect_local_files(root: Path) -> list[tuple[Path, str]]:
    files: list[tuple[Path, str]] = []
    distribution_root = root / "distribution"
    for file in sorted(root.rglob("*")):
        if not file.is_file():
            continue
        if file.name in {"pack.zip", "README.md", ".gitkeep"}:
            continue
        if distribution_root in file.parents:
            continue
        relative = file.relative_to(root).as_posix()
        files.append((file, relative))
    return files


def build_pack(root: Path, bundle: Path, output: Path) -> None:
    local_files = collect_local_files(root)
    local_names = {relative for _, relative in local_files}
    output.parent.mkdir(parents=True, exist_ok=True)

    with zipfile.ZipFile(output, "w", compression=zipfile.ZIP_DEFLATED) as archive_out:
        wrote_pack_metadata = False
        with zipfile.ZipFile(bundle, "r") as bundled_archive:
            for info in bundled_archive.infolist():
                if info.is_dir() or info.filename in local_names:
                    continue
                archive_out.writestr(info, bundled_archive.read(info.filename))
                if info.filename == "pack.mcmeta":
                    wrote_pack_metadata = True

        if not wrote_pack_metadata:
            archive_out.writestr(
                "pack.mcmeta",
                "{\n  \"pack\": {\n    \"pack_format\": 64,\n    \"description\": \"Crownbound\"\n  }\n}\n",
            )

        for file, relative in local_files:
            archive_out.write(file, relative)


def write_checksum(archive_path: Path, checksum_path: Path) -> None:
    digest = hashlib.sha256(archive_path.read_bytes()).hexdigest()
    checksum_path.parent.mkdir(parents=True, exist_ok=True)
    checksum_path.write_text(f"{digest}  {archive_path.name}\n", encoding="utf-8")


def main() -> None:
    args = parse_args()
    build_pack(args.root.resolve(), args.bundle.resolve(), args.output.resolve())
    write_checksum(args.output.resolve(), args.checksum.resolve())


if __name__ == "__main__":
    main()
