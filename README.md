# Concurrent Sliding Window Counter

A Java library implementing a concurrent sliding window counter. This is useful for accumulating values over a series of windows, such as calculating a rolling sum.

## Conceptual Overview

A sliding window counter is a data structure that maintains an accumulated value over a fixed number of "windows" or "panes". As time progresses and the window slides forward, the oldest window's value is dropped from the accumulation, and a new window becomes active.

This implementation uses a circular array (ring buffer) to efficiently manage the windows. It is designed for concurrent use, making it suitable for multi-threaded environments where multiple threads might be incrementing the counter in the current window.

The key components are:
- **`SlidingWindowCounter(int size)`:** The constructor initializes a counter with a specified number of windows.
- **`add(long value)`:** Adds a value to the current active window and the total accumulator. This operation is thread-safe.
- **`advance()`:** Advances to the next window. When the counter is full, the value of the oldest window is subtracted from the accumulator, and that window is reset to zero. This "slides" the counter forward.
- **`getAccumulator()`:** Returns the total accumulated value across all windows. This is also thread-safe.

This structure is ideal for tracking metrics over a recent period, such as the number of requests in the last minute or the failure rate over the last 100 attempts.

## Basic Usage

Here is a simple example of how to use the `SlidingWindowCounter`:

```java
import com.github.tnewman.concurrentslidingwindow.SlidingWindowCounter;

public class BasicExample {
    public static void main(String[] args) {
        // Create a counter with 5 windows
        SlidingWindowCounter counter = new SlidingWindowCounter(5);

        // Add some values to the current window
        counter.add(10);
        counter.add(20);
        System.out.println("Accumulator: " + counter.getAccumulator()); // Outputs: 30

        // Advance to the next window
        counter.advance();
        counter.add(5);
        System.out.println("Accumulator: " + counter.getAccumulator()); // Outputs: 35
    }
}
```

## Timed Sliding Window

The `advance()` method can be driven by a timer to create a time-based sliding window. This is a powerful pattern for use cases like rate limiters and circuit breakers.

By calling `advance()` at a fixed interval (e.g., every second), you can maintain a rolling count over a specific duration (e.g., the last 60 seconds).

Here's an example of how to set up a timed window using a `ScheduledExecutorService`:

```java
import com.github.tnewman.concurrentslidingwindow.SlidingWindowCounter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimedWindowExample {
    public static void main(String[] args) throws InterruptedException {
        // Create a counter with 60 windows, representing 60 seconds
        SlidingWindowCounter counter = new SlidingWindowCounter(60);

        // Schedule advance() to be called every second
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(counter::advance, 1, 1, TimeUnit.SECONDS);

        // Simulate incoming requests
        for (int i = 0; i < 100; i++) {
            // Each call to add() represents a request
            counter.add(1);
            Thread.sleep(500); // Simulate requests every 500ms
            System.out.println("Total requests in last 60 seconds: " + counter.getAccumulator());
        }

        executor.shutdown();
    }
}
```
