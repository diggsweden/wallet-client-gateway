# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.8] - 2025-11-24

### Added

- Add header info to api doc

### Changed

- Update diggsweden/reusable-ci action to v2.3.1
- Update java non-major (#58)
- Update github actions (#57)

## [0.2.7] - 2025-11-20

### Fixed

- Change redis properties to correct spring boot property


## [0.2.6] - 2025-11-20

### Added

- Add session id auth to swagger

### Changed

- Replace postgres with valkey in docker-compose
- Merge pull request #49 from diggsweden/renovate/major-java-major
- Update java major


## [0.2.5] - 2025-11-19

### Changed

- Change in memory session and cache to redis


## [0.2.4] - 2025-11-17

### Added

- Add schema for post challenge openapi

### Changed

- Revert: update diggsweden/reusable-ci action to v2.2.3 (#47)
- Lock file maintenance (#50)
- Update java non-major (#48)
- Update diggsweden/reusable-ci action to v2.2.3 (#47)


## [0.2.3] - 2025-11-12

### Added

- Add challenge-response login sessions

### Changed

- Merge pull request #41 from diggsweden/renovate/docker.io-library-eclipse-temurin-25-jdk-alpine
- Update docker.io/library/eclipse-temurin:25-jdk-alpine docker digest to 0c4c630
- Merge pull request #39 from diggsweden/renovate/cgr.dev-chainguard-jre-latest
- Update cgr.dev/chainguard/jre:latest docker digest to 62ad89c
- Lock file maintenance (#43)
- Update diggsweden/reusable-ci action to v2.1.1 (#40)
- Merge pull request #36 from diggsweden/renovate/java-non-major
- Update dependency org.springframework.boot:spring-boot-starter-parent to v3.5.7
- Increase commit header length
- Merge pull request #37 from diggsweden/renovate/major-java-major
- Update java major
- Merge pull request #38 from diggsweden/renovate/docker.io-library-eclipse-temurin-25.x
- Update docker.io/library/eclipse-temurin docker tag to v25
- Merge pull request #35 from diggsweden/renovate/cgr.dev-chainguard-jre-latest
- Update cgr.dev/chainguard/jre:latest docker digest to 14dc2fa


## [0.2.2] - 2025-11-03

### Added

- Add controller to create accounts

### Changed

- Replace public key model with jwk
- Update actions/setup-java action to v5 (#33)
- Update actions/checkout action to v5 (#32)
- Update postgres:16-alpine docker digest to 0296606
- Update cgr.dev/chainguard/jre:latest docker digest to 6cd7329 (#28)
- Update java non-major (#31)
- Update actions/checkout action to v4.3.0 (#30)
- Pin dependencies
- Pin dependencies (#26)
- Pin sha and version
- Use base renovate config
- Adjust schedule


## [0.2.1] - 2025-10-16

### Fixed

- Fix typo


## [0.2.0] - 2025-10-16

### Added

- Add controller layer and improve test coverage
- Add attestation api endpoints
- Add more correct swagger doc

### Changed

- Merge branch 'feat/add_attestations_API'
- Use reuseable-ci v2
- Use reusable-ci v1
- Document style change


## [0.1.6] - 2025-10-02

### Fixed

- Adapt jreleaser artfiact name


## [0.1.5] - 2025-10-01

### Added

- Add annotation with example post data
- Add basic validation of WUA request
- Add swagger auth button and use relative url
- Add support for front- and backend agreed formats
- Add spring app with generic downstream service

### Changed

- Merge branch 'feat/swagger-fix'
- Improve tests and code structure
- Remap config and clean up autowired
- Re-label the attribute service client
- Apply checkstyle to test source

### Fixed

- Fix wallet-provider urls
- Fix pipeline
- Open actuator path
- Markdown linting conflicting with changelog


## [0.1.4] - 2025-09-16

### Removed

- Remove cache from jar build


## [0.1.3] - 2025-09-16

### Added

- Add missing jar gen to release


## [0.1.2] - 2025-09-16

### Removed

- Remove deploy from release yml


## [0.1.1] - 2025-09-16

### Added

- Add jreleaser for releases


## [0.1.0] - 2025-09-15

### Added

- Add release notes workflow
- Add initial project structure and tooling

### Changed

- Merge pull request #4 from jahwag/feat/ci-publish-image
- Use issuer poc release workflows
- Build image
- Merge branch 'feat/structure'
- Disable dependency review workflow
- Initial commit


[0.2.8]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.2.7..v0.2.8
[0.2.7]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.2.6..v0.2.7
[0.2.6]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.2.5..v0.2.6
[0.2.5]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.2.4..v0.2.5
[0.2.4]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.2.3..v0.2.4
[0.2.3]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.2.2..v0.2.3
[0.2.2]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.2.1..v0.2.2
[0.2.1]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.2.0..v0.2.1
[0.2.0]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.1.6..v0.2.0
[0.1.6]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.1.5..v0.1.6
[0.1.5]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.1.4..v0.1.5
[0.1.4]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.1.3..v0.1.4
[0.1.3]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.1.2..v0.1.3
[0.1.2]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.1.1..v0.1.2
[0.1.1]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.1.0..v0.1.1

<!-- generated by git-cliff -->
