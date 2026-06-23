import { registerPlugin } from '@capacitor/core';
import type { NativeVideoPlugin } from './definitions';

const NativeVideo = registerPlugin<NativeVideoPlugin>('NativeVideo', {
  web: () => import('./web').then((m) => new m.NativeVideoWeb()),
});

export * from './definitions';
export { NativeVideo };
