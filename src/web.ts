import { WebPlugin } from '@capacitor/core';
import type { NativeVideoPlugin } from './definitions';

/** Web stub: there's no native overlay in a browser, so callers must fall back
 *  to a WebView <video>. play() rejects so the caller's catch() does exactly
 *  that; stop() is a no-op. */
export class NativeVideoWeb extends WebPlugin implements NativeVideoPlugin {
  async play(): Promise<void> {
    throw this.unavailable('NativeVideo is not available on web; use a <video> element.');
  }
  async stop(): Promise<void> {
    /* no-op */
  }
}
