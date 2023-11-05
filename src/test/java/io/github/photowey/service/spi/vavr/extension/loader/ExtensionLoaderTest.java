/*
 * Copyright © 2023 the original author or authors.
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
package io.github.photowey.service.spi.vavr.extension.loader;

import io.github.photowey.service.spi.vavr.extension.factory.ExtensionLoaderFactory;
import io.github.photowey.service.spi.vavr.service.TestSPI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * {@code ExtensionLoaderTest}
 *
 * @author photowey
 * @date 2023/11/05
 * @since 1.0.0
 */
class ExtensionLoaderTest {

    @Test
    public void testCreateSPI() {
        ExtensionLoader<TestSPI> loader = ExtensionLoaderFactory.create(TestSPI.class);
        TestSPI helloworldSPI = loader.load("helloworld", ClassLoader.getSystemClassLoader());
        Assertions.assertNotNull(helloworldSPI);

        TestSPI defaultSPI = loader.load("default", ClassLoader.getSystemClassLoader());
        Assertions.assertNotNull(defaultSPI);

        TestSPI notFoundSPI = loader.load("notFoundSPI", ClassLoader.getSystemClassLoader());
        Assertions.assertNull(notFoundSPI);
    }
}