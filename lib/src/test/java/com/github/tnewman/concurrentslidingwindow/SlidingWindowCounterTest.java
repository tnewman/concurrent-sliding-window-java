package com.github.tnewman.concurrentslidingwindow;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SlidingWindowCounterTest {

  @Test
  void testBasicUsage() {
    SlidingWindowCounter window = new SlidingWindowCounter(5);

    window.add(10);
    window.add(20);

    assertEquals(30, window.getAccumulator());

    window.advance();
    assertEquals(30, window.getAccumulator());
  }

  @ParameterizedTest(name = "test eviction with window size {0}")
  @ValueSource(ints = {2, 3, 5, 10})
  void testEvictionParameterized(int size) {
    SlidingWindowCounter window = new SlidingWindowCounter(size);

    long totalAdded = 0;

    for (int i = 1; i <= size; i++) {
      window.add(i);
      totalAdded += i;
      window.advance();
    }

    long expectedAccumulator = totalAdded - 1;
    assertEquals(
        expectedAccumulator,
        window.getAccumulator(),
        "Accumulator incorrect after first eviction.");

    long newValue = size + 1;
    window.add(newValue);
    expectedAccumulator += newValue;
    assertEquals(
        expectedAccumulator,
        window.getAccumulator(),
        "Accumulator incorrect after adding a new value.");

    window.advance();
    expectedAccumulator -= 2;
    assertEquals(
        expectedAccumulator,
        window.getAccumulator(),
        "Accumulator incorrect after second eviction.");
  }

  @Test
  void testConcurrency() throws InterruptedException {
    int threads = 10;
    int additionsPerThread = 1000;
    SlidingWindowCounter window = new SlidingWindowCounter(5);
    ExecutorService executor = Executors.newFixedThreadPool(threads);
    CountDownLatch latch = new CountDownLatch(threads);

    for (int i = 0; i < threads; i++) {
      executor.submit(
          () -> {
            try {
              for (int j = 0; j < additionsPerThread; j++) {
                window.add(1);
              }
            } finally {
              latch.countDown();
            }
          });
    }

    latch.await(5, TimeUnit.SECONDS);
    assertEquals(threads * additionsPerThread, window.getAccumulator());
    executor.shutdown();
  }
}
