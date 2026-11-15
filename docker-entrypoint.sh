#!/bin/sh
# Reconstruye el wallet de Oracle a partir de la variable ORACLE_WALLET_BASE64
# (zip del wallet en base64) y luego inicia la aplicacion.
set -eu

: "${ORACLE_WALLET_BASE64:?ORACLE_WALLET_BASE64 must be provided by ECS Secrets Manager injection}"

umask 077
wallet_archive="$(mktemp)"
trap 'rm -f "$wallet_archive"' EXIT HUP INT TERM

if ! printf '%s' "$ORACLE_WALLET_BASE64" | base64 --decode > "$wallet_archive"; then
  echo "Unable to decode the Oracle wallet secret." >&2
  exit 1
fi

if unzip -Z1 "$wallet_archive" | grep -Eq '(^/|(^|/)\.\.(/|$))'; then
  echo "The Oracle wallet archive contains an unsafe path." >&2
  exit 1
fi

if ! unzip -Z1 "$wallet_archive" | grep -Eq '(^|/)tnsnames\.ora$'; then
  echo "The Oracle wallet archive does not contain tnsnames.ora." >&2
  exit 1
fi

unzip -o -q "$wallet_archive" -d "$TNS_ADMIN"
rm -f "$wallet_archive"
trap - EXIT HUP INT TERM
unset ORACLE_WALLET_BASE64

exec java -jar /app/app.jar
