package com.reactnativeappstartup;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

public class AppStartupProvider extends ContentProvider implements ActivityLifecycleCallbacks {

  private static final long APP_START_TIME = TimeUnit.MILLISECONDS.toMicros(System.currentTimeMillis());

  private Context appContext;
  private static long appStartTime;
  private static long onCreateTime;
  private static long onStartTime;
  private static long onResumeTime;
  private boolean isRegisteredForLifecycleCallbacks;
  private boolean isStartedFromBackground = false;
  private boolean firstActivityCreated = false;

  /** The type of App start coldStart=true -> Cold start, coldStart=false -> Warm start */
  private static @Nullable Boolean coldStart = null;

  /**
   * If the time difference between app starts and creation of any Activity is larger than
   * MAX_LATENCY_BEFORE_UI_INIT, set mTooLateToInitUI to true and we don't send AppStart Trace.
   */
  private boolean isTooLateToInitUI = false;

  public static long getNativeAppStartupTime() {
    return (onResumeTime - appStartTime);
  }

  public static long getAppStartupTime() {
    return appStartTime;
  }

  private void setColdStart(final @Nullable Bundle savedInstanceState) {
    if (!firstActivityCreated) {
      // if Activity has savedInstanceState then its a warm start
      // https://developer.android.com/topic/performance/vitals/launch-time#warm
      coldStart = savedInstanceState == null;
    }
  }

  static Boolean isColdStart() {
    return coldStart;
  }

  @Override
  public void attachInfo(Context context, ProviderInfo info) {
    // super.attachInfo calls onCreate().

    Context appContext = context.getApplicationContext();
    if (appContext instanceof Application) {
      ((Application) appContext).registerActivityLifecycleCallbacks(this);
      this.appContext = appContext;
      isRegisteredForLifecycleCallbacks = true;
    }

    new StartFromBackgroundRunnable(this);
  }

  @Override
  public synchronized void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    if (isStartedFromBackground || onCreateTime != 0 // An activity already called onCreate()
    ) {
      return;
    }

    onCreateTime = TimeUnit.MILLISECONDS.toMicros(System.currentTimeMillis());
    setColdStart(savedInstanceState);

    firstActivityCreated = true;
  }

  @Override
  public synchronized void onActivityStarted(Activity activity) {
    if (isStartedFromBackground
      || onStartTime != 0 // An activity already called onStart()
      || isTooLateToInitUI) {
      return;
    }
    onStartTime = TimeUnit.MILLISECONDS.toMicros(System.currentTimeMillis());
  }

  @Override
  public synchronized void onActivityResumed(Activity activity) {
    if (isStartedFromBackground
      || onResumeTime != 0 // An activity already called onResume()
      || isTooLateToInitUI) {
      return;
    }

    onResumeTime = TimeUnit.MILLISECONDS.toMicros(System.currentTimeMillis());
    appStartTime = APP_START_TIME;
    Log.d(
      "NATIVE_STARTUP_TIME: "
      , onResumeTime - appStartTime + " microseconds");

    if (isRegisteredForLifecycleCallbacks) {
      // After AppStart trace is logged, we can unregister this callback.
      ((Application) appContext).unregisterActivityLifecycleCallbacks(this);
      isRegisteredForLifecycleCallbacks = false;
    }
  }

  /**
   * We use StartFromBackgroundRunnable to detect if app is started from background or foreground.
   * If app is started from background, we do not generate AppStart trace. This runnable is posted
   * to main UI thread from FirebasePerfProvider. If app is started from background, this runnable
   * will be executed before any activity's onCreate() method. If app is started from foreground,
   * activity's onCreate() method is executed before this runnable.
   */
  public static class StartFromBackgroundRunnable implements Runnable {
    private final AppStartupProvider trace;

    public StartFromBackgroundRunnable(final AppStartupProvider trace) {
      this.trace = trace;
    }

    @Override
    public void run() {
      // if no activity has ever been created.
      if (onCreateTime == 0) {
        trace.isStartedFromBackground = true;
      }
    }
  }

  @Override
  public boolean onCreate() {
    return false;
  }

  @Nullable
  @Override
  public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
    return null;
  }

  @Nullable
  @Override
  public String getType(@NonNull Uri uri) {
    return null;
  }

  @Nullable
  @Override
  public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
    return null;
  }

  @Override
  public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
    return 0;
  }

  @Override
  public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
    return 0;
  }

  @Override
  public void onActivityPaused(@NonNull Activity activity) {
  }

  @Override
  public void onActivityStopped(@NonNull Activity activity) {
  }

  @Override
  public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
  }

  @Override
  public void onActivityDestroyed(@NonNull Activity activity) {
  }

}
