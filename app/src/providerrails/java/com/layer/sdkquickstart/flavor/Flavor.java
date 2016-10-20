package com.layer.sdkquickstart.flavor;

import android.content.Context;

import com.layer.sdkquickstart.App;
import com.layer.sdkquickstart.R;
import com.layer.sdkquickstart.flavor.util.CustomEnvironment;
import com.layer.sdkquickstart.util.AuthenticationProvider;
import com.layer.sdkquickstart.util.Log;
import com.layer.sdk.LayerClient;

public class Flavor implements App.Flavor {

    @Override
    public String getLayerAppId() {
        return CustomEnvironment.getLayerAppId();
    }

    @Override
    public LayerClient generateLayerClient(Context context, LayerClient.Options options) {
        String layerAppId = getLayerAppId();
        if (layerAppId == null) {
            if (Log.isLoggable(Log.ERROR)) Log.e(context.getString(R.string.app_id_required));
            return null;
        }
        options.useFirebaseCloudMessaging(false);
        return LayerClient.newInstance(context, layerAppId, options);
    }

    @Override
    public AuthenticationProvider generateAuthenticationProvider(Context context) {
        return new RailsAuthenticationProvider(context);
    }
}
