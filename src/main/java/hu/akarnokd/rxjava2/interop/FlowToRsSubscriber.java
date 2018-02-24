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

package hu.akarnokd.rxjava2.interop;

import java.util.concurrent.Flow;

/**
 * Wraps an RS Subscriber and relays the events of a Flow.Subscriber.
 * @since 0.1.0
 */
final class FlowToRsSubscriber<T> implements Flow.Subscriber<T>, org.reactivestreams.Subscription {

    final org.reactivestreams.Subscriber<? super T> actual;

    Flow.Subscription s;

    FlowToRsSubscriber(org.reactivestreams.Subscriber<? super T> actual) {
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
        actual.onError(throwable);
    }

    @Override
    public void onComplete() {
        actual.onComplete();
    }

    @Override
    public void request(long n) {
        s.request(n);
    }

    @Override
    public void cancel() {
        s.cancel();
    }
}
