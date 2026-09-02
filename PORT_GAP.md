# V0.5 port to 1.21.7 - notes

## Source branch: 1.21.6 (not 1.21.5)
The whole 1.21.5 branch uses CRLF line endings; 1.21.7 and 1.21.6 use LF.
Every cherry-pick from 1.21.5 produced whole-file line-ending conflicts even
with merge.renormalize. Branch 1.21.6 is an equally complete linear V0.5
history on the identical post-1.21.6 API that 1.21.7 targets, so its commits
(range b54f2dc..2c3f7d2) apply cleanly. -x trailers therefore reference 1.21.6
commit hashes (which themselves carry the original 1.21.5 provenance in their
bodies).

## Build-adapt commit
1.21.6's af5cc03 ("Fix the build against MC 1.21.6's ... API changes") was
cherry-picked verbatim (9e15f3d) instead of writing a fresh 1.21.7 adapt commit.
1.21.7's API is identical to 1.21.6's for this code: the clean build passed
immediately at 9e15f3d with no extra changes, and mod_version=0.5 is carried by
the first commit (5cc847e) as on 1.21.6.

## Per-commit build status
The 16 feature commits between 5cc847e and 9e15f3d do not individually compile,
exactly as on branch 1.21.6 - the V0.5 features were authored against pre-1.21.6
API and 9e15f3d is the single consolidated adaptation. Build is green at 9e15f3d
and every commit after it, and `./gradlew clean build` is green at HEAD.

## MRAT hotfix
Applied as f952e13 (cherry-pick of 1.21.6's a7473f5, whose result is byte-
identical to 21c852d's BoundMines.java). Not "already safe" - the fix was needed.

## Pre-existing quirk (not introduced here, present on 1.21.5/1.21.6 too)
Blockstates reinforced_stone_polished_andesite / _granite are V0.4-era alias
files with no matching loot table or lang key (they point at the
reinforced_polished_* model, which resolves). Left as-is.
