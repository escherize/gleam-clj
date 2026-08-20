# PRODUCT.md — gleam-clj docs site

## What it is
A documentation site for **gleam-clj**, a compiler that turns [Gleam](https://gleam.run)
source into readable Clojure that runs on the JVM. It compiles from Gleam's own
typed AST, self-hosts the Gleam standard library, and verifies every claim against
the official Gleam compiler as an oracle.

## This surface
A single, long, self-contained HTML page with sticky in-page section navigation.
Served from GitHub Pages at `escherize.github.io/gleam-clj` (repo path `/docs`).

## Mode
**Read** — the visitor is here to understand and evaluate. Structure for
comprehension; make the reading worth staying in. It is NOT a persuade/marketing
page, but the audience includes skeptics, so evidence is foregrounded.

## Audience (ranked)
1. **Curious Clojure/JVM developers** who have never touched Gleam. They need the
   "why would I want this" and a copy-pasteable getting-started, assuming zero Gleam.
2. **Evaluators / skeptics** deciding whether this is real or a toy. They need the
   receipts up front: the stdout-parity corpora (54/54 Rosetta, 60/60 tour), the
   20,000-case differential fuzz, Metabase running the compiled parser in its own
   test suite, and an honest limits section.

## Sections (single page, anchored)
1. Hero — the one-sentence thesis + the pink→blue/green compile as the visual.
2. Why / getting started — install, first compile, run on the JVM.
3. How it differs from gleam→erlang and gleam→js (the official backends).
4. The runtime model — prelude, FFI core, self-hosted stdlib.
5. Verification / oracles — the method that makes the claims trustworthy.
6. Packaging — consuming compiled Gleam as ordinary Clojure git-dep libs.
7. Receipts / status + honest limits.

## Voice
Precise, technical, unhyped. Claims are always paired with how they were verified.
Never oversell; the honest-limits section is a feature, not an afterthought.

## Visual world
A "two-language" identity: **Gleam pink** (`#FFAFF3`, ink `#151515`) marks the
source side; **Clojure blue + green** (`#5881D8`/`#90B4FE`, `#63B132`/`#91DC47`,
white) is the dominant palette and marks the output/runtime side. The compile
itself — pink source becoming blue/green Clojure — is the throughline. Real logos
for both languages are in `assets/` (Gleam's Lucy, the Clojure lambda-C).
Clojurey flourish: restrained parens/brackets as structural framing; blue+green+
white, not pink-dominant.

## Assumptions (label)
- GitHub Pages hosting at escherize.github.io/gleam-clj is assumed from the interview.
- Exact status numbers pulled from the repo's README/check.sh at build time.
