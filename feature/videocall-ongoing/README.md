# :feature:videocall-ongoing module

## Features

This module provides the UI to manage an ongoing video call.

- Switch audio devices;
- Toggle microphone mute state;
- Camera switch;
- Hang up;

It also starts a foreground service that keeps the access to the microphone and camera
in any application state and posts a notification with video call details to the system tray.
