# ZOoTR Android
Port of OoTR to Android
This is the official randomizer: https://ootrandomizer.com/generator

Here is the git repo for that: https://github.com/OoTRandomizer/OoT-Randomizer/tree/Dev

This application makes use of the python code from the OoT-Randomizer repo, with minor modifications

The first time you generate a randomizer will take the longest as it needs to decompress the rom. After that happens once it caches it on your device so it won't need to happen again.

# Tested/Verified Devices (feel free to make a PR to expand this list after you test)
- Pixel 11
- AYN Thor

# Installation
- Navigate to the [releases tab](https://github.com/EGahhd/ZootrAndroid/releases/latest)
- Download the file called `zootr-release.apk` to your android device
- Open the file and grant permission to install the application
- Because this file is the signed application, this will also update the app if it is already installed

# Initial Setup
- Click the `Select Rom` button, select a rom that is verion 1.0 NTSC-U or NTSC-J. If it asks for permission to access the file, allow it to do so.
- Click the first `Select Directory` button, select or create a directory that is dedicated to storing the generated roms. If it asks for permission to access the folder, allow it to do so.
- Click the second `Select Directory` button, select or create a directory that is dedicated to storing the settings you will save for the randomizer. If it asks for permission to access the folder, allow it to do so.
  - The files in this directory can also be shared with other users by sending them the file and having them place it into their settings directory
