import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.*;

public class Pigzj {
    private static final int BLOCK_SIZE = 128 * 1024;

    public static void main(String[] args) {
        int processes = Runtime.getRuntime().availableProcessors();
        if (args.length > 0) {
            if (args[0].equals("-p") && args.length > 1) {
                try {
                    processes = Integer.parseInt(args[1]);
                    if (processes <= 0 || processes > 4 * Runtime.getRuntime().availableProcessors()) {
                        System.err.println("Error: Invalid number of processes.");
                        System.exit(1);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error: Invalid argument for -p option.");
                    System.exit(1);
                }
            } else {
                System.err.println("Error: Invalid option.");
                System.exit(1);
            }
        }

        writeHeader();
        CRC32 checksummer = new CRC32();
        int bytesCompressed = 0;
        ExecutorService executor = Executors.newFixedThreadPool(processes);
        try {
            bytesCompressed = compress(System.in, System.out, executor, checksummer);
        } catch (IOException e) {
            System.err.println("Error: I/O exception occurred - " + e.getMessage());
            System.exit(1);
        }
        executor.shutdown();
        writeTrailer(checksummer, bytesCompressed);
    }

    private static int compress(InputStream in, OutputStream out, ExecutorService executor, CRC32 checksummer) throws IOException {
        byte[] inputBuffer = new byte[BLOCK_SIZE];
        byte[] outputBuffer = new byte[BLOCK_SIZE];

        int bytesRead;
        int totalBytesRead = 0;
        while ((bytesRead = in.read(inputBuffer)) != -1) {
            checksummer.update(inputBuffer);
            executor.execute(new CompressTask(inputBuffer, bytesRead, outputBuffer, out));
            inputBuffer = new byte[BLOCK_SIZE]; // reset input buffer
            outputBuffer = new byte[BLOCK_SIZE]; // reset output buffer
            totalBytesRead = totalBytesRead + bytesRead;
        }
        return totalBytesRead;
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
        System.err.println("writing trailer to stdout - total bytes compressed: "+contentLength);
        try {
            byte[] trailer = new byte[8];
            writeInt((int)crcChecksummer.getValue(), trailer, 0);
            writeInt(contentLength, trailer, 4);
            System.out.write(trailer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeInt(int value, byte[] buffer, int offset) {
        buffer[offset] = (byte) (value & 0xFF);
        buffer[offset + 1] = (byte) ((value >> 8) & 0xFF);
        buffer[offset + 2] = (byte) ((value >> 16) & 0xFF);
        buffer[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }

    private static class CompressTask implements Runnable {
        private final byte[] inputBuffer;
        private final int bytesRead;
        private final byte[] outputBuffer;
        private final OutputStream out;

        public CompressTask(byte[] inputBuffer, int bytesRead, byte[] outputBuffer, OutputStream out) {
            this.inputBuffer = inputBuffer;
            this.bytesRead = bytesRead;
            this.outputBuffer = outputBuffer;
            this.out = out;
        }

        @Override
        public void run() {
            Deflater deflater = new Deflater();
            deflater.setInput(inputBuffer, 0, bytesRead);
            deflater.finish();
            int compressedSize = deflater.deflate(outputBuffer);
            try {
                out.write(outputBuffer, 0, compressedSize);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                deflater.end();
            }
        }
    }
}
