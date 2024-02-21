import java.util.zip.*;
import java.util.concurrent.*;

class CompressTask implements Runnable {
    private final ProcessQueue processQueue; 
    private final byte[] outputBuffer;

    public CompressTask(ProcessQueue processQueue, byte[] outputBuffer) {
        this.processQueue = processQueue;
        this.outputBuffer = outputBuffer;
    }

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
