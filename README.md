# Allo Bank Backend Developer Take-Home Test — Submission

**GitHub username:** `widifirmaan`
**service_charge_pct:** `3` (1163 % 10 — computed in code, not hardcoded)

## Submission Question

> **"What was the hardest design decision you made while building this, and what trade-off did you accept?"**

The hardest design decision was how far to take flexibility without overcomplicating the codebase: I supported four split strategies (equal, percentage, exact, and a hybrid) via the Strategy pattern so a new strategy needs only one new class, accepting that every expense carries its own split type instead of one global rule.
