#!/system/bin/sh

if [ ! -d '/data/data/org.ethack.orwall' ]; then
  log -p i -t orwall "orWall doesn't seem to be installed."
  exit 0
fi



IP6TABLES=/system/bin/ip6tables
IPTABLES=/system/bin/iptables

log() {
  command log -p d -t orwall "$@"
}

run() {
  log "$@"
  command $@ 2>&1 | while read line ; do
    log "  $line"
  done
}

#ORBOT_UID=$(dumpsys package org.torproject.android | sed -n 's/^[\ ]*userId=//p')
ORBOT_UID=$(cat /data/system/packages.list | sed -n 's/^org.torproject.android //p' | cut -d ' ' -f1)

command ${IPTABLES} -C ow_OUTPUT_LOCK -j REJECT
if [ $? -eq 0 ]; then
    log "orwall seems to be already initialized"
    exit 0
else
    log "Starting orwall init as $(id)"
fi

# FIXME: Running iptables first time seems to initalize it.
sleep 1
run "$IPTABLES -w --list"

run "$IPTABLES -w -P OUTPUT DROP"

run "$IPTABLES -w -N ow_OUTPUT_LOCK"
run "$IPTABLES -w -A ow_OUTPUT_LOCK -m owner --uid-owner $ORBOT_UID -p tcp --dport 9030 -j ACCEPT"

run "$IPTABLES -w -A ow_OUTPUT_LOCK -m owner --uid-owner $ORBOT_UID -m conntrack --ctstate NEW,RELATED,ESTABLISHED -j ACCEPT"
run "$IPTABLES -w -A ow_OUTPUT_LOCK -j REJECT"
run "$IPTABLES -w -I OUTPUT -g ow_OUTPUT_LOCK"

# INPUT
run "$IPTABLES -w -P INPUT DROP"
run "$IPTABLES -w -N ow_INPUT_LOCK"
run "$IPTABLES -w -A ow_INPUT_LOCK -m owner --uid-owner $ORBOT_UID -m conntrack --ctstate NEW,RELATED,ESTABLISHED -j ACCEPT"
run "$IPTABLES -w -A ow_INPUT_LOCK -j REJECT"
run "$IPTABLES -w -I INPUT -g ow_INPUT_LOCK"

## Block all traffic at boot ##
run "$IP6TABLES -w -P INPUT DROP"
run "$IP6TABLES -w -P OUTPUT DROP"
run "$IP6TABLES -w -P FORWARD DROP"
run "$IP6TABLES -w -I INPUT -j REJECT"
run "$IP6TABLES -w -I OUTPUT -j REJECT"
run "$IP6TABLES -w -I FORWARD -j REJECT"

# output iptables status: filter
#run "$IPTABLES -nL -t filter"
# output iptables status: nat
#run "$IPTABLES -nL -t nat"
