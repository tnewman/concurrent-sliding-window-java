package com.github.tnewman.slidingwindowcounter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class SlidingWindowCounterBenchmark {

  private final SlidingWindowCounter counter = new SlidingWindowCounter(60);

  @Benchmark
  @Threads(Threads.MAX)
  public void benchmarkAdd() {
    counter.add(1L);
  }

  @Benchmark
  @Threads(Threads.MAX)
  public long benchmarkSum() {
    return counter.getAccumulator();
  }

  @Benchmark
  @Threads(1) // Advance is not thread-safe
  public void benchmarkAdvance() {
    counter.advance();
  }

  @State(Scope.Group)
  public static class CombinedState {
    final SlidingWindowCounter counter = new SlidingWindowCounter(60);
    private ScheduledExecutorService scheduler;

    @Setup(Level.Trial)
    public void setup() {
      scheduler = Executors.newSingleThreadScheduledExecutor();
      scheduler.scheduleAtFixedRate(counter::advance, 100L, 100L, TimeUnit.MILLISECONDS);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
      if (scheduler != null) {
        scheduler.shutdownNow();
      }
    }
  }

  @Benchmark
  @Group("combined")
  @GroupThreads(3)
  public void combinedAdd(CombinedState state) {
    state.counter.add(1L);
  }

  @Benchmark
  @Group("combined")
  @GroupThreads(1)
  public long combinedSum(CombinedState state) {
    return state.counter.getAccumulator();
  }
}
