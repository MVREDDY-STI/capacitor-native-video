package com.solum.nativevideo

import android.graphics.Color
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin

/**
 * Full-screen hardware-accelerated video overlay for the kiosk.
 *
 * The WebView's <video> (Chromium 66 on the px30 board) only decodes H.264
 * Baseline/Main — HEVC and high-profile clips fail. ExoPlayer/MediaCodec uses
 * the device's HARDWARE decoder, so it plays HEVC + H.264 smoothly with low CPU.
 * The PlayerView is added above the WebView in the Activity's content view.
 */
@OptIn(UnstableApi::class)
@CapacitorPlugin(name = "NativeVideo")
class NativeVideoPlugin : Plugin() {

  private var player: ExoPlayer? = null
  private var view: PlayerView? = null

  @PluginMethod
  fun play(call: PluginCall) {
    val url = call.getString("url")
    if (url.isNullOrBlank()) { call.reject("url is required"); return }
    val fit = call.getString("fit") ?: "cover"
    val loop = call.getBoolean("loop", true) ?: true
    val muted = call.getBoolean("muted", false) ?: false
    val uri = resolveUri(url)

    activity.runOnUiThread {
      try {
        ensureView()
        val p = player ?: return@runOnUiThread
        view?.resizeMode = when (fit) {
          "contain" -> AspectRatioFrameLayout.RESIZE_MODE_FIT
          "fill"    -> AspectRatioFrameLayout.RESIZE_MODE_FILL
          else      -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM   // cover
        }
        p.volume = if (muted) 0f else 1f
        p.repeatMode = if (loop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        p.setMediaItem(MediaItem.fromUri(uri))
        p.prepare()
        p.playWhenReady = true
        call.resolve()
      } catch (e: Exception) {
        call.reject("play failed: ${e.message}")
      }
    }
  }

  @PluginMethod
  fun stop(call: PluginCall) {
    activity.runOnUiThread { teardown(); call.resolve() }
  }

  override fun handleOnDestroy() {
    try { activity?.runOnUiThread { teardown() } } catch (_: Exception) {}
  }

  private fun ensureView() {
    if (player != null && view != null) return
    val exo = ExoPlayer.Builder(context).build()
    val pv = PlayerView(context)
    pv.useController = false
    pv.setBackgroundColor(Color.BLACK)
    pv.player = exo
    val root = activity.findViewById<ViewGroup>(android.R.id.content)
    root.addView(
      pv,
      FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT
      )
    )
    player = exo
    view = pv
  }

  private fun teardown() {
    view?.let { v ->
      (v.parent as? ViewGroup)?.removeView(v)
      v.player = null
    }
    player?.release()
    view = null
    player = null
  }

  /** Capacitor's convertFileSrc gives `https://localhost/_capacitor_file_/<abs path>?v=…`.
   *  ExoPlayer wants the real file path; everything else passes through. */
  private fun resolveUri(url: String): Uri {
    val marker = "_capacitor_file_"
    if (url.contains(marker)) {
      val path = url.substringAfter(marker).substringBefore("?")
      return Uri.parse("file://$path")
    }
    return Uri.parse(url)
  }
}
