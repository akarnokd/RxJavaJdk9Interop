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
