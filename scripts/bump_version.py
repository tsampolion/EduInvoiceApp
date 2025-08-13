#!/usr/bin/env python3
import re
import subprocess
from pathlib import Path

APP_GRADLE = Path('app/build.gradle')

def read_version():
    text = APP_GRADLE.read_text(encoding='utf-8')
    name = re.search(r'versionName\s+"([0-9]+\.[0-9]+\.[0-9]+)"', text).group(1)
    code = int(re.search(r'versionCode\s+(\d+)', text).group(1))
    return name, code

def bump(kind: str, name: str):
    major, minor, patch = map(int, name.split('.'))
    if kind == 'major':
        major += 1; minor = 0; patch = 0
    elif kind == 'minor':
        minor += 1; patch = 0
    else:
        patch += 1
    return f"{major}.{minor}.{patch}"

def infer_kind_from_commits():
    log = subprocess.check_output(['git', 'log', '--pretty=%s', 'origin/main..HEAD']).decode()
    if 'BREAKING CHANGE' in log or re.search(r'!:', log):
        return 'major'
    if re.search(r'^feat\(', log, re.M):
        return 'minor'
    return 'patch'

def write_version(new_name: str, new_code: int):
    text = APP_GRADLE.read_text(encoding='utf-8')
    text = re.sub(r'versionName\s+"[0-9]+\.[0-9]+\.[0-9]+"', f'versionName "{new_name}"', text)
    text = re.sub(r'versionCode\s+\d+', f'versionCode {new_code}', text)
    APP_GRADLE.write_text(text, encoding='utf-8')

def main():
    cur_name, cur_code = read_version()
    kind = infer_kind_from_commits()
    new_name = bump(kind, cur_name)
    new_code = cur_code + 1
    write_version(new_name, new_code)
    print(f"Bumped to {new_name} ({new_code})")

if __name__ == '__main__':
    main()



