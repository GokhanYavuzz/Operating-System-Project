import java.util.concurrent.Semaphore;

public class ReadWriteLock {
    // Semaphore 'S' controls write access and blocks writers when readers are present.
    // It acts as the main lock for the shared resource (database).
    private Semaphore S = new Semaphore(1);
    
    // 'mutex' ensures thread safety for the readCount variable.
    // It prevents race conditions when multiple readers try to enter/exit simultaneously.
    private Semaphore mutex = new Semaphore(1); 
    
    private int readCount = 0; // Tracks the number of active readers

    // Method to acquire the read lock (startRead)
    public void readLock() {
        try {
            mutex.acquire();         // Lock access to readCount to ensure safety
            readCount++;             // Increment the number of active readers
            if (readCount == 1) {    
                S.acquire();         // If this is the FIRST reader, lock the database (block writers)
            }
            mutex.release();         // Release access to readCount
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Method to release the read lock (endRead)
    public void readUnLock() {
        try {
            mutex.acquire();         // Lock access to readCount to ensure safety
            readCount--;             // Decrement the number of active readers
            if (readCount == 0) {
                S.release();         // If this is the LAST reader, unlock the database (allow writers)
            }
            mutex.release();         // Release access to readCount
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Method to acquire the write lock (startWrite)
    public void writeLock() {
        try {
            S.acquire();             // Request write access. Blocks if any readers or another writer are present.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Method to release the write lock (endWrite)
    public void writeUnLock() {
        S.release();                 // Release write access, allowing others to enter.
    }
}