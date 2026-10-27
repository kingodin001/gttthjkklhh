# Arcade artwork — 1.11.0

- `app/src/main/res/drawable-nodpi/arcade_menu.png`: exact, unmodified approved menu composition generated earlier with built-in ImageGen. The logo, maze, paddle and play/levels/progress/settings visual plates are the approved pixels. Logical composition 400×820, stretched as one surface within a fixed aspect viewport (no cropping). Touch controls use the same coordinates as their visual plates. Live score/stock/timer/level/progress values cover the reference placeholders with Canvas panels; interactive screens provide actual progress and reward information.
- `app/src/main/res/drawable-nodpi/arcade_background.png`: built-in ImageGen edit of the approved blue gameplay reference, wallpaper only. No baked UI, bricks, ball or paddle. Used on gameplay and dimmed on secondary screens. The original game board/HUD and controls render above it.

Final background prompt: “Extract/reconstruct ONLY the royal-blue arcade wallpaper. Keep deep cobalt blue, cyan diagonal light streaks and small luminous blue 3D cubes at left/right edges. Center 80% calm blue. Remove all UI, status/navigation bars, text, buttons, bricks, maze, frames, paddle, ball, scoreboard. No UI, text, logo or objects in center. Portrait 1:2.” Built-in ImageGen; no external image API.

Both production assets are committed to the repository. The previous menu artwork is removed from the release. Both Android Canvas and the desktop verification renderer load these same files. No image-generation service or network request is used in the installed game.

## Approved orange icon — 1.13.0

`rustore/icon-orange-master.png` is the exact orange variant approved in conversation, generated with built-in ImageGen (palette edit of the approved maze icon). Prompt: “Keep dense tiny bricks, metallic maze, bottom entrance, white ball and paddle composition; change all bricks to cohesive orange/amber, background to restrained deep royal blue, silver walls, gold-orange paddle, no rainbow regions or text.” Only deterministic resizing is used for launcher densities and `rustore/icon-512.png`; no repainting or additional image generation. `scripts/package-icon.py` reproduces all packaged sizes from the committed master. Menu and game wallpaper are unchanged.
