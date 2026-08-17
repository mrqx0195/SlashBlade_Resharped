## Motivation

When rendering SlashBlade entities in third-person view, `VmdAnimation.setupAnim(float partialTick)`
is called every time a bone transform is queried. With MMD models having 30-60 bones and each bone
queried twice (POSITION + BEND), `setupAnim` runs 60-120 times per entity per frame. But the tick
and partialTick values don't change within a single frame — all 120 calls produce identical results.

This consumes ~20% of the Render thread budget.

## Solution

Cache the last `(currentTick, partialTick)` pair at the head of `setupAnim`. If the values match
the previous call within the same frame, skip the entire pipeline (setVmd + updateMotion + skinning).

## Reference Implementation

I built a standalone mod to validate this approach:
→ https://github.com/Elten-huanghuang/vmd-animation-cache

Only 32 lines of Mixin code (see `VmdAnimationCacheMixin.java`), no coremod hacking.

## Environment

| Component | Version |
|-----------|---------|
| Minecraft | 1.20.1 |
| Forge | 47.3.22 |
| SlashBlade Resharped | 1.9.65 |
| VMD Animation Cache | 1.0.0 |

## Performance Impact

Tested with a single SlashBlade entity in view:
- Before: ~50 FPS
- After:  ~190 FPS

Reduces 120 `setupAnim` calls per entity per frame → 1 call.

## Additional Context

It would be ideal to integrate this caching directly into SlashBlade Resharped,
so users don't need a separate mod for FPS improvement. The implementation
is trivial and I'm happy to open a PR if you'd prefer.
