# Honor of Kings Information Management System — Design Document

> Companion to `plan.md`. This document describes **how** the system is built: architecture, class responsibilities, key algorithms, data flow, and error handling.

---

## 1. Architecture Overview

The system uses a **3-layer architecture** (no framework, plain Java):

    ┌──────────────────────────────────────────────┐
    │  Presentation Layer  (Main.java + CLI menus) │
    │  - Reads user input, prints output           │
    │  - No business logic                         │
    └────────────────────┬─────────────────────────┘
                         │ calls
    ┌────────────────────▼─────────────────────────┐
    │  Service Layer  (service/*.java)             │
    │  - AuthService, PlayerService, HeroService,  │
    │    TeamService, MatchService,                │
    │    LeaderboardService, FileService           │
    │  - Business rules, validation, sorting       │
    └────────────────────┬─────────────────────────┘
                         │ uses
    ┌────────────────────▼─────────────────────────┐
    │  Model Layer  (model/*.java + enums/)        │
    │  - Pure data + simple derived getters        │
    │  - Implements CsvPersistable                 │
    └────────────────────┬─────────────────────────┘
                         │ persisted by
    ┌────────────────────▼─────────────────────────┐
    │  Storage  (data/*.csv via util/CsvUtil)      │
    └──────────────────────────────────────────────┘

**Design principles**

- **Single Responsibility:** each service handles one domain (players, heroes, teams…).
- **Dependency direction:** Presentation → Service → Model. Model never imports Service.
- **Polymorphism over `if-role`:** `Person` subclasses + `Role` enum drive menu choice.
- **Fail loud, recover gracefully:** all I/O wrapped; user always returns to menu.

---

## 2. Class Responsibilities

### 2.1 Model Layer

| Class         | Responsibility                                                                 |
|---------------|--------------------------------------------------------------------------------|
| `Person`      | Abstract base. Holds id, username, passwordHash, salt, nickname. Implements `Authenticatable`. Declares abstract `getRole()`. |
| `Player`      | Game player. Adds level, rank, winCount, matchCount, `List<String> heroPool`, `Map<String, List<String>> equippedItems` (heroId → equipmentIds), teamId. Provides `getWinRate()`. Implements `Rankable`, `CsvPersistable`. |
| `Admin`       | System manager. Adds `List<String> managedTeamIds`. `getRole()` → `ADMIN`. Implements `CsvPersistable`. |
| `Hero`        | Hero data: id, name, `HeroType`, baseAttack, baseDefense, baseHp, `List<String> compatibleEquipmentIds`. |
| `Equipment`   | Equipment data: id, name, `EquipmentType`, price, `Map<String, Integer> statBonuses`. |
| `Team`        | Team data: id, name, captainId, `Rank`, `List<String> memberIds`. Provides `getAverageLevel()`, `getTopPlayer()`. Implements `Rankable`, `CsvPersistable`. |
| `MatchRecord` | One match: id, date, team1Id, team2Id, `MatchResult`, `Map<String,String> heroPicks` (playerId → heroId), mvpPlayerId, durationMinutes. |

### 2.2 Enum Layer

See `plan.md §4.2`. Enums are used to:
- replace magic strings,
- enable safe `switch` in menu dispatch,
- give `Rank` a natural ordering via `ordinal()` for leaderboard tiebreaks.

### 2.3 Interface Layer

| Interface         | Contract                                                                  | Implementers                  |
|-------------------|---------------------------------------------------------------------------|-------------------------------|
| `Authenticatable` | `login(u,p)`, `logout()`, `isAuthenticated()`                             | `Person` (so Player + Admin)  |
| `Rankable`        | `getRankValue()`, `getRank()`, `setRank(Rank)`                            | `Player`, `Team`              |
| `CsvPersistable`  | `toCsvRow(): String` — entity serializes itself                           | All persisted entities        |

### 2.4 Service Layer

| Service              | Key methods                                                                                              |
|----------------------|----------------------------------------------------------------------------------------------------------|
| `AuthService`        | `login(username, password) → Optional<Person>`, `logout(Person)`, `register(...)` (Admin only)           |
| `PlayerService`      | `findById`, `findByName`, `add`, `update`, `delete`, `listAll`                                           |
| `HeroService`        | `findById`, `findByName`, `addHero`, `deleteHero`, `getOwners(heroId)`, `getHeroesOfPlayer(playerId)` |
| `TeamService`        | `findById`, `findByName`, `addTeam`, `deleteTeam`, `addMember`, `removeMember`, `getMembers(team)`, `getTeamWinRate(team)`, `getTotalWins(team)`, `getTotalMatches(team)` |
| `MatchService`       | `findById`, `addMatch`, `deleteMatch`, `getByTeamId(teamId)`, `getByPlayerId(playerId)`, `getHeroPickRate(heroId, matches)`, `formatHeroPicks`, `formatResult` |
| `LeaderboardService` | `topByWinRate(n)`, `topByWins(n)`, `topByLevel(n)` — composite comparators with tie-breaking            |
| `FileService`        | `loadAll()` on startup, `saveAll()` on shutdown; delegates to `CsvUtil`                                  |

### 2.5 Util Layer

| Util            | Responsibility                                                                              |
|-----------------|---------------------------------------------------------------------------------------------|
| `CsvUtil<T>`    | Generic CSV read/write. Takes a `Function<String[], T>` parser and uses `T::toCsvRow`.      |
| `PasswordHasher` | `hash(password, salt)` via SHA-256; constant-time comparison via `MessageDigest`.            |

---

## 3. Key Algorithms

### 3.1 Leaderboard Sort (Top X Players)

Sort `List<Player>` using a composite `Comparator`:

1. `winRate` **descending**
2. `level` descending
3. `nickname` ascending (alphabetical, case-insensitive)

Implementation:

    players.stream()
        .sorted(Comparator
            .comparingDouble(Player::getWinRate).reversed()
            .thenComparing(Comparator.comparingInt(Player::getLevel).reversed())
            .thenComparing(p -> p.getNickname().toLowerCase()))
        .limit(x)
        .toList();

Complexity: **O(n log n)**.

### 3.2 Equipment Usage Ranking

1. Iterate every `Player.equippedItems` value list, count each equipmentId into a `Map<String, Integer>`.
2. Convert entries to a list, sort by:
    - count **descending**,
    - equipment name ascending on tie.
3. Return ranked list.

Complexity: **O(P·E + K log K)** where P=players, E=avg equipped, K=distinct equipment.

### 3.3 Team Overview

Given `teamId`:

1. Look up `Team`; collect `memberIds`.
2. For each member, fetch `Player`.
3. Compute:
    - `averageLevel = sum(levels) / count`
    - `totalMatches = sum(matchCount)`
    - `teamWinRate = sum(winCount) / sum(matchCount)`
    - `topPlayer = max by (winRate, level)`
4. Wrap in `TeamOverviewDTO` and return.

### 3.4 Last-N Match Lookup

1. Filter `matches` where `team1Id == id || team2Id == id` (or any `heroPicks.containsKey(playerId)`).
2. Sort by `date` descending.
3. `limit(n)`.

### 3.5 Authentication

1. User enters username + password.
2. `AuthService` finds `Person` by username.
3. Compute `PasswordUtil.hash(input, person.salt)`; compare to `person.passwordHash` with constant-time check.
4. On success → return `Optional<Person>`; CLI routes to Admin or Player menu via `person.getRole()`.
5. Failed attempts logged (in-memory counter); 5 fails → lock for current session.

---

## 4. Data Flow Examples

### 4.1 Startup

    Main.main()
      → FileService.loadAll()
          → CsvUtil.read("players.csv", Player::fromCsvRow)
          → CsvUtil.read("heroes.csv", Hero::fromCsvRow)
          → ... (equipment, teams, matches)
      → AuthService.promptLogin()
      → dispatch to AdminMenu or PlayerMenu

### 4.2 Admin adds a new Hero

    AdminMenu → "Add Hero"
      → read name, type, stats from stdin
      → Validator.checkNonEmpty(name), checkEnum(type, HeroType.class)
      → HeroService.add(hero)
          → in-memory list updated
      → FileService.saveAll()  (or save on exit)
      → print success, return to menu

### 4.3 Player views leaderboard

    PlayerMenu → "Leaderboard"
      → ask X
      → LeaderboardService.topPlayers(x)
      → CLI prints formatted table

---

## 5. Error Handling Strategy

| Error type            | Where caught            | User sees                                  |
|-----------------------|-------------------------|--------------------------------------------|
| `IOException` on CSV  | `FileService`           | "Data file unreadable, using empty set."   |
| `NumberFormatException` on input | CLI loop     | "Please enter a valid number."             |
| `ValidationException` | Service layer           | The exception's own message                |
| Unknown command       | CLI menu                | "Unknown option, try again."               |
| Null lookup           | Service returns `Optional.empty()` | "No record found."              |

**Rules**

- Never let an exception kill the program; always return to the current menu.
- Log full stack trace to `ai/agent-log.md` style debug log only when developing.
- Custom `ValidationException extends RuntimeException` for invalid user input.

---

## 6. Design Decisions and Trade-offs

| Decision                                          | Reason                                                            |
|---------------------------------------------------|-------------------------------------------------------------------|
| CSV instead of JSON/DB                            | Course scope, simple, human-readable, fits §7 file I/O requirement. |
| `Map<String, List<String>> equippedItems` in Player | Keeps Player <→ Equipment relation without a new entity class.    |
| `Person` abstract instead of interface only       | Shares field state (id, username, passwordHash) across subclasses. |
| Salted SHA-256 (not plain)                        | Minimal real-world security; no plain passwords in CSV.            |
| Services as plain classes (not singletons)        | Easier to test; `Main` wires them once.                            |
| Sorting via `Stream` + `Comparator`               | Demonstrates lambdas/streams required by §3.                       |

---

## 7. Extensibility

- **New entity** (e.g., `Skin`): add model + enum + service + CSV file; register in `FileService`.
- **New role** (e.g., `COACH`): add `Role.COACH`, subclass of `Person`, new menu class. `AuthService` unchanged.
- **Swap storage to SQLite:** replace `CsvUtil` calls inside `FileService`; service layer untouched.

---

## 8. Mapping to Assignment Requirements

| Requirement (§)         | Where satisfied                                                  |
|-------------------------|------------------------------------------------------------------|
| Abstract class          | `model/Person.java`                                              |
| Inheritance             | `Player`, `Admin` extend `Person`                                |
| Interface (≥1)          | `Authenticatable`, `Rankable`, `CsvPersistable`                  |
| Polymorphism            | `Person p = new Player(...)`; service uses `Rankable`            |
| Enums                   | `enums/` package                                                 |
| Collections + Generics  | `List<Hero>`, `Map<...>`, `CsvUtil<T>`                           |
| File I/O                | `FileService` + `CsvUtil` over `data/*.csv`                      |
| Exception handling      | Section 5 of this document                                       |
| AI usage documented     | `ai/prompts.md`, `ai/agent-log.md`, `ai/reflection.md`           |
