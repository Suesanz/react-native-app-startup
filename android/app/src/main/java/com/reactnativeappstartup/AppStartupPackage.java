package com.reactnativeappstartup;

import androidx.annotation.NonNull;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppStartupPackage implements ReactPackage {
  public AppStartupPackage() {
    AppStartupModule.setupListener();
  }

  @Override
  @NonNull
  public List<NativeModule> createNativeModules(@NonNull final ReactApplicationContext reactContext) {
    List<NativeModule> modules = new ArrayList<>();
    modules.add(new AppStartupModule(reactContext));
    return modules;
  }

  @NonNull
  public List<Class<? extends JavaScriptModule>> createJSModules() {
    return Collections.emptyList();
  }

  @Override
  @NonNull
  public List<ViewManager> createViewManagers(@NonNull final ReactApplicationContext reactContext) {
    return Collections.emptyList();
  }
}
