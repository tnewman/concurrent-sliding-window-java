package com.github.tnewman.concurrentslidingwindow;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

public class SlidingWindowCounter {

  private final int size;

  private final AtomicBoolean filled;

  private final AtomicInteger current;

  private final AtomicLongArray ringBuffer;

  private final AtomicLong accumulator;

  public SlidingWindowCounter(final int size) {
    this.size = size;

    this.filled = new AtomicBoolean(false);

    this.current = new AtomicInteger(0);

    this.ringBuffer = new AtomicLongArray(size);
    this.accumulator = new AtomicLong(0);
  }

  public void add(final long number) {
    ringBuffer.addAndGet(current.get(), number);
    accumulator.addAndGet(number);
  }

  public void advance() {
    final int next = (current.get() + 1) % size;

    if (current.get() == size - 1) {
      filled.set(true);
    }

    if (filled.get()) {
      final long evicted = ringBuffer.getAndSet(next, 0);
      accumulator.addAndGet(-evicted);
    }

    current.set(next);
  }

  public long getAccumulator() {
    return accumulator.get();
  }
}
