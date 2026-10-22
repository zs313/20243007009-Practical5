# Verification record

Checked on 22 October 2026. Commands for reproducing the checks are in the README.

## Build and static checks

- `assembleDebug`: passed; the debug APK was generated.
- `assembleDebugAndroidTest`: passed; the device acceptance APK was generated.
- `lintDebug`: passed with **0 errors and 16 warnings**. The full text report is in [docs/verification/lint-results-debug.txt](docs/verification/lint-results-debug.txt).
- Standalone `TextProcessorTest`: **22 municipality cases and blank-input checks passed**.

The remaining Lint warnings concern a newer available Gradle version, optional Android 12 backup rules, legacy/adaptive icon recommendations, densityless bitmap locations and an autofill hint. The supplied city/map assets remain in `res/drawable` as requested by the brief. Autofill is disabled for the search field in guarded API 26+ Java code. No Lint baseline or broad suppression is used.

The API 27 light navigation bar theme attribute is isolated in `values-v27`; the application retains API 24 compatibility. System bars, cutouts and the keyboard are accounted for when laying out both screens.

## Device acceptance

The final APK was installed and tested on the Pixel 9 AVD running **Android 17 / API 37**, at **1080 × 2424, 420 dpi**. The acceptance runner passed **18 scenarios and 217 assertions**. See the [actual runner output](docs/verification/instrumentation.txt).

- Four supported names, capitalization, surrounding spaces, null/empty input and unsupported names.
- Inline validation errors without leaving the home screen.
- Go and Done navigation for every city; hardware Enter creates one detail screen.
- Explicit Activity target and canonical name extra; missing/invalid extras finish safely.
- Correct city images, array mapping, 80–120 word descriptions and programmatic colors.
- Description scrolling and a fully visible Visit site button before and after scrolling.
- Exact implicit ACTION_VIEW URLs for all four cities, without an explicit browser component.
- ActivityNotFoundException handling when no browser is available.

The browser monitor intentionally blocks external navigation during this automated test. Real browser checks are recorded separately below.

Separate screen interaction checks also passed for all four cities; [the output](docs/verification/visual-checks.txt) records unchanged button bounds after a swipe. Screenshots were visually reviewed for text, photos, colors, scrolling and screen insets.

| Screen | Initial view | After scrolling |
| --- | --- | --- |
| Home | [Screenshot](docs/verification/screenshots/home.jpg) | — |
| Beijing | [Screenshot](docs/verification/screenshots/beijing.jpg) | [Screenshot](docs/verification/screenshots/beijing-scrolled.jpg) |
| Shanghai | [Screenshot](docs/verification/screenshots/shanghai.jpg) | [Screenshot](docs/verification/screenshots/shanghai-scrolled.jpg) |
| Chongqing | [Screenshot](docs/verification/screenshots/chongqing.jpg) | [Screenshot](docs/verification/screenshots/chongqing-scrolled.jpg) |
| Tianjin | [Screenshot](docs/verification/screenshots/tianjin.jpg) | [Screenshot](docs/verification/screenshots/tianjin-scrolled.jpg) |

API 24 compatibility was checked statically; no API 24 device run is claimed.

## Actual browser checks

Each city's Visit site button was tapped through Android's input interface, opening Chrome. The address bar and actual official page title/content were checked separately from the intercepted Intent tests. All four pages loaded successfully; Chongqing required a longer wait. See [the recorded results](docs/verification/browser-checks.json).

| Municipality | Destination | Loaded page / evidence |
| --- | --- | --- |
| Beijing | https://english.beijing.gov.cn/ | [BEIJING CHINA — International Web Portal of Beijing](docs/verification/screenshots/browser-beijing.jpg) |
| Shanghai | https://english.shanghai.gov.cn/ | [SHANGHAI CHINA — International Services Shanghai](docs/verification/screenshots/browser-shanghai.jpg) |
| Tianjin | https://en.tj.gov.cn/ | [Tianjin Municipal People's Government](docs/verification/screenshots/browser-tianjin.jpg) |
| Chongqing | https://english.cq.gov.cn/ | [CHONGQING · CHINA](docs/verification/screenshots/browser-chongqing.jpg) |

Website availability depends on the external sites and network; these results describe the observed check on the date above.

Chrome displayed the emulator's “No internet connection” banner even while these pages rendered. The checks above use the actual address and page content as evidence.
