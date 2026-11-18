"""Children's palettes (separate from UI colours, spec §3.2). White has an outline in UI."""
PALETTES = {
 "bright_basic": ["#F25C54", "#FFA94D", "#FFE066", "#4FB477", "#42C8BD", "#5B8DEF", "#9B6BDE", "#FFA8C5", "#B07A55", "#B9C1CC", "#24324B", "#FFFFFF"],
 "warm": ["#E8453C", "#FF786D", "#FF9F43", "#FFC94D", "#FFE066", "#F7B6A3", "#D9A47E", "#B5652B", "#FFD6A5", "#C2410C"],
 "cool": ["#3E6FC2", "#5B8DEF", "#8BD4F6", "#42C8BD", "#7FD8C7", "#4FB477", "#9B6BDE", "#B8A0F3", "#5A6B8C", "#DDEBFF"],
 "pastel": ["#FFC8D9", "#FFE6E0", "#FFF3B8", "#D7F5C8", "#C6F1EC", "#CFE7FF", "#E2D6FF", "#F6E3CF", "#FFFFFF", "#E4E7EC"],
 "rainbow": ["#F25C54", "#FFA94D", "#FFE066", "#4FB477", "#5B8DEF", "#9B6BDE"],
 "earth": ["#6E4630", "#8A5A3C", "#B07A55", "#D9B48F", "#8DB860", "#5E8C3A", "#C9A227", "#7D8796", "#3B2A23", "#E8C39E"],
 "nature": ["#2E8B57", "#4FB477", "#9DDC86", "#C9E77B", "#8DB860", "#B07A55", "#8BD4F6", "#FFE066", "#FF786D", "#FFFFFF"],
 "night": ["#1F2A4D", "#34548D", "#5A6B8C", "#9B6BDE", "#B8A0F3", "#FFE9A8", "#FFD66B", "#DDEBFF", "#24324B", "#FFFFFF"],
 "skin": ["#FBE3D0", "#F6D3B3", "#E8B894", "#D9A47E", "#C08A63", "#A86F4C", "#8A5A3C", "#6E4630", "#4E3324", "#3B2A23"],
}
EXTENDED = sorted({c for v in PALETTES.values() for c in v})
