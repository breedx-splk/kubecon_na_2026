---
theme: uncover
class: invert
paginate: true
---
<style>
  :root {
    /* --color-background: #272424; */
    /* --color-background-code: #ccc; */
    --color-background-paginate: rgba(128, 128, 128, 0.05);
    --color-foreground: rgb(235, 239, 124);
    --color-highlight: #99c;
    --color-highlight-hover: #aaf;
    --color-highlight-heading: #99c;
    --color-header: #bbb;
    --color-header-shadow: transparent;
  }
</style>

# OpenTelemetry<br/>In Your Pocket

### A guide to the OpenTelemetry<br/>Android Agent DSL

Jason Plumb (Splunk)


----

# About Me

![width:200px](profile_pic_480.png)

- [breedx-splk](https://github.com/breedx-splk) (GitHub)
- [opentelemetry-android](https://github.com/open-telemetry/opentelemetry-android)
- [opentelemetry-kotlin](https://github.com/open-telemetry/opentelemetry-kotlin)
- [opentelemetry-java*](https://github.com/orgs/open-telemetry/repositories?q=java)

---

test

```kotlin
thing { 
    doSomething { 
        foo: bar
        bar: baz
    }
}
```