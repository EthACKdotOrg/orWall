#Torrific


Put your apps behind Orbot, and block all unwanted traffic in one row.

### What's the point, we already have Orbot!
Yep, but this one doesn't block all traffic. It only redirect either all, or selected apps. In the latter case, it doesn't take care of the other apps.

### Well, we could do some mix with AFWall!
Orbot flushes all iptables rules before adding its stuff… AFWall does something in the same way, adding new (useless in this very case) tables and so on

### OK, so what… ?
Torrific will allow you to:
 * block all outgoing traffic at boot time
 * select the apps you're wanting to allow
 * force those selected apps through Orbot

### It remembers me something…
Yep. It's the app for [another of our project](https://github.com/EthACKdotOrg/nexus4-iptables).

### Why such a name? "Torrific"… ?
We just think Tor is a terrific application ;)

### What kind of permissions does it need?
All of them. We want to be able to know where you are, what you're talking about, who's with you and so on.
</troll>

Seriously: just have a look in the manifest.xml file, it will let you know the real thing.

### But you need root… ?
Yep. Unfortunately. There's apparently no other way to talk to iptables — well, you're talking to the kernel, that's only for important user. Like root. Not the "shell" one we have by default.

### So I need to root my phone…
Yep

### What's the Answer to the Ultimate Question of Life, the Universe, and Everything?
Easy: 42. But you already know that right?
