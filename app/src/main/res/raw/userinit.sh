#!/system/bin/sh

# check if orwall is installed
if [ ! -d '/data/data/org.ethack.orwall' ]; then
  log -p i -t orwall "orWall doesn't seem to be installed."
  exit 0
fi

IP6TABLES=/system/bin/ip6tables
IPTABLES=/system/bin/iptables

# check if iptables can lock
if ${IPTABLES} --help | grep -q -e "--wait"; then
    IP6TABLES=/system/bin/ip6tables\ -w
    IPTABLES=/system/bin/iptables\ -w
fi

log() {
  command log -p d -t orwall "$@"
}

run() {
  log "$@"
  command $@ 2>&1 | while read line ; do
    log "  $line"
  done
}

ORBOT_UID=$(cat /data/system/packages.list | sed -n 's/^org.torproject.android //p' | cut -d ' ' -f1)

# paranoia / this script should run one time only
command ${IPTABLES} -C ow_OUTPUT_LOCK -j DROP
if [ $? -eq 0 ]; then
    log "orwall seems to be already initialized"
    exit 0
else
    log "Starting orwall init as $(id)"
fi

# FIXME: Running iptables first time seems to initalize it.
sleep 1
run "$IPTABLES --list"

# OUTPUT
run "$IPTABLES -P OUTPUT DROP"
run "$IPTABLES -N ow_OUTPUT_LOCK"
run "$IPTABLES -A ow_OUTPUT_LOCK -m owner --uid-owner $ORBOT_UID -m conntrack --ctstate NEW,RELATED,ESTABLISHED -j ACCEPT"
run "$IPTABLES -A ow_OUTPUT_LOCK -j DROP"
run "$IPTABLES -I OUTPUT -j ow_OUTPUT_LOCK"

# INPUT
run "$IPTABLES -P INPUT DROP"
run "$IPTABLES -N ow_INPUT_LOCK"
run "$IPTABLES -A ow_INPUT_LOCK -m owner --uid-owner $ORBOT_UID -m conntrack --ctstate NEW,RELATED,ESTABLISHED -j ACCEPT"
run "$IPTABLES -A ow_INPUT_LOCK -j DROP"
run "$IPTABLES -I INPUT -j ow_INPUT_LOCK"

## Block all traffic at boot ##
run "$IP6TABLES -P INPUT DROP"
run "$IP6TABLES -P OUTPUT DROP"
run "$IP6TABLES -P FORWARD DROP"
run "$IP6TABLES -I INPUT -j REJECT"
run "$IP6TABLES -I OUTPUT -j REJECT"
run "$IP6TABLES -I FORWARD -j REJECT"

# output iptables status: filter
#run "$IPTABLES -nL -t filter"
# output iptables status: nat
#run "$IPTABLES -nL -t nat"
