# orWall

Because real life is a Dystopia

## Website
https://orwall.org/

## What's this?
orWall will force selected applications through Orbot while preventing unchecked applications to have network access.
In order to do so, it will call the iptables binary. This binary, present on your Android device, requires superuser access (aka root). It's the application that manages the firewall on Linux and, by extension, on Android.

In short, orWall will add special iptables rules in order to redirect traffic for applications through Tor; it will also add required rules in order to block traffic for other apps.
The redirection is based on the application user id. Each android application runs as a dedicated user, and iptables has support for traffic filtering based on the process owner, meaning it's really easy and pretty safe to do this kind of thing on an Android device. 

The application works in two stages: first, an init-script will block all incoming and outgoing traffic. This should prevent leaks, knowing Android sends stuff before you can even access the device.
Second stage comes once the device is fully booted: orWall itself takes the lead on the firewall, and add required rules in order to allow Orbot traffic, and redirect selected application to Orbot TransPort.

### External libraries
- [super-command](https://github.com/dschuermann/superuser-commands) (Apache2) for root accesses
- [NetCipher](https://github.com/guardianproject/NetCipher) (3-clause BSD license) for some Orbot helpers as well as good proxy stuff (coming soon)
