# Voximplant Android SDK v3 Demo (beta)

This repository contains a sample Android application demonstrating the integration and use of the
Voximplant Android SDK 3.x.
The application is developed using Kotlin and Jetpack Compose and also uses App Modularization and
Hilt for a more user-friendly
dependency management.

The application is currently in development.

## Features

- **Audio calls**: Demonstration of the capabilities of Voximplant Android SDK for making audio
  calls.
- **Jetpack Compose UI**: The application's user interface is built using Jetpack Compose for more
  convenient and
  declarative UI development.
- **App Modularization**: The project is divided into modules to make the code extensible and easier to maintain.
- **Hilt DI (Dependency Injection)**: Using Hilt provides a cleaner architecture.

## Project structure

The project is divided into several modules:

- **:app**: The main application module containing screen navigation and user interface.
- **:feature:audiocall**: Audio call main screen.
- **:feature:audiocall-incoming**: Module responsible for incoming audio calls.
- **:feature:audiocall-ongoing**: Module responsible for managing an ongoing call.
- **:feature:videocall**: Video call main screen.
- **:feature:videocall-incoming**: Module responsible for incoming video calls.
- **:feature:videocall-ongoing**: Module responsible for managing an ongoing call.
- **:feature:catalog**: Module responsible for switching between functional features of the
  Voximplant Android SDK.
- **:feature:login**: Module responsible for authorization in the demo application.
- **:core**: Multiple small independent modules, each implementing its own functionality:
  - Business logic implemented using Voximplant Android SDK
  - UI components
  - Data and domain layers
  - Resources
  - Helpers and utils

## Requirements

- Android Studio Arctic Fox (or newer)
- Gradle 8.0 (or newer)
- Java 17 (or newer)
- Android API 21 (or newer)

## Installing

1. Clone the repository with this command:
   ```bash
   git clone https://github.com/voximplant/android-sdk-v3-demo.git
   ```
2. Open the project in Android Studio.
3. Sync the project with Gradle.
4. Run the app on a real device or an emulator.

## How to use

Make sure you have a Voximplant account to make calls. [Register](https://voximplant.com/).

Your Voximplant account should be configured with the following setup:

- Voximplant application
- at least two Voximplant users
- VoxEngine scenario
- routing setup

Push notifications require additional
configuration. [Set up](https://voximplant.com/docs/howtos/sdks/push_notifications/android_sdk).

1. Launch the application on a real device or an emulator.
2. Log in to your account by clicking "Login".
3. Select the desired functionality, such as audio calling.

## Dependencies

Voximplant Android SDK is available on `mavenCentral()`.

```
implementation(platform("com.voximplant:android-sdk-bom:3.0.0-beta1"))
implementation("com.voximplant:android-sdk-core")
implementation("com.voximplant:android-sdk-calls")
```

## Useful links

1. [Quickstart](https://voximplant.com/docs/introduction)
2. [HowTo's](https://voximplant.com/docs/howtos)

## Have a question?

- Contact us via [support@voximplant.com](mailto:support@voximplant.com).
- Create a new [issue](https://github.com/voximplant/android-sdk-v3-demo/issues).
- join our developer [Discord](https://discord.gg/sfCbT5u) community.
