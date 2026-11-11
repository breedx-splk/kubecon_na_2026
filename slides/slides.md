---
# try also 'default' to start simple
# theme: seriph
theme: default
# random image from a curated Unsplash collection by Anthony
# like them? see https://unsplash.com/collections/94734566/slidev
# background: https://cover.sli.dev
# some information about your slides (markdown enabled)
title: OpenTelemetry In Your Pocket
info: |
  ## OpenTelemetry In Your Pocket
  
  A guide to the OpenTelemetry Android Agent DSL

  https://github.com/breedx-splk/kubecon_na_2026
# apply UnoCSS classes to the current slide
class: cncf-title bright-subtitle
layout: default
hideFooter: true
# https://sli.dev/features/drawing
drawings:
  persist: false
# slide transition: https://sli.dev/guide/animations.html#slide-transitions
transition: slide-left
# enable Comark Syntax: https://comark.dev/syntax/markdown
comark: true
# duration of the presentation
duration: 30min

---

<!-- Original intro slide, kept for reference:
---
layout: cover
class: text-center
---

# OpenTelemetry<br/>In Your Pocket

### <span class="android-green">A guide to the OpenTelemetry<br/>Android Agent DSL</span>

Jason Plumb (Splunk)
-->

<!-- Branding: CNCF / The Linux Foundation. Background rendered from the official
KubeCon_CloudNativeCon_North_America_2026_-_Branded_PowerPoint.pptx title slide,
attached to the speaker email dated November 3, 2026.
Event: https://events.linuxfoundation.org/kubecon-cloudnativecon-north-america/ -->
<img
  class="cncf-title-background"
  src="./kubecon_na_2026_title_background.png"
  alt="KubeCon + CloudNativeCon North America 2026 branding with mountain artwork"
/>

<div class="cncf-title-content">
  <h1>OpenTelemetry<br/>In Your Pocket</h1>
  <p class="cncf-title-subtitle">A guide to the OpenTelemetry Android Agent DSL</p>
  <p class="cncf-title-speaker">Jason Plumb (Splunk)</p>
</div>

---
layout: two-cols
---

# About me

<img src="./profile_pic_480.png" width="75%"/>

::right::

<div class="h-10"></div>

## Jason Plumb <span class="text-sm">(he/him)</span>

## 30 years in industry

## 10 years in Observability

## I work at Splunk

<div class="h-10"></div>

- [breedx-splk](https://github.com/breedx-splk) (GitHub)
- [opentelemetry-android](https://github.com/open-telemetry/opentelemetry-android)
- [opentelemetry-kotlin](https://github.com/open-telemetry/opentelemetry-kotlin)
- [opentelemetry-java*](https://github.com/orgs/open-telemetry/repositories?q=java)

<style>
ul {
  font-size: 1.5rem;
}
</style>

---
class: generous-left-pad generous-top-pad bright-subtitle double-text
---

# About this talk

<span class="android-green">What we are going to cover today:</span>

* A little background
* How OTel facilitates RUM
* Features of OTel Android
* <span class="android-green">The DSL</span>
* Live demo!
* What lies ahead...
* <span class="android-green">Q&A</span>

---
class: generous-left-pad2 generous-top-pad bright-subtitle double-text
---

# Assumptions

<img
  src="./scream.png"
  alt="The Scream by Edvard Munch"
  class="absolute right-8 top-8 h-72 w-auto rounded shadow-lg"
/>
<br/>

* Basic knowledge of o11y signals
  * <span class="text-lg">traces, logs, events, metrics</span>
* You have heard of OpenTelemetry
  * <span class="text-lg">tracer, span processor, sampler, etc</span>
* You might have used <span class="android-green">Android</span>

---
class: generous-left-pad2 generous-top-pad bright-subtitle double-text
---

# Background

<!-- Image credit: My Reading Vintage, https://myreadingvintage.com/cdn/shop/articles/books_with_history.png -->
<img
  src="./books_with_history.png"
  alt="Books with history"
  class="absolute right-8 top-8 rounded shadow-lg"
  style="height: 10.40625rem; width: auto"
/>
<br/>

* originally [splunk-otel-android](https://github.com/signalfx/splunk-otel-android)
* offered to OTel in [March 2023](https://github.com/open-telemetry/community/issues/1400)
  * accepted in July 2023
* supported by Elastic and Embrace
  * (and many others!)

---
class: generous-left-pad2 generous-top-pad bright-subtitle double-text
---

# Even Moar Background
<br/>

* primarily java at the time
* limited instrumentation
* only emitted Spans
  * predates <span class="android-green">OTel</span> events
* very limited demo app
* bespoke, wild semconv

---
class: generous-left-pad2 generous-top-pad bright-subtitle double-text
---

# <span class="android-green leading-normal">How does OTel Android facilitate RUM?</span>

* RUM -> "real user monitoring"
* nomenclature: RUM vs. DEM
* UX enhancements
* Deeper understanding of user interactions.

---
class: generous-left-pad2 generous-top-pad bright-subtitle double-text
---

# <span class="android-green leading-normal">How does OTel Android facilitate RUM?</span>

* troubleshooting in production (lol)
* correlate user actions to backend 
* spans for network, activity, fragment
* events for most things (click, crash, etc)

---
class: generous-left-pad2 generous-top-pad bright-subtitle double-text
---
# <span class="android-green leading-normal">Features of OTel Android</span>

<!-- Image credit: Kotlin logo by JetBrains, via Wikimedia Commons (public domain): https://commons.wikimedia.org/wiki/File:Kotlin_Icon.png -->
<img
  src="./kotlin_icon.png"
  alt="Kotlin logo"
  class="absolute right-8 top-8 h-36 w-auto rounded shadow-lg"
/>

* Kotlin
  * JVM language
  * standard for Android development
  * some use of [`otel-kotlin`](https://github.com/open-telemetry/opentelemetry-kotlin)

---
class: generous-left-pad2 generous-top-pad bright-subtitle double-text
---

# <span class="android-green leading-normal">Features of OTel Android</span>

* Lots of instrumentation
  * clicks, crashes, screens, network, ...
* Some build-time instrumentation
  * http clients

---
class: generous-left-pad2 generous-top-pad bright-subtitle double-text
---

# <span class="android-green leading-normal">Features of OTel Android</span>

* Session management
* Simplified event api
* Semantic conventions
* OTel Demo app
* Telemetry disk buffering

---
class: generous-left-pad2 generous-top-pad bright-subtitle double-text
---
# <span class="android-green leading-normal">Features of OTel Android</span>

* Creating an OTel SDK instance
  * autoconfigure 🤔
  * super configurable and complex!
* (currently the otel-java SDK)

---
class: text-center centered-hero
---

# 1.0.0<br/><span class="android-green">Jan 2026</span>

---
class: text-center centered-hero
---

# ((stable))<br/><span class="android-green">android-agent</span>

---
class: android-modules-slide
---
<img class="android-modules-image" src="./android_modules.png" alt="Android modules"/>

---
class: text-center centered-hero
---

# <span class="android-green">((also stable))</span><br/><span class="text-lg">agent-api, instrumentation-api, session-api, </span>

---
class: android-modules-slide
---
<img class="android-modules-image" src="./instrumentation_list.png" alt="Android modules"/>


---
class: text-center centered-hero
---

# <span class="android-green">((development))</span><br/><span class="text-lg">instrumentation, semconv, sessions, etc</span>

---
class: text-center centered-hero
---

# THE DSL<br/><span class="android-green">(android-agent)</span>

---
class: generous-left-pad2 generous-top-pad bright-subtitle double-text
---
# What's this <span class="android-green">DSL</span>?

* a Kotlin (D)omain (S)pecific (L)anguage
* Type-safe configuration
* A set of reasonable defaults

---
class: larger-code
---
# Android Agent <span class="android-green">DSL</span> Example

```kotlin
rum = OpenTelemetryRumInitializer.initialize(
        context = this@OtelDemoApplication,
        configuration = {
          httpExport {
            baseUrl = "http://10.0.2.2:4318"
          }
          globalAttributes {
            Attributes.of(stringKey("toolkit"), "jetpack compose")
          }
          ...
        })
```

---
class: larger-code bright-subtitle
---
# Adding to your Application

<!-- Image credit: Market Prospects, https://www.market-prospects.com/storage/images/301-1200x675.jpg -->
<img
  src="./market_prospects_application.jpg"
  alt="Metal gears inside industrial machinery"
  class="absolute right-8 top-8 h-32 w-auto rounded shadow-lg"
/>

&nbsp;

In your `build.gradle.kts`:

```kotlin
implementation(
  platform("io.opentelemetry.android:opentelemetry-android-bom:1.8.1-alpha")
)
implementation("io.opentelemetry.android:android-agent")
```

In your `Application.onCreate()`:

```kotlin
OpenTelemetryRumInitializer.initialize(
    context = this@YourAppClass,
    configuration = {
    ... // all DSL config here
    })
```

---
class: larger-code
---

# Configuring <span class="android-green">exporters</span>

```kotlin
httpExport {
    baseUrl = "http://10.0.2.2:4318"
    headerSupplier { mapOf("user.id" to getUserId()) }
    compression = Compression.GZIP
    clientTls = ClientTlsConnectivity(pk, cert)
    spans {
        compression = Compression.NONE
        fullUrl = "https://example.com:8067/spans"
    }
}
```
---
class: larger-code
---

# Configuring <span class="android-green">disk buffering</span>

```kotlin
diskBuffering {
    maxCacheSize = 5 * 1024*1024
    exportPeriod = 5.minutes
    telemetryStorageDir = File("/data/user/0/com.lazybonez/cache/otlp")
}
```
---
class: larger-code
---

# Configuring <span class="android-green">instrumentation</span>

```kotlin
instrumentations {
  suppressing (
      "unwanted.instrumentation.name",
      "something.unwanted"
  )
  crashReporter {
      enabled(true)
  }
  slowRenderingReporter {
      detectionPollInterval(300.milliseconds)
      enableVerboseDebugLogging()
  }
  screenOrientation {
      enabled(false)
  }
}
```

---
layout: two-cols
title: demo
class: flex items-center justify-center
---

<h1 class="!text-7xl !m-0 font-bold">demo</h1>

::right::

<img src="./demo_app.png" alt="Lazy Bonez Android demo app" class="h-104 max-w-full object-contain" />

---
class: text-center centered-hero bright-subtitle
---
# ...

---
class: text-center centered-hero bright-subtitle
---
# wHy nOt dECLaRaTiVe coNfIG???

<!-- Image source: ImageResizer, https://imageresizer.com/meme-generator/edit/Y-U-no-guy -->
<img
  src="./y_u_no_guy.jpg"
  alt="Y U NO guy meme"
  class="absolute right-8 top-8 h-36 w-auto rounded shadow-lg"
/>

---
class: generous-left-pad2 generous-top-pad bright-subtitle double-text
---
# (Y U NO <span class="android-green">declarative config</span>)

* startup time on mobile
* loading files is mostly verboten
* isn't this DSL kinda declarative?

---
class: generous-left-pad2 generous-top-pad bright-subtitle double-text
---
# "<span class="android-green">It's so limiting</span>"

* intentionally started small
* open to extension
  * PRs welcome!

---
class: generous-left-pad2 generous-top-pad bright-subtitle double-text
---
# What's ahead

* more DSL features being added

---
class: qna-qr-slide
---
# 🤘🏻 THANK YOU / Q&A 🤘🏻

<div class="qna-qr-grid">
  <div class="qna-qr-column">
    <h2>Slides + App</h2>
    <img src="./repo_qr.svg" alt="QR code linking to the kubecon_na_2026 GitHub repository" />
  </div>
  <div class="qna-qr-column">
    <h2>Session feedback</h2>
    <img src="./kubecon_feedback_code.png" alt="KubeCon session feedback QR code for OpenTelemetry in Your Pocket" />
  </div>
</div>

