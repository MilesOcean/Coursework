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
- **Related Commit:** 
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
- **Related Commit:** ``
- **Prompt Strategy:** Compound-instruction prompt with shared
  context — two related actions ("reconcile" + "update
  DataInitializer") chained with "and" because they target the same
  spec anchor set already locked by Prompt 4. The order is
  deliberate: reconcile first (defines the new model), then
  DataInitializer (consumes it), which lets the AI execute them
  sequentially without needing extra clarification. Naming
  `DataInitializer` explicitly in stage 2 prevents the AI from
  drifting into unrelated cleanup.


