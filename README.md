Demonstration of the selenium browser start/stop issues in CI environments.

Many issues are triggered when there is no DBUS_SESSION_BUS_ADDRESS environment variable set, as is demonstrated
by the travis builds of this demonstration project. Click the status icon below to get the test logs for the
two different CI runs: one with a fake DBUS_SESSION_BUS_ADDRESS set, the other without.

The CI runs involve chromium-browser 50 and chromedriver 2.21, both installed via apt-get from the original
Ubuntu 14.04 (trusty) distribution. The browser is run against Xvfb and java selenium web driver version 2.53.0
is being used.

Apparently, the bug had been identified a few years ago alrady: crbug.com/309093 but the chromium devs
consider it a glib bug...

--
[![Build Status](https://travis-ci.org/jjYBdx4IL/selenium-start-stop-stresstest.png?branch=master)](https://travis-ci.org/jjYBdx4IL/selenium-start-stop-stresstest)


