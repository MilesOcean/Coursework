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
- **Date/Time:** 2026-06-04 14:01
- **AI Tool / Model:** Deepseek-v4-pro
- **Agent Role:** Implementation Agent
- **Prompt (verbatim):**
  > "Implement only player lookup in the existing menu-driven
  > console app.
  > Requirements:
  > - search by player ID or name
  > - display player ID, name, team, level, win rate, owned heroes,
      >   and equipped items
  > - handle not found and invalid input
      > Do not modify unrelated classes unless necessary."
- **AI Response Summary:** Created `src/Main.java` as the CLI entry
  point — menu loop, input handling, and delegation of display
  formatting to `PlayerService`. Flow: (1) **Startup** —
  `DataInitializer` loads 10 players, 15 heroes, 20 equipment,
  3 teams, 10 matches into memory, and `PlayerService` indexes
  them into lookup maps. (2) **Menu loop** — prints options, reads
  the user's choice, dispatches; invalid choices print a message
  and re-display the menu. (3) **Player lookup** — tries an exact
  ID match first, then falls back to case-insensitive
  username/nickname match. On hit, prints a formatted card with
  ID, name, team, level, rank, win rate, owned heroes (resolved
  to hero names), and equipped items per hero (resolved to
  equipment names). (4) **Not found** — prints a clear message
  and returns to the menu. (5) **Empty input** — prints an error
  and returns to the menu. No changes to model, service, or
  `DataInitializer` signatures. Build time ~2 min 9 s.
- **My Decision:** Accepted with review
- **Reason:** This is the first feature implemented after the
  Phase 5 reconcile, so I kept the prompt narrow ("only player
  lookup") to verify the menu skeleton works end-to-end before
  adding more features. I reviewed the diff to confirm the AI
  honoured "Do not modify unrelated classes" — only `Main.java`
  was added, no silent edits to `Player`, `Team`, `PlayerService`,
  or `DataInitializer`. I also like that the AI implemented a
  two-stage lookup (exact ID → case-insensitive name) rather
  than forcing the user to pick a search mode up front, which
  matches the prompt's "search by player ID or name" without
  needing a sub-menu. Manually tested four input branches: a
  valid ID, a valid name with different casing, an unknown ID,
  and an empty string — all four printed the expected output and
  returned to the menu without crashing.
- **Related Commit:** `240a5de`
- **Prompt Strategy:** Scope-locked feature prompt — the opening
  word "only" plus the closing "Do not modify unrelated classes
  unless necessary" form a two-sided fence that blocks AI scope
  creep (a common failure mode where the AI "helpfully" refactors
  neighbouring code). The Requirements list names concrete output
  fields ("player ID, name, team, level, win rate, owned heroes,
  equipped items") rather than abstract behaviour, so the AI has
  a checklist to self-verify against and I have a checklist for
  review. The explicit "handle not found and invalid input" line
  pulls error paths into the first pass instead of leaving them
  as TODOs — confirmed by the response covering both "Not found"
  and "Empty input" as distinct branches.
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
- **AI Response Summary:** Added a "Team Overview" branch to the
  existing menu in `Main.java`. Flow: (1) **List all teams** —
  prints a table with row number, team name, member count, and
  total wins (computed as the sum of each member's `winCount`).
  (2) **Select a team** — accepts three input forms: row number
  (1–3), team name (e.g. "AG超玩会"), or team ID. (3) **Detail
  view** — prints a header with team name, rank, captain (marked
  with a ★ in the roster), average level, total matches, and win
  rate; then a member roster table with row number, name, level,
  rank, win rate, and match count; finally a "top player" line
  showing the highest win rate (ties broken by level, then name),
  delegated to `Team.getTopPlayer()`. Not-found and invalid input
  branches print a clear message and return to the menu. Build
  time ~2 min 2 s.
- **My Decision:** Accepted with review
- **Reason:** The AI delivered everything the prompt asked for
  (team ID / name / member count / total wins / roster) and went
  slightly beyond by adding rank, captain marker, average level,
  win rate, and a "top player" line. I accepted the extras because
  they are derived from data already present in the model and do
  not require schema changes — they make the detail view more
  informative without inventing new fields. I reviewed the diff
  to confirm "Do not modify unrelated classes" was honoured: only
  `Main.java` was edited in the menu wiring. However, the response
  references `Team.getTopPlayer()`, which is a **new method on
  Team** — this technically widens the model's API surface beyond
  what the prompt scoped. I checked the method and accepted it
  because it is a pure read-only helper consistent with
  `design.md §2`, but flagged it in the agent-log so future
  reviewers see the boundary call. Manually tested four input
  forms: row number 2, the string "AG超玩会", a valid team ID,
  and an empty input — all four produced the expected behaviour.
- **Related Commit:** `9a16c20`
- **Prompt Strategy:** Reused the same scope-locked template as
  Prompt 6 ("only ..." + "Do not modify unrelated classes") to
  keep the menu work uniform across features. The requirements
  list deliberately separates the **list view** ("team ID, name,
  member count, total wins") from the **detail view** ("full
  player roster") so the AI knows there are two screens, not one
  combined screen. This decomposition turned out to be useful:
  the AI built them as two distinct flows (list → select → detail)
  rather than dumping all teams + all rosters in a single dense
  output, which would have been technically compliant but unusable.



