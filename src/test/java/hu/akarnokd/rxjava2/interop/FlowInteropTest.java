/*
 * Copyright 2016 David Karnok
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

package hu.akarnokd.rxjava2.interop;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

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
}
