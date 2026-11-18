"""Original sound design for Colora World, synthesised from code (no samples,
no third-party recordings): 12 UI effects, 15 toy-preset sets + animal motifs,
and 3 seamless music loops. Output: OGG Vorbis in app/src/main/assets/audio,
plus manifest.json and an audio build report (duration, peak, RMS, clipping).
Run: python3 tools/audio/synth.py   (needs numpy + ffmpeg with libvorbis)"""
from __future__ import annotations
import json, os, subprocess, tempfile, wave
import numpy as np

SR = 44100
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
OUT = os.path.join(ROOT, "app/src/main/assets/audio")
RNG = np.random.default_rng(20261118)


def t_(d): return np.arange(int(SR * d)) / SR
def hz(note): return 440.0 * 2 ** ((note - 69) / 12)


def env(n, a=0.005, d=0.1, s=0.0, r=0.05, total=None):
    """ADSR on n samples (s = sustain level, held until release)."""
    a_n, d_n, r_n = int(a * SR), int(d * SR), int(r * SR)
    hold = max(0, n - a_n - d_n - r_n)
    e = np.concatenate([np.linspace(0, 1, a_n, endpoint=False), np.linspace(1, s, d_n, endpoint=False),
                        np.full(hold, s), np.linspace(s, 0, r_n)])
    return np.pad(e, (0, max(0, n - len(e))))[:n]


def tone(freq, d, kind="sine", a=0.004, dec=0.25, sus=0.0, rel=0.08, vib=0.0, glide=None):
    t = t_(d)
    f = np.full_like(t, freq) if glide is None else np.geomspace(freq, glide, len(t))
    if vib: f = f * (1 + vib * np.sin(2 * np.pi * 5.5 * t))
    ph = 2 * np.pi * np.cumsum(f) / SR
    if kind == "sine": w = np.sin(ph)
    elif kind == "marimba": w = np.sin(ph) + 0.35 * np.sin(4 * ph) * np.exp(-t * 30) + 0.12 * np.sin(10 * ph) * np.exp(-t * 60)
    elif kind == "bell": w = np.sin(ph) + 0.5 * np.sin(2.76 * ph) * np.exp(-t * 6) + 0.25 * np.sin(5.4 * ph) * np.exp(-t * 12)
    elif kind == "pluck": w = sum((0.6 ** k) * np.sin((k + 1) * ph) * np.exp(-t * (6 + 5 * k)) for k in range(5))
    elif kind == "soft_square": w = np.tanh(2.2 * np.sin(ph)) * 0.6
    elif kind == "flute": w = np.sin(ph) + 0.18 * np.sin(2 * ph) + 0.05 * RNG.standard_normal(len(t)) * env(len(t), 0.02, 0.1, 0.3, 0.05)
    elif kind == "pad": w = (np.sin(ph) + 0.5 * np.sin(ph * 1.003) + 0.3 * np.sin(2 * ph * 0.998)) / 1.8
    else: raise ValueError(kind)
    return w * env(len(t), a, dec, sus, rel)


def noise(d, lo=200, hi=4000):
    n = RNG.standard_normal(int(SR * d))
    spec = np.fft.rfft(n); f = np.fft.rfftfreq(len(n), 1 / SR)
    spec[(f < lo) | (f > hi)] = 0
    return np.fft.irfft(spec, len(n)) / 3


def mix(*parts):
    n = max(len(p) for _, p in parts)
    out = np.zeros(n)
    for off, p in parts:
        o = int(off * SR); m = min(len(p), n - o) if o < n else 0
        if o + len(p) > len(out): out = np.pad(out, (0, o + len(p) - len(out)))
        out[o:o + len(p)] += p
    return out


def reverb(x, wet=0.18, room=0.42):
    y = np.copy(x)
    for d, g in ((0.0297, room), (0.0371, room * 0.95), (0.0411, room * 0.9), (0.0437, room * 0.85)):
        k = int(d * SR); buf = np.zeros(len(x) + k * 12)
        buf[:len(x)] = x
        for i in range(1, 12): buf[k * i:k * i + len(x)] += x * (g ** i)
        y = y + wet * buf[:len(x)] / 4
    return y


def finish(x, peak_db=-3.0, fade=0.004):
    x = x - np.mean(x)
    f = int(fade * SR)
    if f and len(x) > 2 * f: x[:f] *= np.linspace(0, 1, f); x[-f:] *= np.linspace(1, 0, f)
    pk = np.max(np.abs(x)) or 1
    return x * (10 ** (peak_db / 20) / pk)


# ------------------------------------------------------------------- UI SFX
def ui():
    m = {}
    m["ui.tap"] = tone(hz(84), 0.09, "marimba", dec=0.07)
    m["ui.select_color"] = mix((0, tone(hz(79), 0.16, "bell", dec=0.12)), (0.04, 0.5 * tone(hz(86), 0.14, "bell", dec=0.1)))
    m["ui.fill_pop"] = mix((0, tone(hz(72), 0.22, "sine", dec=0.18, glide=hz(84))), (0, 0.3 * noise(0.03, 800, 5000) * env(int(0.03 * SR), 0.001, 0.03)))
    m["ui.brush_soft"] = noise(0.32, 300, 2600) * env(int(0.32 * SR), 0.06, 0.2, 0.4, 0.08) * 0.8
    m["ui.eraser"] = noise(0.26, 1200, 6000) * env(int(0.26 * SR), 0.03, 0.15, 0.3, 0.06) * 0.6
    m["ui.undo"] = tone(hz(76), 0.2, "sine", dec=0.16, glide=hz(67))
    m["ui.redo"] = tone(hz(67), 0.2, "sine", dec=0.16, glide=hz(76))
    m["ui.save_chime"] = reverb(mix(*[(i * 0.09, tone(hz(n), 0.5, "bell", dec=0.45)) for i, n in enumerate((72, 76, 79, 84))]))
    m["ui.page_turn"] = noise(0.38, 500, 7000) * np.concatenate([np.linspace(0, 1, int(0.12 * SR)), np.linspace(1, 0, int(0.26 * SR))]) * 0.7
    m["ui.sticker_pop"] = mix((0, tone(hz(81), 0.12, "sine", dec=0.1, glide=hz(93))), (0.05, 0.5 * tone(hz(88), 0.1, "marimba", dec=0.08)))
    m["ui.open_panel"] = mix(*[(i * 0.035, 0.7 * tone(hz(n), 0.18, "marimba", dec=0.15)) for i, n in enumerate((67, 71, 74))])
    m["ui.gentle_no"] = mix((0, tone(hz(69), 0.18, "sine", dec=0.15)), (0.12, tone(hz(65), 0.22, "sine", dec=0.2)))
    return m


# ------------------------------------------------------------- toy presets
def toys():
    m = {}
    def motif(notes, kind="marimba", step=0.11, d=0.3, rev=True):
        x = mix(*[(i * step, tone(hz(n), d, kind, dec=d * 0.8)) for i, n in enumerate(notes)])
        return reverb(x) if rev else x
    m["sfx.pet_walk"] = mix(*[(i * 0.16, 0.8 * tone(hz(60 + (i % 2) * 7), 0.1, "marimba", dec=0.07)) for i in range(4)])
    m["sfx.flower_bloom"] = reverb(mix((0, tone(hz(72), 0.9, "bell", dec=0.8, glide=hz(84)) * 0.6), (0, motif((79, 83, 86, 91), "bell", 0.08, 0.5, False) * 0.6)))
    m["sfx.nature_react"] = reverb(mix((0, noise(0.8, 200, 1800) * env(int(0.8 * SR), 0.3, 0.4, 0.2, 0.1) * 0.6), (0, 0.4 * motif((74, 79), "flute", 0.2, 0.4, False))))
    m["sfx.family_wave"] = motif((67, 72, 76), "pluck", 0.1, 0.35)
    m["sfx.machine_work"] = mix(*[(i * 0.12, 0.7 * tone(hz(45 + (i % 2) * 2), 0.11, "soft_square", dec=0.09)) for i in range(5)], (0.62, tone(hz(84), 0.2, "bell", dec=0.18) * 0.5))
    m["sfx.vehicle_ride"] = mix((0, 0.5 * tone(hz(48), 0.9, "soft_square", a=0.1, dec=0.2, sus=0.5, rel=0.2, glide=hz(55))), (0.3, motif((76, 76), "bell", 0.18, 0.25, False) * 0.5))
    m["sfx.ocean_swim"] = reverb(mix((0, mix(*[(i * 0.13, 0.6 * tone(hz(80 + i * 3), 0.16, "sine", dec=0.14, glide=hz(88 + i * 3))) for i in range(4)])), (0, noise(0.7, 150, 900) * env(int(0.7 * SR), 0.2, 0.4, 0.2, 0.1) * 0.4)))
    m["sfx.dino_play"] = mix((0, tone(hz(40), 0.45, "soft_square", a=0.03, dec=0.4, glide=hz(36))), (0.3, 0.6 * tone(hz(43), 0.4, "soft_square", a=0.03, dec=0.35, glide=hz(38))))
    m["sfx.space_explore"] = reverb(mix((0, tone(hz(60), 1.2, "pad", a=0.2, dec=0.6, sus=0.5, rel=0.4, vib=0.01, glide=hz(72)) * 0.7), (0, motif((84, 91, 96), "bell", 0.15, 0.6, False) * 0.4)), wet=0.3)
    m["sfx.food_serve"] = mix((0, tone(hz(88), 0.25, "bell", dec=0.22)), (0.07, 0.6 * tone(hz(95), 0.25, "bell", dec=0.22)))
    m["sfx.home_decor"] = motif((72, 74, 79), "marimba", 0.09, 0.25)
    m["sfx.fantasy_magic"] = reverb(mix(*[(i * 0.045, 0.45 * tone(hz(84 + (i * 5) % 12), 0.35, "bell", dec=0.3)) for i in range(10)]), wet=0.35)
    m["sfx.celebration"] = reverb(mix((0, mix(*[(i * 0.1, tone(hz(n), 0.35, "pluck", dec=0.3)) for i, n in enumerate((67, 72, 76, 79, 84))])), (0, 0.3 * noise(0.4, 3000, 9000) * env(int(0.4 * SR), 0.01, 0.35))))
    m["sfx.bird_fly"] = mix(*[(i * 0.09, 0.6 * tone(hz(91 + (i % 3) * 2), 0.07, "sine", dec=0.06, glide=hz(96))) for i in range(5)])
    m["sfx.robot_dance"] = mix(*[(i * 0.1, 0.6 * tone(hz(n), 0.09, "soft_square", dec=0.07)) for i, n in enumerate((72, 79, 76, 84, 79, 72))])
    # gentle animal-like motifs (instrumental imitations, not recordings)
    m["sfx.animal.cat"] = tone(hz(79), 0.45, "flute", a=0.04, dec=0.3, sus=0.4, rel=0.1, vib=0.02, glide=hz(74)) * 0.8
    m["sfx.animal.dog"] = mix((0, tone(hz(64), 0.12, "soft_square", dec=0.1, glide=hz(60))), (0.18, tone(hz(64), 0.12, "soft_square", dec=0.1, glide=hz(60))))
    m["sfx.animal.bird"] = mix(*[(i * 0.11, tone(hz(93), 0.08, "sine", dec=0.07, glide=hz(98 - i))) for i in range(3)])
    m["sfx.animal.elephant"] = tone(hz(50), 0.7, "soft_square", a=0.06, dec=0.3, sus=0.5, rel=0.15, vib=0.03, glide=hz(57)) * 0.8
    m["sfx.animal.rabbit"] = mix(*[(i * 0.12, 0.6 * tone(hz(86), 0.06, "marimba", dec=0.05)) for i in range(3)])
    m["sfx.animal.lion"] = noise(0.6, 80, 600) * env(int(0.6 * SR), 0.1, 0.3, 0.4, 0.15) * 1.2
    m["sfx.candle_blow"] = noise(0.6, 400, 3000) * env(int(0.6 * SR), 0.05, 0.4, 0.2, 0.1)
    m["sfx.water_pour"] = reverb(mix(*[(i * 0.06, 0.4 * tone(hz(84 + RNG.integers(0, 8)), 0.1, "sine", dec=0.08, glide=hz(96))) for i in range(9)]))
    m["sfx.door_open"] = mix((0, tone(hz(55), 0.3, "pluck", dec=0.25)), (0.15, tone(hz(67), 0.4, "bell", dec=0.35) * 0.5))
    return m


# ------------------------------------------------------------------- music
def loop(name, bpm, bars, chords, melody_fn, kind_lead, kind_bass="pluck", pad=True, swing=0.0):
    beat = 60 / bpm; bar = 4 * beat; total = bars * bar; n = int(round(total * SR))
    out = np.zeros(n + SR * 3)
    def put(off, x, g=1.0):
        o = int(off * SR) % n
        seg = x * g
        end = o + len(seg)
        if end <= len(out): out[o:end] += seg
    for b in range(bars):
        root, quality = chords[b % len(chords)]
        third = 3 if quality == "m" else 4
        tri = [root, root + third, root + 7]
        if pad:
            for k, nn in enumerate(tri): put(b * bar, tone(hz(nn + 12), bar + 0.4, "pad", a=0.6, dec=0.5, sus=0.6, rel=0.6) * 0.09)
        put(b * bar, tone(hz(root - 12), beat * 1.6, kind_bass, dec=beat * 1.4) * 0.35)
        put(b * bar + 2 * beat, tone(hz(root - 5), beat * 1.6, kind_bass, dec=beat * 1.4) * 0.3)
        for s in range(8):
            if (b * 8 + s) % 2 == 0 or s in (3, 7):
                put(b * bar + s * beat / 2 + (swing * beat / 2 if s % 2 else 0), tone(hz(tri[s % 3] + 24), 0.25, "marimba", dec=0.2) * 0.07)
        for (pos, nn, dur) in melody_fn(b, tri):
            put(b * bar + pos * beat, tone(hz(nn), dur * beat, kind_lead, a=0.01, dec=dur * beat * 0.8, sus=0.15, rel=0.1, vib=0.006) * 0.22)
    # wrap the tail (reverb/decays past the end) back to the start → seamless loop
    tail = out[n:].copy(); x = out[:n].copy(); x[:len(tail)] += tail
    x = reverb(x, wet=0.12)
    return finish(x, -6.0, fade=0)


def music():
    rng = np.random.default_rng(7)
    def mel(scale_bias):
        def f(b, tri):
            notes = []
            pattern = [(0, 0, 1), (1, 1, 1), (2, 2, 1.5), (3.5, 1, 0.5)] if b % 2 == 0 else [(0, 2, 2), (2, 1, 1), (3, 0, 1)]
            for pos, idx, dur in pattern:
                if rng.random() < 0.85: notes.append((pos, tri[idx] + 12 + scale_bias, dur))
            return notes
        return f
    C, F, G, Am, Dm, Em = (60, ""), (65, ""), (67, ""), (57, "m"), (62, "m"), (64, "m")
    Bb, Eb = (58, ""), (63, "")
    return {
        "music.home_meadow": loop("home", 88, 24, [C, Am, F, G, C, Em, F, G], mel(0), "marimba"),
        "music.play_parade": loop("play", 112, 32, [F, C, Dm, Bb, F, C, Bb, C], mel(0), "pluck", swing=0.25),
        "music.dream_sky": loop("dream", 72, 16, [Eb, Bb, (60, "m"), (65, "m")], mel(0), "bell"),
    }


def write(name, x, sub):
    os.makedirs(os.path.join(OUT, sub), exist_ok=True)
    path = os.path.join(OUT, sub, f"{name}.ogg")
    pcm = np.clip(x, -1, 1)
    with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tf:
        with wave.open(tf.name, "wb") as w:
            w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR); w.writeframes((pcm * 32767).astype("<i2").tobytes())
        q = "3" if sub == "music" else "4"
        subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", tf.name, "-c:a", "libvorbis", "-q:a", q, path], check=True)
        os.unlink(tf.name)
    peak = float(np.max(np.abs(x))); rms = float(np.sqrt(np.mean(x ** 2)))
    return {"id": name, "file": f"audio/{sub}/{name}.ogg", "seconds": round(len(x) / SR, 2), "peakDb": round(20 * np.log10(peak + 1e-9), 1),
            "rmsDb": round(20 * np.log10(rms + 1e-9), 1), "clipped": bool(peak > 0.999), "bytes": os.path.getsize(path),
            "loop": sub == "music", "license": "Original; synthesised by tools/audio/synth.py for Colora World (project-owned)"}


def main():
    entries = []
    for k, v in ui().items(): entries.append(write(k, finish(v, -4.0), "sfx"))
    for k, v in toys().items(): entries.append(write(k, finish(v, -4.0), "sfx"))
    for k, v in music().items(): entries.append(write(k, v, "music"))
    problems = [e["id"] for e in entries if e["clipped"] or (not e["loop"] and e["seconds"] > 2.5) or (e["loop"] and not 45 <= e["seconds"] <= 120)]
    manifest = {"schemaVersion": 1, "sampleRate": SR, "entries": entries, "problems": problems,
                "voice": {"status": "blocked", "reason": "No licensed EN/VI voice recordings or TTS service available in the build environment; see docs/KNOWN_ISSUES.md"}}
    with open(os.path.join(OUT, "manifest.json"), "w") as f: json.dump(manifest, f, indent=1)
    print(len(entries), "clips;", sum(e["bytes"] for e in entries) // 1024, "KiB; problems:", problems)
    for e in entries:
        if e["loop"]: print(e["id"], e["seconds"], "s", e["bytes"] // 1024, "KiB")


if __name__ == "__main__":
    main()
