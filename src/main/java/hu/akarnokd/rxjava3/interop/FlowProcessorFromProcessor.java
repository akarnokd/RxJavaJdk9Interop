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

import org.reactivestreams.Processor;

import java.util.concurrent.Flow;

/**
 * Wraps an RS Processor into a Flow.Processor.
 */
final class FlowProcessorFromProcessor<T, R> implements Flow.Processor<T, R> {

    final org.reactivestreams.Processor<T, R> actual;

    FlowProcessorFromProcessor(Processor<T, R> actual) {
        this.actual = actual;
    }


    @Override
    public void subscribe(Flow.Subscriber<? super R> subscriber) {
        actual.subscribe(new RsToFlowSubscriber<>(subscriber));
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        actual.onSubscribe(new FlowToRsSubscription(subscription));
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
}
