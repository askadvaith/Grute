# Grute

Too many people texting in a group chat when you only care about a certain someone? Someone constantly pinging you in a group when you want to ignore them (but not the rest)? WhatsApp only allows you to mute the group as a whole; why let one rotten apple spoil the party?

### **Grute is the answer!**

Grute (group + mute, imaginative I know) lets you mute specific users within a group chat without muting everyone else. That way, you can keep track of what is going on in the GC without having to deal with annoying people who spam your notifications! 

Grute works by using the `NotificationListenerService` to listen for WhatsApp notifications, and then parses them to ensure only notifications you want get through. It takes care of all sorts of notification formats that may come up, whether it's just a regular message, a reply or a mention. This way, you aren't violating any WhatsApp TOS!

## The App
<p align="center">
  <img src="https://github.com/user-attachments/assets/e2716e36-edc8-4665-9e65-a79d3fc61ba5" width="225" alt="Home screen" />
  <img src="https://github.com/user-attachments/assets/7ed4611e-358f-4120-839c-7e37ee36c812" width="225" alt="Muted users screen" />
  <img src="https://github.com/user-attachments/assets/d5fa7db7-d077-4a25-99c7-2e7796540026" width="225" alt="Settings screen" />
  <img src="https://github.com/user-attachments/assets/f5ebde5a-9b63-4a15-a6ef-0e139058134b" width="225" alt="Proxy notifications screen" />
</p>

## Installation & Troubleshooting

### Play Protect Warning
When sideloading the APK, Google Play Protect may block the installation with a warning. This happens because `NotificationListenerService` is a sensitive service to give permission to.
You can look through the source code to ensure that your information is secure; everything is stored locally and nothing else is being done with your notification data besides some regex parsing (or you can just trust me :D)

**How to resolve it:**
1. Go to the Play Store and tap on your profile on the top right.
2. Click "Play Protect" and click on the settings icon on the top right.
3. Disable/pause app scanning (topmost option).

## Usage

### Level 1: Basic Muting
You can directly start muting users in the app. However, because WhatsApp triggers the notification sound *before* Grute has a chance to intercept it, you might occasionally hear some notification sound leakage even though the notification never shows up in your tray.

### Level 2: Proxy Notifications
To completely eliminate sound leakage:
1. In WhatsApp, mute the notification ringtone for the group (don't just natively mute it!)
2. In Grute, navigate to Settings and enable "Proxy Notifications". 
Grute will now fully take over the notification duties for groups where user muting is in effect, playing your chosen notification sound or vibration only when a non-muted user sends a message.

## Technologies Used
- Kotlin
- Jetpack Compose (AnimatedContent, Custom Layouts)
- Room Database (Local Persistence)
- NotificationListenerService

## License
MIT License
