/*
 * Copyright 2026-Present Datadog, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datadoghq.reggie.benchmark;

import com.datadoghq.reggie.runtime.ReggieMatcher;
import com.datadoghq.reggie.runtime.RuntimeCompiler;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

/**
 * JMH benchmarks for split(String) and split(String, int limit) — PR #53. Compares Reggie's split
 * API against JDK Pattern.split and RE2J across three limit modes (0, positive, negative) and three
 * representative delimiter patterns.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class SplitBenchmark {

  // Simple literal delimiter — CSV parsing
  private static final String CSV_PATTERN = ",";
  private static final String CSV_INPUT = "alpha,bravo,charlie,delta,echo";
  private static final String CSV_WITH_TRAILING = "alpha,bravo,charlie,";

  // Whitespace delimiter — log/text tokenisation
  private static final String WS_PATTERN = "\\s+";
  private static final String WS_INPUT = "hello world foo bar baz qux";

  // Digit run delimiter — structured text with numeric separators
  private static final String DIGIT_PATTERN = "\\d+";
  private static final String DIGIT_INPUT = "foo123bar456baz789qux";

  // JDK patterns
  private Pattern jdkCsv;
  private Pattern jdkWs;
  private Pattern jdkDigit;

  // RE2J patterns
  private com.google.re2j.Pattern re2jCsv;
  private com.google.re2j.Pattern re2jWs;
  private com.google.re2j.Pattern re2jDigit;

  // Reggie matchers
  private ReggieMatcher reggieCsv;
  private ReggieMatcher reggieWs;
  private ReggieMatcher reggieDigit;

  @Setup
  public void setup() {
    jdkCsv = Pattern.compile(CSV_PATTERN);
    jdkWs = Pattern.compile(WS_PATTERN);
    jdkDigit = Pattern.compile(DIGIT_PATTERN);

    re2jCsv = com.google.re2j.Pattern.compile(CSV_PATTERN);
    re2jWs = com.google.re2j.Pattern.compile(WS_PATTERN);
    re2jDigit = com.google.re2j.Pattern.compile(DIGIT_PATTERN);

    reggieCsv = RuntimeCompiler.compile(CSV_PATTERN);
    reggieWs = RuntimeCompiler.compile(WS_PATTERN);
    reggieDigit = RuntimeCompiler.compile(DIGIT_PATTERN);
  }

  // ===== split(input) — default, equivalent to limit=0 =====

  @Benchmark
  public void reggieCsvSplit(Blackhole bh) {
    bh.consume(reggieCsv.split(CSV_INPUT));
  }

  @Benchmark
  public void jdkCsvSplit(Blackhole bh) {
    bh.consume(jdkCsv.split(CSV_INPUT));
  }

  @Benchmark
  public void re2jCsvSplit(Blackhole bh) {
    bh.consume(re2jCsv.split(CSV_INPUT));
  }

  // ===== split(input, 0) — all parts, trailing empties discarded =====

  @Benchmark
  public void reggieCsvSplitLimit0(Blackhole bh) {
    bh.consume(reggieCsv.split(CSV_WITH_TRAILING, 0));
  }

  @Benchmark
  public void jdkCsvSplitLimit0(Blackhole bh) {
    bh.consume(jdkCsv.split(CSV_WITH_TRAILING, 0));
  }

  @Benchmark
  public void re2jCsvSplitLimit0(Blackhole bh) {
    bh.consume(re2jCsv.split(CSV_WITH_TRAILING, 0));
  }

  // ===== split(input, positive) — at most N parts, remainder in last =====

  @Benchmark
  public void reggieCsvSplitLimit2(Blackhole bh) {
    bh.consume(reggieCsv.split(CSV_INPUT, 2));
  }

  @Benchmark
  public void jdkCsvSplitLimit2(Blackhole bh) {
    bh.consume(jdkCsv.split(CSV_INPUT, 2));
  }

  @Benchmark
  public void re2jCsvSplitLimit2(Blackhole bh) {
    bh.consume(re2jCsv.split(CSV_INPUT, 2));
  }

  // ===== split(input, -1) — all parts, trailing empties retained =====

  @Benchmark
  public void reggieCsvSplitLimitNeg1(Blackhole bh) {
    bh.consume(reggieCsv.split(CSV_WITH_TRAILING, -1));
  }

  @Benchmark
  public void jdkCsvSplitLimitNeg1(Blackhole bh) {
    bh.consume(jdkCsv.split(CSV_WITH_TRAILING, -1));
  }

  @Benchmark
  public void re2jCsvSplitLimitNeg1(Blackhole bh) {
    bh.consume(re2jCsv.split(CSV_WITH_TRAILING, -1));
  }

  // ===== Whitespace delimiter =====

  @Benchmark
  public void reggieWsSplit(Blackhole bh) {
    bh.consume(reggieWs.split(WS_INPUT));
  }

  @Benchmark
  public void jdkWsSplit(Blackhole bh) {
    bh.consume(jdkWs.split(WS_INPUT));
  }

  @Benchmark
  public void re2jWsSplit(Blackhole bh) {
    bh.consume(re2jWs.split(WS_INPUT));
  }

  @Benchmark
  public void reggieWsSplitLimit2(Blackhole bh) {
    bh.consume(reggieWs.split(WS_INPUT, 2));
  }

  @Benchmark
  public void jdkWsSplitLimit2(Blackhole bh) {
    bh.consume(jdkWs.split(WS_INPUT, 2));
  }

  @Benchmark
  public void re2jWsSplitLimit2(Blackhole bh) {
    bh.consume(re2jWs.split(WS_INPUT, 2));
  }

  // ===== Digit-run delimiter =====

  @Benchmark
  public void reggieDigitSplit(Blackhole bh) {
    bh.consume(reggieDigit.split(DIGIT_INPUT));
  }

  @Benchmark
  public void jdkDigitSplit(Blackhole bh) {
    bh.consume(jdkDigit.split(DIGIT_INPUT));
  }

  @Benchmark
  public void re2jDigitSplit(Blackhole bh) {
    bh.consume(re2jDigit.split(DIGIT_INPUT));
  }
}
