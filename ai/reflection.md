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

1. **Admin CRUD operations are now fully implemented** (TC-14). The Admin menu includes a "Data Management" submenu with add and delete operations for all five entity types (players, heroes, equipment, teams, matches), with cascade cleanup and confirmation dialogs. Edit/update of individual fields is not yet implemented; records must be deleted and re-created to change values.

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
