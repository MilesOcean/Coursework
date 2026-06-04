# Honor of Kings Information Management System — Project Plan

> This plan strictly follows the 12-section structure required by §8 of the assignment.

---

## 1. Project Goal

Build a **console-based Java application** that manages Honor of Kings (王者荣耀) information: players, heroes, equipment, teams, and match history. The system supports two user roles (Admin / Player), simple login/logout, and reads/writes data from CSV files in `data/`.

The application must demonstrate the core Java concepts taught in this course: classes, inheritance, abstract classes, interfaces, polymorphism, enums, collections, generics, file I/O, and exception handling. All deliverables (plan, design, UML, tests, AI logs) follow the structure required in §9–§11.

**Out of scope:** GUI, network play, real-time match simulation, database.

---

## 2. Requirement Analysis

### 2.1 Player Lookup
Search a player by ID or name. Show ID, name, team, level, win rate, owned heroes, and each hero's equipped items. If no match → clear error, no crash.

### 2.2 Team Overview
Search a team by ID or name. Show all members, average level, total matches, win rate, and the top player (by win rate, level as tiebreaker).

### 2.3 Hero Details
Search a hero by name. Show name, hero type, base stats, compatible equipment, and players who own the hero.

### 2.4 Equipment Statistics
Rank equipment by **usage count**. Tie → alphabetical by name.

### 2.5 Match History
Retrieve the last N matches for a player or team: opponent, date, result, heroes picked, win/loss record, hero pick rate.

### 2.6 Leaderboard
Top X players by win rate. Tie order: win rate → level → name (alphabetical).

### 2.7 Data Management
- **Admin:** add / edit / delete players, heroes, equipment, teams, match records.
- **Player:** view own info, edit limited personal info, view heroes/teams/matches/leaderboard.

### 2.8 Authentication
Simple login/logout with two roles (Admin / Player). Passwords stored as SHA-256 hash with per-user salt; CSV never stores plain text.

---

## 3. Java Concepts Used

| Concept              | Where it appears                                                              |
|----------------------|-------------------------------------------------------------------------------|
| Class / Object       | All `model/` classes                                                          |
| Abstract class       | `Person`                                                                      |
| Inheritance          | `Player extends Person`, `Admin extends Person`                               |
| Interface            | `Authenticatable`, `Rankable`, `CsvPersistable`                               |
| Polymorphism         | `Person ref → Player / Admin`; service methods accept interface types         |
| Enum                 | `Role`, `HeroType`, `Rank`, `MatchResult`, `EquipmentType`                    |
| Collections          | `List<Hero>`, `Map<Hero, List<Equipment>>`, `Set<String>`                     |
| Generics             | `CsvUtil<T>`, `List<Player>`, `Map<K,V>`                                      |
| File I/O             | `FileService` reads/writes CSV in `data/`                                     |
| Exception handling   | `try/catch` around I/O; custom `ValidationException`                          |
| Lambda / Streams     | Sorting leaderboard, filtering matches                                        |
| Static utility       | `PasswordUtil`, `Validator`, `CsvUtil`                                        |

---

## 4. Class Design

### 4.1 Package Structure

    src/
    ├── model/
    │   ├── Person.java (abstract)
    │   ├── Player.java
    │   ├── Admin.java
    │   ├── Hero.java
    │   ├── Equipment.java
    │   ├── Team.java
    │   └── MatchRecord.java
    ├── enums/
    │   ├── Role.java
    │   ├── HeroType.java
    │   ├── Rank.java
    │   ├── MatchResult.java
    │   └── EquipmentType.java
    ├── interfaces/
    │   ├── Authenticatable.java
    │   ├── Rankable.java
    │   └── CsvPersistable.java
    ├── service/
    │   ├── AuthService.java
    │   ├── HeroService.java
    │   ├── PlayerService.java
    │   ├── TeamService.java
    │   ├── MatchService.java
    │   ├── LeaderboardService.java
    │   └── FileService.java
    ├── util/
    │   ├── CsvUtil.java
    │   ├── PasswordUtil.java
    │   └── Validator.java
    └── Main.java

### 4.2 Enums

| Enum            | Values                                                                            |
|-----------------|-----------------------------------------------------------------------------------|
| `Role`          | PLAYER, ADMIN                                                                     |
| `HeroType`      | WARRIOR, MAGE, ASSASSIN, MARKSMAN, SUPPORT, TANK                                  |
| `Rank`          | BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MASTER, GRANDMASTER, LEGEND              |
| `MatchResult`   | WIN, LOSE, DRAW                                                                   |
| `EquipmentType` | ATTACK, DEFENSE, MAGIC, MOVEMENT, JUNGLE, SUPPORT                                 |

### 4.3 Interfaces

- **`Authenticatable`** — `login(u,p)`, `logout()`, `isAuthenticated()`. Implemented by `Person`.
- **`Rankable`** — `getRankValue()`, `getRank()`, `setRank(Rank)`. Implemented by `Player`, `Team`.
- **`CsvPersistable`** — `toCsvRow(): String`. Implemented by all entities that go to CSV.

### 4.4 Key Classes

| Class         | Key fields                                                                                  | Key methods                              |
|---------------|---------------------------------------------------------------------------------------------|------------------------------------------|
| `Person`      | id, username, passwordHash, salt, nickname                                                  | `getRole()` abstract                     |
| `Player`      | level, rank, winCount, matchCount, heroPool, equippedItems, teamId                          | `getWinRate()`                           |
| `Admin`       | managedTeamIds                                                                              | `getRole()` → ADMIN                      |
| `Hero`        | id, name, type, baseAttack, baseDefense, baseHp, compatibleEquipmentIds                     | `toCsvRow()`                             |
| `Equipment`   | id, name, type, price, statBonuses                                                          | `toCsvRow()`                             |
| `Team`        | id, name, captainId, rank, memberIds                                                        | `getAverageLevel()`, `getTopPlayer()`    |
| `MatchRecord` | id, date, team1Id, team2Id, result, heroPicks, mvpPlayerId, durationMinutes                 | `toCsvRow()`                             |

---

## 5. UML Draft

The final UML class diagram is exported as **`docs/uml.png`** (generated from `docs/uml.puml`). Draft summary:

    Authenticatable (I)        Rankable (I)        CsvPersistable (I)
            ▲                       ▲                     ▲
            │                       │                     │
       Person (abstract) ◄── implements                   │
            ▲                                             │
       ┌────┴────┐                                        │
     Player    Admin     Hero, Equipment, Team, MatchRecord
       │                       all implement CsvPersistable
       │ owns *           Player, Team also implement Rankable
       ├──► Hero ──*── compatible ──*── Equipment
       └──► Team ◄── * members
             ▲
             │ 2 participants
       MatchRecord

---

## 6. Data Design

All data lives in `data/` as UTF-8 CSV with a header row. Primary keys are bold.

| File             | Columns                                                                                                              |
|------------------|----------------------------------------------------------------------------------------------------------------------|
| `players.csv`    | **id**, username, passwordHash, salt, nickname, level, rank, winCount, matchCount, teamId                            |
| `admins.csv`     | **id**, username, passwordHash, salt, nickname                                                                        |
| `heroes.csv`     | **id**, name, type, baseAttack, baseDefense, baseHp, compatibleEquipmentIds (`;`-separated)                          |
| `equipment.csv`  | **id**, name, type, price, attackBonus, defenseBonus, magicBonus, description                                        |
| `teams.csv`      | **id**, name, captainId, rank, memberIds (`;`-separated)                                                              |
| `matches.csv`    | **id**, date (YYYY-MM-DD), team1Id, team2Id, result, mvpPlayerId, durationMinutes, heroPicks (`team1:h1;h2\|team2:h3;h4`) |
| `loadouts.csv`   | **playerId**, **heroId**, equipmentIds (`;`-separated)                                                                |

**Relations:** Player.teamId → Team.id; Team.memberIds → Player.id; MatchRecord.team*Id → Team.id; Loadout (playerId, heroId) → Player + Hero; equipmentIds → Equipment.id.

**Integrity rules:** referential integrity checked on load; orphaned references logged and skipped; `winCount ≤ matchCount`; team size 1–5.

---

## 7. AI Usage Plan

AI (ChatGPT) will be used as a **planning assistant and code reviewer**, never as a blind code generator. Planned use cases:

1. **Plan / design review** — check structural compliance with §8 and consistency between plan.md, design.md, UML.
2. **Boilerplate scaffolding** — generate skeletons for enums, interfaces, CSV header parsing; I rewrite logic myself.
3. **Edge-case brainstorming** — list tricky inputs for test-cases.md (empty CSV, malformed row, tie-breaking).
4. **Debugging companion** — paste stack trace + minimal code, ask for hypotheses; I verify before applying.
5. **Refactor suggestions** — naming, duplication, exception granularity.

**Will NOT use AI for:** writing reflection.md, copying full classes without understanding, generating fake test results.

All prompts logged in `ai/prompts.md`; all sessions in `ai/agent-log.md`; honest reflection in `ai/reflection.md`.

---

## 8. Prompt Strategy

Prompts follow a **Role + Context + Task + Constraint + Format** template:

- **Role:** "You are a Java code reviewer for a beginner course project."
- **Context:** paste relevant section of plan.md / design.md / code.
- **Task:** one clear ask ("review", "suggest test cases", "explain error").
- **Constraint:** "Java 17, no external libs, console only, ≤ 50 lines."
- **Format:** "answer in bullet list" / "show diff" / "table".

**Iteration rules:**
1. First prompt = small scope (one class / one method).
2. If output is wrong → reply with the specific error, not "try again".
3. Never accept code I cannot explain line-by-line.
4. Log prompt + verdict (accepted / modified / rejected) in `ai/prompts.md`.

---

## 9. Development Timeline

10-day plan (≈ 2 h/day):

| Day | Milestone                                                              | Deliverable                          |
|-----|------------------------------------------------------------------------|--------------------------------------|
| 1   | Finalize plan.md + design.md + uml.png                                 | `docs/` first commit                 |
| 2   | Enums + interfaces + `Person` / `Player` / `Admin`                     | model layer compiles                 |
| 3   | `Hero`, `Equipment`, `Team`, `MatchRecord` + unit smoke tests          | all models compile                   |
| 4   | `CsvUtil`, `FileService`, sample CSV files in `data/`                  | round-trip read/write works          |
| 5   | `AuthService` + `PasswordUtil` + login/logout CLI                      | login flow demo                      |
| 6   | `PlayerService`, `HeroService` (search, CRUD)                          | feature 2.1 / 2.3 working            |
| 7   | `TeamService`, `MatchService` (overview, last-N matches)               | features 2.2 / 2.5 working           |
| 8   | `LeaderboardService` + equipment stats + tie-breaking                  | features 2.4 / 2.6 working           |
| 9   | Full CLI menu in `Main`, role-based access, validation polish          | end-to-end manual test               |
| 10  | Fill test-cases.md, finalize ai/ logs, write reflection.md, submit zip | final submission                     |

Buffer: weekend can absorb 1–2 days slip.

---

## 10. Testing Plan

Testing is **manual + scripted CLI runs**, documented in `docs/test-cases.md`. Each test case has: ID, feature, input, expected output, actual output, pass/fail.

**Categories:**

1. **Happy-path** — one per requirement in §2 (≥ 8 cases).
2. **Boundary** — empty CSV; 1-member team; player with 0 matches; N larger than history size.
3. **Invalid input** — non-existent ID/name; malformed CSV row; wrong password; non-numeric where number expected.
4. **Authorization** — Player attempting Admin-only operation → denied.
5. **Tie-breaking** — leaderboard with equal win rates; equipment with equal usage.
6. **Persistence** — add → exit → restart → data still present.

Target: **≥ 20 test cases**, all reproducible from a documented seed dataset in `data/`.

---

## 11. Risk Analysis

| Risk                                          | Likelihood | Impact | Mitigation                                                          |
|-----------------------------------------------|------------|--------|---------------------------------------------------------------------|
| CSV parsing breaks on commas inside fields    | M          | H      | Use `;` as internal separator; quote fields containing `,`          |
| Scope creep (GUI, network, animations)        | M          | H      | Freeze §2 requirements; new ideas → "future work" section           |
| Over-reliance on AI → cannot explain own code | M          | H      | Rule: never paste code I haven't read; log every accept/reject      |
| Forgetting tie-breaking rules                 | H          | M      | Write tie-break tests on Day 8 before implementation                |
| Losing work / no version history              | L          | H      | Daily `git commit`; export `git-history.txt` for `ai/` folder       |
| Time underestimate on services layer          | M          | M      | Buffer day 10; cut "nice-to-have" CLI polish first                  |
| Hash/salt bug locks out admin                 | L          | H      | Keep a seed admin account in `data/admins.csv` with known password  |
| UML drifts from code                          | M          | M      | Regenerate `uml.png` from `uml.puml` on Day 9 after code freeze     |

---

## 12. Final Reflection Placeholder

> To be completed after submission and stored in `ai/reflection.md`. Will answer the 10 questions from §6.4 of the assignment:

1. Which parts of the project did AI help with the most?
2. Which parts did I do entirely on my own?
3. Did AI ever give wrong or misleading suggestions? How did I detect them?
4. How did I verify AI-generated code before using it?
5. What did I learn about Java by reviewing AI output?
6. How did my prompts evolve over the project?
7. What would I do differently next time when collaborating with AI?
8. Did AI improve my productivity? By how much (rough estimate)?
9. Did AI affect my understanding — positively or negatively?
10. What is one concrete example where I rejected AI's suggestion and did it my way?
