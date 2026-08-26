---
name: private-sync
description: Read and write the project's private synced notes in .private/ - shared context, decisions (ADRs), and findings that multiple agents across different machines and harnesses need but that must never land in the public repo. Use when STARTING IMPLEMENTATION of any nontrivial change (claim a task in .private/backlog first - see How we work), and when recording a decision, filing a finding, looking up why something was built a certain way, or catching up on what other agents have learned. Triggers on starting to write code, "why did we", "file a finding", "record this decision", "what do we know about", "check the notes", "catch me up", "mark it in progress".
---

# private-sync

`.private/` is a second git repo, checked out inside this one and gitignored. It
holds what the public repo cannot: shared context for every agent working on this
project, across machines and harnesses.

The public repo tells you what the code does. `.private/` tells you **why**, and
**what other agents already learned the hard way**.

## Setup

If `.private/` does not exist:

```sh
git clone "$(cat .private-remote)" .private
```

`.private-remote` holds the remote URL. If it is missing, ask the user - do not
guess a URL.

Auth is per-agent. If the clone fails with a permission error, report it and stop.
Do not work around it, and do not commit notes to the public repo instead.

## Layout

```
.private/
  README.md              what this is, warning, conventions
  wiki/                  current shared truth, edited in place
    index.md             entry point, links to everything
  decisions/             ADRs, immutable, append-only
  findings/<agent-id>/   per-agent discovery log, append-only
  worklogs/<agent-id>/   per-session logs, append-only
  backlog/               tasks, managed entirely by the backlog CLI
  bin/private-index      rebuilds index.jsonl from frontmatter
  index.jsonl            derived search filter layer, gitignored, rebuildable
```

Five directories because they have different write patterns:

| | Write pattern | Frequency | Contention |
|---|---|---|---|
| `wiki/` | edit in place | low | real, but rare |
| `decisions/` | append new file | low | none (immutable) |
| `findings/` | append new file | high | none (per-agent paths) |
| `worklogs/` | append new file | high | none (per-session paths) |
| `backlog/` | CLI-mediated edits | medium | upstream locking + ID allocation |

Findings and worklogs are namespaced per agent (and per session) so ten
concurrent writers never touch the same file. That is what makes this work
without locking.

## Before you start work

Read `.private/wiki/index.md`. Then check what other agents have filed recently:

```sh
git -C .private log --oneline -20
git -C .private log --since=3.days --name-only --format="%s"
```

Searching beats reading. The findings log is append-only and grows without bound -
never read it top to bottom:

```sh
grep -ril "flaky" .private/findings/
grep -ril "sqlite" .private/wiki/ .private/decisions/
```

## How we work

Tracked work lives locally in `.private/backlog/` - same lifecycle as an
issue tracker, no external service. Every nontrivial change follows it:

```
task exists?  backlog search "auth" / backlog task list --plain
no:           backlog task create "Concise title" -d "objective + acceptance criteria"
claim:        backlog task edit <id> -a @<agent-id> -s "In Progress"
              git -C .private add backlog && git -C .private commit -m "claim task-<id>" && git -C .private push
work:         decisions -> decisions/ (ADR), learnings -> findings/<agent-id>/, blockers -> note on the task
commit:       public-repo commits reference the task: "Refs task-<id>" trailer
done:         backlog task edit <id> -s Done, write worklog, push sidecar
```

Rules of the lifecycle:

- **Claim before working, push the claim immediately.** An unpushed claim
  protects nobody. If a task is already `In Progress` under another agent,
  pick different work - unless the claim is days old, then treat it as
  abandoned and reassign.
- **Blockers are recorded, not silently absorbed.** Note on the task what you
  are waiting on and any workaround, so the next agent doesn't rediscover it.
- **Decisions made mid-task get an ADR** in `decisions/`, linked from the
  task, not buried in a commit message.
- **Done means pushed.** Status edits, worklog, findings - none of it exists
  to other agents until `git -C .private push`.

No backlog CLI on this machine? Run ad hoc as `npx backlog.md <command>`. If
that is also impossible, fall back to the flat-file claim marker below and
report the gap - do not skip tracking silently.

## Writing

### Findings - "I learned X"

One file per finding. Path: `findings/<agent-id>/<UTC-timestamp>-<slug>.md`.

```markdown
---
type: finding
agent: a3
date: 2026-08-23T14:22:00Z
tags: [sqlite, wal]
confidence: confirmed
area: storage
related: ["[[use-sqlite-wal]]"]
---

# WAL mode needs an explicit checkpoint before backup

`.backup` on a WAL database silently copies a stale snapshot unless you
`PRAGMA wal_checkpoint(TRUNCATE)` first. Cost us an afternoon.

Repro: write 100 rows, `.backup out.db` without checkpoint, out.db is empty.
```

`confidence` is `confirmed` (reproduced it) or `suspected` (saw it once). Say
which. A suspected finding another agent trusts as confirmed is worse than no
finding.

Keep the title a full sentence stating the finding. `# WAL needs a checkpoint`
beats `# SQLite issue` - the title is what `grep` and other agents' eyes hit first.

### Decisions - "we chose X over Y"

One file per decision, in `decisions/`. Named after the decision as a present-tense
imperative: `use-sqlite-wal.md`, `choose-transport.md`. Not numbered - numbers
collide when two agents write at once, names do not.

Follows Nygard's ADR format:

```markdown
---
type: decision
status: accepted
date: 2026-08-23
deciders: [bcm, a3]
tags: [storage]
---

# Use SQLite in WAL mode

## Status
Accepted.

## Context
Ten agents write findings concurrently. Default rollback-journal SQLite
serializes writers and we saw lock timeouts under load.

## Decision
We will run SQLite in WAL mode.

## Consequences
Concurrent readers no longer block on writers. Backup now requires an explicit
`wal_checkpoint(TRUNCATE)` - see [[2026-08-23-wal-checkpoint-before-backup]].
Database is now three files, not one; deploy scripts must copy all three.
```

**Decisions are immutable.** Never edit an accepted decision to change what it
says. To reverse one:

1. Write a new decision file explaining the new choice.
2. Set the old file's `status: superseded` and add a link to the replacement.

Nygard's rule, and the reason it matters here: it is still relevant to know that
it *was* the decision, but is *no longer* the decision. Without that, agents
re-litigate settled questions and re-make rejected choices.

Status values: `proposed`, `accepted`, `rejected`, `deprecated`, `superseded`.

### Wiki - current truth

`wiki/` is the compressed present state, edited in place. It answers "how does
this work right now" without anyone reading the whole history.

The findings log is raw and chronological; the wiki is curated and current. When a
finding turns out to be generally true, fold it into the wiki and link back to it.

Link every new wiki page from `wiki/index.md` or it will not be found.

### Worklogs - "what I did this session"

One file per session. Path: `worklogs/<agent-id>/<UTC-timestamp>-<slug>.md`.
Never one growing file per agent - two concurrent sessions by the same agent id
would collide.

```markdown
---
type: worklog
agent: a3
date: 2026-08-23T14:22:00Z
tags: [storage]
branch: feature/wal-checkpoint
---

# Session notes

Prose paragraphs: what you did, decisions you took, dead ends you hit,
gotchas worth remembering.
```

Append-only. Never edit yesterday's worklog; write a new one. Never paste raw
transcripts or secret values - link the transcript location instead.

## Tasks

Tracked tasks live in `backlog/`, managed entirely by
[Backlog.md](https://github.com/MrLesk/Backlog.md) - markdown files with real
multi-writer ID allocation and locking, so we carry none of that ourselves.

One-time setup (run from inside the sidecar; use the parent project's name):

```sh
cd .private && backlog init "<project-name>"
```

Daily use:

```sh
backlog task create "Add OAuth System"
backlog task list --plain
backlog task 7 --plain            # view one task
backlog task edit 7 -a @a3        # assign / annotate / set status
backlog task archive 7            # done and dusted
backlog search "auth"             # fuzzy across tasks, docs, decisions
```

Rules:

- **Never hand-edit files under `backlog/`.** All mutation goes through the CLI
  so frontmatter, IDs, filenames, and section markers stay consistent.
- If the CLI is not installed on this machine (`npm i -g backlog.md`, or run ad
  hoc as `npx backlog.md <command>`), do not create tasks by hand - report it
  and move on.
- Committing task changes uses the normal sidecar commit flow:
  `git -C .private add backlog && git -C .private commit && git -C .private push`.

### Claiming without backlog

When work is tracked in a flat file (ISSUES.md or similar) instead of
`backlog/`, claim by putting a `WIP(<agent-id>, <UTC date>)` marker on the
item's title line, and clear it when the work lands or is abandoned. A stale
marker is worse than none - check the date before trusting one, and treat
markers older than a few days as abandoned. Same push rule as backlog claims:
push immediately.

## Search

Two stages: jq filters the derived index by frontmatter, then grep (or qmd)
digs into the surviving files.

Stage 1 - filter. If unsure of freshness, rebuild first:

```sh
.private/bin/private-index --out .private/index.jsonl
jq -c 'select(.type=="finding" and .confidence=="confirmed") | .path' .private/index.jsonl
jq -c 'select(.type=="worklog" and (.tags | index("storage"))) | .path' .private/index.jsonl
jq -c 'select(.type=="decision" and .status=="accepted") | .path' .private/index.jsonl
```

`index.jsonl` is derived state: never hand-edit it, regenerate it. It is
gitignored, so each machine keeps its own copy.

Stage 2 - read or semantically dig into the stage-1 paths:

```sh
grep -il "checkpoint" $(jq -c 'select(.type=="finding") | .path' .private/index.jsonl)
```

Optional per-machine semantic layer with [qmd](https://github.com/tobi/qmd) -
a convenience, not a dependency:

```sh
qmd collection add "$(pwd)/.private" --name "$(basename $PWD)-private" --mask '**/*.md'
qmd query "why does backup need a checkpoint"
```

## Conventions

**Links.** Internal note-to-note links are wikilinks: `[[use-sqlite-wal]]`. Every
tool that reads this (Obsidian, Logseq, Foam, Dendron) derives the backlink graph
from them. External links are ordinary markdown: `[the PR](https://github.com/...)`.
Both, freely.

**Wikilinks inside frontmatter must be quoted**, or the YAML is invalid:

```yaml
related: ["[[use-sqlite-wal]]"]   # correct
related: [[[use-sqlite-wal]]]     # broken YAML
```

**Never write a `backlinks:` field.** Backlinks are derived from forward links.
Storing them by hand creates a second source of truth that drifts on the first
rename.

**Frontmatter types** follow Obsidian Properties: text, list, number, checkbox,
date (`YYYY-MM-DD`), datetime (`YYYY-MM-DDTHH:MM:SS`). `tags` and `aliases` are
reserved list properties - use them as intended.

**Inline checklists** inside notes use GitHub-flavored checkboxes. Owner and due
date inline, since no scheduler reads these. These are ad-hoc scratch, not
tracked work - tracked tasks belong in `backlog/` via the CLI (see Tasks):

```markdown
- [ ] Verify WAL checkpoint on the deploy path @bcm 2026-08-30
- [x] File finding on backup staleness @a3
```

**`index.jsonl` is never hand-edited.** It is derived from frontmatter by
`.private/bin/private-index`; regenerate it whenever you doubt its freshness.

## Committing

Commit to `.private/` separately from the public repo. Push often - other agents
only see pushed work.

```sh
git -C .private add findings/a3
git -C .private commit -m "finding: WAL backup needs explicit checkpoint"
git -C .private push
```

If the push rejects, pull with rebase and push again. Findings live at distinct
paths per agent, so this should merge cleanly:

```sh
git -C .private pull --rebase && git -C .private push
```

## Never

- **Never commit `.private/` contents to the public repo.** It is gitignored and a
  pre-commit hook blocks it. If you find yourself working around either, stop.
- **Never write a secret value into any note** - not into findings, not into the
  wiki. Reference the location instead: "the API key in `config/prod.env` is
  world-readable" - never the key itself. An agent that discovers a leaked
  credential and records it has copied that credential into a synced repo.
- **Never edit an accepted decision** to change its meaning. Supersede it.
- **Never store backlinks by hand.**
