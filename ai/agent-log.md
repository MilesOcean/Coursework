# Agent Log — AI-Assisted Honor of Kings Information Management System

> This log records how I used a Multi-Agent workflow on top of a single
> tool — **Claude Code (DeepSeek V4 Pro backend)** — to deliver this
> Java OOP coursework. I rotated the same tool through four distinct
> agent roles, each with a narrow scope and explicit handoff rules.
> All design decisions, acceptance/rejection calls, and manual fixes
> were made by me; the AI was treated as a constrained code assistant,
> not an autonomous developer.

---

## 1. Tooling

| Item | Value |
|------|-------|
| Tool | Claude Code CLI |
| Backend model | DeepSeek V4 Pro |
| Sessions used | 5 (one per development phase) |
| Spec-anchoring | Every implementation session was bound to `plan.md` + `design.md` + `uml.png` via a hard system constraint (see prompts.md Prompt 5) |

I deliberately stayed on a single tool. Switching models mid-project
would have introduced style drift in the generated code and made the
agent-log harder to audit. Instead I varied the **role** the tool
played, not the tool itself.

---

## 2. Agent Roles

### 2.1 Architect Agent
- **Scope:** Class design, package layout, relationships, UML.
- **Outputs:** First draft of `plan.md` structure; `design.md` skeleton; `uml.puml` source.
- **Boundary I enforced:** No code generation. Names had to match my revised `plan.md` exactly.
- **Sessions:** Prompts 1, 3.

### 2.2 Implementation Agent
- **Scope:** One layer at a time — models → enums → DataInitializer → services → Main.
- **Boundary I enforced:** Spec-anchored to `plan.md` / `design.md` / `uml.png`. If a request conflicted with those files, the agent had to stop and ask, not guess.
- **Sessions:** Prompts 2, 4, 5, and subsequent implementation turns.

### 2.3 Reviewer Agent
- **Scope:** Static review of finished classes for encapsulation, null safety, collection misuse, and OOP smells.
- **Output:** 10 issues raised across the codebase.
- **My decision:** Accepted **6**, rejected **4** (see §4 below).

### 2.4 Tester Agent
- **Scope:** Manual test case design and execution evidence collection for Phase 10.
- **Output:** Drafts for TC-15 through TC-19 (regression for accepted Reviewer fixes + edge cases).
- **Boundary I enforced:** Every "Actual output" had to come from a real CLI run on my machine, not from the model's imagination. I executed each case myself and pasted the real terminal output into `docs/test-cases.md`.

---

## 3. Handoff Discipline

I did **not** let any one agent role bleed into another. Concretely:

- The Architect Agent was never allowed to write `.java` files.
- The Implementation Agent was never allowed to invent new class names not already present in `design.md` / `uml.png`.
- The Reviewer Agent's findings were never auto-applied — every fix was triaged by me, then either implemented by hand or re-issued to the Implementation Agent as a narrow, single-method change request.
- The Tester Agent's "expected outputs" were treated as hypotheses, not facts, until I confirmed them against real CLI runs.

This is the main reason the final commit history is linear and small,
rather than a cascade of AI rewrites.

---

## 4. Reviewer Agent — Triage Decisions

The Reviewer Agent surfaced 10 issues. I accepted 6 and rejected 4.

### Accepted (6)
| # | Issue | Action |
|---|-------|--------|
| R1 | `Player.getHeroPool()` and `getEquippedItems()` returned mutable internal collections | Wrapped in `Collections.unmodifiableList` / `unmodifiableMap`. Verified by TC-16. |
| R2 | Service constructors stored the source list reference directly | Switched all 4 services to `new ArrayList<>(source)` defensive copy. Verified by TC-17. |
| R3 | `CsvUtil.parseCsvLine()` split on raw `,` and broke on quoted commas | Rewrote parser to honour double-quoted fields. Verified by TC-15 (read path). |
| R4 | `FileService.hasData()` used a loose check that returned true on header-only files | Tightened to `!d.players.isEmpty()`. Verified by TC-18. |
| R5 | DataInitializer inlined SHA-256 + salt hashing | Refactored manually to delegate to `PasswordHasher` (done by me, not by the agent). |
| R6 | `LeaderboardService` sort was not deterministic on ties | Added secondary sort by level, tertiary by nickname. Verified by TC-09. |

### Rejected (4)
| # | Issue | Why I rejected it |
|---|-------|-------------------|
| R7 | "Extract a generic `Repository<T>` interface for all services" | Over-engineering for a coursework with 4 services and no swap requirement. Would have inflated the class count without behavioural benefit. |
| R8 | "Replace `enum Role` with a polymorphic `Role` class hierarchy" | The role logic is a single `switch` in `Main`. A class hierarchy adds 2 files for zero readability gain at this scale. |
| R9 | "Add a logging framework (SLF4J + Logback)" | Out of scope. Spec requires `System.out` based CLI; adding a logger would also break the marker's run instructions. |
| R10 | "Make `CsvUtil.writeCsv()` symmetric with the new parser" | Acknowledged the smell but deferred — no seed field contains a comma and no UI entry point lets a user type one. Logged as a Known Limitation instead (see TC-15 note and reflection §5). |

---

## 5. Tester Agent — Evidence Standard

For Phase 10 I held the Tester Agent to one rule: **no fabricated output**.

- TC-01 through TC-13 were existing happy-path / failure-path cases. I re-ran each one through the CLI and pasted the actual terminal output.
- TC-14 is recorded as **N/A — Out of Scope** rather than FAIL, because Admin CRUD was never in the implemented scope (see reflection §5).
- TC-15 through TC-18 are regression cases for the accepted Reviewer fixes (R3, R1, R2, R4 respectively). For TC-16 and TC-17 I wrote a throwaway `TestRunner.java`, ran it, recorded the exception/size deltas, then deleted the file before commit so the production tree stays clean.
- TC-19 is an edge case I added myself after noticing `Scanner.nextLine()` was unguarded. It is recorded as **FAIL (low priority)** rather than hidden, because honest failure reporting is the point of this phase.

Final result: **17 / 18 PASS (94.4%) + 1 N/A**.

---

## 6. Summary of AI Contribution vs. My Contribution

| Area | AI did | I did |
|------|--------|-------|
| Architecture | Suggested initial package layout | Rewrote naming, fixed interface boundaries, finalised `plan.md` |
| Model classes | Generated boilerplate + getters/setters | Reviewed each field for encapsulation; corrected enum usage |
| Seed data | Generated 10/15/20/3/10 dataset | Refactored hashing to use `PasswordUtil`; verified counts |
| Services | First drafts | Accepted defensive-copy + immutability fixes; rejected over-engineered abstractions |
| Main / CLI | Suggested menu skeleton | Wired role-based branching; enforced 3-attempt login limit |
| Testing | Drafted TC-15..TC-19 templates | Executed every case manually; recorded real output; reported real FAIL on TC-19 |
| Documentation | Drafted prose for design.md | Edited for accuracy against actual code; final sign-off |

The AI never made an acceptance decision. Every "PASS", every
"rejected", and every commit message was mine.


