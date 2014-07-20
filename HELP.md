# HEEEELLPP !!

Wait, calm down.

## All is broken, I can't access network anymore!

Breath, all will be ok. In order to get back your network, you have to:
  * uninstall the Torrific app
  ° remove the init-script located in /usr/local/userinit.d/torrific
  * reboot your phone

## Seems the init-script isn't used

May happen — on SlimKat, there's a /etc/init.d/90userinit script allowing to put
user init script in a nice location, /data/loca/userinit.d/

Do you have such support? If not, please fill an issue with your Android version/flavour.
We'll try to get it working :).
