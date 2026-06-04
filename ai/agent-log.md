# Agent Log

> This log documents the use of three distinct AI agent roles.
> For each role: its main contribution, the human decision I made,
> and the related Git commits.

---

## 1. Architect Agent

- **Main Contribution:** Produced the initial class design for
  plan.md (package structure model/enums/interfaces/service/util,
  class responsibilities, and entity relationships). In a second
  pass, it reviewed plan.md and flagged internal contradictions
  (HeroType/HeroClass naming mismatch, duplicated fields, and an
  illegal static generic method in an interface).
- **Human Decision:** I evaluated the proposed structure and kept
  the package layout, but I made the final design decisions myself —
  standardising on HeroType (not HeroClass) and removing the
  duplicated fields. I fixed every flagged issue manually rather
  than re-prompting the AI to rewrite the design.
- **Related Commits:** d437434

---

## 2. Implementation Agent
- **Main Contribution:** Generated the seven model classes
  (Person, Player, Admin, Hero, Equipment, Team, MatchRecord) and
  four enums (Role, HeroType, EquipmentType, MatchResult) in
  src/model, with private fields, constructors, getters/setters,
  and brief explanations. Menu logic was deliberately excluded.
- **Human Decision:** I scoped the agent to the model layer only
  (no menu logic) so I could review the data classes before
  proceeding. I read every generated class and accepted as-is .
- **Related Commits:** e13c546

---

## 3. Implementation Agent
- **Main Contribution:** Generated `src/util/DataInitializer.java`
  (368 lines) to bootstrap an in-memory sample dataset:
  10 players, 3 teams, 15 heroes, 20 equipment items, 10 match
  records — all meeting or exceeding the assignment's
  non-trivial-size requirement. Built entities in dependency order
  (Equipment → Hero → Player → Team → Match), used SHA-256 + per-user
  16-char salt for passwords, UUID-based IDs, realistic Honor of
  Kings names (AG超玩会, 梦泪, 破晓, ...), and returned getters as
  `unmodifiableList` to protect the seed data from mutation.
  Compiled with zero errors on the first try.
- **Human Decision:** I accepted the file because compilation passed,
  the dataset met every count minimum, and spot-checks of hero stats
  and equipment prices matched in-game references. However, I flagged
  two issues for manual follow-up rather than re-prompting:
  (1) the inline `MessageDigest` block duplicates logic that already
  belongs in `PasswordUtil.hash` — I will refactor this by hand to
  keep hashing in one place; (2) 368 lines is acceptable for seed
  data but must not grow further, so any future entities will be
  loaded from CSV instead of added here.
- **Related Commits:** ``

