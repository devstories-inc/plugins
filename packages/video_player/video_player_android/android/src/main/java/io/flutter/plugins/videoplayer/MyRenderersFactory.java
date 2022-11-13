package io.flutter.plugins.videoplayer;

import android.content.Context;
import android.os.Handler;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.AudioSink;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;

import java.util.ArrayList;


public class MyRenderersFactory extends DefaultRenderersFactory {

    private final double centerFrq;

    /**
     * @param context A {@link Context}.
     * @param centerFrq
     */
    public MyRenderersFactory(Context context, double centerFrq) {
        super(context);
        this.centerFrq = centerFrq;
    }

    @Override
    protected void buildAudioRenderers(Context context, int extensionRendererMode, MediaCodecSelector mediaCodecSelector, boolean enableDecoderFallback, AudioSink audioSink, Handler eventHandler, AudioRendererEventListener eventListener, ArrayList<Renderer> out) {

        // AudioProcessor[] audioProcessors = { new SonicAudioProcessor() };
        AudioProcessor[] audioProcessors = { new MyAudioProcessor(centerFrq) };

        MediaCodecAudioRenderer audioRenderer =
                new MediaCodecAudioRenderer(
                        context,
                        mediaCodecSelector,
                        eventHandler,
                        eventListener,
                        AudioCapabilities.DEFAULT_AUDIO_CAPABILITIES,
                        audioProcessors);

        out.add(audioRenderer);

        super.buildAudioRenderers(context, extensionRendererMode, mediaCodecSelector, enableDecoderFallback, audioSink, eventHandler, eventListener, out);

    }

}
