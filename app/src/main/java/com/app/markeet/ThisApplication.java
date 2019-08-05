package com.app.markeet;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.app.markeet.connection.API;
import com.app.markeet.connection.RestAdapter;
import com.app.markeet.connection.callbacks.CallbackDevice;
import com.app.markeet.data.SharedPref;
import com.app.markeet.model.DeviceInfo;
import com.app.markeet.model.SortBy;
import com.app.markeet.utils.NetworkCheck;
import com.app.markeet.utils.Tools;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThisApplication extends Application {

    private static ThisApplication mInstance;
    private SharedPref sharedPref;
    private FirebaseAnalytics mFirebaseAnalytics;

    private int fcm_count = 0;
    private final int FCM_MAX_COUNT = 10;
    private List<SortBy> sorts = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        sharedPref = new SharedPref(this);

        // initialize firebase ads
        MobileAds.initialize(getApplicationContext(), sharedPref.getAdAppId());

        // initialize firebase
        FirebaseApp.initializeApp(this);

        // obtain regId & registering device to server
        obtainFirebaseToken();

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        initSortByData();
    }

    public static synchronized ThisApplication getInstance() {
        return mInstance;
    }

    private void obtainFirebaseToken() {
        if (NetworkCheck.isConnect(this) && sharedPref.isOpenAppCounterReach()) {
            fcm_count++;

            Task<InstanceIdResult> resultTask = FirebaseInstanceId.getInstance().getInstanceId();
            resultTask.addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    String regId = instanceIdResult.getToken();
                    sharedPref.setFcmRegId(regId);
                    if (!TextUtils.isEmpty(regId)) sendRegistrationToServer(regId);
                }
            });

            resultTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (fcm_count > FCM_MAX_COUNT) return;
                    obtainFirebaseToken();
                }
            });
        }
    }

    private void sendRegistrationToServer(String token) {
        Log.d("FCM_TOKEN", token + "");
        DeviceInfo deviceInfo = Tools.getDeviceInfo(this);
        deviceInfo.regid = token;

        API api = RestAdapter.createAPI();
        Call<CallbackDevice> callbackCall = api.registerDevice(deviceInfo);
        callbackCall.enqueue(new Callback<CallbackDevice>() {
            @Override
            public void onResponse(Call<CallbackDevice> call, Response<CallbackDevice> response) {
                CallbackDevice resp = response.body();
                if (resp != null && resp.status.equals("success")) {
                    sharedPref.setOpenAppCounter(0);
                }
            }

            @Override
            public void onFailure(Call<CallbackDevice> call, Throwable t) {
                Log.e("onFailure", t.getMessage());
            }
        });
    }

    public void saveLogEvent(long id, String name, String type) {
        Bundle bundle = new Bundle();
        bundle.putLong(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public void saveCustomLogEvent(String event, String key, String value) {
        Bundle params = new Bundle();
        params.putString(key, value);
        mFirebaseAnalytics.logEvent(event, params);
    }

    private void initSortByData() {
        sorts.add(new SortBy(getString(R.string.sort_new_old), "created_at", "DESC"));
        sorts.add(new SortBy(getString(R.string.sort_old_new), "created_at", "ASC"));
        sorts.add(new SortBy(getString(R.string.sort_low_high), "price", "ASC"));
        sorts.add(new SortBy(getString(R.string.sort_high_low), "price", "DESC"));
        sorts.add(new SortBy(getString(R.string.sort_discount), "price_discount", "DESC"));
    }

    public List<SortBy> getSorts() {
        return sorts;
    }
}
