---
name: run-e2e-suite
description: Use when asked to run the Playwright e2e suite in e2e/, list its scenarios without running them, or summarize/read the last test report. Covers the exact commands to list tests, run all or a filtered subset by title, and how to parse playwright-report/report.json for pass/fail counts and failing-test details.
---

All commands below run from the `e2e/` directory.

## List scenarios (without running anything)

```bash
npx playwright test --list
```

For a machine-readable version (nested `suites[].suites[].specs[].title`):

```bash
npx playwright test --list --reporter=json
```

## Run the suite

All tests:

```bash
npm test
```

Filtered by title (substring or regex, matches the `test(...)` name):

```bash
npx playwright test --grep "Login with valid credentials"
```

A run always regenerates `playwright-report/report.json` and `playwright-report/index.html` (see `playwright.config.ts` reporters).

## Summarize the last report

Read `playwright-report/report.json`. Shape:

```jsonc
{
  "stats": { "expected": 5, "unexpected": 0, "skipped": 0, "flaky": 0, "duration": 4999.4 },
  "suites": [ /* recursive: suite.suites[], suite.specs[] */ ]
}
```

- `stats.expected/unexpected/skipped/flaky` — pass/fail/skip/flaky counts. `unexpected > 0` means the run failed.
- To find *which* tests failed, walk `suites` recursively (a suite has its own `suites[]` and `specs[]`); a spec with `"ok": false` failed. Its error is at `spec.tests[0].results.at(-1).errors[0].message`.
- If the file doesn't exist yet, no run has happened — run the suite first.

Don't invent a report-reading tool or script for this — reading the JSON directly with the file tools is enough; the shape above is stable across runs.
