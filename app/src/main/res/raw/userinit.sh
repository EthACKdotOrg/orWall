#!/bin/sh
IP6TABLES=/system/bin/ip6tables
IPTABLES=/system/bin/iptables


$IPTABLES -P OUTPUT DROP
$IPTABLES -P INPUT DROP
$IPTABLES -P FORWARD ACCEPT

# Accept local connections
$IPTABLES -I INPUT 1 -i lo -j ACCEPT

# Seems some systems just want to play with default Policy. Let block them!
$IPTABLES -I OUTPUT 1 -j REJECT