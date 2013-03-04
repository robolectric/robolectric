package org.robolectric.bytecode;

import org.junit.Assert;
import org.junit.Test;

public class ClassCacheTest {


    @Test
    public void fixForCorberturaAndSonarCodeCoverage() throws InterruptedException {
        final ClassCache classCache = new ZipClassCache("target/test.txt", AndroidTranslator.CACHE_VERSION);
        
        // Substitute this LOCK with your monitor (could be you object you are
        // testing etc.)
        Thread locker = new Thread() {
            @Override
            public void run() {
                synchronized (classCache) {
                    try {
                        Thread.sleep(Long.MAX_VALUE);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        };

        locker.start();

        TestThreadIsWriting attempt = new TestThreadIsWriting(classCache);
        
        try {
            int timeToWait = 500;
            locker.join(timeToWait, 0);

            attempt.start();
            long before = System.currentTimeMillis();
            attempt.join(timeToWait, 0);
            long after = System.currentTimeMillis();
            Assert.assertEquals(false, attempt.ready);
            Assert.assertEquals(1, (after - before) / timeToWait);
            locker.interrupt();
        } finally {
            
        }
    }

    @Test
    public void fixForCorberturaAndSonarCodeCoverageTheOtherWayAround() throws InterruptedException {
        final ZipClassCache classCache = new ZipClassCache("target/test.txt", AndroidTranslator.CACHE_VERSION);
        
        // Substitute this LOCK with your monitor (could be you object you are
        // testing etc.)
        Thread locker = new Thread() {
            @Override
            public void run() {
                synchronized (classCache) {
                    try {
                        Thread.sleep(Long.MAX_VALUE);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        };

        locker.start();

        TestThreadSaveAllClassesToCache attempt = new TestThreadSaveAllClassesToCache(classCache);
        
        try {
            int timeToWait = 500;
            locker.join(timeToWait, 0);

            attempt.start();
            long before = System.currentTimeMillis();
            attempt.join(timeToWait, 0);
            long after = System.currentTimeMillis();
            Assert.assertEquals(false, attempt.ready);
            Assert.assertEquals(1, (after - before) / timeToWait);
            locker.interrupt();
                        
        } finally {
            
        }
    }

    class TestThreadIsWriting extends  Thread {
        public boolean ready = false;
        final ClassCache classCache;
        
        
        public TestThreadIsWriting(ClassCache classCache) {
            super();
            this.classCache = classCache;
        }


        @Override
        public void run() {
            classCache.isWriting();
            ready = true;
        }
    };

    class TestThreadSaveAllClassesToCache extends  Thread {
        public boolean ready = false;
        final ZipClassCache classCache;
        
        
        public TestThreadSaveAllClassesToCache(ZipClassCache classCache) {
            super();
            this.classCache = classCache;
        }


        @Override
        public void run() {
            classCache.saveAllClassesToCache(null, null);
            ready = true;
        }
    };

}
