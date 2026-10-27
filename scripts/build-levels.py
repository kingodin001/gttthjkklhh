#!/usr/bin/env python3
"""Build the 300 enclosed mazes; no old campaign is included."""
from pathlib import Path
import runpy
if __name__=='__main__':
    runpy.run_path(str(Path(__file__).with_name('maze-levels.py')),run_name='__main__')
