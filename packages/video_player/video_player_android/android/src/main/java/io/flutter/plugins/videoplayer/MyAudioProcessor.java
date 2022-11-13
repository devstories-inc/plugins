package io.flutter.plugins.videoplayer;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.audio.BaseAudioProcessor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.InvalidMarkException;

import uk.me.berndporr.iirj.Butterworth;

public class MyAudioProcessor extends BaseAudioProcessor {

    private final double centerFrq;
    private boolean active;

    public MyAudioProcessor(double centerFrq) {
        this.centerFrq = centerFrq;
    }


    @Override
    protected AudioFormat onConfigure(AudioFormat inputAudioFormat) throws UnhandledAudioFormatException {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_8BIT && inputAudioFormat.encoding != C.ENCODING_PCM_16BIT
                && inputAudioFormat.encoding != C.ENCODING_PCM_24BIT && inputAudioFormat.encoding != C.ENCODING_PCM_32BIT
        ) {
            throw new UnhandledAudioFormatException(inputAudioFormat);
        }

        return inputAudioFormat;
    }

    @Override
    public boolean isActive() {
        return super.isActive();
    }

    @Override
    public void queueInput(ByteBuffer inputBuffer) {
        int channelCount = inputAudioFormat.channelCount;
        int sampleRate = inputAudioFormat.sampleRate;
        int outputChannelCount = outputAudioFormat.channelCount;

        int remaining = inputBuffer.remaining();
        if (remaining == 0) {
            return;
        }

        int position = inputBuffer.position();
        int limit = inputBuffer.limit();
        int frameCount = (limit - position) / (2 * channelCount);
        int outputSize = frameCount * outputChannelCount * 2;
        ByteBuffer buffer = replaceOutputBuffer(outputSize);

        // System.out.println("buffer.position() : " + buffer.position());

        // ByteBuffer arr0 = new ByteBuffer(inputBuffer.array().length);
        ByteBuffer clone = deepCopy(inputBuffer);
        // clone.position(inputBuffer.position());
        // clone.limit(inputBuffer.limit());
        // clone.order(inputBuffer.order());

        // System.out.println("po0 : " + inputBuffer.position() + ", po1 : " + clone.position());
        // System.out.println("po0 : " + inputBuffer.limit() + ", po1 : " + clone.limit());
        // System.out.println("po0 : " + inputBuffer.remaining() + ", po1 : " + clone.remaining());

        // byte[] arr0 = inputBuffer.array();
        // byte[] arr1 = clone.array();
        // byte[] arr2 = clone.array();

        /*
        AudioCalculator audioCalculator = new AudioCalculator(arr);
        double frequency = audioCalculator.getFrequency();
        double decibel = audioCalculator.getDecibel();
        double amplitude = audioCalculator.getAmplitude(arr);

        System.out.println("frequency : " + frequency + ", decibel : " + decibel + ", amplitude : " + amplitude);

        if (frequency < 100) {
            // return;
        }
        */

        int highCutoff = 500;
        int lowCutoff = 5000;

        double centreFreq = (highCutoff + lowCutoff) / 2.0;
        double width = Math.abs(highCutoff - lowCutoff);

        centreFreq = this.centerFrq;
        width = 600;

        Butterworth butterworth = new Butterworth();
        butterworth.bandStop(1, sampleRate, centreFreq, width);
        // butterworth.bandPass(1, sampleRate, centreFreq, width);
        // butterworth.bandPass(2, 32000, 2000, 32000);
        // butterworth.bandPass(3, 32000, 2000, 32000);
        // butterworth.bandPass(4, 32000, 2000, 32000);
        butterworth.highPass(2, sampleRate,0.00000000000000000000000000001);

        for(int i = position; i < limit; i++) {
            byte before = inputBuffer.get(i);

            double filtered = butterworth.filter((double) before);
//
//            if ((double) before != filtered) {
//                // System.out.println("before : " + before + ", filtered : " + filtered);
//            }
//
//            if(filtered < 0) {
//                filtered = 0;
//            }

            // System.out.println("filtered : " + filtered);

            // buffer.put(before);
            buffer.put((byte) filtered);

            // System.out.println("before : " + before);

            // double filtered = butterworth.filter(before);

            // System.out.println("before : " + before + ", filtered : " + filtered);

            // arr2[i] = (byte) filtered;

            // buffer.put((byte) filtered);
        }


        // System.out.println(arr);

        // ByteBuffer bf = toByteBuffer(arr1);

        /*
        try {
            buffer.position(0);
            buffer.put(arr1);
        } catch (BufferOverflowException e) {
            e.printStackTrace();
            System.out.println(arr0.length + ", arr1 : " + arr1.length);
        }
        */

        // byte[] buf = doubleToByteArray(arr);

        inputBuffer.position(inputBuffer.limit());
        buffer.flip();

    }

    public static ByteBuffer deepCopy( ByteBuffer orig )
    {
        int pos = orig.position(), lim = orig.limit();
        try
        {
            orig.position(0).limit(orig.capacity()); // set range to entire buffer
            ByteBuffer toReturn = deepCopyVisible(orig); // deep copy range
            toReturn.position(pos).limit(lim); // set range to original
            return toReturn;
        }
        finally // do in finally in case something goes wrong we don't bork the orig
        {
            orig.position(pos).limit(lim); // restore original
        }
    }

    public static ByteBuffer deepCopyVisible( ByteBuffer orig )
    {
        int pos = orig.position();
        try
        {
            ByteBuffer toReturn;
            // try to maintain implementation to keep performance
            if( orig.isDirect() )
                toReturn = ByteBuffer.allocateDirect(orig.remaining());
            else
                toReturn = ByteBuffer.allocate(orig.remaining());

            toReturn.put(orig);
            toReturn.order(orig.order());

            return (ByteBuffer) toReturn.position(0);
        }
        finally
        {
            orig.position(pos);
        }
    }

    public static ByteBuffer cloneByteBuffer(ByteBuffer original)
    {
        //Get position, limit, and mark
        int pos = original.position();
        int limit = original.limit();
        int mark = -1;
        try
        {
            original.reset();
            mark = original.position();
        }
        catch (InvalidMarkException e)
        {
            e.printStackTrace();
            //This happens when the original's mark is -1, so leave mark at default value of -1
        }

        //Create clone with matching capacity and byte order
        ByteBuffer clone = (original.isDirect()) ? ByteBuffer.allocateDirect(original.capacity()) : ByteBuffer.allocate(original.capacity());
        clone.order(original.order());

        //Copy FULL buffer contents, including the "out-of-bounds" part
        original.limit(original.capacity());
        original.position(0);
        clone.put(original);

        //Set mark of both buffers to what it was originally
        if (mark != -1)
        {
            original.position(mark);
            original.mark();

            clone.position(mark);
            clone.mark();
        }

        //Set position and limit of both buffers to what they were originally
        original.position(pos);
        original.limit(limit);
        clone.position(pos);
        clone.limit(limit);

        return clone;
    }

    public final static int copy(final ByteBuffer from, final ByteBuffer to) {
        final int len = from.limit();
        return copy(from, 0, to, 0, len);
    }

    public final static int copy(final ByteBuffer from, final int offset1,
                                 final ByteBuffer to, final int offset2, final int len) {
        System.arraycopy(from.array(), offset1, to.array(), offset2, len);
        to.limit(offset2 + len);
        return len;
    }

    public static ByteBuffer clone(ByteBuffer original) {
        ByteBuffer clone = ByteBuffer.allocate(original.capacity());
        original.rewind();//copy from the beginning
        clone.put(original);
        original.rewind();
        clone.flip();
        return clone;
    }

    public static double toDouble(byte[] bytes) {
        System.out.println(bytes.length);
        return ByteBuffer.wrap(bytes).getDouble();
    }

    private byte[] doubleToByteArray ( final double i ) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeDouble(i);
        dos.flush();
        return bos.toByteArray();
    }

    private ByteBuffer toByteBuffer(byte[] array) {
        return ByteBuffer.wrap(array);
    }
}
