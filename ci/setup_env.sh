#!/bin/bash

set -eu

printf 'apiAccessToken=%s\n' "${GH_API_ACCESS_TOKEN:-}" > develop.properties
