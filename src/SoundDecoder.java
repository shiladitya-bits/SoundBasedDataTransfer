import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

public class SoundDecoder {

    public static void main(String[] args) {

        final float sampleRate = 44100.0f;
        final int bitsPerRecord = 16;
        final int channels = 1;
        final boolean bigEndian = true;
        final boolean signed = true;

        ByteArrayOutputStream byteArrayOutputStream;
        TargetDataLine targetDataLine;
        boolean stopCapture = false;
        byte tempBuffer[] = new byte[8000];

        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            stopCapture = false;
            while (!stopCapture) {
                AudioFormat audioFormat = new AudioFormat(sampleRate, bitsPerRecord, channels, signed, bigEndian);
                DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
                targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
                targetDataLine.open(audioFormat);
                targetDataLine.start();
                int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
                byteArrayOutputStream.write(tempBuffer, 0, cnt);
                byte[] bytearr = byteArrayOutputStream.toByteArray();
                double[] dblArr = toDoubleArray(bytearr);

                boolean isPresent = false;
                for (int i = 0; i < dblArr.length; i++) {
                    double freq = dblArr[i];
                    if (freq > 100 && freq < 5000) {
                        isPresent = true;
                        System.out.print(dblArr[i] + ",");
                    }

                }

                if (isPresent) {
                    System.out.println();
                }
                targetDataLine.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // helper method for converting byte array to Double array
    public static double[] toDoubleArray(byte[] byteArray) {
        int times = Double.SIZE / Byte.SIZE;
        double[] doubles = new double[byteArray.length / times];
        for (int i = 0; i < doubles.length; i++) {
            doubles[i] = ByteBuffer.wrap(byteArray, i * times, times).getDouble();
        }
        return doubles;
    }
}
