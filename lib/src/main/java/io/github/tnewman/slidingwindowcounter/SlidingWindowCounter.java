package io.github.tnewman.slidingwindowcounter;

import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.LongAdder;

/** Implements a sliding window counter. */
public class SlidingWindowCounter {

  private final int size;

  private volatile boolean filled;

  private volatile int current;

  private final AtomicLongArray ringBuffer;

  private final LongAdder accumulator;

  /**
   * Creates a new SlidingWindowCounter with the specified size.
   *
   * @param size the number of buckets to maintain
   */
  public SlidingWindowCounter(final int size) {
    if (size <= 0) {
      throw new IllegalArgumentException("Size must be greater than zero");
    }
    this.size = size;

    this.filled = false;
    this.current = 0;

    this.ringBuffer = new AtomicLongArray(size);
    this.accumulator = new LongAdder();
  }

  /**
   * Adds a number to the sliding window's counter in the current bucket.
   *
   * <p>This method is thread safe.
   *
   * @param number The number to add.
   */
  public void add(final long number) {
    ringBuffer.addAndGet(current, number);
    accumulator.add(number);
  }

  /**
   * Advance the sliding window, which removes the oldest bucket and advances the current bucket to
   * the next one, which will be empty.
   *
   * <p>This method is not thread safe.
   */
  public void advance() {
    final int c = current;
    int next = c + 1;

    if (next >= size) {
      next = 0;
      filled = true;
    }

    if (filled) {
      final long evicted = ringBuffer.getAndSet(next, 0L);
      accumulator.add(-evicted);
    }

    current = next;
  }

  /**
   * Returns the accumulated sum within the sliding window.
   *
   * <p>This method is thread safe; however, the results will not be strongly consistent if other
   * operations are performed concurrently while this method is called.
   *
   * @return The accumulated sum.
   */
  public long getAccumulator() {
    return accumulator.sum();
  }
}
