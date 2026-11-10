#!/usr/bin/env bash
# Servicio Ubuntu: ejecutarlo como root localmente (mysql via socket), con BD y migracion instaladas.
set -u
while true; do
  read -ra cpu1 < /proc/stat
  total1=0; for ((i=1; i<${#cpu1[@]};i++));do total1=$((total1+cpu1[i]));done
  idle1=${cpu1[4]}
  sleep 1
  read -ra cpu2 < /proc/stat
  total2=0; for ((i=1; i<${#cpu2[@]};i++));do total2=$((total2+cpu2[i]));done
  idle2=${cpu2[4]}
  delta=$((total2-total1)); didle=$((idle2-idle1))
  cpu="NULL"
  if ((delta>0));then cpu=$(awk -v d="$delta" -v i="$didle" 'BEGIN{printf "%.2f",100*(d-i)/d}');fi
  total_mb=$(awk '/MemTotal/{printf "%d",$2/1024}' /proc/meminfo)
  avail_mb=$(awk '/MemAvailable/{printf "%d",$2/1024}' /proc/meminfo)
  used_mb=$((total_mb-avail_mb))
  gpu="NULL"
  if command -v nvidia-smi >/dev/null 2>&1; then
    v=$(nvidia-smi --query-gpu=utilization.gpu --format=csv,noheader,nounits 2>/dev/null | head -1 | tr -cd '0-9.')
    [[ -n "$v" ]] && gpu="$v"
  fi
  # La VM puede no exponer GPU física; en ese caso se registra NULL, no un valor inventado.
  read -r queries active rx tx < <(mysql --batch --skip-column-names -e \
    "SHOW GLOBAL STATUS WHERE Variable_name IN ('Questions','Threads_connected','Bytes_received','Bytes_sent')" 2>/dev/null | \
    awk '$1=="Questions"{q=$2} $1=="Threads_connected"{c=$2} $1=="Bytes_received"{r=$2} $1=="Bytes_sent"{t=$2} END{print q+0,c+0,r+0,t+0}')
  mysql -D optem_db --batch --skip-column-names -e "INSERT INTO OPTEM_SERVER_METRICAS(ram_usada_mb,ram_total_mb,cpu_porcentaje,gpu_porcentaje,consultas_mysql,conexiones_mysql,bytes_recibidos,bytes_enviados) VALUES($used_mb,$total_mb,$cpu,$gpu,$queries,$active,$rx,$tx)" 2>/dev/null || echo 'No se pudo insertar telemetría OPTEM' >&2
  sleep 9
 done
