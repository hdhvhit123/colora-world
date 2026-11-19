"""Installs translations into the app and refreshes the content index.

Sources (hand-written translations; English is the reference):
  tools/i18n/<lang>.txt  one "key = text" per line (content and UI keys together)
  -> app/src/main/assets/content/locales/<lang>.json (content keys)
  -> app/src/main/assets/ui/<lang>.json              (UI keys)
Then rewrites assets/content/index.json (size + SHA-256 of every content file)
and res/xml/locales_config.xml. Run: python3 tools/i18n/install.py
"""
import hashlib, json, os, sys
ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SRC = os.path.join(ROOT, "tools", "i18n")
ASSETS = os.path.join(ROOT, "app", "src", "main", "assets")
C = os.path.join(ASSETS, "content")

def load(p):
    with open(p, encoding="utf-8") as f:
        return json.load(f)

def dump(p, d):
    with open(p, "w", encoding="utf-8") as f:
        json.dump(dict(sorted(d.items())), f, ensure_ascii=False, indent=1)
        f.write("\n")

en_c = load(os.path.join(C, "locales", "en.json"))
en_u = load(os.path.join(ASSETS, "ui", "en.json"))
langs = sorted(f[:-4] for f in os.listdir(SRC) if f.endswith(".txt"))

def read_txt(p):
    out = {}
    for n, line in enumerate(open(p, encoding="utf-8"), 1):
        line = line.rstrip("\n")
        if not line.strip() or line.startswith("#"): continue
        k, sep, v = line.partition(" = ")
        if not sep: raise SystemExit(f"{p}:{n}: expected 'key = text'")
        if k in out: raise SystemExit(f"{p}:{n}: duplicate {k}")
        out[k.strip()] = v.strip()
    return out
bad = 0
for l in langs:
    t = read_txt(os.path.join(SRC, l + ".txt"))
    c = {k: v for k, v in t.items() if k in en_c}; u = {k: v for k, v in t.items() if k in en_u}
    stray = set(t) - set(en_c) - set(en_u)
    if stray: bad += 1; print(f"{l}: unknown keys {sorted(stray)[:8]}")
    for name, ref, tr in (("content", en_c, c), ("ui", en_u, u)):
        miss = set(ref) - set(tr); extra = set(tr) - set(ref)
        if miss or extra:
            bad += 1; print(f"{l}/{name}: missing {sorted(miss)[:8]} extra {sorted(extra)[:8]}")
    dump(os.path.join(C, "locales", f"{l}.json"), {k: c[k] for k in en_c if k in c})
    dump(os.path.join(ASSETS, "ui", f"{l}.json"), {k: u[k] for k in en_u if k in u})
    print(f"installed {l}: content {len(c)}/{len(en_c)}, ui {len(u)}/{len(en_u)}")

# content integrity index (same format as tools/art/build_extras.py)
idx = []
for dp, _, fns in os.walk(C):
    for fn in fns:
        if fn == "index.json": continue
        p = os.path.join(dp, fn); b = open(p, "rb").read()
        idx.append({"path": os.path.relpath(p, C).replace(os.sep, "/"), "size": len(b), "sha256": hashlib.sha256(b).hexdigest()})
idx.sort(key=lambda e: e["path"])
old = load(os.path.join(C, "index.json"))
with open(os.path.join(C, "index.json"), "w", encoding="utf-8") as f:
    json.dump({"schemaVersion": old["schemaVersion"], "contentVersion": old["contentVersion"], "files": idx}, f, indent=1)

shipped = ["en"] + sorted(f[:-5] for f in os.listdir(os.path.join(ASSETS, "ui")) if f.endswith(".json") and f != "en.json")
tags = {"pt": "pt-BR", "zh": "zh-Hans"}
xml = '<?xml version="1.0" encoding="utf-8"?>\n<locale-config xmlns:android="http://schemas.android.com/apk/res/android">\n' + \
      "".join(f'    <locale android:name="{tags.get(l, l)}" />\n' for l in shipped) + "</locale-config>\n"
open(os.path.join(ROOT, "app", "src", "main", "res", "xml", "locales_config.xml"), "w").write(xml)
print("languages:", ", ".join(shipped))
sys.exit(1 if bad else 0)
