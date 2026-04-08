#!/usr/bin/env bash

# SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
#
# SPDX-License-Identifier: EUPL-1.2

# Preferences
raplpImage="ghcr.io/diggsweden/rest-api-profil-lint-processor"
raplpVersion="v1.1.1"
scriptDir=$(dirname "$(readlink -f "$0")")
openapiSourceFolder="$scriptDir/src/main/resources/static"
openapiFileFilter="*openapi*.yaml"
targetFolder="$scriptDir/target/raplp"

# Check existence of source openapi spec
if ! [ -d "$openapiSourceFolder" ]; then
  echo "Specified source folder $openapiSourceFolder does not exist."
  echo "Please check path location."
  exit 1
fi

function run_report {
  openapiSpecPath="$openapiSourceFolder/$1"

  if ! [ -x "$(command -v yq)" ]; then
    echo 'Error: yq is not installed.' >&2
    exit 1
  fi
  apiVersion=$(yq '.info.version' "$openapiSpecPath")
  timestamp=$(date "+%Y%m%d-%H%M%S")

  # Copy openapi spec to shared mount folder
  cp "$openapiSpecPath" "$targetFolder"

  # Run linter - creates report files in the shared target/data folder
  podman run --rm -v "$targetFolder":/data "$raplpImage":"$raplpVersion" \
    -f /data/"$1" \
    -l /data/"$1"-"$apiVersion"-"$timestamp".log \
    --dex /data/"$1"-"$apiVersion"-"$timestamp".xlsx |
    tee "$targetFolder"/"$1"-"$apiVersion"-"$timestamp".txt
}

# Create target folder - shared with container
mkdir -p "$targetFolder"
chmod 777 "$targetFolder"

# Work on available openapi specification files
for file in $openapiSourceFolder/$openapiFileFilter; do
  if [ -f "$file" ]; then
    echo ""
    echo "Evaluate" "$file"
    run_report "${file##*/}"
  fi
done
