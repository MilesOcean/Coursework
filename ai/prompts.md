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
- **Related Commit:** 28833ee

---
