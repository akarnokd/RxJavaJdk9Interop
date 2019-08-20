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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;

import org.junit.Test;

public class VHTest {

    public static void checkUtilityClass(Class<?> clazz) {
        try {
            Constructor<?> c = clazz.getDeclaredConstructor();
            c.setAccessible(true);
            c.newInstance();

        } catch (Throwable ex) {
            if ((ex.getCause() instanceof IllegalStateException)
                    && ex.getCause().getMessage().equals("No instances!")) {
                return;
            }
            throw new AssertionError("Wrong exception type or message", ex);
        }
        throw new AssertionError("Not an utility class!");
    }

    @Test
    public void utilityClass() {
        checkUtilityClass(VH.class);
    }

    @Test(expected = InternalError.class)
    public void findInvalid() {
        VH.find(MethodHandles.lookup(), VHTest.class, "field", Object.class);
    }

}