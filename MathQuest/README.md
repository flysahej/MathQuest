# MathQuest — Java Edition

A math board game where players answer questions to advance across a 3×5 board.
Built with Java Swing. Data saved locally using SQLite (no server, no internet needed).

---

## Prerequisites

- **Java JDK 11 or higher**
  Download: https://adoptium.net  |  Check: `java -version`
- **IntelliJ IDEA** (recommended) or VS Code with Extension Pack for Java

---

## One-time setup — download two JARs

Place both in the `lib/` folder inside this project.

### 1. SQLite JDBC driver (required — saves game data)
https://github.com/xerial/sqlite-jdbc/releases/latest
→ Download `sqlite-jdbc-3.x.x.jar`

### 2. JUnit 4 (required only to run tests)
https://github.com/junit-team/junit4/releases/latest
→ Download `junit-4.x.x.jar` and `hamcrest-core-1.3.jar`

---

## Project structure

```
MathQuest/
├── assets/                          ← game images (already included)
│   ├── transparentduck.png          — player tokens, END square, duck dialogs
│   ├── pauseButton.png              — top-left pause button
│   ├── checkmark.png                — correct answer feedback
│   ├── redX.png                     — incorrect answer feedback
│   ├── StreakIcon.png               — streak indicator in score panel
│   └── arrow.png                    — player-moved notification
├── lib/                             ← put your JARs here
│   ├── sqlite-jdbc-3.x.x.jar        ← download (required)
│   ├── junit-4.x.x.jar              ← download (tests only)
│   └── hamcrest-core-1.3.jar        ← download (tests only)
├── src/comp2800_project/
│   ├── Main.java                    ← entry point — run this
│   ├── MainMenu.java                — main menu window
│   ├── GameBoard.java               — game board, all image rendering, save/load
│   ├── Game.java                    — canvas / render loop
│   ├── GameManager.java             — game manager stub
│   ├── Player.java                  — player data model
│   ├── Instructor.java              — instructor login  (PIN: 666473)
│   ├── Developer.java               — developer login   (PIN: 374666)
│   ├── IncorrectPasswordException.java
│   ├── QuestionGenerator.java       — generates math questions by level/row
│   ├── QuestionDialog.java          — 30-second question popup
│   └── DatabaseManager.java         ← SQLite save/load (mirrors Python queryManager.py)
├── tests/
│   ├── TestPlayer.java
│   ├── TestInstructor.java
│   └── TestDeveloper.java
└── README.md
```

The `mathquest.db` file is created automatically in the project root on first run.

---

## Running in IntelliJ IDEA

### Step 1 — Open the project
File → Open → select the `MathQuest/` folder

### Step 2 — Mark the source root
Right-click `src/` → Mark Directory as → Sources Root

### Step 3 — Add the JARs
File → Project Structure (Ctrl+Alt+Shift+S)
→ Libraries → "+" → Java → select all JARs from `lib/` → OK → Apply

### Step 4 — Run
Right-click `src/comp2800_project/Main.java` → Run 'Main.main()'

---

## Running in VS Code

1. Install **Extension Pack for Java**
2. Create `.vscode/settings.json`:
   ```json
   {
     "java.project.sourcePaths": ["src"],
     "java.project.referencedLibraries": ["lib/**/*.jar"]
   }
   ```
3. Open `Main.java` → click **Run** above `main()`

---

## Command line

**Mac / Linux:**
```bash
mkdir -p out
javac -cp "lib/*" -d out src/comp2800_project/*.java
java  -cp "out:lib/*" comp2800_project.Main
```

**Windows:**
```cmd
mkdir out
javac -cp "lib\*" -d out src\comp2800_project\*.java
java  -cp "out;lib\*" comp2800_project.Main
```

---

## Running the tests

**IntelliJ:** Right-click any file in `tests/` → Run

**Command line (Mac/Linux):**
```bash
javac -cp "lib/*:out" -d out src/comp2800_project/*.java tests/*.java
java  -cp "out:lib/*" org.junit.runner.JUnitCore \
    comp2800_project.TestPlayer \
    comp2800_project.TestInstructor \
    comp2800_project.TestDeveloper
```

**Windows:**
```cmd
javac -cp "lib\*;out" -d out src\comp2800_project\*.java tests\*.java
java  -cp "out;lib\*" org.junit.runner.JUnitCore ^
    comp2800_project.TestPlayer ^
    comp2800_project.TestInstructor ^
    comp2800_project.TestDeveloper
```

---

## How images are used

| Image | Size | Where used |
|---|---|---|
| `transparentduck.png` | 50×50 | Player token on every board square |
| `transparentduck.png` | 100×100 | END square decoration |
| `transparentduck.png` | 64×64 | Duck power-up and duck-earned dialogs |
| `pauseButton.png` | 40×40 | Top-left pause button |
| `checkmark.png` | 150×150 | Answer feedback when **correct** |
| `redX.png` | 150×150 | Answer feedback when **incorrect** |
| `StreakIcon.png` | 50×50 | Streak count in score panel and feedback |
| `arrow.png` | 40×40 | Player-moved notification dialog |

---

## Save / Load system

Data is stored in `mathquest.db` (SQLite, created automatically).
Same structure as Python:

```
game_id      → save slot 1, 2, or 3
level_number → current level
player_index → current board position (0–14)
players      → [{ name, streak, duck_count, score }]
```

Python function → Java method:
- `insert_game(game)` → `db.insertGame(gameId, level, playerIndex, players)`
- `find_game_by_id(id)` → `db.findGameById(gameId)`
- `get_player_scores()` → `db.getPlayerScores()`
- `get_player_info()` → `db.getPlayerInfo()`
- `update_player_score(name, score)` → `db.updatePlayerScore(name, score)`

---

## Login PINs

| Role | PIN | Permission |
|---|---|---|
| Player | — | 0 |
| Instructor | 666473 | 1 |
| Developer | 374666 | 2 |

---

## Troubleshooting

**"SQLite JDBC driver not found"**
→ JAR not in `lib/`. Re-do setup step above.

**Game window blank/black**
→ You ran `Game.java`. Always run `Main.java`.

**Images not showing**
→ Make sure the `assets/` folder is in the same directory you run the game from
  (the project root). In IntelliJ this is automatic.

**"cannot find symbol" errors**
→ `src/` is not marked as Sources Root. Right-click it → Mark Directory as → Sources Root.

**`mathquest.db` corrupted**
→ Delete `mathquest.db` from the project root and restart.
