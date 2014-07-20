# HEEEELLPP !!

Wait, calm down.

## All is broken, I can't access network anymore!

Breath, all will be ok. In order to get back your network, you have to:
  * uninstall the Torrific app
  ° remove the init-script located in /usr/local/userinit.sh
  * reboot your phone

## Seems the init-script isn't used

May happen — on SlimKat, there's a /etc/init.d/90userinit script allowing to put
user init script in a nice location, /data/loca/userinit.sh

Do you have such support? If not, please fill an issue with your Android version/flavour.
We'll try to get it working :).

## You override my own userinit.sh script!

Errr… yes… unfortunately, we tried to get the userinit.d directory working, but it seems
it's a bit crappy now :(. Best shot was this other one…

We can, maybe, put the init-script in /etc/init.d/ directory. But it's read-only… Meaning some
more crap in the shell commands, and we'd like to avoid that…
