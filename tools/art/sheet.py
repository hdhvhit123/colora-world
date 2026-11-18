"""Quick contact sheet for one subject module: line art row + sample row."""
import sys, importlib
sys.path.insert(0, '/home/claude/colora-world/tools/art')
import skia
from render import draw_art, PAPER
mod = importlib.import_module(sys.argv[1])
fns = mod.ALL
cell = 256; cols = 6
rows = (len(fns) + cols - 1) // cols
surf = skia.Surface(cell * cols, cell * rows * 2)
c = surf.getCanvas(); c.clear(skia.Color(0xFF, 0xF8, 0xEF))
for i, fn in enumerate(fns):
    art = fn().to_json({})
    for k, samples in enumerate((False, True)):
        x = (i % cols) * cell; y = ((i // cols) * 2 + k) * cell
        c.save(); c.translate(x + 4, y + 4)
        c.drawRect(skia.Rect(0, 0, cell - 8, cell - 8), skia.Paint(Color=PAPER))
        s = (cell - 8) / 1024; c.scale(s, s)
        draw_art(c, art, use_samples=samples)
        c.restore()
    print(fn.__name__, len(art['regions']), end='; ')
surf.makeImageSnapshot().save(sys.argv[2], skia.kPNG)
