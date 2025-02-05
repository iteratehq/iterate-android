# Change Log

All notable changes to this project will be documented in this file.
`iterate-android` adheres to [Semantic Versioning](https://semver.org/).

## [1.4.4](https://github.com/iteratehq/iterate-android/releases/tag/v1.4.4)

Released on 2025-02-05.

**Fixed**

Fixed a crash when deserializing surveys


## [1.4.3](https://github.com/iteratehq/iterate-android/releases/tag/v1.4.3)

Released on 2024-11-12.

**Fixed**

Additional ProGuard rule updates

## [1.4.2](https://github.com/iteratehq/iterate-android/releases/tag/v1.4.2)

Released on 2024-11-11.

**Fixed**

Updated ProGuard rules for dependencies

## [1.4.1](https://github.com/iteratehq/iterate-android/releases/tag/v1.4.1)

Released on 2024-10-11.

**Fixed**

Fixed a crash when showing a survey while the activity is being backgrounded

## [1.4.0](https://github.com/iteratehq/iterate-android/releases/tag/v1.4.0)

Released on 2024-10-04.

**Added**

Added support for multi-language surveys

## [1.3.1](https://github.com/iteratehq/iterate-android/releases/tag/v1.3.1)

Released on 2024-09-10.

**Fixed**

Updated gson dependency to 2.11.0

## [1.3.0](https://github.com/iteratehq/iterate-android/releases/tag/v1.3.0)

Released on 2024-06-11.

**Added**

Added support for survey appearance options (Light/Dark/Auto)

## [1.2.0](https://github.com/iteratehq/iterate-android/releases/tag/v1.2.0)

Released on 2024-04-29.

**Added**

Added support for border radius on the prompt button

**Fixed**

Init method is now main-safe

## [1.1.9](https://github.com/iteratehq/iterate-android/releases/tag/v1.1.9)

Released on 2024-04-09.

**Fixed**

Dynamically set the button text color to work better with dark mode

## [1.1.8](https://github.com/iteratehq/iterate-android/releases/tag/v1.1.8)

Released on 2024-01-31.

**Added**

Added support for Markdown syntax in the survey prompt.

## [1.1.7](https://github.com/iteratehq/iterate-android/releases/tag/v1.1.7)

Released on 2023-09-28.

**Fixed**

Removes usage of java.time.LocalDateTime to restore support for API versions 21–26. 

## [1.1.6](https://github.com/iteratehq/iterate-android/releases/tag/v1.1.6)

Released on 2023-06-09.

**Fixed**

Fixed an issue causing the close icon not to render in the latest Compose UI

## [1.1.5](https://github.com/iteratehq/iterate-android/releases/tag/v1.1.5)

Released on 2023-05-12.

**Added**

Added support for date types in user and response properties

## [1.1.4](https://github.com/iteratehq/iterate-android/releases/tag/v1.1.4)

Released on 2023-02-17.

**Fixed**

Fixed an issue where response properties were not sent in 1.1.2

## [1.1.3](https://github.com/iteratehq/iterate-android/releases/tag/v1.1.3)

Released on 2022-11-29.

**Fixed**

Fixed an issue where the survey prompt sheet was initially collapsed when displayed in landscape mode.


## [1.1.2](https://github.com/iteratehq/iterate-android/releases/tag/v1.1.2)

Released on 2022-10-17.

**Added**

Added support for custom fonts in survey UI

## [1.1.1](https://github.com/iteratehq/iterate-android/releases/tag/v1.1.1)

Released on 2022-09-15.

**Fixed**

- Fixed an issue that prevented external links in survey copy from opening
- Fix crash on show survey screen when fragment manager is destroyed

## [1.1.0](https://github.com/iteratehq/iterate-android/releases/tag/v1.1.0)

Released on 2022-02-01.

**Added**

- Added the ability to use SharedPreferences instead of EncryptedSharedPreferences to avoid a rare crash, see README.md for details

## [1.0.1](https://github.com/iteratehq/iterate-android/releases/tag/v1.0.1)

Released on 2021-09-30.

**Fixed**

- Prevent null pointer error caused by minification

## [1.0.0](https://github.com/iteratehq/iterate-android/releases/tag/v1.0.0)

Released on 2021-08-22.

**Added**

- Initial release of iterate-android.
