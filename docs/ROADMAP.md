# Development Roadmap

## Purpose

This roadmap outlines the vision, milestones, and current priorities for EduInvoice. It serves as a plan-of-record for stakeholders and developers, and a progress tracker for execution. Use the table of contents below to navigate to each milestone group.

## Table of Contents

- [Live Tracker](#live-tracker)
- [Short-term Milestones (0-3 months)](#short-term-milestones-0-3-months)
- [Medium-term Milestones (3-6 months)](#medium-term-milestones-3-6-months)
- [Long-term Milestones (6-12 months)](#long-term-milestones-6-12-months)
- [Issues and Remediation Log](#issues-and-remediation-log)

## Live Tracker

- [ ] Finalize Room re-baseline (v1) documentation and regenerate `1.json` in CI
- [ ] Stabilize build without local Android SDK artifacts
- [ ] Harden DB health checks and integrity validations
- [ ] Add perf smoke tests for cold start and memory footprint
- [ ] Update feature flags for role-based access (RBAC) usage in UI

## Short-term Milestones (0-3 months)

1) Database stability and developer velocity (criticality: 5)
- Why: Ensure reliable local iterations and consistent schema across environments after re-baseline.
- What: Re-baseline confirmed (v1), destructive migration in debug, CI schema export, docs alignment.

2) Performance guardrails (criticality: 4)
- Why: Prevent regressions; keep startup smooth and memory within targets.
- What: BackgroundProcessor usage, MemoryMonitor/PressureHandler hooks, minimal init on UI thread, profiling.

3) Architecture guardrails (criticality: 4)
- Why: Prevent boundary violations across modules.
- What: Konsist tests in app; forbidden imports in detekt; fix remaining violations as they appear.

4) Security posture hardening (criticality: 4)
- Why: Protect user data; maintain encrypted storage when enabled.
- What: Validate SQLCipher availability path; consistent passphrase retrieval and fallback only in debug.

5) Testing and CI reliability (criticality: 3)
- Why: Catch regressions; deterministic builds.
- What: Unit/integration test stabilization; remove reliance on local SDK; targeted performance checks.

Outcome when complete:
- Fresh DBs created reliably on dev, schema export stable in CI, cold start stays under targets, module boundaries enforced, and tests green by default.

## Medium-term Milestones (3-6 months)

1) Payment and reschedule flows refinement (criticality: 3)
- Why: Improve core workflows and data integrity around batches and reschedules.
- What: Validate indices coverage; refine DAO queries; add integration tests.

2) Analytics and reporting uplift (criticality: 3)
- Why: Provide insights for tutors (revenue trends, debts, utilization).
- What: Domain services for analytics; lightweight UI views; export options.

3) Sync and offline robustness (criticality: 4)
- Why: Maintain data integrity across sessions; enable future cloud sync.
- What: Strengthen OfflineDataManager and SyncManager behaviors; conflict resolution policies; retry with backoff.

Outcome when complete:
- Stable operational flows for payments/reschedules; actionable reports; offline queueing and conflict handling that recover predictably.

## Long-term Milestones (6-12 months)

1) Cloud backup and synchronization (criticality: 4)
- Why: Protect against device loss and enable multi-device continuity.
- What: Encrypted cloud backup/restore; background sync strategy; privacy-preserving design.

2) Multi-platform reach (criticality: 2)
- Why: Broaden access (desktop/web) for admin tasks.
- What: API abstraction; shared domain; evaluate Kotlin Multiplatform or service-backed web.

3) Enterprise scalability and observability (criticality: 3)
- Why: Prepare for larger deployments and operational excellence.
- What: Structured logging, performance dashboards, error budgets, configuration hardening.

Outcome when complete:
- Resilient cross-device data, optional web/desktop admin tools, and production-grade observability and scalability options.

## Issues and Remediation Log

1) Local Android SDK artifacts committed previously
- Formation: Local `.android-sdk/` and `local.properties` added during setup
- Files: `.android-sdk/*`, `local.properties`
- Criticality: 2 (noise for contributors; not app-breaking)
- Remediation: Remove from repo, ensure `.gitignore` excludes; CI sets SDK via environment

2) Room schema history bloat and migration complexity
- Formation: Long auto-migration chain; unshipped app allowed re-baseline
- Files: `data/src/main/.../EduInvoiceDatabase.kt`, `data/schemas/...`
- Criticality: 3 (build/KSP noise, not runtime-breaking)
- Remediation: Re-baseline to v1; delete old schemas; document policy; add migrations only when shipping

3) Startup performance claims vs code artifacts
- Formation: Report referenced components not present by name
- Files: `docs/PERFORMANCE_OPTIMIZATION_REPORT.md` (merged), `app/utils/*`, `data/monitoring/*`
- Criticality: 2 (doc drift)
- Remediation: Merged report into PERFORMANCE.md with actual components

4) Architecture rule enforcement gaps
- Formation: Inconsistent application of Konsist/detekt rules across modules
- Files: `app/src/test/java/architecture/*`, `config/detekt/detekt.yml`
- Criticality: 3 (risk of erosion)
- Remediation: Consolidate rules; add failing tests for violations; fix offenders

---

This roadmap will be kept current as priorities evolve. Link new milestones to specific issues/PRs and keep the live tracker updated.
