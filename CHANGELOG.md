# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.4.2] - 2026-02-05

### Added

- Add WuaService test
- Add deprecated annotations

### Changed

- Merge pull request #98 from diggsweden/feat/add-wua-v2
- Clean up errors and warnings from spotbugs etc
- Send nonce as an optional request parameter to wallet-provider
- Use wua v2 from wallet-provider

### Fixed

- Handle empty or null nonce when creating wua
- Format log parameters

## [0.4.1] - 2026-02-03

### Added

- Add session id to response body

### Changed

- Merge pull request #99 from diggsweden/renovate/docker.io-library-eclipse-temurin-25-jdk-alpine
- Update docker.io/library/eclipse-temurin:25-jdk-alpine docker digest to 7ace075
- Merge pull request #101 from diggsweden/fix/update-just-config
- Merge pull request #100 from diggsweden/feat/auth-session-id-body
- Update dependency org.assertj:assertj-core to v3.27.7 [security] (#97)

### Fixed

- License format
- Increase header length for commit messages


## [0.4.0] - 2026-01-26

### Added

- Add swagger header parameters for /account
- Add auth endpoint to get sessionId deeplink

### Changed

- Merge pull request #92 from diggsweden/feat/jwk
- Resolve jwk from session info instead of passing it as a parameter
- Update dependency org.wiremock.integrations:wiremock-spring-boot to v4.0.9 (#96)
- Update dependency prettier to v3.8.1 (#95)
- Update actions/checkout action to v6.0.2 (#94)
- Merge pull request #93 from diggsweden/feat/oidc-session-id
- Update dependency prettier to v3.8.0 (#91)
- Update dependency com.nimbusds:nimbus-jose-jwt to v10.7 (#89)

### Fixed

- Change method name rename test method and parameter
- Adjust tests to wua api v3 with necessary refactors
- Apply autoformat
- Check if granted as separate method for readability
- Use Serial annotation
- Use WalletRuntimeException instead of IllegalArgumentException
- Enable spotbugs and fix or ignore spotbugs reportings

### Removed

- Remove warnings for exceptions not thrown
- Remove deprecated PMD rule


## [0.3.1] - 2026-01-14

### Added

- Add more verification to controller test and fix typo
- Add more tests for oidc solution

### Changed

- Clean up comment
- Refactor class names and packages
- Clean up configuration options
- Update java non-major (#86)
- Update diggsweden/reusable-ci action to v2.6.0 (#84)

### Fixed

- Enable spotbugs and fix or ignore spotbugs reportings

### Removed

- Remove some exclusions and make records more immutable


## [0.3.0] - 2026-01-07

### Added

- Add gitleaksignore addition to gitleaksignore

### Changed

- Adapt to spring boot 4
- Update java major
- Update java non-major (#83)
- Update valkey/valkey docker tag to v9.0.1 (#81)
- Update java non-major (#80)
- Update diggsweden/reusable-ci action to v2.4.3 (#79)
- Use reuseable ci 2.6.0
- Update github actions (#74)
- Change devbase-justkit name, improve dev doc
- Update justfile and reuseable-ci
- Describe overwriting of audience in code

### Fixed

- Fix review issues
- Stop failing on spotbugs errors (until we have fixed them)
- Fix pmd issues
- Correct lintwarnings for docs,container


## [0.2.9] - 2025-12-08

### Added

- Add configuration for pnr claim
- Add configuration for private jwt audience
- Add personal identity number as last name to users
- Add create account v2 with oidc protection

### Changed

- Refactor config files
- Update dependency com.puppycrawl.tools:checkstyle to v12.2.0 (#71)
- Update java non-major to v2.1.0 (#70)
- Update dependency prettier to v3.7.4 (#69)
- Update diggsweden/reusable-ci action to v2.3.8 (#68)
- Merge pull request #56 from diggsweden/renovate/pin-dependencies
- Pin dependencies
- Merge pull request #63 from diggsweden/renovate/actions-checkout-6.x
- Update actions/checkout action to v6
- Start one wiremockserver per service
- Refactor v2 to v1 to match path
- Update dependency org.springframework.boot:spring-boot-starter-parent to v3.5.8 (#62)


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


[0.4.2]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.4.1..v0.4.2
[0.4.1]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.4.0..v0.4.1
[0.4.0]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.3.1..v0.4.0
[0.3.1]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.3.0..v0.3.1
[0.3.0]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.2.9..v0.3.0
[0.2.9]: https://github.com/diggsweden/wallet-client-gateway/compare/v0.2.8..v0.2.9
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
