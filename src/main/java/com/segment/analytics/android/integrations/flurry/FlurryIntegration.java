package com.segment.analytics.android.integrations.flurry;

import android.app.Activity;
import com.flurry.android.Constants;
import com.flurry.android.FlurryAgent;
import com.segment.analytics.Analytics;
import com.segment.analytics.AnalyticsContext;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.ScreenPayload;
import com.segment.analytics.integrations.TrackPayload;
import java.util.Map;

import static com.segment.analytics.Analytics.LogLevel;
import static com.segment.analytics.internal.Utils.isNullOrEmpty;

/**
 * Flurry is the most popular analytics tool for mobile apps because it has a wide assortment of
 * features. It also helps you advertise to the right audiences with your apps.
 *
 * @see <a href="http://www.flurry.com/">Flurry</a>
 * @see <a href="https://segment.com/docs/integrations/flurry/">Flurry Integration</a>
 * @see <a href="http://support.flurry.com/index.php?title=Analytics/GettingStarted/Android">Flurry
 * Android SDK</a>
 */
public class FlurryIntegration extends Integration<Void> {
  public static final Factory FACTORY = new Factory() {
    @Override public Integration<?> create(ValueMap settings, Analytics analytics) {
      return new FlurryIntegration(analytics, settings);
    }

    @Override public String key() {
      return FLURRY_KEY;
    }
  };
  private static final String FLURRY_KEY = "Flurry";
  private final Logger logger;

  FlurryIntegration(Analytics analytics, ValueMap settings) {
    logger = analytics.logger(FLURRY_KEY);

    int sessionContinueSeconds = settings.getInt("sessionContinueSeconds", 10) * 1000;
    FlurryAgent.setContinueSessionMillis(sessionContinueSeconds);
    logger.verbose("FlurryAgent.setContinueSessionMillis(%s);", sessionContinueSeconds);

    boolean captureUncaughtExceptions = settings.getBoolean("captureUncaughtExceptions", false);
    FlurryAgent.setCaptureUncaughtExceptions(captureUncaughtExceptions);
    logger.verbose("FlurryAgent.setCaptureUncaughtExceptions(%s);", captureUncaughtExceptions);

    boolean reportLocation = settings.getBoolean("reportLocation", true);
    FlurryAgent.setReportLocation(reportLocation);
    logger.verbose("FlurryAgent.setReportLocation(%s);", reportLocation);

    boolean logEnabled = logger.logLevel.ordinal() >= LogLevel.DEBUG.ordinal();
    FlurryAgent.setLogEnabled(logEnabled);
    logger.verbose("FlurryAgent.setLogEnabled(%s);", logEnabled);

    boolean logEvents = logger.logLevel.ordinal() >= LogLevel.VERBOSE.ordinal();
    FlurryAgent.setLogEvents(logEvents);
    logger.verbose("FlurryAgent.setLogEvents(%s);", logEvents);

    String apiKey = settings.getString("apiKey");
    FlurryAgent.init(analytics.getApplication(), apiKey);
    logger.verbose("FlurryAgent.init(context, %s);", apiKey);
  }

  @Override public void onActivityStarted(Activity activity) {
    super.onActivityStarted(activity);

    FlurryAgent.onStartSession(activity);
    logger.verbose("FlurryAgent.onStartSession(activity);");
  }

  @Override public void onActivityStopped(Activity activity) {
    super.onActivityStopped(activity);

    FlurryAgent.onEndSession(activity);
    logger.verbose("FlurryAgent.onEndSession(activity);");
  }

  @Override public void screen(ScreenPayload screen) {
    super.screen(screen);
    // todo: verify behaviour here, iOS SDK only does pageView, not event
    FlurryAgent.onPageView();
    logger.verbose("FlurryAgent.onPageView();");

    String event = screen.event();
    Map<String, String> properties = screen.properties().toStringMap();
    FlurryAgent.logEvent(event, properties);
    logger.verbose("FlurryAgent.logEvent(%s, %s);", event, properties);
  }

  @Override public void track(TrackPayload track) {
    super.track(track);

    String event = track.event();
    Map<String, String> properties = track.properties().toStringMap();
    FlurryAgent.logEvent(event, properties);
    logger.verbose("FlurryAgent.logEvent(%s, %s);", event, properties);
  }

  @Override public void identify(IdentifyPayload identify) {
    super.identify(identify);

    String userId = identify.userId();
    FlurryAgent.setUserId(userId);
    logger.verbose("FlurryAgent.setUserId(%s);", userId);

    Traits traits = identify.traits();
    int age = traits.age();
    if (age > 0) {
      FlurryAgent.setAge(age);
      logger.verbose("FlurryAgent.setAge(%s);", age);
    }

    String gender = traits.gender();
    if (!isNullOrEmpty(gender)) {
      byte genderConstant;
      if (gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("m")) {
        genderConstant = Constants.MALE;
      } else if (gender.equalsIgnoreCase("female") || gender.equalsIgnoreCase("f")) {
        genderConstant = Constants.FEMALE;
      } else {
        genderConstant = Constants.UNKNOWN;
      }
      FlurryAgent.setGender(genderConstant);
      logger.verbose("FlurryAgent.setGender(%s);", genderConstant);
    }

    AnalyticsContext.Location location = identify.context().location();
    if (location != null) {
      float latitude = (float) location.latitude();
      float longitude = (float) location.longitude();
      FlurryAgent.setLocation(latitude, longitude);
      logger.verbose("FlurryAgent.setLocation(%s, %s);", latitude, longitude);
    }
  }
}