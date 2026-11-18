import sys, importlib, json
sys.path.insert(0, '/home/claude/colora-world/tools/art')
from render import render_png
mod = importlib.import_module(sys.argv[1])
fn = getattr(mod, sys.argv[2])
art = fn().to_json({})
render_png(art, f'/tmp/claude-0/try_{sys.argv[2]}.png', 512)
render_png(art, f'/tmp/claude-0/try_{sys.argv[2]}_c.png', 512, use_samples=True)
print(sys.argv[2], 'regions', len(art['regions']), 'parts', [p['id'] for p in art['parts']])
