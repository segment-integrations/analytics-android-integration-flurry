package com.segment.analytics.android.integrations.flurry;

import static com.segment.analytics.Analytics.LogLevel.VERBOSE;
import static com.segment.analytics.Utils.createContext;
import static com.segment.analytics.Utils.createTraits;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import android.app.Activity;
import android.app.Application;
import android.util.Log;
import com.flurry.android.Constants;
import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryAgent.Builder;
import com.flurry.android.FlurryAgentListener;
import com.segment.analytics.Analytics;
import com.segment.analytics.AnalyticsContext;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.android.integrations.flurry.FlurryIntegration.FlurryAgentBuilderFactory;
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
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
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
    integration = new FlurryIntegration(analytics, FlurryAgentBuilderFactory.REAL,
        new ValueMap().putValue("apiKey", "foo"));
    // mock it twice so we can initialize it for tests, but reset the mock after initialization.
    PowerMockito.mockStatic(FlurryAgent.class);
  }

  @Test public void initialize() throws IllegalStateException {
    final FlurryAgent.Builder builder = mock(FlurryAgent.Builder.class);
    when(builder.withContinueSessionMillis(20000)).thenReturn(builder);
    when(builder.withCaptureUncaughtExceptions(true)).thenReturn(builder);
    when(builder.withLogEnabled(true)).thenReturn(builder);
    when(builder.withLogLevel(Log.VERBOSE)).thenReturn(builder);
    when(builder.withListener(any(FlurryAgentListener.class))).thenReturn(builder);

    final FlurryAgentBuilderFactory mockBuilderFactory = new FlurryAgentBuilderFactory() {
      @Override
      public Builder create() {
        return builder;
      }
    };

    integration = new FlurryIntegration(analytics, mockBuilderFactory, new ValueMap() //
        .putValue("apiKey", "foo")
        .putValue("sessionContinueSeconds", 20)
        .putValue("captureUncaughtExceptions", true)
        .putValue("reportLocation", false));

    verify(builder).withContinueSessionMillis(20000);
    verify(builder).withCaptureUncaughtExceptions(true);
    verify(builder).withLogEnabled(true);
    verify(builder).withLogLevel(Log.VERBOSE);
    verify(builder).withListener(any(FlurryAgentListener.class));
    verify(builder).build(application, "foo");

    verifyNoMoreInteractions(builder);

    verifyStatic();
    FlurryAgent.setReportLocation(false);
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
