# Concurrent Sliding Window Counter

A Java library implementing a concurrent sliding window counter. This is useful for accumulating 
values over a series of windows, such as calculating a rolling sum. It can be combined with a 
timer to create a time-based sliding window.

The library is thread safe and lock-free, supporting high throughput, concurrent use.

## Example
Creates a sliding window with 5 buckets and adds to the counter, advances the sliding 
window and prints the accumulator value every 100 iterations.

```java
import com.github.tnewman.slidingwindowcounter.SlidingWindowCounter;

public class Example {
    public static void main(String[] args) {
        SlidingWindowCounter counter = new SlidingWindowCounter(5);

        for (int i = 0; i < 100; i++) {
            counter.add(i);
            counter.advance();
            System.out.println("Accumulator: " + counter.getAccumulator());
        }
    }
}
```

## Example with Timer
Creates a sliding window over the last 60 seconds with 1 second buckets 
using a timer to advance the window. The counter is continuously updated 
until the thread is interrupted. The count is reported every 60 seconds.

```java
import com.github.tnewman.slidingwindowcounter.SlidingWindowCounter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimedWindowExample {
    public static void main(String[] args) {
        SlidingWindowCounter counter = new SlidingWindowCounter(60);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

        executor.scheduleAtFixedRate(counter::advance, 1, 1, TimeUnit.SECONDS);

        executor.scheduleAtFixedRate(() -> {
            System.out.println("Total requests in last 60 seconds: " + counter.getAccumulator());
        }, 60, 60, TimeUnit.SECONDS);

        while (!Thread.currentThread().isInterrupted()) {
            counter.add(1);
        }
    }
}
```
