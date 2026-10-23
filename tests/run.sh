#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p out/classes
java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -d out/classes app/src/main/java/pro/brickstorm/game/LevelData.java app/src/main/java/pro/brickstorm/game/Levels.java app/src/main/java/pro/brickstorm/game/Engine.java app/src/main/java/pro/brickstorm/game/Painter.java app/src/main/java/pro/brickstorm/game/AppModel.java app/src/main/java/pro/brickstorm/game/GameRenderer.java tests/EngineTest.java
java -Djava.awt.headless=true -cp out/classes pro.brickstorm.game.EngineTest
