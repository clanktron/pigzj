import Pigzj.*;
import java.util.zip.*;
import java.util.concurrent.*;
import java.io.IOException;
// import java.util.logging.*;

public class Main {
    public static void main(String []args) {

        // ExecutorService executorService = new Executors.newFixedThreadPool(4);
        // int threadCount = parseThreadCount(args);
        int threadCount = 4;
        BlockingQueue<byte[]> inputQueue = new LinkedBlockingDeque<>(threadCount);
        BlockingQueue<byte[]> outputQueue = new LinkedBlockingDeque<>(threadCount);
        CRC32 crcChecksummer = new CRC32();

        writeHeader();

        // spin up threads
        for (int i = 0; i < threadCount; i++) {
            System.err.println("starting thread "+i+"...");
            new Thread(new CompressionProcessor(inputQueue, outputQueue, crcChecksummer)).start();
        }

        int contentLength = 24;
        writeTrailer(crcChecksummer, contentLength);
        System.err.println("done!");

    }

    private static void writeInt(int value, byte[] buffer, int offset) {
        buffer[offset] = (byte) (value & 0xFF);
        buffer[offset + 1] = (byte) ((value >> 8) & 0xFF);
        buffer[offset + 2] = (byte) ((value >> 16) & 0xFF);
        buffer[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }

    private static void writeHeader() {
        System.err.println("writing header to stdout");
        try {
            byte[] defaultHeader = new byte[]{31,-117,8,0,0,0,0,0,0,-1};
            System.out.write(defaultHeader);
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    // Write CRC32 checksum and uncompressed size to stdout
    private static void writeTrailer(CRC32 crcChecksummer, int contentLength) {
        System.err.println("writing trailer to stdout");
        try {
            byte[] trailer = new byte[8];
            writeInt((int)crcChecksummer.getValue(), trailer, 0);
            writeInt(contentLength, trailer, 4);
            System.out.write(trailer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int parseThreadCount(String []args) {
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
