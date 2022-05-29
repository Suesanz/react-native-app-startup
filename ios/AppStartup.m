#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "AppStartup.h"

static NSDate *appStartTime = nil;
static NSDate *doubleDispatchTime = nil;
static NSDate *applicationDidFinishLaunchTime = nil;
static NSTimeInterval gAppStartMaxValidDuration = 60 * 60;  // 60 minutes.

NSString *const kFPRAppStartTraceName = @"_as";
NSString *const kFPRAppStartStageNameTimeToUI = @"_astui";
NSString *const kFPRAppStartStageNameTimeToFirstDraw = @"_astfd";
NSString *const kFPRAppStartStageNameTimeToUserInteraction = @"_asti";


+ (void)load {
  // This is an approximation of the app start time.
  appStartTime = [NSDate date];

  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(windowDidBecomeVisible:)
                                               name:UIWindowDidBecomeVisibleNotification
                                             object:nil];

  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(applicationDidFinishLaunching:)
                                               name:UIApplicationDidFinishLaunchingNotification
                                             object:nil];
}

+ (void)windowDidBecomeVisible:(NSNotification *)notification {
  FPRAppActivityTracker *activityTracker = [self sharedInstance];
  [activityTracker startAppActivityTracking];

  [[NSNotificationCenter defaultCenter] removeObserver:self
                                                  name:UIWindowDidBecomeVisibleNotification
                                                object:nil];
}

+ (void)applicationDidFinishLaunching:(NSNotification *)notification {
  applicationDidFinishLaunchTime = [NSDate date];
  [[NSNotificationCenter defaultCenter] removeObserver:self
                                                  name:UIApplicationDidFinishLaunchingNotification
                                                object:nil];
}

+ (instancetype)sharedInstance {
  static FPRAppActivityTracker *instance;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    instance = [[self alloc] initAppActivityTracker];
  });
  return instance;
}

/**
 * Custom initializer to create an app activity tracker.
 */
- (instancetype)initAppActivityTracker {
  self = [super init];
  _applicationState = FPRApplicationStateUnknown;
  _configurations = [FPRConfigurations sharedInstance];
  return self;
}

- (void)startAppActivityTracking {
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(appDidBecomeActiveNotification:)
                                               name:UIApplicationDidBecomeActiveNotification
                                             object:[UIApplication sharedApplication]];

  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(appWillResignActiveNotification:)
                                               name:UIApplicationWillResignActiveNotification
                                             object:[UIApplication sharedApplication]];
}

/**
 * This gets called whenever the app becomes active. A new trace will be created to track the active
 * foreground session. Any background session trace that was running in the past will be stopped.
 *
 * @param notification Notification received during app launch.
 */
- (void)appDidBecomeActiveNotification:(NSNotification *)notification {
  self.applicationState = FPRApplicationStateForeground;

  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    self.appStartTrace = [[FIRTrace alloc] initInternalTraceWithName:kFPRAppStartTraceName];
    [self.appStartTrace startWithStartTime:appStartTime];
    [self.appStartTrace startStageNamed:kFPRAppStartStageNameTimeToUI startTime:appStartTime];

    // Start measuring time to first draw on the App start trace.
    [self.appStartTrace startStageNamed:kFPRAppStartStageNameTimeToFirstDraw];
  });

  // Stop the active background session trace.
  [self.backgroundSessionTrace stop];
  self.backgroundSessionTrace = nil;

  // Start measuring time to make the app interactive on the App start trace.
  static BOOL TTIStageStarted = NO;
  if (!TTIStageStarted) {
    [self.appStartTrace startStageNamed:kFPRAppStartStageNameTimeToUserInteraction];
    TTIStageStarted = YES;

    // Assumption here is that - the app becomes interactive in the next runloop cycle.
    // It is possible that the app does more things later, but for now we are not measuring that.
    dispatch_async(dispatch_get_main_queue(), ^{
      NSTimeInterval startTimeSinceEpoch = [self.appStartTrace startTimeSinceEpoch];
      NSTimeInterval currentTimeSinceEpoch = [[NSDate date] timeIntervalSince1970];

      // The below check is to account for 2 scenarios.
      // 1. The app gets started in the background and might come to foreground a lot later.
      // 2. The app is launched, but immediately backgrounded for some reason and the actual launch
      // happens a lot later.
      // Dropping the app start trace in such situations where the launch time is taking more than
      // 60 minutes. This is an approximation, but a more agreeable timelimit for app start.
      if ((currentTimeSinceEpoch - startTimeSinceEpoch < gAppStartMaxValidDuration) &&
          [self isAppStartEnabled] && ![self isApplicationPreWarmed]) {
        [self.appStartTrace stop];
      } else {
        [self.appStartTrace cancel];
      }
    });
  }

  // Let the session manager to start tracking app activity changes.
  [[FPRSessionManager sharedInstance] startTrackingAppStateChanges];
}

/**
 * This gets called whenever the app resigns its active status. The currently active foreground
 * session trace will be stopped and a background session trace will be started.
 *
 * @param notification Notification received during app resigning active status.
 */
- (void)appWillResignActiveNotification:(NSNotification *)notification {
  self.applicationState = FPRApplicationStateBackground;

}

- (void)dealloc {
  [[NSNotificationCenter defaultCenter] removeObserver:self
                                                  name:UIApplicationDidBecomeActiveNotification
                                                object:[UIApplication sharedApplication]];

  [[NSNotificationCenter defaultCenter] removeObserver:self
                                                  name:UIApplicationWillResignActiveNotification
                                                object:[UIApplication sharedApplication]];
}

@end
