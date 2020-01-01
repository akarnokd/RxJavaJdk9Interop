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

import java.util.Objects;
import java.util.concurrent.Flow;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.FlowableProcessor;

/**
 * Converters to and from Java 9 Flow components.
 * @since 0.1.0
 */
public final class FlowInterop {

    /** Utility class. */
    private FlowInterop() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * Wraps a Flow.Publisher into a Flowable.
     * @param source the source Flow.Publisher, not null
     * @param <T> the value type
     * @return the new Flowable instance
     * @throws NullPointerException if source is null
     */
    @SuppressWarnings("unchecked")
    public static <T> Flowable<T> fromFlowPublisher(Flow.Publisher<T> source) {
        if (source instanceof Flowable) {
            return (Flowable<T>)source;
        }
        if (source instanceof org.reactivestreams.Publisher) {
            return Flowable.fromPublisher((org.reactivestreams.Publisher<T>)source);
        }
        Objects.requireNonNull(source, "source is null");
        return RxJavaPlugins.onAssembly(new FlowableFromFlowPublisher<>(source));
    }

    /**
     * Converter function from a Flowable into a Publisher.
     * @param <T> the value type
     * @return the converter function instance to be used with {@code Flowable.to()}.
     */
    public static <T> FlowableConverter<T, Flow.Publisher<T>> toFlow() {
        return FlowFromPublisher::new;
    }

    /**
     * Wraps a Flow.Processor (identity) into a FlowableProcessor.
     * @param source the source Flow.Processor, not null
     * @param <T> the input and output type of the Flow.Processor
     * @return the new FlowableProcessor instance
     * @throws  NullPointerException if source is null
     */
    @SuppressWarnings("unchecked")
    public static <T> FlowableProcessor<T> fromFlowProcessor(Flow.Processor<T, T> source) {
        if (source instanceof FlowableProcessor) {
            return (FlowableProcessor<T>)source;
        }
        Objects.requireNonNull(source, "source is null");
        return new FlowableProcessorFromFlowProcessor<>(source);
    }

    /**
     * Wraps an RS Publisher into a Flow.Publisher.
     * @param source the source RS Publisher instance, not null
     * @param <T> the value type
     * @return the new Flow.Publisher instance
     * @throws NullPointerException if source is null
     */
    @SuppressWarnings("unchecked")
    public static <T> Flow.Publisher<T> toFlowPublisher(org.reactivestreams.Publisher<T> source) {
        if (source instanceof Flow.Publisher) {
            return (Flow.Publisher<T>)source;
        }
        Objects.requireNonNull(source, "source is null");
        return new FlowFromPublisher<>(source);
    }

    /**
     * Wraps an RS Processor into a Flow.Processor.
     * @param source the source RS Processor
     * @param <T> the input value type
     * @param <R> the output value type
     * @return the new Flow.Processor instance
     * @throws NullPointerException if source is null
     */
    @SuppressWarnings("unchecked")
    public static <T, R> Flow.Processor<T, R> toFlowProcessor(org.reactivestreams.Processor<T, R> source) {
        if (source instanceof Flow.Processor) {
            return (Flow.Processor<T, R>)source;
        }
        Objects.requireNonNull(source, "source is null");
        return new FlowProcessorFromProcessor<>(source);
    }
}
