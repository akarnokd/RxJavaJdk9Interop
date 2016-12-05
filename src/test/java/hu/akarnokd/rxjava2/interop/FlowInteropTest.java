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

import io.reactivex.subscribers.TestSubscriber;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

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
}
