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

import java.util.concurrent.Flow;

/**
 * Converters to and from Java 9 Flow components.
 * @since 0.1.0
 */
public final class FlowInterop {

    /** Utility class. */
    private FlowInterop() {
        throw new IllegalStateException("No instances!");
    }

    public static <T> Flowable<T> fromFlowPublisher(Flow.Publisher<T> source) {
        if (source instanceof Flowable) {
            return (Flowable<T>)source;
        }
        if (source instanceof org.reactivestreams.Publisher) {
            return Flowable.fromPublisher((org.reactivestreams.Publisher<T>)source);
        }
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> FlowableProcessor<T> fromFlowProcessor(Flow.Processor<T, T> source) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Flow.Publisher<T> toFlowPublisher(org.reactivestreams.Publisher<T> source) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T, R> Flow.Processor<T, R> toFlowProcessor(org.reactivestreams.Processor<T, R> source) {
        // TODO implement
        throw new UnsupportedOperationException();
    }
}
