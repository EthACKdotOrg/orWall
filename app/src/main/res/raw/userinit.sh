#!/bin/sh
IP6TABLES=/system/bin/ip6tables
IPTABLES=/system/bin/iptables

$IPTABLES -P OUTPUT DROP
$IP6TABLES -P OUTPUT DROP
$IPTABLES -P INPUT DROP
$IP6TABLES -P INPUT DROP
$IPTABLES -P FORWARD ACCEPT

# Accept local connections
$IPTABLES -I INPUT -i lo -j ACCEPT
$IP6TABLES -I INPUT -i lo -j ACCEPT

# Seems some systems just want to play with default Policy. Let block them!
$IPTABLES -I OUTPUT -j REJECT
$IP6TABLES -I OUTPUT -j REJECT

# ref: https://lists.torproject.org/pipermail/tor-talk/2014-March/032503.html
$IPTABLES -I OUTPUT ! -o lo ! -d 127.0.0.1 ! -s 127.0.0.1 -p tcp -m tcp --tcp-flags ACK,FIN ACK,FIN -j DROP
$IPTABLES -I OUTPUT ! -o lo ! -d 127.0.0.1 ! -s 127.0.0.1 -p tcp -m tcp --tcp-flags ACK,RST ACK,RST -j DROP
