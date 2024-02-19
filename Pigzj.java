import Pigzj.CompressionThread;

import java.util.zip.*;
import java.io.IOException;
// import java.util.logging.*;

public class Pigzj {
    public static void main(String []args) {

        Deflater deflater = new Deflater(-1, true);
        CRC32 crcChecksummer = new CRC32();
        byte[] defaultHeader = new byte[]{31,-117,8,0,0,0,0,0,0,-1};
        byte[] dictionary = new byte['\u8000'];

        deflater.setDictionary(dictionary);

        try {
            // Read uncompressed data from stdin
            byte[] content = System.in.readAllBytes();
            System.err.println("Size of uncompressed file in bytes: " + content.length);
            
            // Update CRC32 checksum on uncompressed data
            crcChecksummer.update(content);

            // Write default GZIP header to stdout
            System.out.write(defaultHeader);

            // Compress data and write to stdout
            deflater.setInput(content);
            deflater.finish();
            int bufferSize = 128 * 1024;
            byte[] buffer = new byte[bufferSize];
            int compressedBytes;
            while ((compressedBytes = deflater.deflate(buffer)) > 0) {
                System.out.write(buffer, 0, compressedBytes);
            }

            // Write CRC32 checksum and uncompressed size to stdout
            byte[] trailer = new byte[8];
            writeInt((int)crcChecksummer.getValue(), trailer, 0);
            writeInt(content.length, trailer, 4);
            System.out.write(trailer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            deflater.end();
        }
    }

    private static void writeInt(int value, byte[] buffer, int offset) {
        buffer[offset] = (byte) (value & 0xFF);
        buffer[offset + 1] = (byte) ((value >> 8) & 0xFF);
        buffer[offset + 2] = (byte) ((value >> 16) & 0xFF);
        buffer[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }

    private int parseThreadCount(String []args) {
        int numThreads = 1;
        // Check if the flag -p is present
        boolean flagP = false;
        String pFlagValue = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-p")) {
                flagP = true;
                // Check if there is an argument after -p
                if (i + 1 < args.length) {
                    pFlagValue = args[i + 1];
                }
                break;
            }
        }
        // Check if the flag -p was found and if a value was provided
        if (flagP) {
            if (pFlagValue != null) {
                System.err.println("-p flag is present with value: " + pFlagValue);
            } else {
                System.err.println("-p flag is present but no value provided");
                System.exit(1);
            }
        } else {
            numThreads = Runtime.getRuntime().availableProcessors();
            if (numThreads < 1) {
                numThreads = 1;
            }
            System.err.println("The number of threads to be used is: " + numThreads);
        }
        return numThreads;
    }
}

/// NOTE:
// First block does not need a dictionary.
// Finish should be called on the last block only
// You might need to call deflate with Deflater.SYNC_FLUSH flag
