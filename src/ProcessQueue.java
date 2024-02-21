import java.util.concurrent.*;

class ProcessQueue {
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
