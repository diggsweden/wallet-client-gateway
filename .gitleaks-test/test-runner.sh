#!/bin/bash
#
# SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
#
# SPDX-License-Identifier: CC0-1.0
#
# This script provides a way to automatically check the correctness of
# our Gitleaks rules.
#
# The test runner is based on the approval testing methodology
# where we run the program and compare the output to
# a stored and approved copy of a previous test run.
# If the output is identical, the test passes. Otherwise the test fails.
#
# How to run:
#
#    .gitleaks-test/test-runner.sh <suite> <report-dir>
#
# Example:
#
#    .gitleaks-test/test-runner.sh .gitleaks-test/tests/suite.txt logs/gitleaks-test
#
# Exit codes:
#
#  - 0: There is no difference between the expected and actual output
#  - 1: There is a mismatch between the expected and actual output
#  - 2: There is no approved output for the given test suite.
#
# See: https://approvaltests.com/
#

readonly NC=$'\e[0m'
readonly RED=$'\e[31m'
readonly GREEN=$'\e[32m'
readonly YELLOW=$'\e[0;33m'

#Terminal chars
readonly CHECKMARK=$'\xE2\x9C\x94'
readonly MISSING=$'\xE2\x9D\x8C'

readonly suite="$1"
readonly outputDir="$2"
readonly image=zricethezav/gitleaks:v8.20.1
suiteShortName=$(basename "${suite%.*}")
readonly suiteShortName
readonly rawReportPath="${outputDir}/${suiteShortName}-report-raw.json"
readonly actualReportPath="${outputDir}/${suiteShortName}-report-actual.jsonl"
readonly approvedReportPath="${suite%.*}-report-approved.jsonl"
readonly productionConfigPath=.gitleaks.toml
readonly testConfigPath=.gitleaks.test.toml

echo "Creating test specific config '${testConfigPath}' based on '${productionConfigPath}'."

# Disable the allowlist for the gitleaks-test directory
# in order to not ignore the test cases.
sed -E 's|^ *".gitleaks-test|#\0|' "${productionConfigPath}" >"${testConfigPath}"

echo "Running suite file '${suite}' and reporting to '${actualReportPath}'."
podman run -v .:/path $image dir \
  --no-banner \
  --log-level error \
  --config "/path/${testConfigPath}" \
  --report-format json \
  --report-path "/path/${rawReportPath}" \
  "/path/${suite}"

# Extract the properties we are interested in
# and sort to ensure consistency between test runs.
jq -c '[.[] | { RuleID, StartLine, EndLine, StartColumn, EndColumn, Match, Secret }]
        | sort_by(.RuleID, .StartLine, .EndLine, .StartColumn, .EndColumn, .Match, .Secret)
        | .[]' \
  "${rawReportPath}" >"${actualReportPath}"

if [ ! -f "${approvedReportPath}" ]; then
  echo "---"
  cat "${actualReportPath}"
  echo "---"
  printf '%s\n' "${YELLOW}${MISSING} Oops!${NC}"
  echo "There is no approved snapshot for suite '$suite'."
  echo "Please examine the output above and copy '${actualReportPath}' to '${approvedReportPath}' to mark the suite result as approved."
  exit 2
fi

echo "Comparing '${actualReportPath}' to '${approvedReportPath}'"
diff "${approvedReportPath}" "${actualReportPath}"
readonly result=$?

if [ ! $result -eq 0 ]; then
  echo ""
  printf '%s\n' "${RED}${MISSING} FAILURE!${NC}"
  echo ""
  echo "Actual output differs from approved output!"
  echo "Legend:"
  echo " > Unapproved, but present, output"
  echo " < Approved, but absent, output"
  echo ""
  echo "Please examine the diff above and either:"
  echo ""
  echo " 1. Approve the actual output by copying '${actualReportPath}' to '${approvedReportPath}'."
  echo " 2. Fix the production code and try again"
  exit 1
else
  printf '%s\n' "${GREEN}${CHECKMARK} OK${NC}"
fi
