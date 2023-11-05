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
package io.github.photowey.service.spi.vavr.extension.factory;

import io.github.photowey.service.spi.vavr.extension.loader.ExtensionLoader;

import java.util.List;
import java.util.Map;

/**
 * {@code ExtensionFactory}
 *
 * @author photowey
 * @date 2023/11/05
 * @since 1.0.0
 */
public final class ExtensionFactory {

    private ExtensionFactory() {
        throw new AssertionError("No " + ExtensionFactory.class.getName() + " instances for you!");
    }

    // ----------------------------------------------------------------

    public static <T> T create(final Class<T> targetClass) {
        return createLoader(targetClass).load(determineClassLoader());
    }

    // ----------------------------------------------------------------

    public static void stop() {
        Map<Class<?>, ExtensionLoader<?>> loaders = createLoaders();
        loaders.forEach((key, loader) -> loader.stop());
    }

    public static <T> void stop(final Class<T> ext) {
        ExtensionLoader<T> loader = createLoader(ext);
        if (null != loader) {
            loader.stop();
        }
    }

    // ----------------------------------------------------------------

    public static <T> T create(final Class<T> targetClass, final String name) {
        return createLoader(targetClass).load(name, determineClassLoader());
    }

    public static <T> T create(final Class<T> targetClass, final ClassLoader loader) {
        return createLoader(targetClass).load(loader);
    }

    public static <T> T create(final Class<T> targetClass, final String name, final ClassLoader loader) {
        return createLoader(targetClass).load(name, loader);
    }

    public static <T> T create(final Class<T> targetClass, final String name, final Object[] args) {
        return createLoader(targetClass).load(name, args, determineClassLoader());
    }

    public static <T> T create(final Class<T> targetClass, final String name, final Class<?>[] types, final Object[] args) {
        return createLoader(targetClass).load(name, types, args, determineClassLoader());
    }

    public static <T> List<T> creates(final Class<T> targetClass) {
        return createLoader(targetClass).loads(determineClassLoader());
    }

    // ----------------------------------------------------------------

    private static ClassLoader determineClassLoader() {
        return ExtensionFactory.class.getClassLoader();
    }

    private static <T> ExtensionLoader<T> createLoader(final Class<T> targetClass) {
        return ExtensionLoaderFactory.create(targetClass);
    }

    private static Map<Class<?>, ExtensionLoader<?>> createLoaders() {
        return ExtensionLoaderFactory.loaders();
    }
}
