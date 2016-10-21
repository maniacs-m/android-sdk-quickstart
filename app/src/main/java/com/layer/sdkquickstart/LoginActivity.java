package com.layer.sdkquickstart;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.layer.sdkquickstart.conversationlist.ConversationsListActivity;
import com.layer.sdkquickstart.util.CustomEnvironment;
import com.layer.sdkquickstart.util.AuthenticationProvider;
import com.layer.sdkquickstart.util.Log;

public class LoginActivity extends AppCompatActivity {
    EditText mEmail;
    EditText mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();

        mEmail = (EditText) findViewById(R.id.email);
        mEmail.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        mPassword = (EditText) findViewById(R.id.password);
        mPassword.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    final String email = mEmail.getText().toString().trim();
                    if (email.isEmpty()) return true;

                    final String password = mPassword.getText().toString().trim();
                    if (password.isEmpty()) return true;

                    login(email, password);
                    return true;
                }
                return false;
            }
        });

        // Add a CustomEnvironment Spinner if there is more than one environment
        Spinner customEnvironments = CustomEnvironment.createSpinner(this);
        if (customEnvironments != null) {
            customEnvironments.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ((ViewGroup) mPassword.getParent()).addView(customEnvironments);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEmail.setEnabled(true);
        mPassword.setEnabled(true);
    }

    private void login(final String email, final String password) {
        mEmail.setEnabled(false);
        mPassword.setEnabled(false);
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.login_dialog_message));
        progressDialog.show();
        App.authenticate(new DefaultAuthenticationProvider.Credentials(App.getLayerAppId(), email, password, null),
                new AuthenticationProvider.Callback() {
                    @Override
                    public void onSuccess(AuthenticationProvider provider, String userId) {
                        progressDialog.dismiss();
                        if (Log.isLoggable(Log.VERBOSE)) {
                            Log.v("Successfully authenticated as `" + email + "` with userId `" + userId + "`");
                        }
                        Intent intent = new Intent(LoginActivity.this, ConversationsListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        LoginActivity.this.startActivity(intent);
                    }

                    @Override
                    public void onError(AuthenticationProvider provider, final String error) {
                        progressDialog.dismiss();
                        if (Log.isLoggable(Log.ERROR)) {
                            Log.e("Failed to authenticate as `" + email + "`: " + error);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                                mEmail.setEnabled(true);
                                mPassword.setEnabled(true);
                            }
                        });
                    }
                });
    }
}
