from __future__ import annotations

import re
import sys
import textwrap
from dataclasses import dataclass
from pathlib import Path


PAGE_WIDTH = 595.0
PAGE_HEIGHT = 842.0
MARGIN_X = 48.0
MARGIN_TOP = 48.0
MARGIN_BOTTOM = 48.0


def pdf_escape(value: str) -> str:
    return (
        value.replace("\\", "\\\\")
        .replace("(", "\\(")
        .replace(")", "\\)")
        .replace("\r", "")
    )


def normalize_inline_markdown(value: str) -> str:
    value = re.sub(r"\[([^\]]+)\]\([^)]+\)", r"\1", value)
    value = value.replace("`", "")
    value = value.replace("**", "")
    value = value.replace("__", "")
    return value


@dataclass
class TextLine:
    text: str
    font: str = "F1"
    size: int = 10
    leading: int = 13
    gap_before: int = 0


def wrap_text(text: str, size: int, indent: str = "") -> list[str]:
    max_chars = max(32, int((PAGE_WIDTH - 2 * MARGIN_X) / (size * 0.52)))
    initial_indent = indent
    subsequent_indent = " " * len(indent)
    return textwrap.wrap(
        text,
        width=max_chars,
        initial_indent=initial_indent,
        subsequent_indent=subsequent_indent,
        break_long_words=False,
        break_on_hyphens=False,
    ) or [indent.rstrip()]


def markdown_to_lines(markdown: str) -> list[TextLine]:
    result: list[TextLine] = []
    in_code = False

    for raw in markdown.splitlines():
        line = raw.rstrip()

        if line.strip().startswith("```"):
            in_code = not in_code
            result.append(TextLine("", gap_before=4))
            continue

        if in_code:
            for wrapped in wrap_text(line, 8):
                result.append(TextLine(wrapped, "F3", 8, 10))
            continue

        stripped = line.strip()
        if not stripped:
            if result and result[-1].text:
                result.append(TextLine("", leading=8))
            continue

        if stripped.startswith("# "):
            text = normalize_inline_markdown(stripped[2:])
            for wrapped in wrap_text(text, 18):
                result.append(TextLine(wrapped, "F2", 18, 23, gap_before=10))
            continue

        if stripped.startswith("## "):
            text = normalize_inline_markdown(stripped[3:])
            for wrapped in wrap_text(text, 14):
                result.append(TextLine(wrapped, "F2", 14, 18, gap_before=8))
            continue

        if stripped.startswith("### "):
            text = normalize_inline_markdown(stripped[4:])
            for wrapped in wrap_text(text, 12):
                result.append(TextLine(wrapped, "F2", 12, 16, gap_before=6))
            continue

        if stripped.startswith("- "):
            text = normalize_inline_markdown(stripped[2:])
            for wrapped in wrap_text(text, 10, indent="- "):
                result.append(TextLine(wrapped, "F1", 10, 13))
            continue

        numbered = re.match(r"^(\d+)\.\s+(.*)$", stripped)
        if numbered:
            prefix = f"{numbered.group(1)}. "
            text = normalize_inline_markdown(numbered.group(2))
            for wrapped in wrap_text(text, 10, indent=prefix):
                result.append(TextLine(wrapped, "F1", 10, 13))
            continue

        text = normalize_inline_markdown(stripped)
        for wrapped in wrap_text(text, 10):
            result.append(TextLine(wrapped, "F1", 10, 13))

    return result


def paginate(lines: list[TextLine]) -> list[list[TextLine]]:
    pages: list[list[TextLine]] = []
    current: list[TextLine] = []
    y = PAGE_HEIGHT - MARGIN_TOP

    for line in lines:
        needed = line.leading + line.gap_before
        if current and y - needed < MARGIN_BOTTOM:
            pages.append(current)
            current = []
            y = PAGE_HEIGHT - MARGIN_TOP
        current.append(line)
        y -= needed

    if current:
        pages.append(current)
    return pages or [[TextLine("")]]


def page_stream(lines: list[TextLine], page_number: int, total_pages: int) -> str:
    y = PAGE_HEIGHT - MARGIN_TOP
    commands: list[str] = []

    for line in lines:
        y -= line.gap_before
        if line.text:
            commands.append(
                f"BT /{line.font} {line.size} Tf {MARGIN_X:.1f} {y:.1f} Td "
                f"({pdf_escape(line.text)}) Tj ET"
            )
        y -= line.leading

    footer = f"Pagina {page_number} de {total_pages}"
    commands.append(
        f"BT /F1 8 Tf {PAGE_WIDTH / 2 - 28:.1f} 24.0 Td "
        f"({pdf_escape(footer)}) Tj ET"
    )
    return "\n".join(commands)


def build_pdf(markdown: str) -> bytes:
    lines = markdown_to_lines(markdown)
    pages = paginate(lines)
    objects: list[bytes] = []

    def add_object(payload: str | bytes) -> int:
        if isinstance(payload, str):
            payload = payload.encode("latin-1", "replace")
        objects.append(payload)
        return len(objects)

    catalog_id = add_object("PLACEHOLDER")
    pages_id = add_object("PLACEHOLDER")
    font_regular_id = add_object("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>")
    font_bold_id = add_object("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>")
    font_code_id = add_object("<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>")

    page_ids: list[int] = []
    content_ids: list[int] = []

    for index, page in enumerate(pages, start=1):
        stream = page_stream(page, index, len(pages)).encode("latin-1", "replace")
        content_id = add_object(
            b"<< /Length " + str(len(stream)).encode("ascii") + b" >>\nstream\n" + stream + b"\nendstream"
        )
        content_ids.append(content_id)
        page_id = add_object("PLACEHOLDER")
        page_ids.append(page_id)

    objects[catalog_id - 1] = f"<< /Type /Catalog /Pages {pages_id} 0 R >>".encode("ascii")
    kids = " ".join(f"{page_id} 0 R" for page_id in page_ids)
    objects[pages_id - 1] = f"<< /Type /Pages /Kids [{kids}] /Count {len(page_ids)} >>".encode("ascii")

    resources = (
        f"<< /Font << /F1 {font_regular_id} 0 R /F2 {font_bold_id} 0 R "
        f"/F3 {font_code_id} 0 R >> >>"
    )
    for page_id, content_id in zip(page_ids, content_ids):
        objects[page_id - 1] = (
            f"<< /Type /Page /Parent {pages_id} 0 R /MediaBox [0 0 {PAGE_WIDTH:.0f} {PAGE_HEIGHT:.0f}] "
            f"/Resources {resources} /Contents {content_id} 0 R >>"
        ).encode("ascii")

    output = bytearray(b"%PDF-1.4\n%\xe2\xe3\xcf\xd3\n")
    offsets = [0]
    for object_id, payload in enumerate(objects, start=1):
        offsets.append(len(output))
        output.extend(f"{object_id} 0 obj\n".encode("ascii"))
        output.extend(payload)
        output.extend(b"\nendobj\n")

    xref_offset = len(output)
    output.extend(f"xref\n0 {len(objects) + 1}\n".encode("ascii"))
    output.extend(b"0000000000 65535 f \n")
    for offset in offsets[1:]:
        output.extend(f"{offset:010d} 00000 n \n".encode("ascii"))
    output.extend(
        f"trailer\n<< /Size {len(objects) + 1} /Root {catalog_id} 0 R >>\n"
        f"startxref\n{xref_offset}\n%%EOF\n".encode("ascii")
    )
    return bytes(output)


def main() -> int:
    if len(sys.argv) < 3:
        print("Usage: python md_to_simple_pdf.py <output-dir> <input.md> [<input.md> ...]")
        return 2

    output_dir = Path(sys.argv[1])
    output_dir.mkdir(parents=True, exist_ok=True)

    for input_arg in sys.argv[2:]:
        input_path = Path(input_arg)
        markdown = input_path.read_text(encoding="utf-8")
        output_path = output_dir / f"{input_path.stem}.pdf"
        output_path.write_bytes(build_pdf(markdown))
        print(output_path)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
