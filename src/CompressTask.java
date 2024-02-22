import java.util.zip.*;
import java.util.concurrent.*;

class CompressTask implements Runnable {
    private static final int BLOCK_SIZE = 128 * 1024;
    private final ProcessQueue processQueue; 
    private final byte[] dictionary;
    private final byte[] outputBuffer;

    public CompressTask(ProcessQueue processQueue) {
        this.processQueue = processQueue;
        this.dictionary = new byte['\u8000'];
        this.outputBuffer = new byte[BLOCK_SIZE];
    }

    public void run() {
        Deflater deflater = new Deflater(-1, true);
        deflater.setDictionary(this.dictionary);
        Block block = this.processQueue.getNextUncompressedBlock();
        deflater.setInput(block.content);
        deflater.finish();
        int compressedSize = deflater.deflate(outputBuffer);
        // Resize buffer to exact compressed size
        byte[] compressedBuffer = new byte[compressedSize];
        System.arraycopy(outputBuffer, 0, compressedBuffer, 0, compressedSize);
        block.content = compressedBuffer;
        this.processQueue.addBlockToOutputQueue(block);
        deflater.end();
    }
}
