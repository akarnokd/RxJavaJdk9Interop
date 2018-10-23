# RxJava2Jdk9Interop

<a href='https://travis-ci.org/akarnokd/RxJava2Jdk9Interop/builds'><img src='https://travis-ci.org/akarnokd/RxJava2Jdk9Interop.svg?branch=master'></a>
[![codecov.io](http://codecov.io/github/akarnokd/RxJava2Jdk9Interop/coverage.svg?branch=master)](http://codecov.io/github/akarnokd/RxJava2Jdk9Interop?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.akarnokd/rxjava2-jdk9-interop/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.akarnokd/rxjava2-jdk9-interop)

RxJava 2 interop library for supporting Java 9 features such as `Flow.*`

# Release

```groovy
compile 'com.github.akarnokd:rxjava2-jdk9-interop:0.2.3'
```

# Examples

Converting from RxJava 2 to Java 9 Flow

```java

Flow.Publisher<Integer> pub = Flowable.range(1, 5)
    .to(FlowInterop.toFlow());

// --------

Flow.Processor<Integer, Integer> proc = FlowInterop
    .toFlowProcessor(PublishProcessor.create()); 

```

Converting from Java 9 Flow to RxJava 2

```java

SubmissionPublisher<Integer> sp = new SubmissionPublisher<>();

Flowable<Integer> f = FlowInterop
    .fromFlowPublisher(sp);

// --------

Flow.Processor<Integer, Integer> fp = ...

FlowableProcessor<Integer> fproc = FlowInterop
    .fromFlowProcessor(fp);

```

Note that RxJava 2 `FlowableProcessor`s don't support different input and output types
therefore the Flow.Processor should have the same type arguments.

For convenience, there is a `FlowTestSubscriber` that extends `TestSubscriber` and
allows asserting on a Flow.Publisher the same way as with Reactive-Streams Publisher types.

```java
FlowTestSubscriber<Integer> ts = new FlowTestSubscriber<>();

SubmissionPublisher<Integer> sp = new SubmissionPublisher<>();

sp.subscribe(ts);

sp.onNext(1);
sp.onNext(2);
sp.onNext(3);
sp.onNext(4);
sp.onNext(5);
sp.close();

ts.awaitDone(5, TimeUnit.SECONDS) // SubmissionPublisher is async by default
  .assertResult(1, 2, 3, 4, 5);
```
