import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.Deflater;

public class Pigzj {
    private static final int BLOCK_SIZE = 128 * 1024;
    private static final int DICT_SIZE = 32 * 1024;

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

        ExecutorService executor = Executors.newFixedThreadPool(processes);

        try {
            compress(System.in, System.out, executor);
        } catch (IOException e) {
            System.err.println("Error: I/O exception occurred - " + e.getMessage());
            System.exit(1);
        }

        executor.shutdown();
    }

    private static void compress(InputStream in, OutputStream out, ExecutorService executor) throws IOException {
        byte[] inputBuffer = new byte[BLOCK_SIZE];
        byte[] outputBuffer = new byte[BLOCK_SIZE];

        int bytesRead;
        while ((bytesRead = in.read(inputBuffer)) != -1) {
            executor.execute(new CompressTask(inputBuffer, bytesRead, outputBuffer, out));
            inputBuffer = new byte[BLOCK_SIZE]; // reset input buffer
            outputBuffer = new byte[BLOCK_SIZE]; // reset output buffer
        }
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
