#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if [ ! -f verification/level-catalog.json ]; then python3 scripts/build-levels.py; fi
python3 tests/check-layouts.py
mkdir -p out/classes
java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -d out/classes app/src/main/java/pro/brickstorm/game/LevelData.java app/src/main/java/pro/brickstorm/game/Levels.java app/src/main/java/pro/brickstorm/game/Engine.java app/src/main/java/pro/brickstorm/game/Painter.java app/src/main/java/pro/brickstorm/game/RewardBank.java app/src/main/java/pro/brickstorm/game/AppModel.java app/src/main/java/pro/brickstorm/game/GameRenderer.java tests/EngineTest.java tests/RewardTest.java
java -Djava.awt.headless=true -cp out/classes pro.brickstorm.game.EngineTest
java -Djava.awt.headless=true -cp out/classes pro.brickstorm.game.RewardTest
