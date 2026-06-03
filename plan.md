# Honor of Kings Information Management System — Class Design

---

## 1. Package Structure

```
src/
├── model/                    # Domain / entity classes
│   ├── Person.java           (abstract)
│   ├── Player.java
│   ├── Admin.java
│   ├── Hero.java
│   ├── Equipment.java
│   ├── Team.java
│   └── MatchRecord.java
├── enums/                    # Enumerations
│   ├── Role.java
│   ├── HeroClass.java
│   ├── Rank.java
│   ├── MatchResult.java
│   └── EquipmentType.java
├── interfaces/               # Interfaces
│   ├── Authenticatable.java
│   ├── Rankable.java
│   └── CsvPersistable.java
├── service/                  # Business logic / orchestration
│   ├── AuthService.java
│   ├── HeroService.java
│   ├── TeamService.java
│   ├── MatchService.java
│   ├── LeaderboardService.java
│   └── FileService.java
├── util/                     # Shared utilities
│   ├── CsvUtil.java
│   ├── PasswordUtil.java
│   └── Validator.java
└── Main.java                 # Entry point / CLI menu
```

Data directory (outside `src/`):

```
data/
├── players.csv
├── heroes.csv
├── equipment.csv
├── teams.csv
└── matches.csv
```

---

## 2. Enums

### `Role`
| Value   | Description                  |
|---------|------------------------------|
| PLAYER  | Regular game player          |
| ADMIN   | System administrator         |

### `HeroClass`
| Value     | Description       |
|-----------|-------------------|
| WARRIOR   | Melee fighter     |
| MAGE      | Magic damage      |
| ASSASSIN  | Burst / flank     |
| MARKSMAN  | Ranged AD carry   |
| SUPPORT   | Heal / utility    |
| TANK      | Damage sponge     |

### `Rank`
| Value       | Ordinal | Description        |
|-------------|---------|--------------------|
| BRONZE      | 0       |                     |
| SILVER      | 1       |                     |
| GOLD        | 2       |                     |
| PLATINUM    | 3       |                     |
| DIAMOND     | 4       |                     |
| MASTER      | 5       |                     |
| GRANDMASTER | 6       |                     |
| LEGEND      | 7       | Highest tier        |

### `MatchResult`
| Value | Description      |
|-------|------------------|
| WIN   | Team won         |
| LOSE  | Team lost        |
| DRAW  | Tie (rare)       |

### `EquipmentType`
| Value    | Description          |
|----------|----------------------|
| ATTACK   | Physical damage      |
| DEFENSE  | Armour / HP          |
| MAGIC    | Ability power        |
| MOVEMENT | Speed / boots        |
| JUNGLE   | Jungle item          |
| SUPPORT  | Warding / utility    |

---

## 3. Interfaces

### `Authenticatable`
```java
// Defines the contract for any class that can authenticate.
boolean login(String username, String password);
void logout();
boolean isAuthenticated();
```
- Implemented by: `Person` (abstract — shared by both `Player` and `Admin`).

### `Rankable`
```java
// Defines the contract for any class that participates in ranked ordering.
int getRankValue();       // returns the ordinal of the Rank enum
Rank getRank();
void setRank(Rank rank);
```
- Implemented by: `Player`, `Team` (teams also have a rank).
- Extends `Comparable<T>` to support natural ordering in `Collections.sort()`.

### `CsvPersistable`
```java
// Defines the contract for objects that can be serialized to/from CSV rows.
String toCsvRow();
static T fromCsvRow(String row);   // factory method (per implementing class)
```
- Implemented by: `Player`, `Hero`, `Equipment`, `Team`, `MatchRecord`.
- This gives `FileService` a uniform way to read/write each entity type.

---

## 4. Model Classes — Responsibilities & Relationships

### 4.1 `Person` (abstract)
**Role:** Base class for all human actors in the system.

| Field          | Type              | Notes                           |
|----------------|-------------------|---------------------------------|
| id             | String            | UUID                            |
| username       | String            | Unique, login credential        |
| hashedPassword | String            | SHA-256 hashed                  |
| nickname       | String            | Display name                    |
| registrationDate | LocalDateTime   | Account creation timestamp      |
| authenticated  | boolean           | Session flag (transient)        |

| Method              | Purpose                                   |
|---------------------|-------------------------------------------|
| `login(u, p)`       | Validate credentials, set `authenticated` |
| `logout()`          | Clear `authenticated` flag                |
| `changePassword()`  | Update hash after old-password check      |
| `getRole()`         | **abstract** → returns `Role` enum        |

**Relationships:** None outward. `Player` and `Admin` extend it.

---

### 4.2 `Player` extends `Person`
**Role:** A game player who owns heroes, joins a team, and plays matches.

| Field          | Type                 | Notes                          |
|----------------|----------------------|--------------------------------|
| rank           | Rank                 | Current competitive tier       |
| heroPool       | List\<Hero\>          | Heroes the player owns         |
| ownedEquipment | List\<Equipment\>     | Equipment inventory            |
| matchHistory   | List\<MatchRecord\>   | All past matches               |
| team           | Team                 | Current team (nullable)        |
| winCount       | int                  | Total wins                     |
| totalGames     | int                  | Total matches played           |

| Method                  | Purpose                                  |
|-------------------------|------------------------------------------|
| `addHero(Hero)`         | Add hero to pool                         |
| `removeHero(Hero)`      | Remove hero from pool                    |
| `joinTeam(Team)`        | Assign to a team                         |
| `leaveTeam()`           | Leave current team                       |
| `getWinRate()`          | Returns `winCount / totalGames` as %     |
| `getRankValue()`        | Delegates to `rank.ordinal()`            |

**Relationships:**
- Owns: `Hero` (1:N), `Equipment` (1:N), `MatchRecord` (1:N)
- Belongs to: `Team` (N:1, optional)

---

### 4.3 `Admin` extends `Person`
**Role:** System administrator who manages heroes and players.

| Field        | Type             | Notes                       |
|--------------|------------------|-----------------------------|
| adminLevel   | int              | 1 = junior, 2 = senior …    |
| managedTeams | List\<Team\>      | Teams under supervision     |

| Method                     | Purpose                           |
|----------------------------|-----------------------------------|
| `createHero(…)`            | Add a new hero to the catalogue   |
| `removeHero(Hero)`         | Delete a hero                     |
| `banPlayer(Player)`        | Disable a player account          |
| `unbanPlayer(Player)`      | Re-enable a player account        |
| `manageTeam(Team)`         | Assign team to admin supervision  |

**Relationships:**
- Manages: `Team` (1:N), implicitly `Player` (via ban/unban)

---

### 4.4 `Hero`
**Role:** A playable character in the game.

| Field      | Type                    | Notes                          |
|------------|-------------------------|--------------------------------|
| id         | String                  | UUID                           |
| name       | String                  | Unique hero name               |
| heroClass  | HeroClass               | Warrior / Mage / …             |
| difficulty | int                     | 1–10                           |
| skills     | List\<String\>           | Skill names (4 per hero)       |
| baseStats  | Map\<String, Integer\>   | HP, ATK, DEF, SPD, …          |
| winRate    | double                  | Aggregate across all players   |

| Method              | Purpose                              |
|---------------------|--------------------------------------|
| `updateStats(…)`    | Recalculate base stats               |
| `addSkill(String)`  | Add a skill name                     |
| `compareTo(Hero)`   | By win rate (for leaderboard)        |

**Relationships:**
- Belongs to: `Player` (N:1 via `heroPool`)
- Used in: `MatchRecord` via `participants` map

---

### 4.5 `Equipment`
**Role:** An item that boosts hero stats.

| Field       | Type                  | Notes                         |
|-------------|-----------------------|-------------------------------|
| id          | String                | UUID                          |
| name        | String                | e.g. "Blade of Despair"       |
| type        | EquipmentType         | Attack / Defense / …          |
| statBonuses | Map\<String, Integer\> | e.g. {ATK: +60, SPD: +5}     |
| price       | int                   | Gold cost                     |
| description | String                | Flavour text                  |

**Relationships:**
- Owned by: `Player` (N:1 via `ownedEquipment`)

---

### 4.6 `Team`
**Role:** A group of players who compete together.

| Field        | Type            | Notes                         |
|--------------|-----------------|-------------------------------|
| id           | String          | UUID                          |
| name         | String          | Unique team name              |
| members      | List\<Player\>   | Max 5 players                 |
| captain      | Player          | Team leader                   |
| creationDate | LocalDate       |                               |
| rank         | Rank            | Aggregate team rank           |

| Method                       | Purpose                                 |
|------------------------------|-----------------------------------------|
| `addMember(Player)`          | Add player (enforce ≤ 5)                |
| `removeMember(Player)`       | Remove player; reassign captain if needed |
| `setCaptain(Player)`         | Change captain (must be member)         |
| `getAverageRankValue()`      | Average of member ranks, rounded down   |
| `isFull()`                   | `members.size() >= 5`                   |

**Relationships:**
- Contains: `Player` (1:N, via `members`)
- Has: one `Player` as `captain`

---

### 4.7 `MatchRecord`
**Role:** Historical record of a single match between two teams.

| Field        | Type                          | Notes                          |
|--------------|-------------------------------|--------------------------------|
| id           | String                        | UUID                           |
| matchDate    | LocalDateTime                 | When the match occurred        |
| team1        | Team                          | First team                     |
| team2        | Team                          | Second team                    |
| result       | MatchResult                   | Outcome for team1              |
| duration     | int                           | Match length in seconds        |
| mvp          | Player                        | Most valuable player           |
| participants | Map\<Player, Hero\>            | Which hero each player used    |

| Method                | Purpose                                   |
|-----------------------|-------------------------------------------|
| `getWinner()`         | Returns winning `Team`                    |
| `getLoser()`          | Returns losing `Team`                     |
| `getHeroFor(Player)`  | Lookup hero used by a specific player     |

**Relationships:**
- References: `Team` (2×), `Player` (MVP + map keys), `Hero` (map values)

---

## 5. Service Classes — Responsibilities

### 5.1 `AuthService`
- `register(username, password, nickname, role)` → creates `Player` or `Admin`, persists.
- `login(username, password)` → validates hash, sets session, returns `Person`.
- `logout(person)` → clears session flag.
- Holds a `Map<String, Person>` in memory (loaded by `FileService`).

### 5.2 `HeroService`
- CRUD operations on the hero catalogue.
- `searchByName(keyword)` → returns filtered `List<Hero>`.
- `filterByClass(HeroClass)` → returns filtered `List<Hero>`.
- `getTopHeroes(int n)` → sorted by win rate (descending).

### 5.3 `TeamService`
- `createTeam(name, captain)` → new `Team`, captain joins automatically.
- `disbandTeam(team)` → remove team, free all members.
- `addMember(team, player)` / `removeMember(team, player)`.
- `transferCaptain(team, newCaptain)`.

### 5.4 `MatchService`
- `recordMatch(team1, team2, result, participants, mvp)` → create `MatchRecord`, update all players' `matchHistory`, `winCount`, `totalGames`.
- `getPlayerHistory(player)` → returns `List<MatchRecord>`.
- `getTeamHistory(team)` → returns `List<MatchRecord>`.

### 5.5 `LeaderboardService`
- `getTopPlayers(int n)` → sort by `rank.ordinal()` desc, then by `winRate` desc.
- `getTopHeroesByWinRate(int n)` → sort by `Hero.winRate` desc.
- `getTopTeamsByRank(int n)` → sort by `Team.getAverageRankValue()` desc.
- Uses `Collections.sort()` with custom `Comparator` implementations.

### 5.6 `FileService`
- `loadAll()` → reads all 5 CSV files, populates in-memory stores, wires references.
- `saveAll()` → writes all 5 CSV files from in-memory stores.
- `loadPlayers()`, `savePlayers()`, etc. — per-type methods.
- Handles `IOException`, malformed rows, missing files gracefully.
- Delegates row parsing to each model's `CsvPersistable` implementation.

---

## 6. Utility Classes

| Class           | Purpose                                               |
|-----------------|-------------------------------------------------------|
| `CsvUtil`       | Shared CSV parsing helpers (escape commas, quotes)    |
| `PasswordUtil`  | SHA-256 hashing + salt, `matches(plain, hash)` check  |
| `Validator`     | Input validation: non-empty strings, positive ints, valid enum values |

---

## 7. Data Flow Summary

```
                      +------------------+
                      |   FileService    |
                      | (CsvPersistable) |
                      +--------+---------+
                               |
           +-------------------+-------------------+
           |                   |                   |
    players.csv          heroes.csv          teams.csv
    equipment.csv        matches.csv
                               |
                      +--------+---------+
                      |   Main.java      |
                      |   (CLI Menu)     |
                      +--------+---------+
                               |
         +---------------------+---------------------+
         |                     |                     |
   AuthService          HeroService            TeamService
   MatchService         LeaderboardService
         |                     |                     |
   +-----+------+      +------+------+      +------+------+
   | Person     |      | Hero        |      | Team        |
   | Player     |      | Equipment   |      | MatchRecord |
   | Admin      |      +-------------+      +-------------+
   +------------+
```

---

## 8. OOP Concepts Checklist

| Concept           | Where                                                       |
|-------------------|-------------------------------------------------------------|
| **Abstract class**  | `Person`                                                    |
| **Inheritance**     | `Player → Person`, `Admin → Person`                         |
| **Interface**       | `Authenticatable`, `Rankable`, `CsvPersistable`             |
| **Polymorphism**    | `FileService` works with `CsvPersistable` regardless of type; `Person` reference holds `Player` or `Admin` |
| **Encapsulation**   | All fields `private`; access via getters/setters             |
| **Enum**            | `Role`, `HeroClass`, `Rank`, `MatchResult`, `EquipmentType` |
| **Collections**     | `ArrayList` (heroPool, members, matchHistory), `HashMap` (baseStats, statBonuses, participants), `TreeMap` (leaderboard) |
| **Comparable/Comparator** | `Rankable extends Comparable`; custom `Comparator` in `LeaderboardService` |
| **File I/O**        | `FileService` + `CsvUtil`; CSV persistence for all entities  |
| **Authentication**  | `AuthService` with SHA-256 password hashing                  |
| **Leaderboard**     | `LeaderboardService` with sorted collections                 |
| **Exception handling** | `FileService` catches `IOException`; `Validator` throws custom `ValidationException` |

---

## 9. Class Diagram (text)

```
┌──────────────────────┐
│   <<interface>>      │
│   Authenticatable    │
├──────────────────────┤
│ + login(u,p): bool   │
│ + logout(): void     │
│ + isAuth(): bool     │
└─────────▲────────────┘
          │ implements
┌─────────┴──────────┐          ┌──────────────────────┐
│      Person        │          │   <<interface>>      │
│     (abstract)     │          │   Rankable           │
├────────────────────┤          ├──────────────────────┤
│ - id, username     │          │ + getRankValue(): int │
│ - hashedPassword   │          │ + getRank(): Rank     │
│ - nickname         │          │ + setRank(Rank): void │
│ + getRole() abstract│          └──────────────────────┘
└────▲──────────▲────┘
     │          │
┌────┴────┐ ┌───┴──────┐
│ Player  │ │  Admin   │
├─────────┤ ├──────────┤
│ - rank  │ │ - level  │
│ - heroPool[]    │ │ - teams[]│
│ - equip[]       │ │          │
│ - matchHist[]   │ │          │
│ - team  │ │          │
│ - winCount      │ │          │
└──┬──┬───┘ └──────────┘
   │  │
   │  │ owns         ┌────────────┐
   │  └──────────────► Equipment  │
   │                 ├────────────┤
   │  owns           │ - name     │
   ├─────────────────► Hero       │ - type     │
   │                 ├────────────┤ - statBonuses
   │                 │ - name     │ - price    │
   │  belongs to     │ - class    │ - desc     │
   └─────────────────► Team       └────────────┘
                     ├────────────┤
                     │ - name     │
                     │ - members[]│
                     │ - captain  │
                     │ - rank     │
                     └─────┬──────┘
                           │ participates
              ┌────────────┴────────────┐
              │    MatchRecord          │
              ├─────────────────────────┤
              │ - team1, team2          │
              │ - result                │
              │ - participants (Map)    │
              │ - mvp                   │
              │ - duration              │
              └─────────────────────────┘
```
