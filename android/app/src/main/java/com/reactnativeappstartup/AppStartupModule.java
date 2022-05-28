package com.reactnativeappstartup;

import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

public class AppStartupModule extends ReactContextBaseJavaModule {

  private static boolean didFetchAppStart;

  public AppStartupModule(@NonNull final ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  @NonNull
  public String getName() {
    return "AppStartup";
  }

  @ReactMethod
  public void getAppStartupTime(Promise promise) {
    WritableMap appStart = Arguments.createMap();
    appStart.putDouble("startupTime", AppStartupProvider.getAppStartupTime());
    appStart.putBoolean("isColdStart", AppStartupProvider.isColdStart());
    appStart.putBoolean("didFetchAppStart", didFetchAppStart);
    promise.resolve(appStart);

    // This is always set to true, as we would only allow an app start fetch to only
    // happen once in the case of a JS bundle reload, we do not want it to be
    // instrumented again.
    didFetchAppStart = true;
  }

}
