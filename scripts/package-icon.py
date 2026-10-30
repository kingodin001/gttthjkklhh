#!/usr/bin/env python3
"""Resize the exact approved icon for Android densities and the store listing."""
from pathlib import Path
from PIL import Image

root=Path(__file__).resolve().parents[1]
image=Image.open(root/'rustore/icon-orange-master.png').convert('RGB')
assert image.width==image.height
for density,size in [('mdpi',48),('hdpi',72),('xhdpi',96),('xxhdpi',144),('xxxhdpi',192)]:
    image.resize((size,size),Image.Resampling.LANCZOS).save(root/f'app/src/main/res/mipmap-{density}/ic_launcher.png')
image.resize((512,512),Image.Resampling.LANCZOS).save(root/'rustore/icon-512.png')
