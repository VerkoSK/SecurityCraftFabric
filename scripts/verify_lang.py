#!/usr/bin/env python3
"""
Automated verification tool for SecurityCraft language translations.
Checks that all blocks and items registered in SCContent.java have corresponding
entries in en_us.json.
"""

import argparse
import json
import os
import re
import sys
from pathlib import Path

def check_translations(root_path: Path):
    lang_file = root_path / "src" / "main" / "resources" / "assets" / "securitycraft" / "lang" / "en_us.json"
    content_file = root_path / "src" / "main" / "java" / "net" / "geforcemods" / "securitycraft" / "SCContent.java"

    if not lang_file.exists():
        print(f"ERROR: Language file not found: {lang_file}")
        return False
    if not content_file.exists():
        print(f"ERROR: SCContent.java not found: {content_file}")
        return False

    with open(lang_file, "r", encoding="utf-8") as f:
        lang = json.load(f)

    with open(content_file, "r", encoding="utf-8") as f:
        content = f.read()

    missing = []

    # 1. REINFORCED blocks
    m_reinf = re.search(r'private static final String\[\]\[\] REINFORCED = \{(.*?)\};', content, re.DOTALL)
    if m_reinf:
        for row in re.finditer(r'\{\s*"([^"]+)"', m_reinf.group(1)):
            name = row.group(1)
            key = f"block.securitycraft.{name}"
            if key not in lang:
                missing.append(("REINFORCED Block", name, key))

    # 2. REINFORCED_COPIES blocks
    m_copies = re.search(r'private static final Object\[\]\[\] REINFORCED_COPIES = \{(.*?)\};', content, re.DOTALL)
    if m_copies:
        for row in re.finditer(r'\{\s*"([^"]+)"', m_copies.group(1)):
            name = row.group(1)
            key = f"block.securitycraft.{name}"
            if key not in lang:
                missing.append(("REINFORCED_COPIES Block", name, key))

    # 3. Item description overrides (e.g. secret signs)
    overrides = {}
    for stmt in content.split(";"):
        if "registerItem" in stmt and "overrideDescription" in stmt:
            m_item = re.search(r'registerItem\(\s*"([^"]+)"', stmt)
            m_desc = re.search(r'overrideDescription\(\s*"([^"]+)"', stmt)
            if m_item and m_desc:
                overrides[m_item.group(1)] = m_desc.group(1)

    # 4. Registered Items and Modules
    for m in re.finditer(r'register(?:Item|Module|ConverterItem)\(\s*"([^"]+)"', content):
        name = m.group(1)
        if name in overrides:
            desc_key = overrides[name]
            if desc_key not in lang:
                missing.append(("Item (override)", name, desc_key))
        else:
            k_item = f"item.securitycraft.{name}"
            k_block = f"block.securitycraft.{name}"
            if k_item not in lang and k_block not in lang:
                missing.append(("Item", name, f"{k_item} or {k_block}"))

    # 5. NamedBlocks & FunctionalReinforced
    for m in re.finditer(r'registerNamedBlock\(\s*"([^"]+)"', content):
        name = m.group(1)
        key = f"block.securitycraft.{name}"
        if key not in lang:
            missing.append(("NamedBlock", name, key))

    for m in re.finditer(r'registerFunctionalReinforced\(\s*"([^"]+)"', content):
        name = m.group(1)
        key = f"block.securitycraft.{name}"
        if key not in lang:
            missing.append(("FunctionalReinforced Block", name, key))

    if missing:
        print(f"FAILED: Found {len(missing)} missing translation keys in {lang_file}:")
        for kind, name, key in missing:
            print(f"  [{kind}] '{name}' -> missing: {key}")
        return False
    else:
        print(f"SUCCESS: All registered content in {content_file.name} is translated in {lang_file.name} ({len(lang)} keys present).")
        return True

def main():
    parser = argparse.ArgumentParser(description="Verify SecurityCraft translation completeness.")
    parser.add_argument("--dir", default=".", help="Root directory of the mod workspace (default: current directory)")
    args = parser.parse_args()

    root = Path(args.dir).resolve()
    ok = check_translations(root)
    sys.exit(0 if ok else 1)

if __name__ == "__main__":
    main()
