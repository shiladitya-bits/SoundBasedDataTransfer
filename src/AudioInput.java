import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class AudioInput {

    TargetDataLine microphone;

    final int audioFrames = 4096; // power ^ 2

    final float sampleRate = 8000.0f;
    final int bitsPerRecord = 16;
    final int channels = 1;
    final boolean bigEndian = false;
    final boolean signed = true;

    byte byteData[]; // length=audioFrames * 2
    double doubleData[]; // length=audioFrames only reals needed for apache lib.
    AudioFormat format;
    FastFourierTransformer transformer;

    public AudioInput() {

        byteData = new byte[audioFrames * 2]; // two bytes per audio frame, 16 bits

        // doubleData= new double[audioFrames * 2]; // real & imaginary
        doubleData = new double[audioFrames]; // only real for apache

        transformer = new FastFourierTransformer(DftNormalization.STANDARD);

        System.out.print("Microphone initialization\n");
        format = new AudioFormat(sampleRate, bitsPerRecord, channels, signed, bigEndian);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); // format is an AudioFormat object

        if (!AudioSystem.isLineSupported(info)) {
            System.err.print("isLineSupported failed");
            System.exit(1);
        }

        try {
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            System.out.print("Microphone opened with format: " + format.toString() + "\n");
            microphone.start();
        } catch (Exception ex) {
            System.out.println("Microphone failed: " + ex.getMessage());
            System.exit(1);
        }

    }

    public int readPcm() {
        int numBytesRead = microphone.read(byteData, 0, byteData.length);
        if (numBytesRead != byteData.length) {
            System.out.println("Warning: read less bytes than buffer size");
            System.exit(1);
        }
        return numBytesRead;
    }

    public void byteToDouble() {
        ByteBuffer buf = ByteBuffer.wrap(byteData);
        buf.order(ByteOrder.BIG_ENDIAN);
        int i = 0;

        while (buf.remaining() > 2) {
            short s = buf.getShort();
            doubleData[i] = (new Short(s)).doubleValue();
            ++i;
        }
        // System.out.println("Parsed "+i+" doubles from "+byteData.length+" bytes");
    }

    public void findFrequency() {
        double frequency;
        Complex[] cmplx = transformer.transform(doubleData, TransformType.FORWARD);
        double real;
        double im;
        double mag[] = new double[cmplx.length];

        for (int i = 0; i < cmplx.length; i++) {
            real = cmplx[i].getReal();
            im = cmplx[i].getImaginary();
            mag[i] = Math.sqrt((real * real) + (im * im));
        }

        double peak = -1.0;
        int index = -1;
        for (int i = 0; i < cmplx.length; i++) {
            if (peak < mag[i]) {
                index = i;
                peak = mag[i];
            }
        }
        frequency = (sampleRate * index) / audioFrames;
        if (frequency > 700 && frequency < 1600) {
            System.out.print("Index: " + index + ", Frequency: " + frequency + "\n");
        }

    }

    public void printFreqs() {
        for (int i = 0; i < audioFrames / 4; i++) {
            System.out.println("bin " + i + ", freq: " + (sampleRate * i) / audioFrames);
        }
    }

    public static void main(String[] args) {
        AudioInput ai = new AudioInput();
        int turns = 10000;
        while (turns-- > 0) {
            ai.readPcm();
            ai.byteToDouble();
            ai.findFrequency();
        }

        // ai.printFreqs();
    }
}
