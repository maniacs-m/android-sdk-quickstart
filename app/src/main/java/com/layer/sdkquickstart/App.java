package com.layer.sdkquickstart;

import android.app.Activity;
import android.app.Application;
import android.os.StrictMode;

import com.layer.sdk.LayerClient;
import com.layer.sdk.authentication.AuthenticationChallengeListener;
import com.layer.sdk.authentication.AuthenticationListener;
import com.layer.sdkquickstart.util.AuthenticationProvider;
import com.layer.sdkquickstart.util.CustomEnvironment;
import com.layer.sdkquickstart.util.Log;

/**
 * App provides static access to a LayerClient. It also provides an AuthenticationProvider to use
 * with the LayerClient
 *
 * @see LayerClient
 * @see AuthenticationProvider
 */
public class App extends Application {

    private static Application sInstance;

    private static LayerClient sLayerClient;
    private static AuthenticationProvider sAuthProvider;


    //==============================================================================================
    // Application Overrides
    //==============================================================================================

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable verbose logging and strict mode in debug builds
        if (BuildConfig.DEBUG) {
            com.layer.sdkquickstart.util.Log.setAlwaysLoggable(true);
            LayerClient.setLoggingEnabled(true);

            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build());
        }

        sInstance = this;

        LayerClient.registerAuthenticationChallengeListener(new AuthenticationChallengeListener() {
            @Override
            public void onAuthenticationChallenge(LayerClient layerClient, String nonce) {
                sAuthProvider.onAuthenticationChallenge(layerClient, nonce);
            }
        });
    }

    public static Application getInstance() {
        return sInstance;
    }


    //==============================================================================================
    // Identity Provider Methods
    //==============================================================================================

    /**
     * Routes the user to the proper Activity depending on their authenticated state.  Returns
     * `true` if the user has been routed to another Activity, or `false` otherwise.
     *
     * @param from Activity to route from.
     * @return `true` if the user has been routed to another Activity, or `false` otherwise.
     */
    public static boolean routeLogin(Activity from) {
        return getAuthenticationProvider().routeLogin(getLayerClient(), getLayerAppId(), from);
    }

    /**
     * Authenticates with the AuthenticationProvider and Layer, returning asynchronous results to
     * the provided callback.
     *
     * @param credentials Credentials associated with the current AuthenticationProvider.
     * @param callback    Callback to receive authentication results.
     */
    @SuppressWarnings("unchecked")
    public static void authenticate(Object credentials, AuthenticationProvider.Callback callback) {
        LayerClient client = getLayerClient();
        if (client == null) return;
        String layerAppId = getLayerAppId();
        if (layerAppId == null) return;
        getAuthenticationProvider()
                .setCredentials(credentials)
                .setCallback(callback);
        client.requestAuthenticationNonce();
    }

    public static void deauthenticate(AuthenticationListener deauthenticationListener) {
        LayerClient client = getLayerClient();
        if (client != null) {
            client.registerAuthenticationListener(deauthenticationListener);
            client.deauthenticate();
        }
    }

    //==============================================================================================
    // Getters / Setters
    //==============================================================================================

    /**
     * Gets or creates a LayerClient, using a default set of LayerClient.Options and the App ID from
     * the selected environment. Returns null if no App ID has been set yet.
     *
     * @return New or existing LayerClient.
     */
    public static LayerClient getLayerClient() {
        if (sLayerClient == null) {

            String layerAppId = getLayerAppId();
            if (layerAppId == null) {
                if (Log.isLoggable(Log.ERROR)) Log.e(sInstance.getString(R.string.app_id_required));
                return null;
            }

            LayerClient.Options options = new LayerClient.Options.Builder()
                    .runAgainstStandalone(true)
                    .customEndpoint(null, "https://35.184.171.16:5556/certificates", "https://35.184.171.16:9933/websocket")
                    .build();
            // Uncomment the following line to enable push notifications from FCM
            // options.useFirebaseCloudMessaging(true);
            sLayerClient = LayerClient.newInstance(layerAppId, options);

            /* Register AuthenticationProvider for handling authentication challenges */
            sLayerClient.registerAuthenticationListener(getAuthenticationProvider());
        }
        return sLayerClient;
    }

    public static String getLayerAppId() {
        return CustomEnvironment.getLayerAppId();
    }

    public static AuthenticationProvider getAuthenticationProvider() {
        if (sAuthProvider == null) {
            sAuthProvider = new DefaultAuthenticationProvider(sInstance);

//            // If we have cached credentials, try authenticating with Layer
//            LayerClient layerClient = getLayerClient();
//            if (layerClient != null && sAuthProvider.hasCredentials()) layerClient.requestAuthenticationNonce();
        }
        return sAuthProvider;
    }
}
