#Torrific

Put your apps behind Orbot and block all unwanted traffic in one go.

## Prerequisites
    * Root access on your device
    * Orbot

Important note: this application does NOT (and will never) replace Orbot.

## How to use

  * Run Orbot, and deactivate its root access as well as its transparent proxy stuff
  * Start Torridic, select apps you're wanting to redirect to Orbot
  * Reboot
  * Enjoy privacy!

## FAQ

### What's the point, we already have Orbot!
Yep, but it doesn't block all traffic. It only redirects either all apps or selected ones. In the latter case, it doesn't take care of the other apps.

### Well, we could do some mix with AFWall!
Orbot flushes all iptables rules before adding its stuff… AFWall does something in the same way, adding new tables (useless, in this very case) and so on.

### OK, so what?
Torrific will allow you to:
 * block all outgoing traffic at boot time;
 * select the apps you want to allow;
 * force those selected apps through Orbot.

### It reminds me of something…
Yep. It's the app for [one of of our other projects](https://github.com/EthACKdotOrg/nexus4-iptables).

### Why such a name? "Torrific"… ?
We just think Tor is a terrific application. ;)

### What kind of permissions does it need?
All of them. We want to be able to know where you are, what you're talking about, who's with you and so on.
</troll>

Seriously: just have a look at the manifest.xml file, it will let you know the real thing.

### But you need to be root?
Yep. Unfortunately. There's apparently no other way to talk to iptables — well, you're talking to the kernel, that's only for an important user, like root. Not the "shell" one we have by default.

### So I need to root my phone…
Yep.

### What's the Answer to the Ultimate Question of Life, the Universe, and Everything?
Easy: 42. But you already knew that right?
