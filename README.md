# Board Gamer App

Name: Game Nights App

## Overview
Board Gamer App is an Android application designed to help users plan and organize board game nights. 
The app is tested on the Pixel 8 Emulator but is expected to work on 99% of current Android versions. 
It functions out of the box, except for sending notifications, which requires a private key for Google Cloud Messaging.

---

## Features
- Always stay informed about the next game night schedule and location. 
- Ensure fair rotation of hosting responsibilities among players. 
- Suggest one or multiple games to influence the game night selection. 
- Vote on proposed games in advance to shape the evening. 
- Rate the host, food, and overall experience after the event. 
- Send quick messages to other players (Notifications), for example, to notify delays. 
- Optional: Receive reminders to choose your preferred food category (Italian, Greek, Turkish, etc.) before ordering. 
- Optional: Hosts receive timely notifications about the preferred food choice to select an appropriate local delivery service.

---

## Quick Start: Developing or Testing

#### Prerequisites
- Android Studio installed
- Git installed
- Android Emulator (recommended: Pixel 8 Emulator)

#### Steps to Get Started

1. Clone the repository: `git clone https://github.com/Fabo011/board-gamer-app.git`
2. Open the project in Android Studio.
3. Create a `raw` folder inside the res folder.
4. Inside `res/raw/`, create a file named `service_account.json`. 
5. For notifications: Request the private key from @Fabo011 and paste it into `service_account.json`. For testing without notifications: Create an empty service_account.json file.
6. Build the project using Gradle. 
7. Start the emulator and run the app.

---

### Additional Information
The `google-service.json` file contains an API key, but these details are public and not confidential.
If you encounter any issues, reach out to Fabo011 or @VolkBeck.
