#!/usr/bin/env python3
import argparse
import subprocess
import re

CATEGORIES = {
    'feat': 'Features',
    'fix': 'Bug Fixes',
    'perf': 'Performance Improvements',
    'refactor': 'Refactoring',
    'docs': 'Documentation',
    'chore': 'Chores',
}

def get_commits_for_tag(tag: str) -> str:
    prev_tag = subprocess.check_output(['git', 'describe', '--tags', '--abbrev=0', tag+'^']).decode().strip()
    log = subprocess.check_output(['git', 'log', f'{prev_tag}..{tag}', '--pretty=%s']).decode()
    return log

def categorize(lines):
    buckets = {k: [] for k in CATEGORIES.keys()}
    for line in lines:
        m = re.match(r'^(\w+)(\(.+\))?!?:\s*(.+)$', line)
        if not m:
            continue
        t = m.group(1)
        msg = m.group(3)
        if t in buckets:
            buckets[t].append(msg)
    return buckets

def render(tag: str, buckets) -> str:
    out = [f'# {tag}', '']
    for key, title in CATEGORIES.items():
        items = buckets.get(key) or []
        if not items:
            continue
        out.append(f'## {title}')
        for msg in items:
            out.append(f'- {msg}')
        out.append('')
    return '\n'.join(out).strip() + '\n'

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--tag', required=True)
    parser.add_argument('--output', required=True)
    args = parser.parse_args()
    commits = get_commits_for_tag(args.tag).splitlines()
    buckets = categorize(commits)
    text = render(args.tag, buckets)
    with open(args.output, 'w', encoding='utf-8') as f:
        f.write(text)

if __name__ == '__main__':
    main()



