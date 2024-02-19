package Pigzj;

import java.util.zip.Deflater;

public class CompressionThread extends Thread {
    private byte[] dictionary;
    private byte[] content;
    public void run() {
        System.out.println("Compressing a 128KiB section...");
    }
    public void CompressionThread() {
        this.dictionary = new byte['\u8000'];
        int contentSize = 128*1024;
        this.content = new byte[contentSize];
    }
    public Deflater setupDeflater() {
        Deflater newDeflater = new Deflater(-1, true);
        newDeflater.setDictionary(this.dictionary);
        newDeflater.setInput(this.content);
        return newDeflater;
    }

}
