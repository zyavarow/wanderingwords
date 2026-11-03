"""Rebuild the word page and the app's word files from web/words-data.json.

Usage (from the repo root):  python3 tools/build.py
Writes web/word-page.html, app/src/main/assets/words.json and app/src/main/assets/bonus.json.
"""
import json, pathlib
root = pathlib.Path(__file__).resolve().parent.parent
d = json.loads((root / "web/words-data.json").read_text())
page = lambda ws: [{k: w[k] for k in ("id", "w", "p", "pos", "d", "e", "t")} | ({"b": 1} if w.get("b") else {}) for w in ws]
data = json.dumps({"words": page(d["words"]), "bonus": page(d["bonus"])}, ensure_ascii=False).replace("</", "<\\/")
(root / "web/word-page.html").write_text((root / "web/template.html").read_text().replace("__DATA__", data))
app = lambda ws: [[w["w"], w["p"], w["pos"], w["d"], w["e"], w["t"]] for w in ws]
assets = root / "app/src/main/assets"
(assets / "words.json").write_text(json.dumps(app(d["words"]), ensure_ascii=False, separators=(",", ":")))
(assets / "bonus.json").write_text(json.dumps(app(d["bonus"]), ensure_ascii=False, separators=(",", ":")))
print(f"{len(d['words'])} daily words, {len(d['bonus'])} extra words written.")
