#!/usr/bin/env python3
"""Rosetta harness: for each tasks/<slug>.gleam, compare `gleam run` stdout
(the oracle) with the stdout of the compiled-to-Clojure version.

Statuses:
  pass       stdout matches
  diff       runs, wrong stdout
  clj_fail   generated Clojure fails to load/run (often a missing shim)
  emit_fail  gleam-to-clj panicked (unsupported feature)
  ref_fail   reference gleam run itself fails (stale/partial snippet)
  excluded   uses erlang/OTP/FFI — out of scope for the JVM backend

Writes expected/<slug>.txt, gen/<slug>.clj, status.json; prints a histogram.
"""
import json
import re
import subprocess
import sys
from collections import Counter
from pathlib import Path

REPO = Path(__file__).parent.parent
_args = sys.argv[1:]
SUITE = _args.pop(0) if _args and (REPO / _args[0] / "tasks").is_dir() else "rosetta"
ROOT = REPO / SUITE
TASKS = sorted((ROOT / "tasks").glob("*.gleam"))
EMITTER = REPO / "gleam-to-clj" / "target" / "debug" / "gleam-to-clj"
REF = ROOT / "work" / "ref"

EXCLUDE = re.compile(r"import gleam/(erlang|otp)|@external\(")


def sh(cmd, timeout, cwd=None):
    try:
        return subprocess.run(cmd, capture_output=True, text=True,
                              timeout=timeout, cwd=cwd)
    except subprocess.TimeoutExpired:
        return None


def ref_run(src):
    """Returns (stdout, error_detail, crashed). Intentional runtime crashes
    (todo/panic/assert demos) are still comparable: stdout + nonzero exit."""
    (REF / "src" / "ref.gleam").write_text(src)
    r = sh(["gleam", "run"], 30, cwd=REF)
    if r is None:
        return None, "timeout", False
    if r.returncode != 0:
        if "runtime error" in r.stderr:
            return r.stdout, None, True
        return None, "; ".join(
            line for line in r.stderr.splitlines() if "error" in line.lower())[:120], False
    return r.stdout, None, False


def panic_msg(stderr):
    m = re.search(r"panicked at [^\n]+\n([^\n]+)", stderr)
    msg = m.group(1) if m else (stderr.strip().splitlines() or ["?"])[-1]
    # Collapse per-task specifics (AST debug dumps) so the histogram groups
    # by feature, but keep lowercase feature descriptions after a colon.
    return re.sub(r": [A-Z{\[].*$", "", msg)[:80]


def clj_fail_detail(stderr):
    m = re.search(r"Could not locate (\S+?)__init", stderr)
    if m:
        return f"missing shim: {m.group(1)}"
    m = re.search(r"No such var: ([\w./-]+)", stderr)
    if m:
        return f"missing fn: {m.group(1)}"
    for line in stderr.splitlines():
        if "Exception" in line or "error" in line.lower():
            return line.strip()[:100]
    return (stderr.strip().splitlines() or ["?"])[0][:100]


def main():
    only = _args  # optional slugs to (re)run
    (ROOT / "expected").mkdir(exist_ok=True)
    (ROOT / "gen").mkdir(exist_ok=True)
    if not REF.exists():
        REF.parent.mkdir(parents=True, exist_ok=True)
        subprocess.run(["gleam", "new", "ref", "--skip-git"], cwd=REF.parent,
                       check=True, capture_output=True)

    status_path = ROOT / "status.json"
    status = json.loads(status_path.read_text()) if status_path.exists() else {}

    for task in TASKS:
        slug = task.stem
        if only and slug not in only:
            continue
        src = task.read_text()
        if EXCLUDE.search(src):
            status[slug] = {"status": "excluded", "detail": ""}
            continue
        nondet = re.search(r"\b(int|float)\.random\b", src) is not None

        expected, err, crashed = ref_run(src)
        if expected is None:
            status[slug] = {"status": "ref_fail", "detail": err or ""}
            continue
        (ROOT / "expected" / f"{slug}.txt").write_text(expected)

        gen = ROOT / "gen" / f"{slug}.clj"
        r = sh([str(EMITTER), str(task), str(gen)], 30)
        if r is None or r.returncode != 0:
            status[slug] = {"status": "emit_fail",
                            "detail": panic_msg(r.stderr if r else "timeout")}
            continue

        ns = slug.replace("_", "-")
        r = sh(["clojure", "-J-Xss512m", f"-A:{SUITE}", "-M", "-m", ns], 60, cwd=REPO)
        if r is None:
            status[slug] = {"status": "clj_fail", "detail": "timeout"}
        elif crashed:
            # Reference crashed on purpose: require nonzero exit + same stdout.
            if r.returncode != 0 and r.stdout.rstrip("\n") == expected.rstrip("\n"):
                status[slug] = {"status": "pass", "detail": "crash parity"}
            else:
                status[slug] = {"status": "diff", "detail": "crash parity failed"}
        elif r.returncode != 0:
            status[slug] = {"status": "clj_fail",
                            "detail": clj_fail_detail(r.stderr)}
        elif nondet:
            # Random output can't be compared; a clean run counts.
            status[slug] = {"status": "pass", "detail": "nondet: exit-0 only"}
        elif r.stdout.rstrip("\n") != expected.rstrip("\n"):
            status[slug] = {"status": "diff", "detail": ""}
        else:
            status[slug] = {"status": "pass", "detail": ""}
        print(f"{status[slug]['status']:9} {slug}", file=sys.stderr)

    # Ratchet: a full run may never pass fewer tasks than the last full run.
    prev = json.loads(status_path.read_text()) if status_path.exists() else {}
    prev_pass = sum(1 for v in prev.values() if v["status"] == "pass")
    new_pass = sum(1 for v in status.values() if v["status"] == "pass")
    status_path.write_text(json.dumps(status, indent=1, sort_keys=True))

    counts = Counter(v["status"] for v in status.values())
    print("\n== totals ==")
    for k in ["pass", "diff", "clj_fail", "emit_fail", "ref_fail", "excluded"]:
        print(f"{k:9} {counts.get(k, 0)}")
    print("\n== emit_fail histogram ==")
    for detail, n in Counter(v["detail"] for v in status.values()
                             if v["status"] == "emit_fail").most_common():
        print(f"{n:3}  {detail}")
    print("\n== clj_fail histogram ==")
    for detail, n in Counter(v["detail"] for v in status.values()
                             if v["status"] == "clj_fail").most_common():
        print(f"{n:3}  {detail}")

    if not only and new_pass < prev_pass:
        print(f"\nRATCHET FAILED: {new_pass} passing, was {prev_pass}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
