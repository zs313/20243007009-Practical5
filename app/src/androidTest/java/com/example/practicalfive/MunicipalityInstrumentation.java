package com.example.practicalfive;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Dependency-free device acceptance tests; invoke as the configured test runner.
 * Uses real Activities and keyboard actions. Browser Intents are captured and blocked.
 * The API 26 requirement applies only to this runner, not to the application.
 */
@android.annotation.TargetApi(26)
public final class MunicipalityInstrumentation extends Instrumentation {
    private static final String TAG = "MunicipalityAcceptance";
    private static final long TIMEOUT_MS = 5000;
    private static final String[] CITIES = {"Beijing", "Shanghai", "Chongqing", "Tianjin"};
    private static final String[] WEBSITES = {
            "https://english.beijing.gov.cn/", "https://english.shanghai.gov.cn/",
            "https://english.cq.gov.cn/", "https://en.tj.gov.cn/"
    };
    private static final int[] IMAGES = {
            R.drawable.beijing, R.drawable.shanghai, R.drawable.chongqing, R.drawable.tianjin
    };
    private static final int[] COLORS = {
            R.color.municipality_beijing, R.color.municipality_shanghai,
            R.color.municipality_chongqing, R.color.municipality_tianjin
    };

    private final List<Activity> activities = new ArrayList<>();
    private volatile Activity resumedActivity;
    private volatile Activity createdDetail;
    private volatile CountDownLatch detailCreated;
    private volatile int detailCreationCount;
    private MainActivity main;
    private int assertions;
    private int scenarios;

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        start();
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle state) {
        synchronized (activities) {
            activities.add(activity);
        }
        super.callActivityOnCreate(activity, state);
        if (activity instanceof Municipality) {
            createdDetail = activity;
            detailCreationCount++;
            CountDownLatch latch = detailCreated;
            if (latch != null) {
                latch.countDown();
            }
        }
    }

    @Override
    public void callActivityOnResume(Activity activity) {
        super.callActivityOnResume(activity);
        resumedActivity = activity;
    }

    @Override
    public void callActivityOnDestroy(Activity activity) {
        super.callActivityOnDestroy(activity);
        synchronized (activities) {
            activities.remove(activity);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle result = new Bundle();
        int exitCode = Activity.RESULT_CANCELED;
        sendStatus(1, testEvent());
        try {
            require(Build.VERSION.SDK_INT >= 26, "Test runner requires an API 26+ device");
            testTextProcessor();
            Intent launch = new Intent(getTargetContext(), MainActivity.class);
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            main = (MainActivity) startActivitySync(launch);
            waitForIdleSync();
            require(main != null, "Home screen launches");
            testRejectedInput();
            testAllMunicipalities();
            testHardwareEnter();
            testInvalidDetailIntents();
            testMissingBrowserFallback();
            String summary = "PASS: " + scenarios + " scenarios, " + assertions + " assertions.\n";
            result.putString("stream", summary);
            result.putInt("scenarios", scenarios);
            result.putInt("assertions", assertions);
            Log.i(TAG, summary);
            Bundle passed = testEvent();
            passed.putString("stream", summary);
            sendStatus(0, passed);
            exitCode = Activity.RESULT_OK;
        } catch (Throwable failure) {
            Log.e(TAG, "Acceptance test failed", failure);
            result.putString("stream", "FAIL after " + scenarios + " completed scenarios and "
                    + assertions + " assertions:\n" + Log.getStackTraceString(failure));
            Bundle failed = testEvent();
            failed.putString("stack", Log.getStackTraceString(failure));
            failed.putString("stream", result.getString("stream"));
            sendStatus(-2, failed);
        } finally {
            finishActivities();
            finish(exitCode, result);
        }
    }

    private void testTextProcessor() {
        TextProcessor processor = new TextProcessor();
        for (String city : CITIES) {
            equal(city, processor.processMunicipality(city), city + " canonical validation");
            equal(city, processor.processMunicipality(city.toUpperCase(Locale.ROOT)),
                    city + " uppercase validation");
            equal(city, processor.processMunicipality("  " + city.toLowerCase(Locale.ROOT) + "  "),
                    city + " case and surrounding whitespace validation");
        }
        for (String invalid : new String[] {null, "", "   ", "\t\r\n", "Paris", "Beijing!",
                "Beijing Shanghai", "Shang hai"}) {
            equal(null, processor.processMunicipality(invalid), "Reject invalid input: " + invalid);
        }
        require(TextProcessor.isBlank(null), "Null is blank");
        require(TextProcessor.isBlank(" \t\r\n"), "Whitespace is blank");
        require(!TextProcessor.isBlank(" Beijing "), "A municipality is not blank");
        scenario("TextProcessor accepts four cities and rejects invalid input");
    }

    private void testRejectedInput() {
        for (String invalid : new String[] {"", "   ", "Paris"}) {
            ActivityMonitor unexpected = addMonitor(Municipality.class.getName(),
                    new ActivityResult(Activity.RESULT_CANCELED, null), true);
            try {
                ui(() -> {
                    EditText input = main.findViewById(R.id.municipality_input);
                    input.setText(invalid);
                    input.requestFocus();
                    input.onEditorAction(EditorInfo.IME_ACTION_GO);
                    require(input.getError() != null && input.getError().length() > 0,
                            "Invalid input shows an inline error: " + invalid);
                    require(!main.isFinishing(), "Invalid input keeps the home screen open");
                });
                waitForIdleSync();
                equal(0, unexpected.getHits(), "Invalid input does not attempt detail navigation");
                require(resumedActivity == main, "Home remains the current Activity");
            } finally {
                removeMonitor(unexpected);
            }
            scenario("Home rejects '" + invalid + "'");
        }
    }

    private void testAllMunicipalities() {
        String[] descriptions = getTargetContext().getResources()
                .getStringArray(R.array.municipalities_array);
        equal(4, descriptions.length, "Description resource has four entries");
        for (int i = 0; i < CITIES.length; i++) {
            final int cityIndex = i;
            Activity canonical = navigate(CITIES[i], EditorInfo.IME_ACTION_GO, false);
            inspectDetails(canonical, cityIndex, descriptions[i]);
            verifyWebsiteIntent(canonical, cityIndex);
            finishActivity(canonical);
            scenario(CITIES[i] + " Go navigation, content, image, color and website");

            String mixed = "  " + CITIES[i].substring(0, 1).toLowerCase(Locale.ROOT)
                    + CITIES[i].substring(1).toUpperCase(Locale.ROOT) + "  ";
            Activity padded = navigate(mixed, EditorInfo.IME_ACTION_DONE, false);
            ui(() -> equal(CITIES[cityIndex],
                    ((TextView) padded.findViewById(R.id.municipality_name)).getText().toString(),
                    "Done normalizes surrounding whitespace and case"));
            finishActivity(padded);
            scenario(CITIES[i] + " Done navigation with padded mixed-case input");
        }
    }

    private void testHardwareEnter() {
        Activity detail = navigate("tIaNjIn", EditorInfo.IME_NULL, true);
        ui(() -> equal("Tianjin",
                ((TextView) detail.findViewById(R.id.municipality_name)).getText().toString(),
                "Hardware Enter resolves mixed case"));
        finishActivity(detail);
        scenario("Hardware Enter navigates exactly once");
    }

    private Activity navigate(String inputText, int editorAction, boolean hardwareEnter) {
        int previousCount = detailCreationCount;
        ActivityMonitor monitor = addMonitor(Municipality.class.getName(), null, false);
        try {
            ui(() -> {
                EditText input = main.findViewById(R.id.municipality_input);
                input.setText(inputText);
                input.requestFocus();
                if (hardwareEnter) {
                    input.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    input.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                } else {
                    input.onEditorAction(editorAction);
                }
            });
            Activity detail = monitor.waitForActivityWithTimeout(TIMEOUT_MS);
            require(detail instanceof Municipality, "Valid input opens Municipality: " + inputText);
            waitForIdleSync();
            equal(previousCount + 1, detailCreationCount, "One submission creates one detail screen");
            ui(() -> {
                Intent received = detail.getIntent();
                require(received.getComponent() != null, "Navigation uses an explicit Intent");
                equal(Municipality.class.getName(), received.getComponent().getClassName(),
                        "Explicit Intent targets Municipality");
                equal(new TextProcessor().processMunicipality(inputText),
                        received.getStringExtra(Municipality.EXTRA_MUNICIPALITY),
                        "Intent transports the canonical city name");
                require(!detail.isFinishing(), "A valid detail screen remains open");
            });
            return detail;
        } finally {
            removeMonitor(monitor);
        }
    }

    private void inspectDetails(Activity detail, int cityIndex, String expectedDescription) {
        ui(() -> {
            TextView title = detail.findViewById(R.id.municipality_name);
            TextView information = detail.findViewById(R.id.municipality_information);
            ImageView photo = detail.findViewById(R.id.municipality_image);
            ScrollView scroll = detail.findViewById(R.id.information_scroll);
            Button visit = detail.findViewById(R.id.visit_site);
            require(detail.findViewById(R.id.municipality_root) != null, "Detail root exists");
            require(title != null && information != null && photo != null && scroll != null
                    && visit != null, "All required detail controls exist");
            equal(CITIES[cityIndex], title.getText().toString(), "Title identifies the chosen city");
            equal(expectedDescription, information.getText().toString(),
                    "Description maps to the required array order");
            require(expectedDescription.contains(CITIES[cityIndex]),
                    "Description identifies its municipality");
            int words = expectedDescription.trim().split("\\s+").length;
            require(words >= 80 && words <= 120, "Description has 80-120 words: " + words);
            require(isDescendant(information, scroll), "Description is inside the ScrollView");
            require(information.getEllipsize() == null, "Description is not ellipsized");
            require(visit.isEnabled() && visit.isClickable(), "Visit site button is actionable");
            Rect visibleButton = new Rect();
            require(visit.getWidth() > 0 && visit.getHeight() > 0
                            && visit.getGlobalVisibleRect(visibleButton)
                            && visibleButton.width() == visit.getWidth()
                            && visibleButton.height() == visit.getHeight(),
                    "Visit site is fully visible on the device screen");
            int initialScroll = scroll.getScrollY();
            scroll.scrollTo(0, information.getBottom());
            require(!scroll.canScrollVertically(1), "The end of the description can be reached");
            require(information.getHeight() <= scroll.getHeight() || scroll.getScrollY() > 0,
                    "Overflowing descriptions actually scroll");
            require(visit.getGlobalVisibleRect(visibleButton)
                            && visibleButton.height() == visit.getHeight(),
                    "Visit site remains visible after scrolling");
            scroll.scrollTo(0, initialScroll);
            require(photo.getDrawable() != null, "Municipality photo is present");
            Drawable expectedImage = detail.getResources().getDrawable(IMAGES[cityIndex], detail.getTheme());
            require(sameImage(photo.getDrawable(), expectedImage), "Municipality photo matches " + CITIES[cityIndex]);
            int expectedColor = detail.getResources().getColor(COLORS[cityIndex], detail.getTheme());
            equal(expectedColor, backgroundColor(title), "Title uses its municipality color");
            equal(expectedColor, information.getCurrentTextColor(), "Description uses its municipality color");
            equal(expectedColor, backgroundColor(visit), "Visit button uses its municipality color");
        });
    }

    private void verifyWebsiteIntent(Activity detail, int cityIndex) {
        AtomicReference<Intent> captured = new AtomicReference<>();
        ActivityMonitor browser = new ActivityMonitor() {
            @Override
            public ActivityResult onStartActivity(Intent intent) {
                if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                    captured.set(new Intent(intent));
                    return new ActivityResult(Activity.RESULT_CANCELED, null);
                }
                return null;
            }
        };
        addMonitor(browser);
        try {
            ui(() -> ((Button) detail.findViewById(R.id.visit_site)).performClick());
            waitForIdleSync();
            Intent website = captured.get();
            require(website != null, "Visit site launches an Intent");
            equal(Intent.ACTION_VIEW, website.getAction(), "Website uses ACTION_VIEW");
            require(website.getComponent() == null && website.getPackage() == null,
                    "Website Intent is implicit and allows a browser choice");
            equal(WEBSITES[cityIndex], website.getDataString(), "Website matches the selected municipality");
            equal(1, browser.getHits(), "One click requests one website launch");
            ui(() -> require(!detail.isFinishing(), "Detail stays open after website interception"));
        } finally {
            removeMonitor(browser);
        }
    }

    private void testInvalidDetailIntents() throws InterruptedException {
        for (String value : new String[] {null, "", "   ", "Paris"}) {
            Intent invalid = new Intent(main, Municipality.class);
            if (value != null) {
                invalid.putExtra(Municipality.EXTRA_MUNICIPALITY, value);
            }
            createdDetail = null;
            CountDownLatch latch = new CountDownLatch(1);
            detailCreated = latch;
            try {
                // startActivitySync is unsuitable here: this Activity intentionally
                // finishes during onCreate, before it can become idle.
                ui(() -> main.startActivity(invalid));
                require(latch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS),
                        "Invalid detail Intent reached onCreate");
                waitForIdleSync();
                Activity rejected = createdDetail;
                require(rejected != null, "Invalid detail Activity was observed");
                ui(() -> {
                    require(rejected.isFinishing() || rejected.isDestroyed(),
                            "Missing/invalid extra finishes safely: " + value);
                    require(!main.isFinishing(), "Home survives invalid detail data");
                });
            } finally {
                detailCreated = null;
            }
            scenario("Detail rejects missing/invalid extra: " + value);
        }
    }

    private void testMissingBrowserFallback() {
        Activity detail = navigate("Beijing", EditorInfo.IME_ACTION_GO, false);
        AtomicReference<Intent> attempted = new AtomicReference<>();
        ActivityMonitor unavailableBrowser = new ActivityMonitor() {
            @Override
            public ActivityResult onStartActivity(Intent intent) {
                if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                    attempted.set(new Intent(intent));
                    throw new ActivityNotFoundException("Test: no browser can handle this Intent");
                }
                return null;
            }
        };
        addMonitor(unavailableBrowser);
        try {
            // Inject the documented platform failure without uninstalling/disabling a browser.
            ui(() -> ((Button) detail.findViewById(R.id.visit_site)).performClick());
            waitForIdleSync();
            require(attempted.get() != null, "Missing-browser fallback was exercised");
            ui(() -> require(!detail.isFinishing() && !detail.isDestroyed(),
                    "No available browser does not crash or close the detail screen"));
        } finally {
            removeMonitor(unavailableBrowser);
            finishActivity(detail);
        }
        scenario("Missing browser is handled without a crash");
    }

    private static boolean isDescendant(View child, View ancestor) {
        ViewParent parent = child.getParent();
        while (parent != null) {
            if (parent == ancestor) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    private static boolean sameImage(Drawable actual, Drawable expected) {
        if (actual instanceof BitmapDrawable && expected instanceof BitmapDrawable) {
            return ((BitmapDrawable) actual).getBitmap().sameAs(((BitmapDrawable) expected).getBitmap());
        }
        Bitmap actualPixels = render(actual);
        Bitmap expectedPixels = render(expected);
        boolean same = actualPixels.sameAs(expectedPixels);
        actualPixels.recycle();
        expectedPixels.recycle();
        return same;
    }

    private static Bitmap render(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
        Rect original = new Rect(drawable.getBounds());
        drawable.setBounds(0, 0, 64, 64);
        drawable.draw(new Canvas(bitmap));
        drawable.setBounds(original);
        return bitmap;
    }

    private static int backgroundColor(View view) {
        ColorStateList tint = view.getBackgroundTintList();
        if (tint != null) {
            return tint.getColorForState(view.getDrawableState(), tint.getDefaultColor());
        }
        Drawable background = view.getBackground();
        if (background instanceof ColorDrawable) {
            return ((ColorDrawable) background).getColor();
        }
        if (background == null) {
            throw new AssertionError("Expected a colored background on " + view.getId());
        }
        Bitmap pixels = render(background);
        int color = pixels.getPixel(32, 32);
        pixels.recycle();
        return color;
    }

    private void finishActivity(Activity activity) {
        ui(() -> {
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                activity.finish();
            }
        });
        waitForIdleSync();
    }

    private void finishActivities() {
        List<Activity> snapshot;
        synchronized (activities) {
            snapshot = new ArrayList<>(activities);
        }
        try {
            ui(() -> {
                for (int i = snapshot.size() - 1; i >= 0; i--) {
                    Activity activity = snapshot.get(i);
                    if (!activity.isFinishing() && !activity.isDestroyed()) {
                        activity.finish();
                    }
                }
            });
            waitForIdleSync();
        } catch (Throwable cleanupFailure) {
            Log.w(TAG, "Activity cleanup failed", cleanupFailure);
        }
    }

    private void ui(Runnable action) {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        runOnMainSync(() -> {
            try {
                action.run();
            } catch (Throwable error) {
                failure.set(error);
            }
        });
        Throwable error = failure.get();
        if (error != null) {
            throw new AssertionError("Main-thread assertion/action failed", error);
        }
    }

    private void require(boolean condition, String message) {
        assertions++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private void equal(Object expected, Object actual, String message) {
        require(expected == null ? actual == null : expected.equals(actual),
                message + " (expected=" + expected + ", actual=" + actual + ")");
    }

    private void scenario(String description) {
        scenarios++;
        Bundle progress = new Bundle();
        progress.putString("stream", "PASS " + scenarios + ": " + description + "\n");
        sendStatus(2, progress);
        Log.i(TAG, description);
    }

    private Bundle testEvent() {
        Bundle event = new Bundle();
        event.putString("id", "MunicipalityInstrumentation");
        event.putString("class", getClass().getName());
        event.putString("test", "municipalityAcceptance");
        event.putInt("numtests", 1);
        event.putInt("current", 1);
        return event;
    }
}
