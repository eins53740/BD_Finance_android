# Changelog

## [Unreleased]
### Added
- Epic 1 foundations: multi-source stock metrics aggregation with Yahoo Finance primary connector and Alpha Vantage fallback.
- Room-backed stock metrics repository with observable snapshot and WorkManager refresh worker.
- Robolectric-based unit tests covering aggregator merge logic, repository persistence, and refresh worker scheduling.
- Application-level WorkManager configuration for periodic background synchronization.
- Epic 2: fundamental scorecard with sector/decade comparisons, profitability & stability metrics, and growth trends.
- Epic 2: intrinsic valuation models (DCF, Ben Graham, DDM) with UI surfacing of cheap/fair/expensive bands.
- Expanded connector coverage (FMP/Alpha Vantage) for sector medians, 5Y/10Y fundamentals, and associated unit tests.
