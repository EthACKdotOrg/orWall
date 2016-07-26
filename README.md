[![Build Status](https://travis-ci.org/EthACKdotOrg/orWall.svg?branch=master)](https://travis-ci.org/EthACKdotOrg/orWall)

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

## Where can we find the APK?
orWall is published on [f-droid](https://f-droid.org/repository/browse/?fdid=org.ethack.orwall), and we provide a GPG signed APK in the release tab.

Beware, we found out people are messing around and push the APK on Google Play, as a paid app, without mentioning sources nor author. That's not the official one. We don't know if the app code is the same we provide.

If you find such an app, please contact us.

### Coming soon
- Support for other Onion Router applications (i2p)
- Support for application dedicated stream (for Orbot)

### External libraries
- [super-command](https://github.com/dschuermann/superuser-commands) (Apache2) for root accesses

### Support us
- Bitcoin: 1Kriu9owRhEsFkj8Lc6Wr5xTv8YTNphhXn
- Litecoin: LXjW5tKRHbrbxwTZmitj4JeBqcm4xpqvJ2

### Follow us on Twitter
https://twitter.com/orWallApp
