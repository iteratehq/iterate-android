# Iterate for Android

[![build](https://img.shields.io/travis/com/iteratehq/iterate-android)](https://travis-ci.com/github/iteratehq/iterate-android) [![version](https://img.shields.io/github/v/tag/iteratehq/iterate-android?label=version)](https://github.com/iteratehq/iterate-android/releases) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.iteratehq/iterate/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.iteratehq/iterate) [![license](https://img.shields.io/github/license/iteratehq/iterate-android?color=%23000000)](https://github.com/iteratehq/iterate-android/blob/master/LICENSE.txt)

---

[Iterate](https://iteratehq.com) surveys put you directly in touch with your app users to learn how you can change for the better—from your product to your app experience.

Run surveys that are highly targeted, user-friendly, and on-brand. You’ll understand not just what your visitors are doing, but why.

## Requirements

This SDK requires Android 5.0 Lollipop (API level 21) or higher.

## Install

Ensure that you include the Maven Central repository on your root folder `build.gradle` file.

```Groovy
buildscript {
    repositories {
        ...
        mavenCentral()
    }
}
```

Add the latest `iterate` dependency to your `app/build.gradle` file.

```Groovy
dependencies {
    implementation "com.iteratehq:iterate:<latest_version>"
}
```

## Usage

Within your app, surveys are shown in response to _events_. An event can be anything from viewing a screen, clicking a button, or any other user action. You use the Iterate SDK to send events to Iterate, then from your Iterate dashboard you create surveys that target those events.

**Quick start**

Create your [Iterate](https://iteratehq.com) account if you haven't already.

1. Create a new survey and select "Install in your mobile app"
2. Go to the "Preview & Publish" tab and copy your SDK API key
3. Initialize the SDK in the `onCreate()` method of your `Application` subclass or an `Activity`.

```Kotlin
import com.iteratehq.iterate.Iterate

class IterateApp : Application() {

    override fun onCreate() {
        // ...
        Iterate.init(this, API_KEY)
    }
}
```

4. Implement events

Here's an example of an event being fired when the user views the activity feed screen

```Kotlin
import com.iteratehq.iterate.Iterate

class ActivityFeedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // ...
        Iterate.sendEvent("viewed-activity-feed", supportFragmentManager)
    }
}
```

5. Create your survey on iteratehq.com and target it to that event
6. Publish your survey and you're done 🎉

## Previewing your survey

You'll likely want to preview your survey before publishing it so you can test that everything works correctly. When previewing a survey you'll be able to see a survey before it's published. When previewing a survey all targeting options for that survey are ignored (e.g. rate limiting, targeting user properties), the only thing you need to do is trigger the event that your survey is targeting and it will show up.

1. In the "Preview & Publish" tab select 'Android' and copy the preview code.
2. Implement `Iterate.preview` into your application, this can be done once in any component that's rendered before the event you're targeting

```Kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    // ...
    Iterate.preview("your-survey-id")
}
```

## Recommendations

When implementing Iterate for the first time, we encourage you to implement events for _all_ of your core use cases which you may want to target surveys to in the future. e.g. sign up, purchased, viewed X screen, tapped notification, etc. This way you can easily launch new surveys targeting these events without needing to instrument a new event each time.

## Associating data with a user

Using the `identify` method, you can easily add 'user properties' to a user that can be used to target surveys to them and associate the information with all of their future responses. We recommend setting the `external_id` (needs to be a string) which represents your internal id for the user, this allows us to associate this user across multiple platforms and sessions'

```Kotlin
fun onLoginSuccessful() {
    Iterate.identify(
        UserTraits(
            "external_id" to "12abc34",
            "email" to "example@email.com"
        )
    )
}
```

You can also associate 'response properties' with the user's responses to a specific survey (not associated with any future surveys they fill out), by passing an object to the `sendEvent` method.

```Kotlin
fun onActivityFeedViewed() {
    Iterate.sendEvent(
        "viewed-activity-feed",
        supportFragmentManager,
        EventTraits(
            "selected_product_id" to 12345,
            "timestamp" to 140002658477
        )
    )
}
```

For more information see our [help article](https://help.iteratehq.com/en/articles/4457590-associating-data-with-a-user-or-response).

## Event callbacks

If you need access to the user's responses on the client, you can use the `onResponse` method to pass a callback function that will return the question and response

```Kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    // ...
    Iterate.onResponse { response, question, survey ->
        // Your logic here
    }
}
```

If you need access to other events on the survey (dismiss, survey-complete, etc), you can use the `onEvent` method to pass a callback function that will fire with each of the events listed below

```Kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    // ...
    Iterate.onEvent { type, data ->
        // Your logic here
    }
}
```

| Event             | Data                                                                                                                             | Notes                                                                                                                                                                                                                                                                                                                                             |
| ----------------- | -------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| "dismiss"         | `data class InteractionEventDismissData(val progress: ProgressEventMessageData?, val source: InteractionEventSource, val survey: Survey)`<p></p><p></p>`data class ProgressEventMessageData(val completed: Int, val total: Int, val currentQuestion: Question?)` | `progress` contains data about how far the user was in the survey when they dismissed. `completed` is number of questions they've completed (regardless of if they responded to the question or skipped it). `total` is the total number of questions in the survey. `currentQuestion` is the question they were on when they dismissed the survey. |
| "displayed"       | `data class InteractionEventDisplayedData(val source: InteractionEventSource, val survey: Survey)`                                                                               |
| "response"        | `data class InteractionEventResponseData(val response: Response, val question: Question, val survey: Survey)`                                                                     |
| "survey-complete" | `data class InteractionEventSurveyCompleteData(val survey: Survey)`                                                                                                             | Called once when the user reaches the 'thank you' screen                                                                                                                                                                                                                                                                                          |

## Clearing data

To clear all data Iterate has stored (user api key, any user properties stored by calling the `identify` method, etc) call the `reset` method. This is commonly called when you log a user out of your app.

```Kotlin
fun logout() {
    Iterate.reset()
    // Your other logout logic here
}
```

## Survey eligibility and frequency

By default surveys are only shown once per person and user's can only see at most 1 survey every 72 hours (which is configurable). You can learn more about how [eligibility and frequency works](https://help.iteratehq.com/en/articles/2835008-survey-eligibility-and-frequency).

## Troubleshooting

If you have any issues you can head over to our [help center](https://help.iteratehq.com) to search for an answer or chat with our support team.
