# Architecture: Iterate Android SDK

The Iterate Android SDK delivers surveys to Android app users and collects their responses. This
document describes the delivery model, event flow, targeting architecture, callbacks, storage, and
integration model. For installation and usage examples, see the [README](README.md).

**Distribution:** Maven Central, as `com.iteratehq:iterate`.
**Language:** Kotlin, compiled against Java 17. The public API is annotated `@JvmStatic` and
`@JvmOverloads`, so it is callable from Java.
**minSdk:** 21. See the note below: survey display requires API 25 or higher.
**compileSdk / targetSdk:** 36.
**Dependencies:** `androidx.appcompat`, `androidx.core:core-ktx`,
`com.google.android.material`, `com.google.code.gson:gson`,
`org.jetbrains.kotlinx:kotlinx-coroutines`, [Markwon](https://github.com/noties/Markwon) (`core` and
`image`) for rendering Markdown in prompt text, and
[androidsvg](https://github.com/BigBadaboom/androidsvg). Networking uses the platform's own
`URLConnection` rather than a third-party HTTP client.
**License:** MIT.

There is a separate [React Native
SDK](https://help.iteratehq.com/en/articles/5002440-installing-the-iterate-react-native-sdk).

> **API level and survey display.** `minSdkVersion` is 21, so the SDK compiles into and installs on
> Android 5.0 and above. However `send`, which every survey path goes through, returns early on
> `Build.VERSION.SDK_INT <= 24`. On API 21 to 24 the SDK initializes and accepts events but no survey
> is ever displayed. The effective minimum for survey display is API 25, Android 7.1.

---

## Delivery Architecture

Surveys are delivered inside the host app in response to in-app events. The user is never sent to an
external browser and no survey URL is opened outside the app.

The SDK is initialized once, in the `onCreate()` of an `Application` subclass or an Activity. `init`
does its work on `Dispatchers.IO` and takes an optional completion callback, which is when other
Iterate methods become safe to call:

```kotlin
Iterate.init(this, API_KEY) {
    // safe to call other Iterate methods
}
```

The full signature is `init(context, apiKey, urlScheme, surveyTextFontAssetPath,
buttonFontAssetPath, useEncryptedSharedPreferences, onComplete)`. `urlScheme` is the app's own
registered scheme, reported to the platform so a preview link can be generated for the app; the iOS
SDK detects this from `Info.plist` rather than taking it as a parameter. Calling `identify`,
`sendEvent`, or `send` before `init` throws `IllegalStateException`.

### Presentation

Both UI surfaces are fragments, which is why the event methods take a `FragmentManager`.

The prompt, meaning the small invitation shown before the survey opens, is a native
`BottomSheetDialogFragment` (`PromptView`) built from an XML layout through view binding. Markdown in
the prompt text is rendered with Markwon, and the button and text colors are applied from the survey's
own styling.

The survey itself is a `DialogFragment` (`SurveyView`) hosting a `WebView`.

### The survey body

The survey renders as HTML inside a `WebView` with JavaScript enabled. At survey time the SDK
requests the survey's markup from the Iterate API host, at `<host>/<companyId>/<surveyId>/mobile`,
passing the user's auth token, any event traits, and paths to bundled custom fonts as query
parameters. The returned markup is then injected with `loadDataWithBaseURL`.

Two consequences worth being precise about. The `WebView` never navigates to an Iterate URL and the
user is never handed off to Chrome or an external browser, so the survey opens over the screen the
person was already on. But the markup is fetched over the network at survey time rather than shipped
in the AAR, so a survey does require connectivity.

Custom fonts are read from the app's `assets` folder, with their paths passed to `init` relative to
`/assets`:

```kotlin
Iterate.init(this, API_KEY, null, "Merriweather-Regular.ttf", "WorkSans-VariableFont_wght.ttf")
```

`buttonFontAssetPath` applies to all survey interface buttons (question responses, previous and
next). `surveyTextFontAssetPath` applies to all other survey text. True Type and Open Type fonts are
supported. Font assets are loaded from the app's assets folder, not from an external domain.

Navigation inside the `WebView` is filtered by `shouldOverrideUrlLoading`. Requests to the Iterate
host load normally; anything else is opened with an `ACTION_VIEW` intent, so a link in survey copy
opens in the user's browser rather than inside the survey.

### Native to web bridge

The web layer talks back to the SDK through a JavaScript interface registered on the `WebView`,
exposing a single `postMessage` entry point. That bridge is what drives the callbacks described below,
so response and progress data is available to app code without any additional configuration. The iOS
SDK uses a `WKScriptMessageHandler` for the same purpose.

---

## Event Model

Survey delivery is tied to in-app events. An event can represent any product interaction: viewing a
screen, completing a flow, reaching a lifecycle state. Events are instrumented in the app with
`sendEvent`:

```kotlin
Iterate.sendEvent("viewed-activity-feed", supportFragmentManager)
```

Targeting logic, meaning which surveys fire on which events and for which users, is configured in the
Iterate dashboard rather than in the SDK. The SDK builds an embed context carrying user traits and
targeting information, calls the `/embed` endpoint, and the platform decides whether a survey comes
back. If one does, the SDK shows either the prompt or the survey depending on how the survey is
configured.

This separation means new surveys can be created and targeted to already-instrumented events without
additional SDK instrumentation and without a new app release. Because of that, we recommend
instrumenting events for all core use cases at integration time (sign up, purchase, key screen views,
notification taps, lifecycle transitions) so that any future survey can be targeted to an existing
event without returning to the SDK.

A survey can also be requested directly, outside the event model, with
`install(surveyId, fragmentManager)`. `sendEvent` and `install` are both thin wrappers over the
general `send(fragmentManager, triggerContext, eventContext, eventTraits)`.

---

## Localization

A survey can carry a primary language plus a set of translations, and the SDK resolves which one to
use for the native prompt text and prompt button label. It picks in this order: the `language` user
trait if it names an available language, then `Locale.getDefault().language` if that is available,
then English as the fallback. If no translation matches, the prompt falls back to the survey's own
`prompt.message` and `prompt.buttonText`.

So setting `language` through `identify` lets the app override the device setting. Note that the iOS
SDK resolves this slightly differently: it normalizes the `language` property to a lowercase
two-letter code before matching, walks the whole ordered `Locale.preferredLanguages` list rather than
only the single default, and falls back to the survey's primary language before English. An app
passing a regional code such as `en-US` will match on iOS and not here.

---

## Eligibility and Frequency

Eligibility is decided by the platform, not by the SDK. A person sees a survey only if they match the
targeting criteria configured in the survey builder, and once a survey has been displayed they will
not see it again unless it is configured to allow multiple submissions.

Separately, a person is limited to one survey in any 72 hour period, counting surveys they complete,
dismiss, or ignore. That window is configurable by administrators in company settings. The one
exception is the "No limit" targeting option for allowing repeat submissions, intended for cases where
a survey should always show, such as a post-checkout survey or a feedback button.

See [Survey Eligibility And Frequency](https://help.iteratehq.com/en/articles/2835008-survey-eligibility-and-frequency)
for the full rule set.

---

## Preview

A survey can be previewed before it is published, so the delivery path can be verified end to end
without exposing the survey to real users. `preview` is called once in any component rendered before
the targeted event fires:

```kotlin
Iterate.preview("your-survey-id")
```

In preview mode all targeting options for that survey are ignored, including the frequency cap and
user property targeting. Triggering the targeted event is sufficient to display the survey.

---

## Callbacks

`onResponse` surfaces response data client-side as each answer is submitted, returning the response,
the question, and the survey:

```kotlin
Iterate.onResponse { response, question, survey ->
    // your logic here
}
```

`onEvent` covers the wider survey interaction lifecycle and fires for four event types:

| Event | Data | Notes |
| --- | --- | --- |
| `displayed` | `InteractionEventDisplayedData(source, survey)` | `source` distinguishes the prompt from the survey. |
| `response` | `InteractionEventResponseData(response, question, survey)` | |
| `dismiss` | `InteractionEventDismissData(progress, source, survey)` | `progress` is a `ProgressEventMessageData(completed, total, currentQuestion)` describing how far the user was into the survey when they dismissed it. `completed` counts questions passed, whether answered or skipped. |
| `survey-complete` | `InteractionEventSurveyCompleteData(survey)` | Fires once, when the user reaches the thank you screen. |

Both callbacks require no additional configuration. Note that the iOS SDK has no `onResponse`; there,
responses arrive through `onEvent` with `type == .response`. Cross-platform code cannot assume a
shared callback shape.

---

## Targeting and User Association

User properties set via `identify` are available for survey targeting in the Iterate dashboard and are
associated with all future responses from that user. `email`, `first_name`, `last_name`, and
`external_id` are treated as special properties and surface the person's identity on the dashboard
alongside their responses. `external_id` is a string holding your own internal ID for the user, and
setting it lets Iterate match one person across platforms and sessions, which prevents the same
survey being shown twice to the same person arriving from different places. We recommend setting it:

```kotlin
Iterate.identify(
    UserTraits(
        "external_id" to "12abc34",
        "email" to "example@email.com",
        "date_joined" to Date()
    )
)
```

Property values must be a boolean, string, or number, and once a property's type is established it
cannot be changed. See
[Identifying users across multiple platforms](https://help.iteratehq.com/en/articles/8582987-identifying-users-across-multiple-platforms).

Response properties are passed per-event through `sendEvent`, associating additional context with the
responses to that specific survey without affecting targeting for future surveys:

```kotlin
Iterate.sendEvent(
    "viewed-activity-feed",
    supportFragmentManager,
    EventTraits("selected_product_id" to 12345)
)
```

On iOS, response properties are set through a second `identify` overload,
`identify(responseProperties:)`, rather than through the event call. The two platforms differ here.

To clear everything the SDK has stored, including the user API key and any traits set through
`identify`, call `reset()`. This is normally done on logout:

```kotlin
Iterate.reset()
```

---

## Storage and Encryption

Persisted values are held in `SharedPreferences`, and by default their contents are encrypted before
being written. The implementation is the SDK's own `CryptoManager` rather than
`androidx.security`'s `EncryptedSharedPreferences`: it generates an AES key in the `AndroidKeyStore`,
and encrypts with `AES/GCM/NoPadding` using a 128-bit authentication tag.

Encryption is active when it has been left enabled and the device is on API 23 or higher. On older
devices, or when `useEncryptedSharedPreferences` is passed as `false`, values are written to plain
`SharedPreferences`:

```kotlin
Iterate.init(this, API_KEY, useEncryptedSharedPreferences = false)
```

Storage is split between an in-memory cache and these preferences, behind
`IterateRepository`.

---

## Integration Model

When a user answers a survey question, the response is transmitted to the Iterate platform. From
there, responses route to connected integrations based on the configuration in the Iterate dashboard,
so the routing and the writeback themselves are platform-side and no transport code is needed in the
app.

There is one thing the app does have to do. For a survey delivered through this SDK, rather than sent
from the engagement platform itself, Iterate needs the destination platform's identifier for the
person in order to write to the right profile. For Braze that means passing `braze_id` or
`braze_external_id` as a user trait through `identify`, and then enabling the integration on the
survey:

```kotlin
Iterate.identify(UserTraits("braze_external_id" to brazeExternalId))
```

Without that identifier, responses are collected normally but there is no profile to attach them to.
Surveys deployed from a Braze Canvas or an Iterable Journey do not need this, because the engagement
platform supplies the identity.

With the integration enabled, each response is written back two ways. It is set as a **custom user
attribute**, named after the question prompt by default, and renameable or excludable per question in
the survey's integration settings. It also fires a **custom event** named `survey-question-response`,
once per answered question, carrying `question`, `label`, and `response`, plus one typed field
(`response_int`, `response_string`, or `response_array`) depending on the question type.

See the [Braze integration](https://help.iteratehq.com/en/articles/3303016-braze-integration) and
[Iterable integration](https://help.iteratehq.com/en/articles/3366005-iterable-integration) articles
for the current setup steps and plan requirements.
