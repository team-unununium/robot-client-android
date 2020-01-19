/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.unununium.vrrobot.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.ExoMediaDrm;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.ext.gvr.GvrAudioProcessor;
import com.google.android.exoplayer2.source.ClippingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.concurrent.TimeUnit;

/**
 * Wrapper for ExoPlayer2 functionality. Handles all the necessary setup for video playback and
 * provides access to simple video playback controls. Plays unprotected videos from a specified Uri,
 * and plays DRM video under the Widevine test license.
 */
public class VideoExoPlayer2 implements Player.EventListener {

    private static final String TAG = VideoExoPlayer2.class.getSimpleName();

    private final Context context;
    private final DefaultDataSourceFactory mediaDataSourceFactory;
    private GvrAudioProcessor gvrAudioProcessor;
    private SimpleExoPlayer player;
    private ExoMediaDrm<FrameworkMediaCrypto> mediaDrm;

    public VideoExoPlayer2(Context context) {
        this.context = context;
        String userAgent = Util.getUserAgent(context, "VideoExoPlayer");
        DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(userAgent);
        mediaDataSourceFactory = new DefaultDataSourceFactory(context, httpDataSourceFactory);
    }

    private void play() {
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    private void pause() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    SimpleExoPlayer getPlayer() {
        return player;
    }

    /**
     * Returns the {@link GvrAudioProcessor} the player is using to render spatial audio, or
     * {@code null} if not set. The audio processor is part of the ExoPlayer GVR extension.
     */
    GvrAudioProcessor getGvrAudioProcessor() {
        return gvrAudioProcessor;
    }

    /**
     * Initializes the video player. The player can playback a video at the specified Uri. DRM videos
     * hosted by the Widevine test license can be played
     *
     * <p>Note: this should be called in the host activity's onStart() method.
     *
     * @param uri The uri of the source video to play.
     */
    @SuppressWarnings("ConstantConditions")
    public void initPlayer(Uri uri) {
        DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;

        gvrAudioProcessor = new GvrAudioProcessor();
        player =
                ExoPlayerFactory.newSimpleInstance(
                        context,
                        new GvrRenderersFactory(context, gvrAudioProcessor),
                        new DefaultTrackSelector(),
                        drmSessionManager);
        player.addListener(this);

        // Auto play the video.
        player.setPlayWhenReady(true);

        MediaSource mediaSource = buildMediaSource(uri);
        long lengthMicroseconds = TimeUnit.SECONDS.toMicros(3);
        mediaSource =
                new ClippingMediaSource(
                        mediaSource,
                        /* startPositionUs= */ 0,
                        lengthMicroseconds,
                        /* enableInitialDiscontinuity= */ false,
                        /* allowDynamicClippingUpdates= */ false,
                        /* relativeToDefaultPosition= */ false);
        // Prepare the player with the source.
        player.prepare(mediaSource);
    }

    /**
     * Releases the video player. Once the player is released, the client must call initPlayer()
     * before the video player can be used again.
     *
     * <p>Note: this should be called in the host activity's onStop().
     */
    public void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
            gvrAudioProcessor = null;
            if (mediaDrm != null) {
                mediaDrm.release();
                mediaDrm = null;
            }
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        int type = Util.inferContentType(uri.getLastPathSegment());
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    private static final class GvrRenderersFactory extends DefaultRenderersFactory {

        private final GvrAudioProcessor gvrAudioProcessor;

        private GvrRenderersFactory(Context context, GvrAudioProcessor gvrAudioProcessor) {
            super(context);
            this.gvrAudioProcessor = gvrAudioProcessor;
        }

        @Override
        public AudioProcessor[] buildAudioProcessors() {
            return new AudioProcessor[] {gvrAudioProcessor};
        }

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_ENDED) {
            pause();
            Log.i(TAG, "Total video frames decoded: " + getRenderedOutputBufferCount());
        }
    }

    private int getRenderedOutputBufferCount() {
        DecoderCounters counters = player.getVideoDecoderCounters();
        counters.ensureUpdated();
        return counters.renderedOutputBufferCount;
    }

}

