export interface NativeVideoPlugin {
  /**
   * Show a full-screen looping video overlay above the WebView, decoded by
   * ExoPlayer/MediaCodec (hardware) — plays HEVC/H.264 the WebView can't.
   * @param options.url  file:// path, content:// URI, http(s) URL, or a
   *                     Capacitor `_capacitor_file_` localhost URL (auto-resolved).
   * @param options.fit  'cover' (crop, default) | 'contain' (letterbox) | 'fill' (stretch).
   * @param options.loop loop forever (default true).
   * @param options.muted mute audio (default false).
   */
  play(options: { url: string; fit?: 'cover' | 'contain' | 'fill'; loop?: boolean; muted?: boolean }): Promise<void>;
  /** Hide and release the overlay. */
  stop(): Promise<void>;
}
