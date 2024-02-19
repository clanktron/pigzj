import Pigzj.CompressionThread;

import java.util.zip.Deflater;
import java.io.IOException;
// import java.util.logging.*;

public class Pigzj {
    public int Counter = 0;
    public static void main(String []args) {
        Pigzj mainPigzj = new Pigzj();
        int numThreads = mainPigzj.parseThreadCount(args);

        byte[] defaultHeader = new byte[]{31,-117,8,0,0,0,0,0,0,-1};

        byte[] dictionary = new byte['\u8000'];
        byte[] contentSize = new byte[131072];
        byte[] compressedBlock = new byte[131072];

        Deflater newDeflater = new Deflater(-1, true);
        newDeflater.setDictionary(dictionary);
        newDeflater.setInput(contentSize);
        // only call finish on last block of file
        newDeflater.finish();
        int sizeOfCompressedBlock = newDeflater.deflate(compressedBlock);
        // when done with deflater (destructor?)
        newDeflater.end();
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
