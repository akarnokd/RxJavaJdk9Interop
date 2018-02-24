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
 * Very basic Flow.Processor.
 * @param <T> the input and output item type
 */
public final class TestFlowProcessor<T> implements Flow.Processor<T, T>, Flow.Subscription {

    Flow.Subscriber<? super T> actual;

    @Override
    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        this.actual = subscriber;
        subscriber.onSubscribe(this);
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(T t) {
        Flow.Subscriber<? super T> a = actual;
        if (a != null) {
            a.onNext(t);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        Flow.Subscriber<? super T> a = actual;
        if (a != null) {
            a.onError(throwable);
        }
    }

    @Override
    public void onComplete() {
        Flow.Subscriber<? super T> a = actual;
        if (a != null) {
            a.onComplete();
        }
    }

    @Override
    public void request(long l) {
        // ignored
    }

    @Override
    public void cancel() {
        actual = null;
    }
}
