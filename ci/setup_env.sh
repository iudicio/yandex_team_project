#!/bin/sh

set -eu
umask 077

printf 'apiAccessToken=%s\n' "${GH_API_ACCESS_TOKEN:-}" > develop.properties
