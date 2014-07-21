#!/bin/sh
IP6TABLES=/system/bin/ip6tables
IPTABLES=/system/bin/iptables

$IPTABLES -F INPUT
$IPTABLES -F OUTPUT
$IPTABLES -N LAN
$IPTABLES -F LAN

$IPTABLES -t nat -F OUTPUT

$IPTABLES -t nat -A OUTPUT ! -o lo -p udp -m udp --dport 53 -j REDIRECT --to-ports 5400
$IPTABLES -t nat -A OUTPUT -d 10.0.0.0/8 -j RETURN
$IPTABLES -t nat -A OUTPUT -d 172.16.0.0/12 -j RETURN
$IPTABLES -t nat -A OUTPUT -d 192.168.0.0/16 -j RETURN

# Accept connections to the LAN.
$IPTABLES -A OUTPUT -d 10.0.0.0/8 -j LAN
$IPTABLES -A OUTPUT -d 172.16.0.0/12 -j LAN
$IPTABLES -A OUTPUT -d 192.168.0.0/16 -j LAN

$IPTABLES -A LAN -p tcp -m tcp --dport 53 -j REJECT --reject-with icmp-port-unreachable
$IPTABLES -A LAN -p udp -m udp --dport 53 -j REJECT --reject-with icmp-port-unreachable
$IPTABLES -A LAN -j ACCEPT

$IPTABLES -P OUTPUT DROP
$IPTABLES -P INPUT DROP
$IPTABLES -P FORWARD ACCEPT

# ensure there is really NO output
$IPTABLES -A OUTPUT -j REJECT
# Well, some rules are added to INPUT in order to accept stuff? No, sorry. You can't!
$IPTABLES -I INPUT 1 -j REJECT
$IPTABLES -I INPUT 1 -i lo -j ACCEPT
$IPTABLES -I INPUT 1 -m conntrack --ctstate RELATED,ESTABLISHED -j ACCEPT
