# Lazy Bonez 💀

## Local plot generation model

Plotify uses **Gemma 3 1B IT, INT4** through LiteRT-LM for local plot generation. The model is not
bundled into the APK. Download the `gemma3-1b-it-int4.litertlm` artifact from the
[LiteRT Community model page](https://huggingface.co/litert-community/Gemma3-1B-IT); Hugging Face
requires signing in and accepting the Gemma model terms before downloading it.

The app looks for this exact file under its app-specific external files directory:

```text
Android/data/com.lazybonez.app/files/models/gemma3-1b-it-int4.litertlm
```

From this project directory, after installing the app on the emulator or phone, sideload the model
with:

```sh
adb shell mkdir -p /sdcard/Android/data/com.lazybonez.app/files/models
adb push gemma3-1b-it-int4.litertlm /sdcard/Android/data/com.lazybonez.app/files/models/
```

The app uses LiteRT-LM `0.19.0` with its CPU backend for this first pass. On Plotify, spin and stop
the reels; the plot generates automatically when the third reel locks. If you copied the model after
opening Plotify, spin again to check for it. The first generation initializes the model and may take
several seconds.

The `.litertlm` model file is intentionally ignored by Git (`*.litertlm`) and must not be added to
the APK or repository. The emulator is suitable for trying the UI and generation flow; measure
latency and memory on the Pixel 7 for device performance.
