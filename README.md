# Chinese Municipalities — Practical 5

A Java and XML Android application for exploring **Beijing, Shanghai, Tianjin and Chongqing**. The Android Studio project name is `PracticalFive`.

## Run

1. Open this repository folder in Android Studio and let Gradle sync.
2. Select an installed Android SDK and an emulator or device (Android 7.0 / API 24 or later).
3. Run the `app` configuration.
4. Type a supported English municipality name and press **Go**, **Done**, or hardware **Enter**. Capitalization and surrounding spaces are accepted.
5. Read its information, scroll the description, or tap **Visit site** to open the corresponding official government website.

Blank input and unsupported names display an inline error and stay on the home screen. The internal detail Activity also rejects missing or invalid extras safely.

The project uses Android Gradle plugin 9.3.0, Gradle 9.6.0, compile/target SDK 37, and a Gradle JVM 25 toolchain. Application code is Java (Java 11 compatibility); layouts and reusable resources are XML. `local.properties` is intentionally excluded because each computer has its own SDK path.

## Structure

- `MainActivity.java`: keyboard actions, inline validation errors and explicit Intent navigation.
- `TextProcessor.java`: shared, Android-independent municipality validation and normalization.
- `Municipality.java`: city resource selection, programmatic styling and implicit `ACTION_VIEW` website navigation.
- `UiInsets.java`: system bar, cutout and keyboard insets.
- `res/values/arrays.xml`: four original 80–120 word introductions, in **Beijing → Shanghai → Chongqing → Tianjin** order.
- `SOURCES.md`: factual sources and official website verification notes.
- `tests/TextProcessorTest.java`: standalone Java regression checks.
- `app/src/androidTest/`: dependency-free device acceptance runner.

The supplied transparent map is converted to a true JPG on a white background to match the brief. The launcher icons retain the `ic_launcher` name and include adaptive and legacy variants. Named colors are reused throughout. Chongqing uses bright orange information text on a dark panel for legibility; its orange title and button use dark text.

## Build and checks

From a terminal at the repository root, with the configured JDK and Android SDK available:

```powershell
.\gradlew.bat assembleDebug assembleDebugAndroidTest lintDebug
```

Run the device acceptance runner on an **API 26+** device (the app itself supports API 24+):

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r -t app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w com.example.practicalfive.test/com.example.practicalfive.MunicipalityInstrumentation
```

The acceptance runner checks real Activities, Go/Done/Enter input, invalid data, all four descriptions, images and colors, the exact explicit/implicit Intent contracts, and the missing-browser exception path. Its browser monitor blocks external navigation during automated checks. Browser page loading is checked separately.

The final device run passed **18 scenarios and 217 assertions**. See [TESTING.md](TESTING.md) for build results, screenshots and browser verification limits.

Run the standalone Java checks:

```powershell
javac -d build/text-processor-test app/src/main/java/com/example/practicalfive/TextProcessor.java tests/TextProcessorTest.java
java -cp build/text-processor-test com.example.practicalfive.TextProcessorTest
```

## Submission

Submit the public repository HTTPS URL on Canvas:

https://github.com/zs313/20243007009-Practical5

Development is recorded as incremental feature commits with descriptive summaries and details. No timestamps are rewritten.
