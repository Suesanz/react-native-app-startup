package com.reactnativeappstartup;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMarker;
import com.facebook.react.bridge.ReactMarkerConstants;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AppStartupModule extends ReactContextBaseJavaModule {
  public static final String BRIDGE_SETUP_START = "bridgeSetupStart";

  private static boolean didFetchAppStart;
  private boolean eventsBuffered = true;
  private static final Map<String, Long> markBuffer = new HashMap<>();

  public AppStartupModule(@NonNull final ReactApplicationContext reactContext) {
    super(reactContext);
    setupMarkerListener();
  }

  @Override
  @NonNull
  public String getName() {
    return "AppStartup";
  }

  @ReactMethod
  public void getAppStartupTime(Promise promise) {
    WritableMap appStart = Arguments.createMap();
    long now = TimeUnit.MILLISECONDS.toMicros(System.currentTimeMillis());
    long totalStartupTime = now - AppStartupProvider.getAppStartupTime();

    appStart.putDouble("nativeAppStartupTime", AppStartupProvider.getNativeAppStartupTime());
    appStart.putDouble("totalStartupTime", totalStartupTime);
    appStart.putBoolean("isColdStart", !didFetchAppStart && AppStartupProvider.isColdStart());
    appStart.putBoolean("didFetchAppStart", didFetchAppStart);
    promise.resolve(appStart);

    // This is always set to true, as we would only allow an app start fetch to only
    // happen once in the case of a JS bundle reload, we do not want it to be
    // instrumented again.
    didFetchAppStart = true;
  }


  @RequiresApi(api = Build.VERSION_CODES.N)
  @ReactMethod
  public static void getReactMarkersList(Promise promise) {
    promise.resolve(Arrays.toString(ReactMarkerConstants.values()));
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public static void setupListener() {
    ReactMarker.addListener(
      (name, tag, instanceKey) -> {
        switch (name) {
          case RELOAD:
            markBuffer.clear();
            markBuffer.put(BRIDGE_SETUP_START, System.currentTimeMillis());
            break;
          case CREATE_REACT_CONTEXT_START:
          case CREATE_REACT_CONTEXT_END:
          case PROCESS_PACKAGES_START:
          case PROCESS_PACKAGES_END:
          case BUILD_NATIVE_MODULE_REGISTRY_START:
          case BUILD_NATIVE_MODULE_REGISTRY_END:
          case CREATE_CATALYST_INSTANCE_START:
          case CREATE_CATALYST_INSTANCE_END:
          case DESTROY_CATALYST_INSTANCE_START:
          case DESTROY_CATALYST_INSTANCE_END:
          case RUN_JS_BUNDLE_START:
          case RUN_JS_BUNDLE_END:
          case NATIVE_MODULE_INITIALIZE_START:
          case NATIVE_MODULE_INITIALIZE_END:
          case SETUP_REACT_CONTEXT_START:
          case SETUP_REACT_CONTEXT_END:
          case CHANGE_THREAD_PRIORITY:
          case CREATE_UI_MANAGER_MODULE_START:
          case CREATE_UI_MANAGER_MODULE_END:
          case CREATE_VIEW_MANAGERS_START:
          case CREATE_VIEW_MANAGERS_END:
          case CREATE_UI_MANAGER_MODULE_CONSTANTS_START:
          case CREATE_UI_MANAGER_MODULE_CONSTANTS_END:
          case NATIVE_MODULE_SETUP_START:
          case NATIVE_MODULE_SETUP_END:
          case CREATE_MODULE_START:
          case CREATE_MODULE_END:
          case PROCESS_CORE_REACT_PACKAGE_START:
          case PROCESS_CORE_REACT_PACKAGE_END:
          case CREATE_I18N_MODULE_CONSTANTS_START:
          case CREATE_I18N_MODULE_CONSTANTS_END:
          case I18N_MODULE_CONSTANTS_CONVERT_START:
          case I18N_MODULE_CONSTANTS_CONVERT_END:
          case CREATE_I18N_ASSETS_MODULE_START:
          case CREATE_I18N_ASSETS_MODULE_END:
          case GET_CONSTANTS_START:
          case GET_CONSTANTS_END:
          case INITIALIZE_MODULE_START:
          case INITIALIZE_MODULE_END:
          case ON_HOST_RESUME_START:
          case ON_HOST_RESUME_END:
          case ON_HOST_PAUSE_START:
          case ON_HOST_PAUSE_END:
          case CONVERT_CONSTANTS_START:
          case CONVERT_CONSTANTS_END:
          case PRE_REACT_CONTEXT_END:
          case UNPACKING_JS_BUNDLE_LOADER_CHECK_START:
          case UNPACKING_JS_BUNDLE_LOADER_CHECK_END:
          case UNPACKING_JS_BUNDLE_LOADER_EXTRACTED:
          case UNPACKING_JS_BUNDLE_LOADER_BLOCKED:
          case loadApplicationScript_startStringConvert:
          case loadApplicationScript_endStringConvert:
          case PRE_SETUP_REACT_CONTEXT_START:
          case PRE_SETUP_REACT_CONTEXT_END:
          case PRE_RUN_JS_BUNDLE_START:
          case ATTACH_MEASURED_ROOT_VIEWS_START:
          case ATTACH_MEASURED_ROOT_VIEWS_END:
          case CONTENT_APPEARED:
          case DOWNLOAD_START:
          case DOWNLOAD_END:
          case REACT_CONTEXT_THREAD_START:
          case REACT_CONTEXT_THREAD_END:
          case GET_REACT_INSTANCE_MANAGER_START:
          case GET_REACT_INSTANCE_MANAGER_END:
          case GET_REACT_INSTANCE_HOLDER_SPEC_START:
          case GET_REACT_INSTANCE_HOLDER_SPEC_END:
          case BUILD_REACT_INSTANCE_MANAGER_START:
          case BUILD_REACT_INSTANCE_MANAGER_END:
          case PROCESS_INFRA_PACKAGE_START:
          case PROCESS_INFRA_PACKAGE_END:
          case PROCESS_PRODUCT_PACKAGE_START:
          case PROCESS_PRODUCT_PACKAGE_END:
          case CREATE_MC_MODULE_START:
          case CREATE_MC_MODULE_END:
          case CREATE_MC_MODULE_GET_METADATA_START:
          case CREATE_MC_MODULE_GET_METADATA_END:
          case REGISTER_JS_SEGMENT_START:
          case REGISTER_JS_SEGMENT_STOP:
          case VM_INIT:
          case ON_FRAGMENT_CREATE:
          case JAVASCRIPT_EXECUTOR_FACTORY_INJECT_START:
          case JAVASCRIPT_EXECUTOR_FACTORY_INJECT_END:
          case LOAD_REACT_NATIVE_SO_FILE_START:
          case LOAD_REACT_NATIVE_SO_FILE_END:
          case ROOT_VIEW_ON_MEASURE_START:
          case ROOT_VIEW_ON_MEASURE_END:
          case ROOT_VIEW_ATTACH_TO_REACT_INSTANCE_MANAGER_START:
          case ROOT_VIEW_ATTACH_TO_REACT_INSTANCE_MANAGER_END:
          case ROOT_VIEW_UPDATE_LAYOUT_SPECS_START:
          case ROOT_VIEW_UPDATE_LAYOUT_SPECS_END:
            // Fabric-specific constants below this line
          case LOAD_REACT_NATIVE_FABRIC_SO_FILE_START:
          case LOAD_REACT_NATIVE_FABRIC_SO_FILE_END:
          case FABRIC_COMMIT_START:
          case FABRIC_COMMIT_END:
          case FABRIC_FINISH_TRANSACTION_START:
          case FABRIC_FINISH_TRANSACTION_END:
          case FABRIC_DIFF_START:
          case FABRIC_DIFF_END:
          case FABRIC_LAYOUT_START:
          case FABRIC_LAYOUT_END:
          case FABRIC_BATCH_EXECUTION_START:
          case FABRIC_BATCH_EXECUTION_END:
          case FABRIC_UPDATE_UI_MAIN_THREAD_START:
          case FABRIC_UPDATE_UI_MAIN_THREAD_END:
            // New markers used by bridge and bridgeless loading below this line
          case REACT_BRIDGE_LOADING_START:
          case REACT_BRIDGE_LOADING_END:
          case REACT_BRIDGELESS_LOADING_START:
          case REACT_BRIDGELESS_LOADING_END:
          case LOAD_REACT_NATIVE_MAPBUFFER_SO_FILE_START:
          case LOAD_REACT_NATIVE_MAPBUFFER_SO_FILE_END:
            long startTime = System.currentTimeMillis();
            markBuffer.put(getMarkName(name), startTime);
            break;
        }
      }
    );
  }

  private static String getMarkName(ReactMarkerConstants name) {
    StringBuffer sb = new StringBuffer();
    for (String s : name.toString().toLowerCase().split("_")) {
      if (sb.length() == 0) {
        sb.append(s);
      } else {
        sb.append(Character.toUpperCase(s.charAt(0)));
        if (s.length() > 1) {
          sb.append(s.substring(1, s.length()));
        }
      }
    }
    return sb.toString();
  }

  private void setupMarkerListener() {
    ReactMarker.addListener(
      (name, tag, instanceKey) -> {
        switch (name) {
          case CONTENT_APPEARED:
            eventsBuffered = false;
            emitBufferedMarks();
            break;
          case RELOAD:
            eventsBuffered = true;
            break;
        }
      }
    );
  }

  private void emitBufferedMarks() {
    for (Map.Entry<String, Long> entry : markBuffer.entrySet()) {
      emitMark(entry.getKey(), entry.getValue());
    }
  }

  private void emitMark(String name,
                        long startTime) {
    emit("mark", name, startTime);
  }

  private void emit(String eventName,
                    String name,
                    long startTime) {
    WritableMap params = Arguments.createMap();
    params.putString("name", name);
    params.putDouble("startTime", startTime);
    getReactApplicationContext()
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }

}
