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

import java.util.concurrent.Flow;

/**
 * Wraps and converts an RS Publisher into a Flow.Publisher.
 * @since 0.1.0
 */
final class FlowFromPublisher<T> implements Flow.Publisher<T> {

    final org.reactivestreams.Publisher<T> source;

    FlowFromPublisher(org.reactivestreams.Publisher<T> source) {
        this.source = source;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super T> subscriber) {

    }
}
