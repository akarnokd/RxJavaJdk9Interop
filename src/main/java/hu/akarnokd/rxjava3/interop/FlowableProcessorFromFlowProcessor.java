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

package hu.akarnokd.rxjava3.interop;

import io.reactivex.processors.FlowableProcessor;
import org.reactivestreams.Subscriber;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wraps a Flow.Processor and exposes it as a FlowableProcessor.
 * @since 0.1.0
 */
final class FlowableProcessorFromFlowProcessor<T> extends FlowableProcessor<T> {

    final Flow.Processor<T, T> actual;

    volatile int subscribers;

    static final VarHandle SUBSCRIBERS = VH.find(MethodHandles.lookup(), FlowableProcessorFromFlowProcessor.class, "subscribers", Integer.TYPE);;

    volatile boolean done;
    Throwable error;

    FlowableProcessorFromFlowProcessor(Flow.Processor<T, T> actual) {
        this.actual = actual;
    }

    @Override
    public boolean hasSubscribers() {
        return subscribers != 0;
    }

    @Override
    public boolean hasThrowable() {
        return done && error != null;
    }

    @Override
    public boolean hasComplete() {
        return done && error == null;
    }

    @Override
    public Throwable getThrowable() {
        return done ? error : null;
    }

    @Override
    protected void subscribeActual(org.reactivestreams.Subscriber<? super T> s) {
        SUBSCRIBERS.getAndAdd(this, 1);
        actual.subscribe(new FlowSubscriber(s));
    }

    @Override
    public void onSubscribe(org.reactivestreams.Subscription s) {
        actual.onSubscribe(new RsToFlowSubscription(s));
    }

    @Override
    public void onNext(T t) {
        actual.onNext(t);
    }

    @Override
    public void onError(Throwable t) {
        error = t;
        done = true;
        actual.onError(t);
    }

    @Override
    public void onComplete() {
        done = true;
        actual.onComplete();
    }

    void decrement() {
        SUBSCRIBERS.getAndAdd(this, -1);
    }

    final class FlowSubscriber extends AtomicBoolean implements Flow.Subscriber<T>, org.reactivestreams.Subscription {

        private static final long serialVersionUID = 3807666270718714448L;

        final org.reactivestreams.Subscriber<? super T> actual;

        Flow.Subscription s;

        FlowSubscriber(Subscriber<? super T> actual) {
            this.actual = actual;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.s = subscription;
            actual.onSubscribe(this);
        }

        @Override
        public void onNext(T t) {
            actual.onNext(t);
        }

        @Override
        public void onError(Throwable throwable) {
            if (compareAndSet(false, true)) {
                decrement();
            }
            actual.onError(throwable);
        }

        @Override
        public void onComplete() {
            if (compareAndSet(false, true)) {
                decrement();
            }
            actual.onComplete();
        }

        @Override
        public void request(long n) {
            s.request(n);
        }

        @Override
        public void cancel() {
            if (compareAndSet(false, true)) {
                decrement();
                s.cancel();
            }
        }
    }
}
