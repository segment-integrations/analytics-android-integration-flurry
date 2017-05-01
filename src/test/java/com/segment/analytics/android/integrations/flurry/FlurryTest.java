package com.segment.analytics.android.integrations.flurry;

import android.app.Activity;
import android.app.Application;
import com.flurry.android.Constants;
import com.flurry.android.FlurryAgent;
import com.segment.analytics.Analytics;
import com.segment.analytics.AnalyticsContext;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.core.tests.BuildConfig;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.test.IdentifyPayloadBuilder;
import com.segment.analytics.test.ScreenPayloadBuilder;
import com.segment.analytics.test.TrackPayloadBuilder;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static com.segment.analytics.Analytics.LogLevel.VERBOSE;
import static com.segment.analytics.Utils.createContext;
import static com.segment.analytics.Utils.createTraits;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 18, manifest = Config.NONE)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@PrepareForTest(FlurryAgent.class)
public class FlurryTest {
  @Rule public PowerMockRule rule = new PowerMockRule();
  @Mock Application application;
  @Mock Analytics analytics;
  FlurryIntegration integration;

  @Before public void setUp() {
    initMocks(this);

    when(analytics.getApplication()).thenReturn(application);
    when(analytics.logger("Flurry")).thenReturn(Logger.with(VERBOSE));

    PowerMockito.mockStatic(FlurryAgent.class);
    integration = new FlurryIntegration(analytics, new ValueMap().putValue("apiKey", "foo"));
    // mock it twice so we can initialize it for tests, but reset the mock after initialization.
    PowerMockito.mockStatic(FlurryAgent.class);
  }

  @Test public void initialize() throws IllegalStateException {
    integration = new FlurryIntegration(analytics, new ValueMap() //
        .putValue("apiKey", "foo")
        .putValue("sessionContinueSeconds", 20)
        .putValue("captureUncaughtExceptions", true)
        .putValue("reportLocation", false));

    verifyStatic();
    FlurryAgent.setContinueSessionMillis(20000);
    verifyStatic();
    FlurryAgent.setCaptureUncaughtExceptions(true);
    verifyStatic();
    FlurryAgent.setReportLocation(false);
    verifyStatic();
    FlurryAgent.setLogEnabled(true);
    verifyStatic();
    FlurryAgent.setLogEvents(true);
    verifyStatic();
    FlurryAgent.init(application, "foo");
    verifyStatic();
    FlurryAgent.onStartSession(application);
  }

  @Test public void activityStart() {
    Activity activity = mock(Activity.class);
    integration.onActivityStarted(activity);
    verifyStatic();
    FlurryAgent.onStartSession(activity);
  }

  @Test public void activityStop() {
    Activity activity = mock(Activity.class);
    integration.onActivityStopped(activity);
    verifyStatic();
    FlurryAgent.onEndSession(activity);
  }

  @Test public void screen() {
    integration.screen(new ScreenPayloadBuilder().name("foo").category("bar").build());
    verifyStatic();
    FlurryAgent.onPageView();
    verifyStatic();
    FlurryAgent.logEvent(eq("foo"), Matchers.<Map<String, String>>any());
  }

  @Test public void track() {
    integration.track(new TrackPayloadBuilder().event("bar").build());
    verifyStatic();
    FlurryAgent.logEvent(eq("bar"), Matchers.<Map<String, String>>any());
  }

  @Test public void identify() {
    integration.identify(new IdentifyPayloadBuilder().traits(createTraits("foo")).build());
    verifyStatic();
    FlurryAgent.setUserId("foo");
    verifyNoMoreInteractions(FlurryAgent.class);
  }

  @Test public void identifyWithTraits() {
    Traits traits = createTraits("bar").putAge(3).putGender("f");
    AnalyticsContext analyticsContext = createContext(traits).putLocation(
        new AnalyticsContext.Location().putLatitude(20).putLongitude(20));
    integration.identify(
        new IdentifyPayloadBuilder().traits(traits).context(analyticsContext).build());
    verifyStatic();
    FlurryAgent.setUserId("bar");
    verifyStatic();
    FlurryAgent.setAge(3);
    verifyStatic();
    FlurryAgent.setGender(Constants.FEMALE);
    verifyStatic();
    FlurryAgent.setLocation(20, 20);
  }
}
