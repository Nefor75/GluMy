package com.app.markeet.data;

import android.app.Activity;
import androidx.annotation.NonNull;

import com.app.markeet.BuildConfig;
import com.app.markeet.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class RemoteConfig {

    // firebase remote config key property
    private static final String PUBLISHER_KEY = "publisher_id";
    private static final String PRIVACY_POLICY_URL = "privacy_policy_url";

    private static final String AD_APP_ID = "ad_app_id";
    private static final String INTERSTITIAL_KEY = "interstitial_ad_unit_id";
    private static final String BANNER_KEY = "banner_ad_unit_id";

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    public RemoteConfig() {
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();

        // Get Remote Config instance.
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

    }

    public void fetchData(Activity activity) {
        long cacheExpiration = 3600;
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(activity, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mFirebaseRemoteConfig.activateFetched();
                }
            }
        });
    }

    public String getPublisherId() {
        return mFirebaseRemoteConfig.getString(PUBLISHER_KEY);
    }

    public String getBannerUnitId() {
        return mFirebaseRemoteConfig.getString(BANNER_KEY);
    }

    public String getInterstitialUnitId() {
        return mFirebaseRemoteConfig.getString(INTERSTITIAL_KEY);
    }

    public String getAdAppId() {
        return mFirebaseRemoteConfig.getString(AD_APP_ID);
    }

    public String getPrivacyPolicyUrl() {
        return mFirebaseRemoteConfig.getString(PRIVACY_POLICY_URL);
    }
}
