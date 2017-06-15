# Layer Android SDK Quickstart

The SDK Quickstart app is a minimal messaging app designed to provide a starting point for using the Layer SDK.

##<a name="setup"></a>Setup

* Import the project into Android Studio.
* Enter your Layer App ID in the `layer_environments.json` file. This can be obtained from the Layer developer console.
* Enter your Layer Identity Provider URL in the `layer_environments.json` file.

##<a name="run-on-standalone"></a>Running on standalone

* Disable hostname validation by setting `LayerClient.Options.runAgainstStandalone(true)`
* Modify endpoints: Use `https://{standalone-ip}:5556/certificates` for certs and `https://{standalone-ip}:9933/websocket` for sync
* Use app ID `layer:///apps/staging/00000000-0000-1000-8000-000000000000`

##<a name="enabling-push-notifications"></a>Enabling push notifications
Push notifications are disabled by default to ease initial setup. To enable them, follow these steps:
 * Generate a JSON configuration file for FCM using the Firebase developers console. There is a tutorial here: https://docs.layer.com/sdk/android/push#setting-up-push-with-layer.
 * Replace the `google-service.json` file in this project with the generated one.
 * In `App.java`, set the Layer SDK to use FCM by uncommenting the line `options.useFirebaseCloudMessaging(true)` in the method `getLayerClient()`.

##<a name="structure"></a>Structure

* **App:** Application class. This maintains instances of `LayerClient` and `AuthenticationProvider`, along with accessors to each for use throughout the app.
* Activities:
  * **BaseActivity:** Base Activity class for handling menu titles and the menu back button and ensuring the `LayerClient` is connected when resuming Activities.
  * **ConversationsListActivity:** List of all Conversations for the authenticated user.
  * **MessagesListActivity:** List of Messages within a particular Conversation.  Also handles message composition.
  * **SelectParticipantsActivity** Allows selection of users to start a Conversation.
  * **SettingsActivity:** Global application settings.
  * **ConversationSettingsActivity:** Settings for a particular Conversation.
* **PushNotificationReceiver:** Handles `com.layer.sdk.PUSH` Intents and displays notifications.
* **AuthenticationProvider:** Interface used to authenticate users. A default implementation is provided.

##<a name="identity-providers"></a>Identity Providers

The SDK Quickstart uses the `AuthenticationProvider` interface to authenticate with various backends.  Additional identity providers can be integrated by implementing `AuthenticationProvider` and using a custom login Activity, or by modifying the existing `BasicAuthenticationProvider`.

##<a name="contributing"></a>Contributing
The SDK Quickstart is an Open Source project maintained by Layer. Feedback and contributions are always welcome and the maintainers try to process patches as quickly as possible. Feel free to open up a Pull Request or Issue on Github.

##<a name="license"></a>License

Atlas is licensed under the terms of the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html). Please see the [LICENSE](LICENSE) file for full details.

##<a name="contact"></a>Contact

The SDK Quickstart was developed in San Francisco by the Layer team. If you have any technical questions or concerns about this project feel free to reach out to [Layer Support](mailto:support@layer.com).

###<a name="credits"></a>Credits

* [Peter Elliott](https://github.com/smpete)
* [Amar Srinivasan](https://github.com/sriamar)
