package com.layer.sdkquickstart.util;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.layer.sdkquickstart.App;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CustomEnvironment provides a mechanism for switching between multiple app IDs (e.g. production
 * and staging) without needing to recompile the application.
 */
public class CustomEnvironment {
    private static Environment sEnvironment;
    private static Map<String, Environment> sEnvironments;

    public static String getLayerAppId() {
        Environment environment = getEnvironment();
        return environment == null ? null : environment.getAppId();
    }

    public static String getProviderUrl() {
        Environment environment = getEnvironment();
        return environment == null ? null : environment.getProviderUrl();
    }

    public static boolean hasEnvironments() {
        Map<String, Environment> environments = getEnvironments();
        return environments != null && !environments.isEmpty();
    }

    public static Spinner createSpinner(Context context) {
        Set<String> environmentNames = getNames();
        // Don't create a spinner if there are no environments
        if (environmentNames == null || environmentNames.size() < 1) return null;

        List<String> namesList = new ArrayList<>(environmentNames);
        Collections.sort(namesList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, namesList);
        Spinner spinner = new Spinner(context);
        spinner.setAdapter(adapter);

        Environment environment = getEnvironment();
        if (environment != null) {
            int position = namesList.indexOf(environment.getName());
            if (position != -1) spinner.setSelection(position);
        }
        setEnvironmentName((String) spinner.getSelectedItem());

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setEnvironmentName((String) parent.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setEnvironmentName(null);
            }
        });

        return spinner;
    }

    private static Set<String> getNames() {
        Map<String, Environment> environments = getEnvironments();
        return environments == null ? null : environments.keySet();
    }

    private static void setEnvironmentName(String name) {
        App.getInstance().getSharedPreferences("layer_custom_environment", Context.MODE_PRIVATE).edit().putString("name", name).apply();
        Map<String, Environment> environments = getEnvironments();
        sEnvironment = (environments == null) ? null : environments.get(name);
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Setting custom environment to: " + sEnvironment);
    }

    private static Environment getEnvironment() {
        if (sEnvironment != null) return sEnvironment;
        String savedEnvironmentName = App.getInstance().getSharedPreferences("layer_custom_environment", Context.MODE_PRIVATE).getString("name", null);
        if (savedEnvironmentName == null) return null;
        Map<String, Environment> environments = getEnvironments();
        sEnvironment = (environments == null) ? null : environments.get(savedEnvironmentName);
        return sEnvironment;
    }

    private static Map<String, Environment> getEnvironments() {
        if (sEnvironments != null) return sEnvironments;
        sEnvironments = new HashMap<>();

        // Check for environments in resources
        Context context = App.getInstance();
        int resId = context.getResources().getIdentifier("layer_environments", "raw", context.getPackageName());
        if (resId == 0) return null;

        // Read environments from resources
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        InputStream is = context.getResources().openRawResource(resId);
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (Exception e) {
            if (Log.isLoggable(Log.ERROR)) Log.e(e.getMessage(), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    if (Log.isLoggable(Log.ERROR)) Log.e(e.getMessage(), e);
                }
            }
        }
        String content = writer.toString().trim();
        if (content.isEmpty()) return null;

        // Parse environments from JSON
        try {
            JSONArray array = new JSONArray(content);
            for (int i = 0; i < array.length(); i++) {
                Environment environment = new Environment(array.getJSONObject(i));
                sEnvironments.put(environment.getName(), environment);
            }
            return sEnvironments;
        } catch (JSONException e) {
            if (Log.isLoggable(Log.ERROR)) Log.e(e.getMessage(), e);
        }
        return null;
    }

    private static class Environment {
        final String mName;
        final String mAppId;
        final String mProviderUrl;

        Environment(JSONObject o) throws JSONException {
            mName = o.getString("name");
            mAppId = o.getString("appId");
            mProviderUrl = o.getString("providerUrl");
        }

        String getName() {
            return mName;
        }

        String getAppId() {
            return mAppId;
        }

        String getProviderUrl() {
            return mProviderUrl;
        }

        @Override
        public String toString() {
            return "Environment{" +
                    "mName='" + mName + '\'' +
                    ", mAppId='" + mAppId + '\'' +
                    ", mProviderUrl='" + mProviderUrl + '\'' +
                    '}';
        }
    }
}
