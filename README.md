# Layer Android SDK Quickstart

The SDK Quickstart app is a minimal messaging app designed to provide a starting point for using the Layer SDK.

##<a name="setup"></a>Setup

* Import the project into Android Studio.
* Enter your Layer App ID in the `layer_environments.json` file. This can be obtained from the Layer developer console.
* Enter your Layer Identity Provider URL in the `layer_environments.json` file.
* Set the build variant to use `providerrailsDebug`.

##<a name="enabling-push-notifications"></a>Enabling push notifications
Push notifications are disabled by default to ease initial setup. To enable them, follow these steps:
 * Generate a JSON configuration file for FCM using the Firebase developers console. There is a tutorial here: https://docs.layer.com/sdk/android/push#setting-up-push-with-layer.
 * Replace the `google-service.json` file in this project with the generated one.
 * In the active `Flavor.java` class, set the Layer SDK to use FCM by changing the boolean passed to `options.useFirebaseCloudMessaging()` to true. This call is in the method `generateLayerClient()`.

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
* **AuthenticationProvider:** Interface used to authenticate users.  Default implementations are provided by gradle `flavors`; see *Build Variants* below.

##<a name="identity-providers"></a>Identity Providers

The SDK Quickstart uses the `AuthenticationProvider` interface to authenticate with various backends.  Additional identity providers can be integrated by implementing `AuthenticationProvider` and using a custom login Activity, similar to the provided flavors below.

###<a name="build-variants"></a>Provided Flavors
Two default implementations are provided via [product flavors](http://developer.android.com/tools/building/configuring-gradle.html#workBuildVariants), where each flavor implements a custom `AuthenticationProvider` and provides login Activities for gathering their required credentials:

1. **demoprovider:** For use in the [Layer Atlas demo](https://getatlas.layer.com/android).  This authentication flow utilizes a QR-Code scanner to capture a Layer App ID from the Layer developer dashboard.  The scanner can be bypassed by supplying your Quickstart App ID in the `App.LAYER_APP_ID` constant.
2. **railsprovider:** For use with the deployable [Rails Provider](https://github.com/layerhq/layer-identity-provider) backend.

In Android Studio, switch flavors using Build Variants, typically in the side tab on the lower left of the Android Studio window.

##<a name="contributing"></a>Contributing
The SDK Quickstart is an Open Source project maintained by Layer. Feedback and contributions are always welcome and the maintainers try to process patches as quickly as possible. Feel free to open up a Pull Request or Issue on Github.

##<a name="license"></a>License

Atlas is licensed under the terms of the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html). Please see the [LICENSE](LICENSE) file for full details.

##<a name="contact"></a>Contact

The SDK Quickstart was developed in San Francisco by the Layer team. If you have any technical questions or concerns about this project feel free to reach out to [Layer Support](mailto:support@layer.com).

###<a name="credits"></a>Credits

* [Peter Elliott](https://github.com/smpete)
* [Amar Srinivasan](https://github.com/sriamar)
