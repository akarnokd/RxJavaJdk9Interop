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
 * Wraps a Flow.Subscriber and relays the events of an RS Subscriber.
 * @since 0.1.0
 */
final class RsToFlowSubscriber<T> implements org.reactivestreams.Subscriber<T>, Flow.Subscription {

    final Flow.Subscriber<? super T> actual;

    org.reactivestreams.Subscription s;

    RsToFlowSubscriber(Flow.Subscriber<? super T> actual) {
        this.actual = actual;
    }

    @Override
    public void onSubscribe(org.reactivestreams.Subscription s) {
        this.s = s;
        actual.onSubscribe(this);
    }

    @Override
    public void onNext(T t) {
        actual.onNext(t);
    }

    @Override
    public void onError(Throwable t) {
        actual.onError(t);
    }

    @Override
    public void onComplete() {
        actual.onComplete();
    }

    @Override
    public void request(long l) {
        s.request(l);
    }

    @Override
    public void cancel() {
        s.cancel();
    }
}
