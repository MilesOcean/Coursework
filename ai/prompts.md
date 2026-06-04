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
- **Related Commit:** 28833ee
- **Prompt Strategy:** Constraint-first prompt — stated minimum counts
  up front so the AI could not under-deliver, gave a style hint
  ("Honor of Kings style, but simple") to prevent over-engineering,
  and split the output into "code only + chat explanation" so the
  source file stays clean while rationale lands in this log.
