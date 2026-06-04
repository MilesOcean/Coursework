# AI-Assisted Honor of Kings Information Management System

## 1. Project Overview

This is a Java console application that manages information for an *Honor of Kings* themed dataset: players, heroes, equipment, teams, and match records. The system supports role-based access (Player / Admin), CSV-based data persistence, leaderboard ranking, and a menu-driven CLI.

The project was developed as a Java OOP coursework under an AI-assisted workflow. All design decisions, code triage, and final acceptance were made by the author; AI was used as a constrained code assistant whose outputs were reviewed before commit. Full prompt records, agent logs, and reflection are stored under `ai/` for assessor inspection.

- **Language:** Java 17
- **Build:** plain `javac` / `java` — no external build tool required
- **Dependencies:** JDK standard library only
- **Persistence:** CSV files under `data/`
- **UI:** Console (text menus, `Scanner` input)

---

## 2. How to Run

### 2.1 Compile

From the project root (macOS / Linux):

```
javac -d out $(find src -name "*.java")
```

On Windows PowerShell:

```
javac -d out (Get-ChildItem -Recurse src -Filter *.java | % { $_.FullName })
```

### 2.2 Run

```
java -cp out Main
```

### 2.3 First-run behaviour

On first launch the program detects that `data/players.csv`, `data/heroes.csv`, `data/equipment.csv`, `data/teams.csv`, and `data/matches.csv` are empty or missing, calls `DataInitializer` to seed the in-memory dataset, and persists it to CSV before showing the login menu. On subsequent runs the CSV files are loaded directly and the console prints `Loaded from CSV`.

### 2.4 Exit

Choose `Logout` from any menu to return to the login screen. Closing the program after logout flushes all data to CSV before the JVM exits.

---

## 3. Default Login Accounts

The default accounts below are created by `DataInitializer` on first run. Passwords are stored as SHA-256 hashes with a per-user salt; the plain-text values listed here are for grading and demonstration only.

| Role   | Username    | Password    | Notes                                  |
|--------|-------------|-------------|----------------------------------------|
| Admin  | `admin`     | `admin`    | Full access to data management menus   |
| Player | `menglei`   | `123456`   | Sample player, member of AG超玩会      |
| Player | `feiniu`    | `123456`   | Sample player, member of 重庆狼队      |
| Player | `huahai`    | `123456`   | Sample player, member of 武汉eStarPro  |

A total of 15 player accounts are seeded (5 per team); the three above are the most useful for demonstrating leaderboard and team-overview features.

---

## 4. Implemented Features

All eight functional areas from the specification are implemented:

| § in spec | Feature              | Status      | Entry point in CLI                    |
|-----------|----------------------|-------------|---------------------------------------|
| 5.1       | Player Lookup        | Done        | Admin Menu → "Player Lookup"          |
| 5.2       | Team Overview        | Done        | Player Menu → "View Teams"            |
| 5.3       | Hero Details         | Done        | Player Menu → "View Heroes"           |
| 5.4       | Equipment Statistics | Done        | Admin Menu → "Equipment Statistics"   |
| 5.5       | Match History        | Done        | Player Menu → "View Match History"    |
| 5.6       | Leaderboard          | Done        | Player Menu → "View Leaderboard"      |
| 5.7       | Data Management      | Done        | Admin Menu → "Data Management"        |
| 5.8       | Authentication       | Done        | Main login screen                     |

Additional behaviour:

- Role-based menu routing (Admin and Player see different options).
- Editable player nickname for the logged-in player.
- Deterministic leaderboard tie-breaking (wins → level → nickname).
- CSV persistence on every state-mutating action.

---

## 5. Java Concepts Used

The implementation exercises the OOP concepts required by §3.2 of the specification:

| Concept             | Where it appears                                              |
|---------------------|---------------------------------------------------------------|
| Abstract class      | `model.Person` (superclass of `Player`, `Admin`)              |
| Inheritance         | `Player extends Person`, `Admin extends Person`               |
| Interfaces          | `Authenticatable`, `Rankable`, `CsvPersistable` under `interfaces/` |
| Encapsulation       | All model fields are `private`; access via getters/setters   |
| Polymorphism        | `Person`-typed login result dispatched to role-specific menu  |
| Enums               | `Role`, `HeroType`, `EquipmentType`, `MatchResult`            |
| Collections         | `ArrayList`, `HashMap`, `Collections.unmodifiableList/Map`    |
| Generics            | Service-layer list operations and `Repository`-style methods  |
| File I/O            | `CsvUtil` for read/write; `FileService` for orchestration     |
| Exception handling  | Try/catch around all I/O and parsing; user-friendly error msgs|
| Static utilities    | `PasswordHasher` (SHA-256 + salt), `CsvUtil`, `DataInitializer` |
| Lambda / streams    | Leaderboard sort and filtered lookups                         |

A full class-by-class explanation is available in `docs/design.md`.

---

## 6. AI Usage Summary

### 6.1 Tool and model

- **Tool:** Claude Code CLI
- **Backend model:** DeepSeek V4 Pro
- All interactions occurred through this single tool. No other AI assistants were used at any phase.

### 6.2 Multi-agent workflow

Following Appendix A.9 of the specification, the same tool was rotated through four agent roles, each with a narrow scope:

| Agent role         | Purpose                                                | Phases    |
|--------------------|--------------------------------------------------------|-----------|
| Design Agent       | Class design, package layout, UML draft                | 1, 3      |
| Implementation Agent | Layer-by-layer code generation, spec-anchored        | 2, 4–8    |
| Debugging Agent    | Targeted bug isolation (e.g. CSV quoted-comma parser)  | 6, 9      |
| Testing / Reviewer | Static review and test-case drafting                   | 9, 10     |

The Testing / Reviewer Agent surfaced 10 issues across the codebase. The author accepted 6 and rejected 4 as out-of-scope or over-engineering; full triage is recorded in `ai/agent-log.md`.

### 6.3 Evidence files

- `ai/prompts.md` — every prompt issued, with phase, intent, and acceptance notes
- `ai/agent-log.md` — agent-role handoffs, reviewer triage decisions, tester evidence rules
- `ai/reflection.md` — what worked, what failed, and what the author would do differently

---

## 7. Testing Summary

Manual testing was performed against the running CLI. Test cases, inputs, expected outputs, actual outputs, and pass/fail verdicts are recorded in `docs/test-cases.md`.

| Metric             | Value         |
|--------------------|---------------|
| Total test cases   | 19            |
| Passed             | 18            |
| Failed             | 1 (TC-19)     |
| Not applicable     | 0             |
| Pass rate          | 94.7%         |

Coverage breakdown:

- Functional cases (§5.1 – §5.8): TC-01 through TC-14 — all PASS
- Regression cases for accepted Reviewer fixes: TC-15, TC-16, TC-17, TC-18 — all PASS
- Edge / robustness cases: TC-19 (piped-EOF) — FAIL, documented in §8

All "Actual output" entries in `docs/test-cases.md` were produced by real CLI runs on the author's machine and pasted verbatim. The Testing / Reviewer Agent drafted candidate test cases; the author executed and verified each one.

---

## 8. Known Limitations

These are limitations the author is aware of and chose not to fix within the coursework scope. Each is documented honestly to support the reflection requirement in §6.4 of the specification.

1. **Admin "Data Management" supports add, delete, and edit operations** for all five entity types (players, heroes, equipment, teams, matches).

2. **CSV write path is not symmetric with the new parser.** The read path correctly handles double-quoted fields containing commas (TC-15 PASS). The write path concatenates fields and then re-splits on `,`, so a field containing a literal comma would not round-trip cleanly. This is latent rather than live because no seed field contains a comma and no user-facing input path admits one (nickname editing is the only writable field and is validated).

3. **`Scanner.nextLine()` is unguarded against EOF** (TC-19 FAIL). When standard input is piped from a file that ends mid-prompt, the program throws `NoSuchElementException` and exits with code 1 instead of shutting down cleanly. Interactive terminal use is unaffected; only scripted pipelines trigger this.

4. **Passwords are hashed but the salt store is not rotated.** The per-user salt is generated once at seed time and never rotated. For a coursework console app this is acceptable; a production system would need a password-reset flow.

---

*For full design rationale see `docs/design.md`. For AI-use evidence see `ai/prompts.md`, `ai/agent-log.md`, and `ai/reflection.md`. For test evidence see `docs/test-cases.md`.*
