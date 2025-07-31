import re
import sys
import subprocess

CHANGELOG = 'CHANGELOG.md'
START_VERSION = '[0.22.0]'
HEADER_RE = re.compile(r'^## \[\d+\.\d+\.\d+\] -')

def main():
    try:
        with open(CHANGELOG, 'r') as f:
            lines = f.readlines()
    except FileNotFoundError:
        print(f'Could not find {CHANGELOG}', file=sys.stderr)
        sys.exit(1)

    start_index = None
    for i, line in enumerate(lines):
        if START_VERSION in line:
            start_index = i
            break
    if start_index is None:
        print(f'Could not locate version {START_VERSION} in {CHANGELOG}', file=sys.stderr)
        sys.exit(1)

    # Only check headings from the start version up to the most recent release.
    noncompliant = []
    for idx, line in enumerate(lines[:start_index + 1], start=1):
        if line.startswith('## '):
            if not HEADER_RE.match(line):
                noncompliant.append((idx, line.strip()))

    if noncompliant:
        print('Non-compliant changelog headings:')
        for lineno, text in noncompliant:
            print(f'{lineno}: {text}')
        exit_code = 1
    else:
        print('All changelog headings from [0.22.0] onward follow MAJOR.MINOR.PATCH.')
        exit_code = 0

    print('\nLast 5 commits touching CHANGELOG.md:')
    result = subprocess.run(
        ['git', 'log', '-n', '5', '--date=short', '--pretty=format:%h %ad %s', '--', CHANGELOG],
        capture_output=True, text=True
    )
    print(result.stdout.strip())

    sys.exit(exit_code)

if __name__ == '__main__':
    main()
