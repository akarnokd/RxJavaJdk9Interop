/*
 * Copyright 2016-2018 David Karnok
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

package hu.akarnokd.rxjava3.jdk9interop;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.Flow.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.internal.subscriptions.BooleanSubscription;
import io.reactivex.processors.*;
import io.reactivex.subscribers.TestSubscriber;

public class FlowInteropTest {

    @Test
    public void flowToFlowable() {

        SubmissionPublisher<Integer> sp = new SubmissionPublisher<>();

        TestSubscriber<Integer> ts = FlowInterop.fromFlowPublisher(sp)
        .test();

        sp.submit(1);
        sp.submit(2);
        sp.submit(3);
        sp.submit(4);
        sp.submit(5);

        sp.close();

        ts.awaitDone(5, TimeUnit.SECONDS)
                .assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void flowToFlowableError() throws Exception {

        SubmissionPublisher<Integer> sp = new SubmissionPublisher<>();

        TestSubscriber<Integer> ts = FlowInterop.fromFlowPublisher(sp)
                .test();

        sp.submit(1);
        sp.submit(2);
        sp.submit(3);
        sp.submit(4);
        sp.submit(5);

        Thread.sleep(1000); // JDK bug workaround, otherwise no onSubscribe is called

        sp.closeExceptionally(new IOException());


        ts.awaitDone(5, TimeUnit.SECONDS)
                .assertFailure(IOException.class, 1, 2, 3, 4, 5);
    }

    @Test
    public void flowableToFlow() {
        FlowTestSubscriber<Integer> ts = new FlowTestSubscriber<>();

        Flowable.range(1, 5)
                .to(FlowInterop.toFlow())
                .subscribe(ts);

        ts.assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void flowableToFlow2() {
        FlowTestSubscriber<Integer> ts = new FlowTestSubscriber<>();

        FlowInterop.toFlowPublisher(Flowable.range(1, 5))
                .subscribe(ts);

        ts.assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void flowableToFlowError() {
        FlowTestSubscriber<Integer> ts = new FlowTestSubscriber<>();

        Flowable.range(1, 5).concatWith(Flowable.<Integer>error(new IOException()))
                .to(FlowInterop.toFlow())
                .subscribe(ts);

        ts.assertFailure(IOException.class, 1, 2, 3, 4, 5);
    }

    @Test
    public void flowProcessorToFlowableProcessor() {
        TestFlowProcessor<Integer> tfp = new TestFlowProcessor<>();

        FlowableProcessor<Integer> fp = FlowInterop.fromFlowProcessor(tfp);

        assertFalse(fp.hasSubscribers());
        assertFalse(fp.hasComplete());
        assertFalse(fp.hasThrowable());
        assertNull(fp.getThrowable());

        TestSubscriber<Integer> ts = fp.test();

        assertTrue(fp.hasSubscribers());
        assertFalse(fp.hasComplete());
        assertFalse(fp.hasThrowable());
        assertNull(fp.getThrowable());

        fp.onNext(1);
        fp.onNext(2);
        fp.onNext(3);
        fp.onNext(4);
        fp.onNext(5);
        fp.onComplete();

        assertFalse(fp.hasSubscribers());
        assertTrue(fp.hasComplete());
        assertFalse(fp.hasThrowable());
        assertNull(fp.getThrowable());

        ts.assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void flowProcessorToFlowableProcessorTake() {
        TestFlowProcessor<Integer> tfp = new TestFlowProcessor<>();

        FlowableProcessor<Integer> fp = FlowInterop.fromFlowProcessor(tfp);

        assertFalse(fp.hasSubscribers());
        assertFalse(fp.hasComplete());
        assertFalse(fp.hasThrowable());
        assertNull(fp.getThrowable());

        TestSubscriber<Integer> ts = fp.take(3).test();

        assertTrue(fp.hasSubscribers());
        assertFalse(fp.hasComplete());
        assertFalse(fp.hasThrowable());
        assertNull(fp.getThrowable());

        fp.onNext(1);
        fp.onNext(2);
        fp.onNext(3);

        assertFalse(fp.hasSubscribers());
        assertFalse(fp.hasComplete());
        assertFalse(fp.hasThrowable());
        assertNull(fp.getThrowable());

        fp.onNext(4);
        fp.onNext(5);
        fp.onComplete();

        assertFalse(fp.hasSubscribers());
        assertTrue(fp.hasComplete());
        assertFalse(fp.hasThrowable());
        assertNull(fp.getThrowable());

        ts.assertResult(1, 2, 3);
    }

    @Test
    public void flowProcessorToFlowableProcessorError() {
        TestFlowProcessor<Integer> tfp = new TestFlowProcessor<>();

        FlowableProcessor<Integer> fp = FlowInterop.fromFlowProcessor(tfp);

        assertFalse(fp.hasSubscribers());
        assertFalse(fp.hasComplete());
        assertFalse(fp.hasThrowable());
        assertNull(fp.getThrowable());

        TestSubscriber<Integer> ts = fp.test();

        assertTrue(fp.hasSubscribers());
        assertFalse(fp.hasComplete());
        assertFalse(fp.hasThrowable());
        assertNull(fp.getThrowable());

        fp.onNext(1);
        fp.onNext(2);
        fp.onNext(3);
        fp.onNext(4);
        fp.onNext(5);
        fp.onError(new IOException());

        assertFalse(fp.hasSubscribers());
        assertFalse(fp.hasComplete());
        assertTrue(fp.hasThrowable());
        assertNotNull(fp.getThrowable());

        ts.assertFailure(IOException.class, 1, 2, 3, 4, 5);
    }

    @Test
    public void flowableProcessorToFlowProcessor() {
        PublishProcessor<Integer> pp = PublishProcessor.create();

        Flow.Processor<Integer, Integer> fp = FlowInterop.toFlowProcessor(pp);

        FlowTestSubscriber<Integer> ts = new FlowTestSubscriber<>();
        fp.subscribe(ts);

        pp.onNext(1);
        pp.onNext(2);
        pp.onNext(3);
        pp.onNext(4);
        pp.onNext(5);
        pp.onComplete();

        ts.assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void flowableProcessorToFlowProcessorError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();

        Flow.Processor<Integer, Integer> fp = FlowInterop.toFlowProcessor(pp);

        FlowTestSubscriber<Integer> ts = new FlowTestSubscriber<>();
        fp.subscribe(ts);

        pp.onNext(1);
        pp.onNext(2);
        pp.onNext(3);
        pp.onNext(4);
        pp.onNext(5);
        pp.onError(new IOException());

        ts.assertFailure(IOException.class, 1, 2, 3, 4, 5);
    }

    @Test
    public void flowableToFlowPublisherCancel() {
        PublishProcessor<Integer> pp = PublishProcessor.create();

        Flow.Publisher<Integer> fp = FlowInterop.toFlowPublisher(pp);

        FlowTestSubscriber<Integer> fts = new FlowTestSubscriber<>();

        fp.subscribe(fts);

        assertTrue(pp.hasSubscribers());

        fts.cancel();

        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void flowPublisherToFlowableCancel() {
        SubmissionPublisher<Integer> sp = new SubmissionPublisher<>(Runnable::run, 32);

        Flowable<Integer> f = FlowInterop.fromFlowPublisher(sp);

        TestSubscriber<Integer> ts = f.test();

        assertEquals(1, sp.getNumberOfSubscribers());

        ts.cancel();

        assertEquals(0, sp.getNumberOfSubscribers());
    }

    @Test
    public void utilityClass() {
        VHTest.checkUtilityClass(FlowInterop.class);
    }

    @Test
    public void flowProcessorToFlowableProcessorOnSubscribe() {
        TestFlowProcessor<Integer> tfp = new TestFlowProcessor<>();

        FlowableProcessor<Integer> fp = FlowInterop.fromFlowProcessor(tfp);

        BooleanSubscription bs1 = new BooleanSubscription();

        fp.onSubscribe(bs1);

        assertFalse(bs1.isCancelled());

        BooleanSubscription bs2 = new BooleanSubscription();

        fp.onSubscribe(bs2);

        assertFalse(bs1.isCancelled());
        assertFalse(bs2.isCancelled());

    }

    @Test
    public void flowProcessorToFlowableProcessorDecrementOnce() {
        AtomicReference<Flow.Subscriber<? super Integer>> sub = new AtomicReference<>();

        Flow.Processor<Integer, Integer> proc = new Flow.Processor<Integer, Integer>() {

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onNext(Integer item) {
            }

            @Override
            public void onSubscribe(Subscription subscription) {
            }

            @Override
            public void subscribe(Subscriber<? super Integer> subscriber) {
                subscriber.onSubscribe(new RsToFlowSubscription(new BooleanSubscription()));
                sub.set(subscriber);
            }
        };

        FlowableProcessor<Integer> fp = FlowInterop.fromFlowProcessor(proc);

        assertFalse(fp.hasSubscribers());

        AtomicReference<org.reactivestreams.Subscription> subscr = new AtomicReference<>();

        fp.subscribe(new FlowableSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onSubscribe(org.reactivestreams.Subscription s) {
                subscr.set(s);
            }
        });

        assertTrue(fp.hasSubscribers());

        subscr.get().cancel();

        assertFalse(fp.hasSubscribers());

        sub.get().onError(new IOException());
        sub.get().onComplete();
        subscr.get().cancel();

        assertFalse(fp.hasSubscribers());
    }

    static final class FlowFlowable<T> extends Flowable<T> implements Flow.Publisher<T> {
        @Override
        public void subscribe(Subscriber<? super T> subscriber) {
            subscribeActual(new RsToFlowSubscriber<T>(subscriber));
        }

        @Override
        protected void subscribeActual(org.reactivestreams.Subscriber<? super T> s) {
            s.onSubscribe(new BooleanSubscription());
            s.onComplete();
        }
    }


    static final class FlowRsPublisher<T> implements org.reactivestreams.Publisher<T>, Flow.Publisher<T> {
        @Override
        public void subscribe(Subscriber<? super T> subscriber) {
            subscribe(new RsToFlowSubscriber<T>(subscriber));
        }

        @Override
        public void subscribe(org.reactivestreams.Subscriber<? super T> s) {
            s.onSubscribe(new BooleanSubscription());
            s.onComplete();
        }
    }

    @Test
    public void fromFlowPublisherAlreadyFlowable() {
        FlowFlowable<Integer> ff = new FlowFlowable<>();

        Flowable<Integer> f = FlowInterop.fromFlowPublisher(ff);

        assertSame(f, ff);

        f.test().assertResult();
    }

    @Test
    public void fromFlowPublisherAlreadyRsPublisher() {
        FlowRsPublisher<Integer> ff = new FlowRsPublisher<>();

        Flowable<Integer> f = FlowInterop.fromFlowPublisher(ff);

        f.test().assertResult();
    }

    @Test
    public void fromFlowablePublisherAlreadyFlowPublisher() {
        FlowRsPublisher<Integer> ff = new FlowRsPublisher<>();

        Flow.Publisher<Integer> f = FlowInterop.toFlowPublisher(ff);

        assertSame(f, ff);

        FlowTestSubscriber<Integer> fts = new FlowTestSubscriber<>();
        f.subscribe(fts);

        fts.assertResult();
    }

    static final class FlowFlowableProcessor<T> extends FlowableProcessor<T>
    implements Flow.Processor<T, T> {

        @Override
        public void onComplete() {
        }

        @Override
        public void onError(Throwable arg0) {
        }

        @Override
        public void onNext(T arg0) {
        }

        @Override
        public void onSubscribe(Subscription arg0) {
        }

        @Override
        public void subscribe(Subscriber<? super T> arg0) {
        }

        @Override
        public void onSubscribe(org.reactivestreams.Subscription s) {
        }

        @Override
        public boolean hasSubscribers() {
            return false;
        }

        @Override
        public boolean hasThrowable() {
            return false;
        }

        @Override
        public boolean hasComplete() {
            return false;
        }

        @Override
        public Throwable getThrowable() {
            return null;
        }

        @Override
        protected void subscribeActual(org.reactivestreams.Subscriber<? super T> s) {
        }
    }

    @Test
    public void fromFlowProcessorAlreadyFlowableProcessor() {
        FlowFlowableProcessor<Integer> ffp = new FlowFlowableProcessor<>();

        FlowableProcessor<Integer> fp = FlowInterop.fromFlowProcessor(ffp);

        assertSame(fp, ffp);
    }

    @Test
    public void fromRsProcessorAlreadyFlowProcessor() {
        FlowFlowableProcessor<Integer> ffp = new FlowFlowableProcessor<>();

        Flow.Processor<Integer, Integer> fp = FlowInterop.toFlowProcessor(ffp);

        assertSame(fp, ffp);
    }

    @Test
    public void asFlowProcessorNormal() {
        Flow.Processor<Integer, Integer> fp = FlowInterop.toFlowProcessor(PublishProcessor.create());

        fp.onSubscribe(new RsToFlowSubscription(new BooleanSubscription()));

        FlowTestSubscriber<Integer> fts = new FlowTestSubscriber<>();

        fp.subscribe(fts);

        fp.onNext(1);
        fp.onComplete();

        fts.assertResult(1);
    }


    @Test
    public void asFlowProcessorError() {
        Flow.Processor<Integer, Integer> fp = FlowInterop.toFlowProcessor(PublishProcessor.create());

        fp.onSubscribe(new RsToFlowSubscription(new BooleanSubscription()));

        FlowTestSubscriber<Integer> fts = new FlowTestSubscriber<>();

        fp.subscribe(fts);

        fp.onError(new IOException());

        fts.assertFailure(IOException.class);
    }
}
