#!/bin/sh
IP6TABLES=/system/bin/ip6tables
IPTABLES=/system/bin/iptables


$IPTABLES -P OUTPUT DROP
$IPTABLES -P INPUT DROP
$IPTABLES -P FORWARD ACCEPT

# Well, some rules are added to INPUT in order to accept stuff? No, sorry. You can't!
$IPTABLES -I INPUT 1 -j REJECT
$IPTABLES -I INPUT 1 -i lo -j ACCEPT
$IPTABLES -I INPUT 1 -m conntrack --ctstate RELATED,ESTABLISHED -j ACCEPT

# Seems some systems just want to play with default Policy. Let block them!
$IPTABLES -I OUTPUT 1 -j REJECT