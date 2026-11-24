#!/usr/bin/env bash
set -euo pipefail

UMBRAL_CPU="${UMBRAL_CPU:-85}"
UMBRAL_MEMORIA="${UMBRAL_MEMORIA:-90}"
UMBRAL_DISCO="${UMBRAL_DISCO:-85}"
alertas=0

alertar() {
    echo "ALERTA: $1"
    alertas=$((alertas + 1))
}

uso_cpu() {
    local inactivo
    inactivo=$(top -bn2 -d1 | awk '/^%Cpu/ {valor=$8} END {print valor}')
    awk -v inactivo="${inactivo}" 'BEGIN {printf "%d", 100 - inactivo}'
}

uso_memoria() {
    free | awk '/^Mem:/ {printf "%d", ($3 / $2) * 100}'
}

uso_disco() {
    df --output=pcent / | tail -1 | tr -dc '0-9'
}

revisar() {
    local nombre="$1" valor="$2" umbral="$3"
    echo "${nombre}: ${valor}% (umbral ${umbral}%)"
    if (( valor >= umbral )); then
        alertar "${nombre} en ${valor}%"
    fi
}

echo "=== Stream-Chat: revisión de recursos $(date '+%Y-%m-%d %H:%M:%S') ==="
revisar "CPU" "$(uso_cpu)" "${UMBRAL_CPU}"
revisar "Memoria" "$(uso_memoria)" "${UMBRAL_MEMORIA}"
revisar "Disco /" "$(uso_disco)" "${UMBRAL_DISCO}"
free -h | awk 'NR <= 3'

if ! command -v docker > /dev/null 2>&1; then
    alertar "docker no está instalado"
    exit 1
fi

echo "--- Contenedores ---"
docker stats --no-stream --format 'table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}'

no_sanos=$(docker ps -a --filter 'name=streamchat' --format '{{.Names}} {{.Status}}' \
    | grep -v '(healthy)' | grep -v 'kafka_init' || true)
if [[ -n "${no_sanos}" ]]; then
    alertar "contenedores que no están healthy:"
    echo "${no_sanos}"
fi

echo "=== Alertas: ${alertas} ==="
(( alertas == 0 ))
