import java.util.zip.*;
import java.util.concurrent.*;

class CompressTask implements Runnable {
    private static final int BLOCK_SIZE = 128 * 1024;
    private final ProcessQueue processQueue; 
    private final byte[] outputBuffer;

    public CompressTask(ProcessQueue processQueue) {
        this.processQueue = processQueue;
        this.outputBuffer = new byte[BLOCK_SIZE];
    }

    public void run() {
        Deflater deflater = new Deflater();
        Block block = this.processQueue.getNextUncompressedBlock();
        deflater.setInput(block.content);
        deflater.finish();
        deflater.deflate(outputBuffer);
        block.content = outputBuffer;
        this.processQueue.addBlockToOutputQueue(block);
        deflater.end();
    }
}
