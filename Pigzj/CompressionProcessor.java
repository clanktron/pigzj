package Pigzj;

import java.util.concurrent.*;
import java.util.zip.*;

public class CompressionProcessor implements Runnable {
    private BlockingQueue<byte[]> inputQueue;
    private BlockingQueue<byte[]> outputQueue;
    private int BLOCK_SIZE = 128*1024;
    private Deflater deflater;
    private CRC32 checksummer;

    public CompressionProcessor(BlockingQueue<byte[]> inputQueue, BlockingQueue<byte[]> outputQueue, CRC32 checksummer) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.deflater = new Deflater(-1, true);
        byte[] dictionary = new byte['\u8000'];
        deflater.setDictionary(dictionary);
        this.checksummer = checksummer;
    }
    public void run() {
        while(true) {
            try {
                byte[] block = getNextBlock();
                synchronized(checksummer) {
                    this.checksummer.update(block);
                }
                this.deflater.setInput(block);
                byte[] compressedBlock = new byte[BLOCK_SIZE];
                this.deflater.deflate(compressedBlock);
                this.outputQueue.put(compressedBlock);
                writeCompressedBlock();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.getStackTrace();
            }
        }
    }

    private byte[] getNextBlock() {
        byte []nextBlock = null;
        try {
            synchronized(System.in) {
                nextBlock = System.in.readNBytes(BLOCK_SIZE);
            }
            this.inputQueue.put(nextBlock);
        }
        catch (Exception e) {
            e.getStackTrace();
        }
        return nextBlock;
    }
    private void writeCompressedBlock() {
        try {
            byte[] compressedBlock = this.outputQueue.take();
            synchronized(System.out) {
                System.out.write(compressedBlock, 0, compressedBlock.length);
            }
        }
        catch (Exception e) {
            e.getStackTrace();
        }
    }
}
