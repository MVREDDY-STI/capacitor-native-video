# capacitor-native-video

Full-screen, hardware-accelerated video overlay for Capacitor kiosk apps, backed
by **ExoPlayer (androidx.media3)**.

## Why

The stock Android WebView (Chromium ~66 on the px30 board) only decodes **H.264
Baseline/Main** in `<video>`. HEVC/H.265 and high-profile clips fail with
`DECODER_ERROR_NOT_SUPPORTED`. ExoPlayer uses the device's **hardware MediaCodec**
decoder, so it plays HEVC and H.264 smoothly, with low CPU and no memory spike.

The plugin adds a `PlayerView` above the WebView in the Activity's content view.
On web (no native overlay) `play()` rejects so callers fall back to `<video>`.

## API

```ts
import { NativeVideo } from 'capacitor-native-video';

await NativeVideo.play({
  url: fileUrl,          // file://, content://, http(s)://, or a Capacitor _capacitor_file_ URL
  fit: 'cover',          // 'cover' (crop) | 'contain' (letterbox) | 'fill' (stretch)
  loop: true,            // default true
  muted: false,          // default false
});

await NativeVideo.stop();
```

## Install (host app)

1. Add the dependency (already added to `newtontouch-lcd/package.json`):
   ```json
   "capacitor-native-video": "file:../capacitor-native-video"
   ```
2. From the host app:
   ```bash
   npm install
   npx cap sync android
   ```
3. Rebuild the Android app. `npx cap sync` discovers the plugin via the
   `capacitor.android.src` field and links the Gradle module + Kotlin source.

No JS import is required to register it — the LCD app calls
`registerPlugin('NativeVideo')` and detects availability with
`Capacitor.isPluginAvailable('NativeVideo')`, falling back to the WebView
`<video>` when the native plugin isn't present (browser/relay).

## Notes

- `minSdk 21`, media3 `1.3.1`.
- The Android plugin is written in Kotlin and applies `kotlin-android`.
- The plugin declares `androidx.appcompat:appcompat` as `compileOnly` because
  Capacitor's Android `Plugin.activity` API is typed as `AppCompatActivity`.
  Without AppCompat on the plugin compile classpath, Gradle fails with:
  `Cannot access class 'androidx.appcompat.app.AppCompatActivity'` and
  unresolved `runOnUiThread` / `findViewById` references.
- `@OptIn(UnstableApi::class)` is used for `PlayerView`/`AspectRatioFrameLayout`;
  if a future media3 version moves these, adjust the import.
- The overlay sits above the WebView, so call `stop()` when leaving the media
  page or switching to an image (the LCD `media.component` already does this).

## Troubleshooting

### `:capacitor-native-video:compileDebugKotlin FAILED`

If the error mentions `AppCompatActivity`, make sure this plugin's
`android/build.gradle` contains:

```gradle
compileOnly "androidx.appcompat:appcompat:1.6.1"
```

Then reinstall/sync from the host app:

```bash
npm install
npx cap sync android
npx cap run android
```

### `ExoPlaybackException: Source error` / `FileNotFoundException`

If ExoPlayer reports a missing `/files/ntimg/<id>.mp4`, the native plugin is
working but the media file was not available on disk when playback started.
Deploy must send media chunks first, wait for the LCD receiver to verify
`ntimg/index.json` and exact file sizes, and only then apply `layout.json`.

The plugin also rejects unresolved `ntimg:<id>` refs and missing `file://` paths
early so the app can fall back or show a useful error instead of failing later in
the Media3 playback thread.
