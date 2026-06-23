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
- `@OptIn(UnstableApi::class)` is used for `PlayerView`/`AspectRatioFrameLayout`;
  if a future media3 version moves these, adjust the import.
- The overlay sits above the WebView, so call `stop()` when leaving the media
  page or switching to an image (the LCD `media.component` already does this).
