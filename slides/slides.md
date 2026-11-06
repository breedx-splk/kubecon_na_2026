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
class: text-center
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

# OpenTelemetry<br/>In Your Pocket

### <span class="android-green">A guide to the OpenTelemetry<br/>Android Agent DSL</span> 

Jason Plumb (Splunk)

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
layout: two-cols
---

# Left

chud

::right::

# Right

pud

---
class: generous-left-pad
---
# test

some java for you

why is this not bold

```java
String foo = "love";
doSomething(foo);
```

and this is apparently kotlin

but what about this

```kotlin
thing { 
    doSomething { 
        foo: "bar"
        bar: baz
        beep: execute("it")
    }
}
```

---
---

# test

flim

flam
