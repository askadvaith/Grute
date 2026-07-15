# Grute

Too many people texting in a group chat when you only care about a certain someone? Someone constantly pinging you in a group but you want to ignore them (but not the rest)? WhatsApp only allows you to mute the group as a whole; why let one rotten apple spoil the party?

**Grute is the answer!**

Grute (group + mute, imaginative I know) lets you mute specific users within a group chat without muting everyone else. That way, you can keep track of what is going on in the GC without having to deal with annoying people who spam your notifications! 

Grute works by using the `NotificationListenerService` to listen for WhatsApp notifications, and then parses them to ensure only notifications you want get through. It takes care of all sorts of notification formats that may come up, whether it's just a regular message, a reply or a mention. This way, you aren't violating any WhatsApp TOS!

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
