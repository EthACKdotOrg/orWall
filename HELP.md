# HEEEELLPP !!

Wait, calm down.

## Everything is broken, I can't access the network anymore!

Keep breathing, everything is going to be OK. In order to get your network back, you have to:
  * uninstall the Torrific app
  * remove the init-script located in /usr/local/userinit.sh
  * reboot your phone

## It seems like the init-script isn't being used.

That may happen — on SlimKat, there's a /etc/init.d/90userinit script allowing to put the
user init script in a nice location: /data/loca/userinit.sh

Do you have any such support? If not, please fill in an issue for your Android version/flavor.
We'll try to get it working :).

## You override my own userinit.sh script!

Errr… Yes… Unfortunately, we tried to get the userinit.d directory working, but it seems like 
it's a bit crappy now :(. Best shot was for this one…

Maybe we can put the init-script in the /etc/init.d/ directory. But it's read-only… Meaning some
more crap in the shell commands, and we'd like to avoid that…
