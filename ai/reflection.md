# Reflection — AI-Assisted Honor of Kings Information Management System

## 1. Workflow I Actually Used

I ran the project as a **single-tool, multi-role** workflow.
The tool was Claude Code CLI with a DeepSeek V4 Pro backend. The four
roles I rotated it through were Architect, Implementation, Reviewer,
and Tester. Role boundaries were enforced by me, not by the tool —
each session began with an explicit scope statement and ended with
a handoff artifact (a doc, a class, a review list, or a test result).

I picked this structure for one reason: a single freeform "build my
project" prompt produces code I cannot defend in a viva. Narrow,
role-scoped prompts produce small diffs that I can read, accept,
reject, or rewrite by hand. The cost is more prompts; the benefit is
that I understand every class in the final tree.

## 2. What Worked

**Spec-anchoring (Prompt 5) was the single most useful technique.**
Once I told the Implementation Agent it had to re-read
`plan.md` + `design.md` + `uml.png` before generating code, and stop
on conflict rather than guess, naming drift dropped to zero. Earlier
sessions had renamed `MatchRecord` to `Match` and reordered
constructor parameters; after Prompt 5, that stopped entirely.

**Separating Reviewer from Implementation produced honest critique.**
When the same session both writes and reviews code, the review is
shallow — the model defends its own output. Spinning the Reviewer up
as a fresh session with only the finished class as context produced
10 substantive issues, including 2 I had genuinely missed
(`hasData()` header-only false positive; `parseCsvLine` quote bug).

**Tester-as-evidence-collector, not Tester-as-oracle.** I treated the
Tester Agent's "expected outputs" as hypotheses and confirmed them
against actual CLI runs. This caught TC-19 (Scanner EOF crash) which
the agent had initially predicted as PASS.

## 3. What Did Not Work / What I Had to Override

- **The Reviewer Agent over-recommends abstractions.** Four of its
  ten suggestions (generic `Repository<T>`, polymorphic `Role`
  hierarchy, SLF4J logging, symmetric `writeCsv`) were textbook-good
  but coursework-bad. I rejected them in `agent-log.md` §4 with
  reasoning, rather than silently ignoring them.

- **The Implementation Agent inlined cryptography.** In Prompt 4 it
  hand-rolled SHA-256 + salt inside `DataInitializer` instead of
  delegating to the existing `PasswordUtil`. Rather than re-prompt,
  I refactored manually — re-prompting for a 6-line change tends to
  produce a 60-line rewrite.

- **Edge cases were not volunteered.** TC-19 (EOF on stdin) and the
  TC-15 write-side asymmetry were both found by me reading the code,
  not by any agent flagging them. The agents are reactive; proactive
  edge-case discovery still has to come from the human.

## 4. Specific AI vs. Human Split

| Phase | Primary author |
|-------|----------------|
| Plan & scope | Me (AI drafted package list only) |
| UML & design.md | AI draft, my edits for accuracy |
| Model classes | AI draft, accepted with review |
| DataInitializer | AI draft, I refactored hashing |
| Services | AI draft, I applied 2 review fixes |
| CsvUtil parser fix | AI implemented per my spec, I verified |
| Main / CLI loop | AI skeleton, I added retry counter + role branching |
| Test execution | Me, end-to-end |
| Triage of review findings | Me, end-to-end |
| All commit messages | Me |

## 5. Known Limitations (carried into submission)

1. **Admin CRUD operations are fully implemented** (TC-14). The Admin menu includes a "Data Management" submenu with add, delete, and edit operations for all five entity types (players, heroes, equipment, teams, matches), with cascade cleanup and confirmation dialogs.

2. **`CsvUtil.writeCsv()` is asymmetric with `parseCsvLine()`** (TC-15
   note). The read path now honours double-quoted commas; the write
   path still concatenates and re-splits on `,`, so a field
   containing a literal comma would not round-trip cleanly. No seed
   value contains a comma and no UI input path admits one, so this
   is latent rather than live. Fix would emit per-field tokens
   directly instead of post-splitting `toCsvRow()`.

3. **`Scanner.nextLine()` is unguarded against EOF** (TC-19). Piped
   input that runs out mid-prompt produces a `NoSuchElementException`
   and exit code 1 rather than a clean shutdown. Only affects
   scripted pipelines; interactive terminal use is unaffected.

## 6. What I Would Do Differently Next Time

- **Write the test plan before the Implementation Agent runs.** I
  wrote tests in Phase 10, after the code stabilised. If TC-19's
  expected behaviour had been pinned down in Phase 4, the Scanner
  guard would have been in v1.

- **Give the Reviewer Agent an explicit "scope is coursework, not
  production" preamble.** That would have pre-empted at least three
  of the four rejected suggestions and saved triage time.

- **Stop letting the AI generate prose for documentation files.**
  Every paragraph the AI drafted in `design.md` and `README.md`
  needed editing for accuracy against the actual code. Next time
  I would generate diagrams with AI but write prose myself.

## 7. Honest Self-Assessment

The AI accelerated the boilerplate (models, getters, seed data,
parser fix) by maybe 3–4×. It did not accelerate the parts that
actually carry marks — the design decisions, the review triage, the
test execution, and the limitation analysis. Those took the same time
they would have without AI, because they require judgement the tool
cannot supply. Treating the AI as a fast junior pair-programmer
rather than as an autonomous developer is what kept the project
defendable.

## 8. Answers to Required Reflection Questions (§6.4)

The 10 questions from §6.4 are answered below (some are cross-
referenced to earlier sections to avoid repetition).

### Q1 — Which AI tools or models did you use?
Claude Code CLI with a DeepSeek V4 Pro backend (see §1).

### Q2 — Which prompt was the most useful? Why?
Prompt 5 (spec-anchoring) was the most useful. Telling the
Implementation Agent to re-read plan.md + design.md + uml.png before
every code generation, and to stop on conflict rather than guess,
eliminated naming drift entirely (see §2).

### Q3 — Which AI-generated suggestion was wrong, incomplete, or misleading?
Three categories: the Reviewer Agent over-recommended abstractions
(R7–R10), the Implementation Agent inlined cryptography instead of
delegating to PasswordHasher, and no agent volunteered edge cases
like the Scanner EOF crash or CSV write-side asymmetry (see §3).

### Q4 — How did you check whether AI-generated code was correct?
I used four methods: (a) spec-anchoring — every implementation was
checked against plan.md + design.md requirements; (b) manual CLI
execution — I ran every feature myself and compared output against
expected behaviour; (c) separate-agent review — the Reviewer Agent
was a fresh session that had not seen the implementation prompts;
(d) code reading — I read every generated class line-by-line before
committing (see §2 for the Tester-as-evidence-collector approach).

### Q5 — What bugs did you fix yourself instead of asking AI to fix?

I fixed the following bugs manually, without re-prompting the AI:

1. **Password hashing inlined in DataInitializer.** The Implementation
   Agent hand-rolled SHA-256 + salt directly inside `DataInitializer`
   instead of calling the existing `PasswordHasher.hash()`. I
   refactored this myself — a 6-line change that would have risked a
   60-line rewrite if I had sent it back to the agent (see §3).

2. **Reviewer fixes R1 and R2 applied manually.** The `Collections.
   unmodifiableList/Map` wrapping in `Player.getHeroPool()` and
   `getEquippedItems()` (R1), and the `new ArrayList<>(source)`
   defensive copies in all 4 service constructors (R2), were applied
   by me directly rather than via the Implementation Agent, because
   they required careful judgement about which collection methods to
   wrap and where.

3. **CSV write path asymmetry with read path (TC-15 note).** I
   discovered during code reading that `CsvUtil.writeCsv()` splits
   `toCsvRow()` output on raw commas, which cannot distinguish field-
   internal commas from delimiters. I documented this as a known
   limitation rather than asking the agent to fix it, because the
   agent's likely response (a full streaming CSV writer) would have
   been over-engineered for the actual risk.

4. **Scanner EOF unguarded (TC-19).** I found this edge case myself by
   reading `Main.java` line-by-line. No agent flagged it. I chose to
   document it as a known limitation rather than implementing a fix,
   to be honest about the trade-off.

### Q6 — What Java concept did you understand better after using AI?

**Defensive copying and collection immutability.** When the Reviewer
Agent flagged R1 and R2, I initially thought "the getter returns a
reference, so what?" But reading the agent's explanation and then
writing the TC-16 and TC-17 regression tests made me understand the
real risk: a caller can call `player.getHeroPool().clear()` and
silently corrupt the model's internal state. I now understand why
`Collections.unmodifiableList()` and copy-constructors in service
layers are standard practice — they are not just boilerplate, they
are contracts that prevent one layer from breaking another.

### Q7 — What Java concept are you still unsure about?

**Generic type design for repository patterns.** The Reviewer Agent
suggested extracting a generic `Repository<T>` interface (R7,
rejected). I understand the syntax of generics, but I am not confident
I could design a clean generic repository that handles the different
query needs of Player, Hero, Equipment, Team, and MatchRecord without
becoming either too abstract to be useful or too specific to be
reusable. I chose to keep 4 separate service classes because I could
explain each one, but I recognise this is a gap in my understanding
that I want to fill in a future project.

### Q8 — Did AI make the project easier, harder, or both? Explain.
Both (see §7). Easier for boilerplate; harder for the parts requiring
judgement, because reviewing AI output takes as much focus as writing
code from scratch.

### Q9 — Which parts of the final project were mainly written by you?
See the table in §4. Plan & scope, triage, test execution, Main CLI
wiring (retry counter, role branching), and all commit messages.

### Q10 — Which parts were mainly generated or heavily assisted by AI?
See the table in §4. Model classes, DataInitializer skeleton, service
first drafts, and documentation prose (all edited for accuracy).
