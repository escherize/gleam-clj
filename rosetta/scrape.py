#!/usr/bin/env python3
"""Scrape Gleam solutions from Rosetta Code into rosetta/tasks/<slug>.gleam.

Content is GFDL-licensed; tasks/ is local fixture data, attribute if published.
"""
import json
import re
import sys
import time
import urllib.parse
import urllib.request
from pathlib import Path

API = "https://rosettacode.org/w/api.php"
HEADERS = {"User-Agent": "gleam-clj-fixture-scraper/0.1 (personal compiler project)"}
OUT = Path(__file__).parent / "tasks"


def api(params):
    params = dict(params, format="json")
    url = API + "?" + urllib.parse.urlencode(params)
    req = urllib.request.Request(url, headers=HEADERS)
    with urllib.request.urlopen(req, timeout=30) as r:
        return json.load(r)


def gleam_members():
    cont = {}
    while True:
        r = api({"action": "query", "list": "categorymembers",
                 "cmtitle": "Category:Gleam", "cmlimit": "500",
                 "cmtype": "page", **cont})
        yield from r["query"]["categorymembers"]
        if "continue" not in r:
            return
        cont = r["continue"]


def gleam_section(wikitext):
    m = re.search(r"==\{\{header\|Gleam\}\}==(.*?)(?===\{\{header\||\Z)",
                  wikitext, re.S)
    if not m:
        return None
    blocks = re.findall(
        r'<syntaxhighlight\s+lang="?gleam"?\s*>(.*?)</syntaxhighlight>',
        m.group(1), re.S | re.I)
    return blocks[0].strip() + "\n" if blocks else None


def slugify(title):
    s = re.sub(r"[^a-z0-9]+", "_", title.lower()).strip("_")
    if s[0].isdigit():
        s = "t_" + s
    return s


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    n = 0
    for page in gleam_members():
        title = page["title"]
        if title.startswith(("Category:", "Rosetta Code")):
            continue
        r = api({"action": "parse", "page": title, "prop": "wikitext"})
        code = gleam_section(r["parse"]["wikitext"]["*"])
        if not code:
            continue
        (OUT / f"{slugify(title)}.gleam").write_text(code)
        n += 1
        if n % 25 == 0:
            print(f"{n} tasks...", file=sys.stderr)
        time.sleep(0.3)
    print(f"scraped {n} tasks into {OUT}", file=sys.stderr)


if __name__ == "__main__":
    main()
