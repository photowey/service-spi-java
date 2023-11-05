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
package io.github.photowey.service.spi.vavr.service;

import io.github.photowey.service.spi.vavr.core.annotation.SPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code DefaultTestSPI}
 *
 * @author photowey
 * @date 2023/11/05
 * @since 1.0.0
 */
@SPI(value = "default")
public class DefaultTestSPI implements TestSPI {

    private static final Logger log = LoggerFactory.getLogger(HelloWorldTestSPI.class);

    @Override
    public String sayHello() {
        return "say hello: default!";
    }

    @Override
    public void init() {
        log.info("init the DefaultTestSPI");
    }
}
