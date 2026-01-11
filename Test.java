import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {
    // Atomic counters to track active readers and writers for validation purposes
    static AtomicInteger activeReaders = new AtomicInteger(0);
    static AtomicInteger activeWriters = new AtomicInteger(0);

    public static void main(String[] args) {
        ReadWriteLock rwLock = new ReadWriteLock();
        
        // Configuration: 5 Readers and 2 Writers
        int numReaders = 5;
        int numWriters = 2;

        // Start Reader Threads
        for (int i = 1; i <= numReaders; i++) {
            new Thread(new Reader(rwLock), "Reader-" + i).start();
        }

        // Start Writer Threads
        for (int i = 1; i <= numWriters; i++) {
            new Thread(new Writer(rwLock), "Writer-" + i).start();
        }
    }

    // --- Reader Task ---
    static class Reader implements Runnable {
        private ReadWriteLock lock;
        private Random rand = new Random();
        
        public Reader(ReadWriteLock lock) { 
            this.lock = lock; 
        }

        @Override
        public void run() {
            try {
                while (true) {
                    // Sleep to simulate arrival time 
                    // (Slightly longer delay to give Writers a chance to enter)
                    Thread.sleep(rand.nextInt(200) + 100); 

                    lock.readLock();

                    // --- AUTOMATED INTEGRITY CHECK START ---
                    // Violation Check: If a writer is currently active, this is a CRITICAL ERROR.
                    if (activeWriters.get() > 0) {
                        System.err.println("🔥🔥🔥 ERROR! Reader entered while Writer was active! 🔥🔥🔥");
                        System.exit(1);
                    }
                    activeReaders.incrementAndGet();
                    // ---------------------------------------

                    System.out.println(Thread.currentThread().getName() + " is reading... (Active Readers: " + activeReaders.get() + ")");
                    Thread.sleep(rand.nextInt(100)); // Simulate reading duration

                    // --- CHECK END ---
                    activeReaders.decrementAndGet();
                    // -----------------

                    lock.readUnLock();
                }
            } catch (InterruptedException e) { 
                e.printStackTrace(); 
            }
        }
    }

    // --- Writer Task ---
    static class Writer implements Runnable {
        private ReadWriteLock lock;
        private Random rand = new Random();
        
        public Writer(ReadWriteLock lock) { 
            this.lock = lock; 
        }

        @Override
        public void run() {
            try {
                while (true) {
                    // Sleep to simulate arrival time
                    Thread.sleep(rand.nextInt(300) + 200);

                    System.out.println(">>> " + Thread.currentThread().getName() + " wants to write...");
                    lock.writeLock();

                    // --- AUTOMATED INTEGRITY CHECK START ---
                    // Violation Check: If any other writer OR any reader is present, this is a CRITICAL ERROR.
                    if (activeWriters.get() > 0 || activeReaders.get() > 0) {
                        System.err.println("🔥🔥🔥 ERROR! Writer entered while others were present! (R:" + activeReaders.get() + ", W:" + activeWriters.get() + ") 🔥🔥🔥");
                        System.exit(1);
                    }
                    activeWriters.incrementAndGet();
                    // ---------------------------------------

                    System.out.println("✅ " + Thread.currentThread().getName() + " IS WRITING (Exclusively!)");
                    Thread.sleep(rand.nextInt(200)); // Simulate writing duration

                    // --- CHECK END ---
                    activeWriters.decrementAndGet();
                    // -----------------

                    lock.writeUnLock();
                    System.out.println("<<< " + Thread.currentThread().getName() + " finished writing.");
                }
            } catch (InterruptedException e) { 
                e.printStackTrace(); 
            }
        }
    }
}