import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.*;

public class Pigzj {
    private static final int BLOCK_SIZE = 128 * 1024;

    public static void main(String[] args) {

        int threads = parseThreads(args);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CRC32 checksummer = new CRC32();
        writeHeader();
        int bytesCompressed = 0;
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
        ProcessQueue processQueue = new ProcessQueue();

        int bytesRead;
        int totalBytesRead = 0;
        while ((bytesRead = in.read(inputBuffer)) != -1) {
            totalBytesRead = totalBytesRead + bytesRead;
            checksummer.update(inputBuffer);
            processQueue.addBlockToCompressionQueue(inputBuffer);
            executor.execute(new CompressTask(processQueue, outputBuffer));
            processQueue.outputCompressedBlock();
            inputBuffer = new byte[BLOCK_SIZE]; // reset input buffer
            outputBuffer = new byte[BLOCK_SIZE]; // reset output buffer
        }
        processQueue.outputCompressedBlock();
        return totalBytesRead;
    }

    private static class Block {
        long index;
        byte[] content;
        public Block(long index, byte[] block) {
            this.index = index;
            this.content = block;
        }
    }

    private static class ProcessQueue {
        BlockingQueue<byte[]> compressionQueue; 
        ConcurrentHashMap<Long, byte[]> outputQueue; 
        volatile long outputBlockIndex;
        volatile long compressedBlockIndex;

        public ProcessQueue() {
            this.outputQueue = new ConcurrentHashMap<>();
            this.compressionQueue = new LinkedBlockingQueue<byte[]>();
            this.compressedBlockIndex = 0 ;
            this.outputBlockIndex = 0 ;
            this.compressionQueue = compressionQueue;
        }

        public Block getNextUncompressedBlock() {
            byte[] uncompressedContent = new byte[0];
            try {
                uncompressedContent = this.compressionQueue.take(); 
            } catch (Exception e) {
                e.getStackTrace();
            }
            Block block = new Block(this.compressedBlockIndex, uncompressedContent);
            this.compressedBlockIndex++;
            return block;
        }

        public void addBlockToOutputQueue(Block block) {
            System.err.println("adding block to outputQueue at index: "+block.index);
            this.outputQueue.put(block.index, block.content);
        }

        public void addBlockToCompressionQueue(byte[] block) {
            try {
                // System.err.println("adding block to compressionQueue");
                this.compressionQueue.put(block);
            } catch (Exception e) {
                e.getStackTrace();
            }
        }

        public synchronized void outputCompressedBlock() {
            if (this.outputQueue.containsKey(this.outputBlockIndex)) {
                byte[] block = this.outputQueue.get(this.outputBlockIndex);
                System.err.println("writing block from outputQueue at index: "+this.outputBlockIndex);
                System.out.write(block, 0, block.length);
                this.outputBlockIndex++;
            } else{
                System.err.println("attempted to write block from outputQueue at index: "+this.outputBlockIndex+"...no block there yet");
            }
        }
    }

    private static class CompressTask implements Runnable {
        private final ProcessQueue processQueue; 
        private final byte[] outputBuffer;

        public CompressTask(ProcessQueue processQueue, byte[] outputBuffer) {
            this.processQueue = processQueue;
            this.outputBuffer = outputBuffer;
        }

        @Override
        public void run() {
            Deflater deflater = new Deflater();
            Block block = this.processQueue.getNextUncompressedBlock();
            deflater.setInput(block.content, 0, block.content.length);
            deflater.finish();
            deflater.deflate(outputBuffer);
            block.content = outputBuffer;
            this.processQueue.addBlockToOutputQueue(block);
            deflater.end();
        }
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

    private static int parseThreads(String[] args) {
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
        return processes;
    }
}
