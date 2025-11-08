fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android build

```sh
[bundle exec] fastlane android build
```

Build the Android app (SIGNED AAB for Play Store) - DEFAULT

### android build_apk

```sh
[bundle exec] fastlane android build_apk
```

Build SIGNED RELEASE APK (for testing or direct distribution)

### android build_debug

```sh
[bundle exec] fastlane android build_debug
```

Build debug APK (UNSIGNED - for local development only, NOT for distribution)

### android deploy

```sh
[bundle exec] fastlane android deploy
```

Build and deploy signed AAB ONLY to DeployGate

### android deploy_debug

```sh
[bundle exec] fastlane android deploy_debug
```

Build and deploy debug APK to DeployGate

### android deploy_with_version

```sh
[bundle exec] fastlane android deploy_with_version
```

Build and deploy with version bump

### android clean

```sh
[bundle exec] fastlane android clean
```

Clean build artifacts

### android test

```sh
[bundle exec] fastlane android test
```

Run tests

### android ci

```sh
[bundle exec] fastlane android ci
```

Build, test, and deploy to DeployGate

### android release

```sh
[bundle exec] fastlane android release
```

Full pipeline: clean, build, and deploy

### android build_playstore

```sh
[bundle exec] fastlane android build_playstore
```

Build AAB for Play Store submission

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
