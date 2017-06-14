package com.layer.sdkquickstart.util;


import android.support.annotation.NonNull;

import com.layer.sdk.messaging.Identity;

public class IdentityUtils {

    @NonNull
    public static String getDisplayName(Identity identity) {

        // TODO requires identity support
        return "TODO";

//        if (TextUtils.isEmpty(identity.getDisplayName())) {
//            String first = identity.getFirstName();
//            String last = identity.getLastName();
//            if (!TextUtils.isEmpty(first)) {
//                if (!TextUtils.isEmpty(last)) {
//                    return String.format("%s %s", first, last);
//                }
//                return first;
//            } else if (!TextUtils.isEmpty(last)) {
//                return last;
//            } else {
//                return identity.getUserId();
//            }
//        }
//        return identity.getDisplayName();
    }
}
