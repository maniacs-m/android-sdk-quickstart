package com.layer.sdkquickstart;

// public class AppSettingsActivity extends BaseActivity {

//    // TODO requires conversation and query support
//
//    public AppSettingsActivity() {
//        super(R.layout.activity_app_settings, R.menu.menu_settings, R.string.title_settings, true);
//    }
//
//}

import static com.layer.sdkquickstart.util.Log.ERROR;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.layer.sdk.LayerClient;
import com.layer.sdk.LayerDataObserver;
import com.layer.sdk.LayerDataRequest;
import com.layer.sdk.authentication.AuthenticationListener;
import com.layer.sdk.changes.LayerChangeEvent;
import com.layer.sdk.internal.LayerClientImpl;
import com.layer.sdk.messaging.LayerObject;
import com.layer.sdkquickstart.util.Log;

// public class AppSettingsActivity extends BaseActivity implements LayerConnectionListener, LayerAuthenticationListener, LayerChangeEventListener, View.OnLongClickListener {
public class AppSettingsActivity extends BaseActivity implements AuthenticationListener,
        LayerDataObserver, View.OnLongClickListener {

    private static final String TAG = LayerClientImpl.class.getSimpleName();

    /* Account */
    private TextView mUserName;
    private TextView mUserState;
    private Button mLogoutButton;

    /* Notifications */
    private Switch mShowNotifications;

    /* Debug */
    private Switch mVerboseLogging;
    private TextView mAppVersion;
    private TextView mAndroidVersion;
    private TextView mLayerVersion;
    private TextView mUserId;

    /* Statistics */
    private TextView mConversationCount;
    private TextView mMessageCount;
    private TextView mUnreadMessageCount;

    /* Rich Content */
    private TextView mDiskUtilization;
    private TextView mDiskAllowance;
    private TextView mAutoDownloadMimeTypes;

    public AppSettingsActivity() {
        super(R.layout.activity_app_settings, R.menu.menu_settings, R.string.title_settings, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // View cache
        mUserName = (TextView) findViewById(R.id.user_name);
        mUserState = (TextView) findViewById(R.id.user_state);
        mLogoutButton = (Button) findViewById(R.id.logout_button);
        mShowNotifications = (Switch) findViewById(R.id.show_notifications_switch);
        mVerboseLogging = (Switch) findViewById(R.id.logging_switch);
        mAppVersion = (TextView) findViewById(R.id.app_version);
        mLayerVersion = (TextView) findViewById(R.id.layer_version);
        mAndroidVersion = (TextView) findViewById(R.id.android_version);
        mUserId = (TextView) findViewById(R.id.user_id);
        mConversationCount = (TextView) findViewById(R.id.conversation_count);
        mMessageCount = (TextView) findViewById(R.id.message_count);
        mUnreadMessageCount = (TextView) findViewById(R.id.unread_message_count);
        mDiskUtilization = (TextView) findViewById(R.id.disk_utilization);
        mDiskAllowance = (TextView) findViewById(R.id.disk_allowance);
        mAutoDownloadMimeTypes = (TextView) findViewById(R.id.auto_download_mime_types);

        // Long-click copy-to-clipboard
        mUserName.setOnLongClickListener(this);
        mUserState.setOnLongClickListener(this);
        mAppVersion.setOnLongClickListener(this);
        mAndroidVersion.setOnLongClickListener(this);
        mLayerVersion.setOnLongClickListener(this);
        mUserId.setOnLongClickListener(this);
        mConversationCount.setOnLongClickListener(this);
        mMessageCount.setOnLongClickListener(this);
        mUnreadMessageCount.setOnLongClickListener(this);
        mDiskUtilization.setOnLongClickListener(this);
        mDiskAllowance.setOnLongClickListener(this);
        mAutoDownloadMimeTypes.setOnLongClickListener(this);

        // Buttons and switches
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                setEnabled(false);
                new AlertDialog.Builder(AppSettingsActivity.this)
                        .setCancelable(false)
                        .setMessage(R.string.alert_message_logout)
                        .setPositiveButton(R.string.alert_button_logout, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Log.isLoggable(Log.VERBOSE)) {
                                    Log.v("Deauthenticating");
                                }
                                dialog.dismiss();
                                final ProgressDialog progressDialog = new ProgressDialog(AppSettingsActivity.this);
                                progressDialog.setMessage(getResources().getString(R.string.alert_dialog_logout));
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                                App app = ((App) getApplication());
                                app.deauthenticate(new DeauthenticationListenerLayer(progressDialog));
                            }
                        })
                        .setNegativeButton(R.string.alert_button_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                setEnabled(true);
                            }
                        })
                        .show();
            }
        });

        mShowNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // PushNotificationReceiver.getNotifications(AppSettingsActivity.this).setEnabled(isChecked);
                if (Log.isLoggable(ERROR)) {
                    Log.e("Ignoring mShowNotifications.setOnCheckedChangeListener.onCheckedChanged");
                }
            }
        });

        mVerboseLogging.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LayerClient.setLoggingEnabled(isChecked);
                Log.setAlwaysLoggable(isChecked);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLayerClient()
                .registerAuthenticationListener(this);
        getLayerClient().registerDataObserver(this);
                //.registerConnectionListener(this)
        refresh();
    }

    @Override
    protected void onPause() {
        getLayerClient()
                .unregisterAuthenticationListener(this);
        getLayerClient().unregisterDataObserver(this);
                //.unregisterConnectionListener(this)
        super.onPause();
    }

    public void setEnabled(final boolean enabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLogoutButton.setEnabled(enabled);
                mShowNotifications.setEnabled(enabled);
                mVerboseLogging.setEnabled(enabled);
            }
        });
    }

    private void refresh() {
        if (!getLayerClient().isAuthenticated()) return;

        /* Account */
        // TODO this is dependent on identity sync so we can get the display name for the current user
//        // TODO fix this async pattern. This should work for now since it should always be cached at this point.
//        LayerObjectRequest<Identity> authenticatedUserRequest = getLayerClient().getAuthenticatedUser();
//        Identity currentUser = authenticatedUserRequest.getObject();
//        if (currentUser != null) {
//            mUserName.setText(IdentityUtils.getDisplayName(currentUser));
//        } else {
//            mUserName.setText(null);
//        }
//        mUserState.setText(getLayerClient().isConnected() ? R.string.settings_content_connected : R.string.settings_content_disconnected);
//
//        /* Notifications */
//        mShowNotifications.setChecked(PushNotificationReceiver.getNotifications(this).isEnabled());

        /* Debug */
        // enable logging through adb: `adb shell setprop log.tag.LayerSDK VERBOSE`
        boolean enabledByEnvironment = android.util.Log.isLoggable("LayerSDK", Log.VERBOSE);
        mVerboseLogging.setEnabled(!enabledByEnvironment);
        mVerboseLogging.setChecked(enabledByEnvironment || LayerClient.isLoggingEnabled());
        mAppVersion.setText(getString(R.string.settings_content_app_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        mLayerVersion.setText(LayerClient.getVersion());
        mAndroidVersion.setText(getString(R.string.settings_content_android_version, Build.VERSION.RELEASE, Build.VERSION.SDK_INT));
//        if (currentUser != null) {
//            mUserId.setText(currentUser.getUserId());
//        } else {
//            mUserId.setText(R.string.settings_not_authenticated);
//        }

        /* Statistics */
//        long totalMessages = 0;
//        long totalUnread = 0;
//        List<Conversation> conversations = getLayerClient().getConversations();
//        for (Conversation conversation : conversations) {
//            totalMessages += conversation.getTotalMessageCount();
//            totalUnread += conversation.getTotalUnreadMessageCount();
//        }
//        mConversationCount.setText(String.format(Locale.getDefault(), "%d", conversations.size()));
//        mMessageCount.setText(String.format(Locale.getDefault(), "%d", totalMessages));
//        mUnreadMessageCount.setText(String.format(Locale.getDefault(), "%d", totalUnread));

        /* Rich Content */
//        mDiskUtilization.setText(readableByteFormat(getLayerClient().getDiskUtilization()));
//        long allowance = getLayerClient().getDiskCapacity();
//        if (allowance == 0) {
//            mDiskAllowance.setText(R.string.settings_content_disk_unlimited);
//        } else {
//            mDiskAllowance.setText(readableByteFormat(allowance));
//        }
//        mAutoDownloadMimeTypes.setText(TextUtils.join("\n", getLayerClient().getAutoDownloadMimeTypes()));
    }

    private String readableByteFormat(long bytes) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        double value;
        int suffix;
        if (bytes >= gb) {
            value = (double) bytes / (double) gb;
            suffix = R.string.settings_content_disk_gb;
        } else if (bytes >= mb) {
            value = (double) bytes / (double) mb;
            suffix = R.string.settings_content_disk_mb;
        } else if (bytes >= kb) {
            value = (double) bytes / (double) kb;
            suffix = R.string.settings_content_disk_kb;
        } else {
            value = (double) bytes;
            suffix = R.string.settings_content_disk_b;
        }
        return getString(R.string.settings_content_disk_usage, value, getString(suffix));
    }


    @Override
    public void onAuthenticated(LayerClient layerClient, String userId) {
        refresh();
    }

    @Override
    public void onDeauthenticated(LayerClient layerClient, String userId) {
        refresh();
    }
//
//    @Override
//    public void onAuthenticationChallenge(LayerClient layerClient, String s) {
//
//    }
//
    @Override
    public void onAuthenticationError(LayerClient layerClient, Exception e) {

    }
//
//    @Override
//    public void onConnectionConnected(LayerClient layerClient) {
//        refresh();
//    }
//
//    @Override
//    public void onConnectionDisconnected(LayerClient layerClient) {
//        refresh();
//    }
//
//    @Override
//    public void onConnectionError(LayerClient layerClient, LayerException e) {
//
//    }

    @Override
    public void onDataChanged(LayerChangeEvent layerChangeEvent) {
        refresh();
    }

    @Override
    public void onDataRequestCompleted(LayerDataRequest request, LayerObject object) {
    }

    @Override
    public boolean onLongClick(View v) {
        if (v instanceof TextView) {
            CharSequence content = ((TextView) v).getText();
            String description = getString(R.string.settings_clipboard_description);
            ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = new ClipData(description, new String[]{"text/plain"}, new ClipData.Item(content));
            manager.setPrimaryClip(clipData);
            Toast.makeText(this, R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private class DeauthenticationListenerLayer implements AuthenticationListener {
        private final ProgressDialog progressDialog;

        public DeauthenticationListenerLayer(ProgressDialog progressDialog) {
            this.progressDialog = progressDialog;
        }

        @Override
        public void onAuthenticated(LayerClient layerClient, String s) {
        }

        @Override
        public void onDeauthenticated(LayerClient layerClient, String userId) {
            if (Log.isLoggable(Log.VERBOSE)) {
                Log.v("Successfully deauthenticated");
            }
            progressDialog.dismiss();
            setEnabled(true);
            layerClient.unregisterAuthenticationListener(this);
            App app = ((App) getApplication());
            app.getAuthenticationProvider().setCredentials(null);
            app.routeLogin(AppSettingsActivity.this);
        }

//        @Override
//        public void onAuthenticationChallenge(LayerClient layerClient, String s) {
//        }

        @Override
        public void onAuthenticationError(LayerClient layerClient, Exception e) {
            if (Log.isLoggable(ERROR)) {
                Log.e("Failed to deauthenticate: " + e.getMessage());
            }
            progressDialog.dismiss();
            setEnabled(true);
            layerClient.unregisterAuthenticationListener(this);
            Toast.makeText(AppSettingsActivity.this, getString(R.string.toast_failed_to_deauthenticate, e.getMessage()), Toast.LENGTH_SHORT).show();

        }
    }
}
