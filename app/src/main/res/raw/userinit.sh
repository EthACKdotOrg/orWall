#!/system/bin/sh

if [ ! -d '/data/data/org.ethack.orwall' ]; then
  log -p i -t orwall "orWall doesn't seem to be installed."
  exit 0
fi

IP6TABLES=/system/bin/ip6tables
IPTABLES=/system/bin/iptables

log() {
	command log -t orwall "$@"
}

run() {
	log "$@"

	su --context "u:r:init:s0" -c "$@" 2>&1 | while read line ; do
		log "  $line"
	done
}

log "Starting orwall init as $(id)"

# FIXME: Running iptables first time seems to initalize it.
sleep 1

run "$IPTABLES -w --flush"

run "$IPTABLES -w --list"

run "$IPTABLES -w -P OUTPUT DROP"
run "$IPTABLES -w -I OUTPUT -j REJECT"
run "$IPTABLES -w -I OUTPUT -o lo -m conntrack --ctstate NEW,ESTABLISHED -j ACCEPT"

run "$IPTABLES -w -P INPUT DROP"
run "$IPTABLES -w -I INPUT -j REJECT"
run "$IPTABLES -w -I INPUT -i lo -m conntrack --ctstate NEW,ESTABLISHED -j ACCEPT"

run "$IPTABLES -w -P FORWARD ACCEPT"

run "$IPTABLES -w -N witness"
run "$IPTABLES -w -A witness -j RETURN"

## Block all traffic at boot ##
run "$IP6TABLES -w -t nat -F"
run "$IP6TABLES -w -F"
run "$IP6TABLES -w -A INPUT -j LOG --log-prefix 'Denied bootup IPv6 input: '"
run "$IP6TABLES -w -A INPUT -j DROP"
run "$IP6TABLES -w -A OUTPUT -j LOG --log-prefix 'Denied bootup IPv6 output: '"
run "$IP6TABLES -w -A OUTPUT -j DROP"

run "$IPTABLES --list"
