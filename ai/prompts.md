# Prompts Record

> Every AI interaction is logged below with the required fields:
> date/time, AI tool & model, agent role, the exact prompt, a summary
> of the AI response, my decision (accepted / modified / rejected),
> and the related Git commit hash.

---

## Prompt 1
- **Date/Time:** 2026-06-04 0:10
- **AI Tool / Model:** Deepseek-v4-pro
- **Agent Role:** Architect Agent
- **Prompt (verbatim):**
  > "You are my Architect Agent for a Java OOP coursework project.
  > The project is an Honor of Kings information management system.
  > Please suggest a class design only. Do not write full code.
  > Requirements: Person abstract class, Player/Admin subclasses,
  > Hero, Equipment, Team, MatchRecord, interface, enums, collections,
  > file I/O, authentication, leaderboard.
  > Give responsibilities, relationships, and possible package structure."
- **AI Response Summary:** Proposed a package structure
  (model / enums / interfaces / service / util), described class
  responsibilities, and outlined relationships (Person as abstract
  superclass, Player/Admin subclasses, Team aggregating Players, etc.).
- **My Decision:** Modified — I accepted the package structure but
  later corrected naming and interface issues myself (see Prompt 2 / reflection).
- **Related Commit:** d437434

---
## Prompt 2
- **Date/Time:** 2026-06-04 1:40
- **AI Tool / Model:** Deepseek-v4-pro
- **Agent Role:** Implementation Agent
- **Prompt (verbatim):**
  > "You are my Implementation Agent.
  > Implement only the model classes in src/model.
  > Use private fields, constructors, getters, setters, and simple methods.
  > Do not implement menu logic yet.
  > Classes: Person, Player, Admin, Hero, Equipment, Team, MatchRecord.
  > Also create enums Role, HeroType, EquipmentType, MatchResult.
  > Keep the code beginner-friendly and explain every design choice briefly."
- **AI Response Summary:** Generated the seven model classes and four
  enums with encapsulated fields, constructors, getters/setters, and
  short explanations of each design choice. No menu logic was included.
- **My Decision:** Accepted
- **Related Commit:** e13c546

---

## Prompt 3
- **Date/Time:** 2026-06-04 11:00
- **AI Tool / Model:** Deepseek-v4-pro
- **Agent Role:** Design Agent
- **Prompt (verbatim):**
  > "Generate design.md and uml.png based on plan.md"
- **AI Response Summary:** Produced a full `design.md` covering
  3-layer architecture, class responsibilities, 5 key algorithms with
  complexity notes, data-flow examples, error handling, and security.
  Also produced a `uml.puml` source covering 3 interfaces, abstract
  `Person`, and all 7 model classes with correct multiplicities,
  ready to be rendered into `uml.png` via PlantUML.
- **My Decision:** Accepted
- **Reason:** Class names, packages, enums, and interfaces all matched
  the revised plan.md with zero drift; the UML source rendered cleanly
  on the first try.
- **Related Commit:** 7e50ac0
- **Prompt Strategy:** Context-reuse prompt — kept the request short 
  to force the AI to ground both artifacts in the
  already-approved plan.md rather than invent new structure, and asked
  for both artifacts in one turn so they would stay mutually consistent.

---

## Prompt 4
- **Date/Time:** 2026-06-04 12:20
- **AI Tool / Model:** Deepseek-v4-pro
- **Agent Role:** Implementation Agent
- **Prompt (verbatim):**
  > "Create DataInitializer for this Java project.
  > It should create at least 3 teams, 10 players, 15 heroes,
  > 20 equipment items, and 10 match records.
  > Use Honor of Kings style names, but keep the data simple.
  > Return Java code only for DataInitializer and explain the
  > dataset counts."
- **AI Response Summary:** Generated `src/util/DataInitializer.java`
  (368 lines) that builds Equipment → Hero → Player → Team → Match
  in dependency order, with SHA-256 + per-user salt password hashing,
  UUID IDs, realistic Honor of Kings names (AG超玩会, 梦泪, 破晓...),
  and unmodifiableList getters. Compiled with zero errors. Counts met
  all minimums (10/3/15/20/10).
- **My Decision:** Accepted with one follow-up refactor planned.
- **Reason:** Code compiles, dataset satisfies the size requirement,
  and the naming makes demo output look authentic. The only issue is
  that hashing is inlined instead of delegating to `PasswordUtil`,
  which I will refactor manually rather than re-prompt for.
- **Related Commit:** 3d8796e
- **Prompt Strategy:** Constraint-first prompt — stated minimum counts
  up front so the AI could not under-deliver, gave a style hint
  ("Honor of Kings style, but simple") to prevent over-engineering,
  and split the output into "code only + chat explanation" so the
  source file stays clean while rationale lands in this log.

---

## Prompt 5
- **Date/Time:** 2026-06-04 13:31
- **AI Tool / Model:** Deepseek-v4-pro
- **Agent Role:** Implementation Agent (spec-anchoring)
- **Prompt (verbatim):**
  > "From this point on, every step you take must strictly follow
  > the contents of docs/plan.md, docs/design.md, and docs/uml.png.
  > Before generating any new code, re-read these three files. If a
  > request conflicts with them, stop and ask me rather than
  > guessing. Do not invent class names, packages, methods, or
  > fields that are not already defined in those documents."
- **AI Response Summary:** Acknowledged the constraint and
  confirmed it would re-read `plan.md`, `design.md`, and `uml.png`
  before any subsequent code-generation step, and would halt to ask
  whenever a request conflicted with the three spec documents.
- **My Decision:** Accepted
- **Reason:** Locking the AI to the three approved spec documents
  prevents the structural drift seen earlier (e.g. the
  `HeroType` vs `HeroClass` naming mismatch flagged by the
  Architect Agent, and the duplicate fields between `Person` and
  `Player`). It turns plan.md / design.md from passive
  documentation into **active constraints**, which is what the
  assignment expects — the AI follows the design, it does not
  redesign.
- **Related Commit:** `d7f3dbf`
- **Prompt Strategy:** Spec-anchoring prompt — sets a long-lived
  rule for the rest of the session rather than a one-off task.
  Three reinforcement techniques are combined in one prompt:
  (1) an **explicit file list** so the AI knows exactly which docs
  to re-load, (2) a **"stop and ask" clause** to block silent
  guessing when specs are ambiguous, and (3) a **negative
  constraint** ("Do not invent...") to suppress the AI's natural
  tendency to hallucinate plausible-looking class or method names.

---

## Prompt 6
- **Date/Time:** 2026-06-04 13:53
- **AI Tool / Model:** Deepseek-v4-pro
- **Agent Role:** Refactor Agent
- **Prompt (verbatim):**
  > "reconcile existing code with the docs, and update DataInitializer
  > to match the new model classes"
- **AI Response Summary:** Re-read `plan.md`, `design.md`, and the
  current `src/` tree, then performed a two-stage refactor in one
  pass. Stage 1: produced a divergence list between existing code
  and the spec documents (e.g. inline SHA-256 in `DataInitializer`
  instead of delegating to `PasswordUtil`, residual `heroClass`
  references, drifted field / method signatures vs. `design.md §2`)
  and applied minimal fixes file-by-file. Stage 2: updated
  `src/util/DataInitializer.java` so that every entity construction
  call matches the post-reconcile model — corrected constructor
  argument lists, renamed fields, removed references to dropped
  fields, and filled in values for newly introduced ones. Kept the
  original dataset scale (10 players / 3 teams / 15 heroes /
  20 equipment / 10 matches) and the unmodifiable-list getters.
  Reported a per-file diff summary at the end.
- **My Decision:** Accepted with review
- **Reason:** Merging the two stages into one prompt is safe here
  because they share the same goal (pull existing code back in line
  with `plan.md` / `design.md`) and the second stage is a mechanical
  consequence of the first — `DataInitializer` hard-codes constructor
  calls against the model, so it must move together with model
  changes or the project will not compile in between. Reviewing
  them in a single pass also avoids a "broken middle commit" where
  the model is reconciled but `DataInitializer` is not yet patched.
  I still reviewed every modified file individually before
  accepting, because refactor edits have higher blast radius than
  greenfield generation.
- **Related Commit:** `a5f76fe`
- **Prompt Strategy:** Compound-instruction prompt with shared
  context — two related actions ("reconcile" + "update
  DataInitializer") chained with "and" because they target the same
  spec anchor set already locked by Prompt 4. The order is
  deliberate: reconcile first (defines the new model), then
  DataInitializer (consumes it), which lets the AI execute them
  sequentially without needing extra clarification. Naming
  `DataInitializer` explicitly in stage 2 prevents the AI from
  drifting into unrelated cleanup.

---

## Prompt 7
- **Date/Time:** 2026-06-04 14:06
- **AI Tool / Model:** Deepseek-v4-pro
- **Agent Role:** Implementation Agent
- **Prompt (verbatim):**
  > "Implement only player lookup in the existing menu-driven
  > console app.
  > Requirements:
  > - search by player ID or nickname
  > - display player ID, nickname, level, win count, total
      >   matches, and team name
  > - handle not found and invalid input
      > Do not modify unrelated classes unless necessary."
- **AI Response Summary:** Added a "Player Lookup" branch to
  `Main.java` only. Search accepts player ID or case-insensitive
  nickname; display shows the six required fields with team name
  resolved from team ID; not-found and empty input return to the
  menu. Reused existing `PlayerService`, no model changes.
- **My Decision:** Accepted
- **Reason:** Single-file diff in `Main.java`, scope fence held,
  service layer used as designed. Manually tested valid ID, valid
  nickname, unknown nickname, and empty input — all four behaved
  correctly.
- **Related Commit:** `240a5de`
- **Prompt Strategy:** Scope-locked template ("only ..." + "Do
  not modify unrelated classes") to fence the AI into the CLI
  layer; named the six output fields explicitly so the AI had a
  self-verification checklist.

---
## Prompt 8
- **Date/Time:** 2026-06-04 14:19
- **AI Tool / Model:** Deepseek-v4-pro
- **Agent Role:** Implementation Agent
- **Prompt (verbatim):**
  > "Implement only team overview in the existing menu-driven
  > console app.
  > Requirements:
  > - list all teams with team ID, name, member count, and total
      >   wins
  > - allow selecting one team to view its full player roster
  > - handle not found and invalid input
      > Do not modify unrelated classes unless necessary."
- **AI Response Summary:** Added a "Team Overview" branch to
  `Main.java`. List view shows row number, team name, member
  count, and total wins; selection accepts row number, team
  name, or team ID; detail view shows the roster plus rank,
  captain marker, average level, win rate, and a top-player
  line via a new read-only `Team.getTopPlayer()` helper.
- **My Decision:** Accepted with review
- **Reason:** AI added rank / captain / avg level / top-player
  beyond the prompt; accepted because they are derived from
  existing model data. The new `Team.getTopPlayer()` widens the
  model API slightly but is a pure read-only helper consistent
  with design.md §2. Manually tested row number, team name,
  team ID, and empty input — all four behaved correctly.
- **Related Commit:** `9a16c20`
- **Prompt Strategy:** Reused the same scope-locked template as
  Prompt 6 to keep menu features uniform; separated "list" and
  "detail" requirements so the AI built two flows instead of
  one dense screen.


---
## Prompt 9
- **Date/Time:** 2026-06-04 14:45
- **AI Tool / Model:** Deepseek-v4-pro
- **Agent Role:** Implementation Agent
- **Prompt (verbatim):**
  > "Implement only hero details in the existing menu-driven
  > console app.
  > Requirements:
  > - search by hero ID or name
  > - display hero ID, name, type, base stats, and the players
      >   who currently own this hero
  > - handle not found and invalid input
      > Do not modify unrelated classes unless necessary."
- **AI Response Summary:** Added a "Hero Details" branch to
  `Main.java` only. Search accepts hero ID or case-insensitive
  name; display shows ID, name, type, base stats, compatible
  equipment, and owner nicknames; not-found and empty input
  return to the menu. Reused existing `HeroService` methods, no
  model changes.
- **My Decision:** Accepted
- **Reason:** Single-file diff in `Main.java`, scope fence held,
  service layer reused as designed. Manually tested valid ID,
  valid name (mixed case), unknown name, and empty input —
  all four behaved correctly.
- **Related Commit:** `20f770c`
- **Prompt Strategy:** Same scope-locked template as Prompt 6/7
  ("only ..." + "Do not modify unrelated classes"), reused
  deliberately to keep all three menu features added as clean
  single-file diffs.
---
## Prompt 10
- **Date/Time:** 2026-06-04 15:29
- **AI Tool / Model:** Deepseek-v4-pro
- **Agent Role:** Implementation Agent
- **Prompt (verbatim):**
  > "Implement only equipment statistics in the existing
  > menu-driven console app.
  > Requirements:
  > - list all equipment ranked by how many players currently
      >   equip it
  > - display equipment ID, name, type, and usage count
  > - handle empty equipment list and invalid input
      > Do not modify unrelated classes unless necessary."
- **AI Response Summary:** Created a new `EquipmentService`
  that walks every player's `equippedItems`, flattens the IDs,
  and counts occurrences; `getRankedEquipment()` sorts by count
  descending with alphabetical tie-break per plan.md §2.4.
  Added menu option "4. Equipment Statistics" to `Main.java`
  which prints a ranked table of rank, ID, name, type, and
  usage count. Empty-list branch prints a friendly message.
- **My Decision:** Accepted with review
- **Reason:** AI widened scope by creating a new
  `EquipmentService` class — accepted because the existing
  service layer had no equipment aggregator and adding one is
  consistent with the per-entity service pattern in design.md.
  The "invalid input" requirement turned out to be vacuous
  (the view takes no user input), which the AI handled
  correctly by only implementing the empty-list branch.
  Manually verified with a populated dataset (ranking + tie-
  break) and an empty equipment list — both behaved correctly.
- **Related Commit:** ``
- **Prompt Strategy:** Same scope-locked template as Prompt
  7/8/9; left the "invalid input" clause in deliberately even
  though no input is read, to see whether the AI would
  fabricate a fake input loop or correctly recognize the
  clause as non-applicable. It chose the latter.

---
## Prompt 11
- **Date/Time:** 2026-06-04 16:02
- **AI Tool / Model:** Deepseek-v4-pro
- **Agent Role:** Implementation Agent
- **Prompt (verbatim):**
  > "Implement only match history in the existing menu-driven
  > console app.
  > Requirements:
  > - list recent matches with match ID, date, participating
      >   teams, final score, and winner
  > - allow filtering the list by a chosen team
  > - handle not found and invalid input
      > Do not modify unrelated classes unless necessary."
- **AI Response Summary:** Added menu option "5. Match History"
  to `Main.java` backed by a new `MatchService` that sorts
  matches by date descending on construction. List view shows
  date, teams, result (formatted as "TeamA W – TeamB L"), MVP
  name, and duration, with a `Picks:` sub-line per match
  showing every `PlayerName → HeroName` pair. Filter prompts
  for a team name (case-insensitive exact match) and delegates
  to `MatchService.getByTeamId()`. Not-found prints "No team
  found with name: X"; Enter or 0 cancels back to the menu.
- **My Decision:** Accepted with review
- **Reason:** AI created a new `MatchService` (same precedent
  as Prompt 9) and added a hero-picks sub-line not asked for
  by the prompt. Accepted because both are derived from
  existing model data and improve readability. Manually
  tested unfiltered list, valid team filter, unknown team
  name, and empty input — all four behaved correctly.
- **Related Commit:** ``
- **Prompt Strategy:** Same scope-locked template as Prompt
  6–9; the filter clause was kept short ("by a chosen team")
  to let the AI decide between ID-based and name-based input,
  which it resolved by choosing name (consistent with the
  player/team lookup UX already in the menu).

---
## Prompt 11
- **Date/Time:** 2026-06-04 16:53
- **AI Tool / Model:** Deepseek-v4-pro
- **Agent Role:** Implementation Agent
- **Prompt (verbatim):**
  > "Implement only the player leaderboard in the existing
  > menu-driven console app.
  > Requirements:
  > - let the user choose a ranking dimension: win rate, total
      >   wins, or player level
  > - display rank, player ID, nickname, team name, and the
      >   value of the chosen dimension
  > - show top 10 players; if fewer than 10 exist, show all
  > - handle empty player list and invalid input
      > Do not modify unrelated classes unless necessary."
- **AI Response Summary:** Created a new `LeaderboardService`
  exposing `topByWinRate(n)`, `topByWins(n)`, `topByLevel(n)`,
  each implemented with Java Streams and a composite
  Comparator (primary dimension desc → win rate/level desc →
  nickname asc) for deterministic tie-breaking. Added menu
  option "6. Leaderboard" to `Main.java` with a sub-menu
  (1=Win Rate, 2=Total Wins, 3=Level); selection invokes the
  matching service method, caps at 10, and prints a table of
  rank, nickname, team name, and the chosen dimension's value.
- **My Decision:** Accepted with review
- **Reason:** AI created a new `LeaderboardService` class
  (third precedent after Prompt 9/10) — accepted because
  multi-dimension sorting with tie-break logic belongs in
  the service layer, not the view. Manually tested all three
  dimensions, the <10-players branch (only 10 exist, so cap
  was trivially exercised), and invalid sub-menu input — all
  behaved correctly.
- **Related Commit:** ``
- **Prompt Strategy:** Deliberately diverged from Prompt 9
  (fixed sort, no input) by making the sort key user-selectable
  and capping output, so the two ranking-style features produce
  visibly different code and UX. The "invalid input" clause
  was meaningful here (unlike Prompt 9), covering the sub-menu
  selector.
---
## Prompt 12
- **Date/Time:** 2026-06-04 17:01
- **AI Tool / Model:** Deepseek-v4-pro
- **Agent Role:** Implementation Agent
- **Prompt (verbatim):**
  > "Add an Admin class to the existing Person hierarchy.
  > Requirements: [...] Do not implement login logic yet."
- **AI Response Summary:** AI performed a diff between the
  prompt requirements and the existing codebase, then reported
  that no changes were needed: `Person` already declares
  `abstract Role getRole()` (Person.java:36), the `Role` enum
  already exists in `enums/` with `PLAYER` and `ADMIN`, and
  both `Player.getRole()` and `Admin.getRole()` are already
  implemented. AI also flagged one intentional deviation from
  the prompt — fields are `passwordHash + salt` (per design.md
  §6 SHA-256 mandate), not plain `password`.
- **My Decision:** Rejected
- **Reason:** The class hierarchy was already built in Phase 3
  following design.md. AI correctly identified this and asked
  for confirmation instead of fabricating changes. The
  password-hash deviation is consistent with my own design
  spec, so no correction is needed.
- **Related Commit:** no code change
- **Prompt Strategy:** This is a useful negative result — it
  validates that Phase 3 modeling was thorough enough to
  preempt Phase 7's class-layer work. Going forward, Phase 7
  collapses from 3 prompts to 2 (AuthenticationService +
  role-based menu).
---
## Prompt 13
- **Date/Time:** 2026-06-04 17:31
- **AI Tool / Model:** Deepseek-v4-pro
- **Agent Role:** Implementation Agent
- **Prompt (verbatim):**
  > "Add authentication session management to the existing
  > console app. Do NOT modify Main.java or any menu code in
  > this step. [Context block listing 5 existing pieces.]
  > Requirements: (1) create AuthenticationService with
  > currentUser field, login/logout/getCurrentUser/isLoggedIn;
  > (2) constructor takes List<Admin> + List<Player>, no
  > hard-coded accounts; (3) implement Person.login() using
  > SHA-256(salt + password); (4) reuse util/PasswordHasher
  > if it exists, otherwise create it; (5) report what was
  > created vs modified."
- **AI Response Summary:** Implemented `Person.login()` to
  compute `SHA-256(salt + password)` via `PasswordHasher.hash`
  and compare against `passwordHash`, with null-guard and
  case-insensitive username match; sets the `authenticated`
  flag only on success. Created `service/AuthenticationService`
  with a `currentUser` field, constructor that builds lookup
  maps from `List<Admin>` + `List<Player>` (admins searched
  first), and `login / logout / getCurrentUser / isLoggedIn`
  methods. `PasswordHasher` was reused (already in util/).
  AI confirmed Main.java and other services were not touched.
- **My Decision:** Accepted with review
- **Reason:** Clean separation — `Person.login()` owns its
  own credential check (multipolymorphic via Authenticatable),
  `AuthenticationService` only orchestrates lookup + session
  state. Verified compile, manually tested login with both
  Admin and Player accounts, wrong password, unknown username,
  and null inputs.
- **Related Commit:** `d9f4901`
- **Prompt Strategy:** Used an explicit **Context block** of
  "already exists, do not recreate" items (5 lines) — this
  pre-empted the kind of redundant-rebuild AI sometimes does
  and let it focus on the gap. Also added a **Report section**
  forcing AI to declare creates-vs-modifies, which made
  drafting this Summary trivial.
---
---

## Prompt 14
- **Date/Time:** 2026-06-04 17:58
- **AI Tool / Model:** Deepseek-v4-pro
- **Agent Role:** Implementation Agent
- **Prompt (verbatim):**
> "Now wire AuthenticationService into Main.java. At startup,
> ask for username and password before showing any menu. If
> login fails let them retry, but kill the program after 3
> failed attempts. After a successful login, check
> currentUser.getRole(): Admin gets the full existing menu
> (hero / equipment / team / player / match / leaderboard
> CRUD), plus a logout option. Player gets a read-only-ish
> menu: view heroes, view teams, view leaderboard, view match
> history, edit their own nickname, and logout. Important:
> when a player edits their nickname, read the id from
> currentUser — don't let them type in an id or pick someone
> else. Logout should go back to the login prompt, not exit.
> Also seed one admin account in Main so the app is usable on
> first run: username 'admin', pick any password but hash it
> with PasswordHasher and store the salt properly. Try not to
> touch the service classes. The only exception: if
> PlayerService doesn't already have something like
> updateNickname(id, newNickname), you can add it. When you're
> done, tell me which menu options ended up under Admin vs
> Player, and confirm a Player can't edit another player's
> data."

- **AI Response Summary:** AI restructured `Main.java` into a
  login loop wrapping a role-dispatched menu. Admin sees the
  six existing CRUD modules plus logout; Player sees four
  read-only views plus an "edit my nickname" option and
  logout. The nickname handler reads `currentUser` from
  `authService`, casts to `Player`, and passes
  `self.getId()` directly to `playerService.updateNickname()`
  — no user-facing id selector — so a player has no way to
  target another player's record. Default admin account
  (`admin` / `admin`) is seeded in `loadData()` with proper
  SHA-256 + salt via `PasswordHasher`. Players seeded by
  `DataInitializer` can also log in with password `123456`.
  Added `PlayerService.updateNickname(id, newNickname)` since
  it didn't exist before.

- **Files Changed:**
  - `src/Main.java` — restructured with login loop + dual menus
  - `src/service/PlayerService.java` — added `updateNickname`

- **Verification:** Logged in as `admin/admin` → saw full
  CRUD menu. Logged in as a seeded player with `123456` →
  saw read-only menu, edited own nickname successfully,
  could not see any option targeting another player's id.
  Three wrong passwords → program exited.

- **Related Commit:** `acd0a19`


---
---

## Prompt 15
- **Date/Time:** 2026-06-04 18:50
- **AI Tool / Model:** Deepseek-v4-pro
- **Agent Role:** Implementation Agent
- **Prompt (verbatim):**
>  Implement simple file saving and loading for players, heroes, equipment, teams, and match records.                                                                                
Use plain text or CSV files.                                                                                                                                                        
Handle missing files and invalid lines with exception handling.                                                                                                                     
Do not use external libraries.If it has already been implemented,skip it directly.

- **AI Response Summary:** AI introduced a `CsvPersistable`
  interface with a single `toCsvRow()` method, and made all
  six persistable entities (Admin, Player, Hero, Equipment,
  Team, MatchRecord) implement it. Each class also exposes a
  static `fromCsvRow(String)` factory by convention. The
  `DataInitializer` was reworked to read/write seven CSV
  files under a `data/` directory: `admins.csv`,
  `players.csv`, `heroes.csv`, `equipment.csv`, `teams.csv`,
  `matches.csv`, plus `loadouts.csv` for the
  player-to-equipment many-to-one relationship that couldn't
  be inlined into `players.csv` without losing structure.
  Missing files are treated as "empty collection, fresh
  start" rather than errors; malformed lines are logged and
  skipped so one bad row can't poison the whole load. Sample
  data on first run: 10 players, 1 seeded admin, 15 heroes,
  20 equipment items, 3 teams, 10 matches.

- **Files Changed:**
  - `src/interfaces/CsvPersistable.java` (new)
  - `src/model/Admin.java`, `Player.java`, `Hero.java`,
    `Equipment.java`, `Team.java`, `MatchRecord.java`
  - `src/util/DataInitializer.java`
  - `src/Main.java`

- **Verification:** Ran the program with `data/` deleted →
  app booted with empty state, then seeded sample data on
  exit. Manually corrupted one line of `heroes.csv` (removed
  a field) → next launch logged the bad row, skipped it, and
  loaded the remaining 14 heroes cleanly. Re-ran twice to
  confirm round-trip (save → kill → reload → save again
  produces identical files).

- **Related Commit:** `bf4da48`

---
---

## Prompt 16
- **Date/Time:** 2026-06-04 19:23
- **AI Tool / Model:** Deepseek-v4-pro
- **Agent Role:** Reviewer Agent
- **Prompt (verbatim):**
> You are my Testing/Reviewer Agent for a Java coursework                                                                                                                           
  project (Honor of Kings information system, console app,
  no external libraries allowed). I will paste the source code below. Review it for:
> missing coursework requirements (the spec requires:                                                                                                                               
  abstract class + inheritance, at least one interface,                                                                                                                             
  enums, exception handling, file I/O with CSV, login                                                                                                                               
  with role separation, leaderboard, match history)
> OOP design problems (bad encapsulation, leaked                                                                                                                                    
  mutability, wrong inheritance, etc.)
> invalid input bugs (what happens on empty input, wrong                                                                                                                            
  type, out-of-range numbers, very long strings)
> file I/O bugs (missing file, corrupt line, partial                                                                                                                                
  write, encoding)
> leaderboard tie handling (two players with equal score)
> admin/player permission bugs (can a player reach an                                                                                                                               
  admin-only path somehow)

Rules:
- Do NOT rewrite the project.
- Do NOT produce a refactored version.
- Give me a numbered list of concrete issues, each with:                                                                                                                            
  file + rough location, what's wrong, severity (high /                                                                                                                             
  medium / low), and a minimal fix suggestion (1-3 lines).
- If something is fine, don't mention it.

- **AI Response Summary:** Reviewer produced a numbered audit
  of 10 issues against the full source tree (Main, all
  services, all models, CsvUtil, DataInitializer,
  FileService, PasswordHasher). Distribution: 1 High, 3
  Medium, 6 Low. High issue is a CSV-injection-style bug in
  CsvUtil where `split(",", -1)` and naive comma-join
  corrupt the file irreversibly if any field contains an
  ASCII comma — currently masked only because Equipment
  descriptions happen to use Chinese commas. Medium issues:
  (a) Player.getHeroPool / getEquippedItems leak internal
  collection references, (b) PlayerService/TeamService/
  HeroService store the caller's list reference without
  defensive copy while MatchService/LeaderboardService do
  copy — inconsistent pattern, (c) FileService.hasData
  returns true if ANY collection is non-empty, masking a
  corrupt players.csv. Low issues cover atomic-write
  pattern, a duplicated hash() method in DataInitializer,
  missing nickname length validation, dense vs. ranking-with-
  ties display, null check on setEquippedItems, and a
  trailing empty field in Admin's CSV row. Reviewer also
  explicitly confirmed OK: role-separated login, player
  cannot edit other players' data, all enums/interfaces/
  abstract class present, exception handling in I/O is in
  place, leaderboard tie-breaking is deterministic via
  composite comparator with nickname as final key.

- **Files Changed:** none in this commit (review only)
  - `ai/prompts.md` (this entry)
  - `ai/review-report.md` (full reviewer output saved verbatim)
  - `ai/agent-log.md` (third agent role recorded)

- **Verification:** Spot-checked 3 of the 10 findings
  against the actual source:
  (a) #3 CSV comma — confirmed by manually editing
  equipment.csv to use an English comma in description:
  next load split the row into the wrong number of fields
  and the row was dropped.
  (b) #1 collection leak — confirmed via REPL-style test:
  `player.getHeroPool().clear()` does empty the internal
  pool, no copy was returned.
  (c) #6 duplicated hash — confirmed by diff: the private
  method in DataInitializer.java lines 70-81 is byte-
  equivalent in logic to PasswordHasher.hash but returns
  "hash_error" on failure instead of throwing.

- **Related Commit:** NO
- **Prompt Strategy:** Used adversarial framing ("you did
  NOT write this code, find flaws as if reviewing a
  stranger's homework, be harsh"), provided the explicit
  coursework checklist (abstract class, interface, enum,
  exception handling, file I/O, login roles, leaderboard,
  match history) so the reviewer could flag missing
  requirements, and forbade any rewrite ("numbered list,
  1-3 line minimal fix only"). The "Confirmed OK" section
  in the output indicates the reviewer engaged with the
  actual code rather than producing generic advice.

- **Triage decision (added by human):**
  - **Accept and fix:** #1, #2, #3, #6, #7, #9
  - **Accept with modification:** #4 (use both players AND
    admins empty as the false condition, not just players —
    safer)
  - **Reject (documented in reflection.md):** #5 (atomic
    write is out of scope for a coursework console app),
    #8 (dense ranking is a valid display choice; tie-
    breaking order is already deterministic), #10
    (changing the CSV column count breaks backward
    compatibility with already-saved data files)

---
---

## Prompt 17
- **Date/Time:** 2026-06-04 19:40
- **AI Tool / Model:** Deepseek-v4-pro
- **Agent Role:** Implementation Agent
- **Prompt (verbatim):**
> fix the high and medium issues from the review

- **Context provided:** ai/review-report.md (full reviewer
  output from Prompt 16). Scope limited to High (#3) and
  Medium (#1, #2, #4); Low issues deferred.

- **AI Response Summary:** Four fixes applied across
  CsvUtil, Player, four service classes, and FileService:
  - **#3 (High) — CSV comma corruption:** CsvUtil now
    exposes `parseCsvLine()` that handles RFC-4180-style
    double-quoted fields (commas, quotes, newlines inside
    fields), and the write side quotes any field containing
    a comma, quote, or newline. Replaces the previous
    `split(",", -1)` + naive `String.join` pair.
  - **#1 (Medium) — leaked internals in Player:**
    `getHeroPool()` now returns
    `Collections.unmodifiableList(heroPool)`,
    `getEquippedItems()` returns
    `Collections.unmodifiableMap(...)`. Both setters now
    make defensive copies (`new ArrayList<>(...)`,
    `new HashMap<>(...)`) and null-guard.
  - **#2 (Medium) — missing defensive copies in services:**
    PlayerService, TeamService, HeroService,
    EquipmentService constructors now do
    `this.xxx = new ArrayList<>(xxx)`, matching
    MatchService / LeaderboardService.
  - **#4 (Medium) — hasData false positive:** FileService
    `hasData` now requires `!players.isEmpty()`
    specifically; corrupt players.csv falls back to
    DataInitializer instead of running with zero players.

- **Files Changed:**
  - `src/util/CsvUtil.java` (parseCsvLine added, write-side
    quoting added)
  - `src/model/Player.java` (unmodifiable getters,
    defensive setters, null guard)
  - `src/service/PlayerService.java` (defensive copy)
  - `src/service/TeamService.java` (defensive copy)
  - `src/service/HeroService.java` (defensive copy)
  - `src/service/EquipmentService.java` (defensive copy)
  - `src/service/FileService.java` (hasData tightened)

- **Verification:**
  - Compiled clean (no warnings introduced).
  - Manual test: edited equipment.csv to put an English
    comma inside a description wrapped in double quotes —
    row now loads correctly (previously dropped).
  - Manual test: `player.getHeroPool().clear()` from
    outside now throws `UnsupportedOperationException`.
  - Manual test: emptied players.csv, restarted — app
    correctly re-seeds from DataInitializer instead of
    booting into a half-empty state.
  - All existing menu flows (login, team view, match,
    leaderboard, file save/load round-trip) still work.

- **Related Commit:** `81b2d2d` 

- **Suggestions rejected from review (deferred to
  reflection.md):** #5 atomic write, #8 ranking tie
  display, #10 Admin trailing comma — reasons recorded in
  reflection.md.

- **Low issues deferred:** #6 (hash duplication), #7
  (nickname length), #9 (setEquippedItems null) — will be
  handled in a follow-up `[Refactor]` commit so this `[Fix]`
  commit stays focused on the High/Medium severity scope.

---
## Prompt 18
- **Date/Time:** 2026-06-04 20:22
- **AI Tool / Model:** Claude Code / Deepseek-v4-pro
- **Agent Role:** Review Agent
- **Prompt (verbatim):**
  > "You are my AI Review Agent.
  >
  > Please read the requirement document at:
  > C:\Users\35107\Desktop\requirement.pdf
  >
  > Then review my current Java project and check whether it satisfies all coursework requirements.
  >
  > Tasks:
  > 1. Extract the key requirements from the PDF.
  > 2. Compare each requirement with my current project files.
  > 3. Identify which requirements are satisfied, partially satisfied, or missing.
  > 4. Point out any risks related to Git history, prompts.md, documentation, testing, UML/design, OOP structure, file I/O, authentication, and reflection.
  > 5. Do not modify any files yet.
  > 6. Output a clear checklist with pass/fail status and recommended fixes."
- **AI Response Summary:** Reviewed the project against the coursework PDF and produced a checklist of satisfied, partially satisfied, and missing requirements, including risks in Git history, documentation, testing, design/UML, OOP structure, file I/O, authentication, and reflection.
- **My Decision:** Accepted
- **Reason:** The review helped verify compliance before final submission and identified remaining risks without changing project files.
- **Related Commit:** 

