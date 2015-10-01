import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SoundGenerator {

    public static void main(String[] args) throws LineUnavailableException {

        // sound(800, 500, 0.1);
        // sound(1000, 500, 0.1);

        Map<Character, Double[]> freqMap = new HashMap<>();
        // freqMap.put('a', 525D, 5D);
        double[] hzs = { 800, 1200 };
        double[] hzs2 = { 800, 1500 };
        new SoundGenerator().multiplePlay(hzs, 5.5, 0.2);
        new SoundGenerator().multiplePlay(hzs, 5.5, 0.2);

    }

    public void multiplePlay(double[] hzs, double duration, double amplitude) {
        amplitude = amplitude / hzs.length;
        int N = (int) (SAMPLE_RATE * duration);
        double sum;
        for (int i = 0; i <= N; i++) {
            sum = 0;
            for (int j = 0; j < hzs.length; j++)
                sum += amplitude * Math.sin(2 * Math.PI * i * hzs[j] / SAMPLE_RATE);
            this.play(sum);
        }
    }

    public void play(double[] input) {
        for (int i = 0; i < input.length; i++) {
            play(input[i]);
        }
    }

    /**
     * Write one sample (between -1.0 and +1.0) to standard audio. If the sample
     * is outside the range, it will be clipped.
     */
    public void play(double in) {

        // clip if outside [-1, +1]
        if (in < -1.0)
            in = -1.0;
        if (in > +1.0)
            in = +1.0;

        // convert to bytes
        short s = (short) (MAX_16_BIT * in);
        buffer[bufferSize++] = (byte) s;
        buffer[bufferSize++] = (byte) (s >> 8); // little Endian

        // send to sound card if buffer is full
        if (bufferSize >= buffer.length) {
            line.write(buffer, 0, buffer.length);
            bufferSize = 0;
        }
    }

    /**
     * Close standard audio.
     */
    public void close() {
        line.drain();
        line.stop();
    }

    /**
     * The sample rate - 44,100 Hz for CD quality audio.
     */
    public final int SAMPLE_RATE = 44100;

    private final int BYTES_PER_SAMPLE = 2; // 16-bit audio
    private final int BITS_PER_SAMPLE = 16; // 16-bit audio
    private final double MAX_16_BIT = Short.MAX_VALUE; // 32,767
    private final int SAMPLE_BUFFER_SIZE = 4096;

    private SourceDataLine line; // to play the sound
    private byte[] buffer; // our internal buffer
    private int bufferSize = 0; // number of samples currently in internal
                                // buffer

    // initializer
    {
        init();
    }

    // open up an audio stream
    private void init() {
        try {
            // 44,100 samples per second, 16-bit audio, mono, signed PCM, little
            // Endian
            AudioFormat format = new AudioFormat((float) SAMPLE_RATE, BITS_PER_SAMPLE, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format, SAMPLE_BUFFER_SIZE * BYTES_PER_SAMPLE);

            // the internal buffer is a fraction of the actual buffer size, this
            // choice is arbitrary
            // it gets divided because we can't expect the buffered data to line
            // up exactly with when
            // the sound card decides to push out its samples.
            buffer = new byte[SAMPLE_BUFFER_SIZE * BYTES_PER_SAMPLE / 3];
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        // no sound gets made before this call
        line.start();
    }
}
