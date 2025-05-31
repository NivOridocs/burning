# Changelog

## [Unreleased]

### Changed

+ Major change to the APIs due to the introduction of `BurningContext` and `BurnDurationFunction`.
+ Stabilize APIs for 1.0 release.

### Added

+ Add `#burning:blacklist` block tag for blacklisting blocks.

## [0.4] - 2025-01-01

### Fixed

+ Catch potential exceptions when creating dummy entities during `BurningStorage` registration. ([#1](https://github.com/NivOridocs/burning/issues/1))

## [0.3] - 2024-10-13

### Added

+ Add new utility methods for `SimpleBurningStorage`.
+ Add tag loading/saving logic for `Burning` and `SimpleBurningStorage`.

## [0.2] - 2024-10-12

### Fixed

+ Fix the empty block list error on BurningStorage registration when reloading a server.
+ Split the the loom configurations for main and test.

## [0.1] - 2024-10-09

Alpha release.

[0.1]: https://github.com/NivOridocs/burning/releases/tag/0.1
[0.2]: https://github.com/NivOridocs/burning/releases/tag/0.2
[0.3]: https://github.com/NivOridocs/burning/releases/tag/0.3
[0.4]: https://github.com/NivOridocs/burning/releases/tag/0.4
