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

import io.github.photowey.service.spi.vavr.core.annotation.SPI;
import io.github.photowey.service.spi.vavr.core.domain.entity.ExtensionEntity;
import io.github.photowey.service.spi.vavr.core.enums.Scoped;
import io.github.photowey.service.spi.vavr.extension.generator.DefaultExtensionNameGenerator;
import io.github.photowey.service.spi.vavr.extension.generator.ExtensionNameGenerator;
import io.github.photowey.service.spi.vavr.extension.lifecycle.InitializeLifeCycle;
import io.github.photowey.service.spi.vavr.extension.lifecycle.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * {@code ExtensionLoader}
 *
 * @author photowey
 * @date 2023/11/05
 * @since 1.0.0
 */
public final class ExtensionLoader<T> {

    private static final Logger log = LoggerFactory.getLogger(ExtensionLoader.class);

    private static final String EXTENSION_DIRECTORY = "META-INF/extensions/";

    private static final Map<Class<?>, ExtensionLoader<?>> CLASS_LOADERS = new ConcurrentHashMap<>();

    private final TypeHolder<List<ExtensionEntity>> entitiesHolder = new TypeHolder<>();
    private final Map<String, TypeHolder<T>> cachedSingletonInstances = new ConcurrentHashMap<>();

    private final Map<String, ExtensionEntity> nameToEntityMap = new ConcurrentHashMap<>();
    private final Map<Class<?>, ExtensionEntity> classToEntityMap = new ConcurrentHashMap<>();

    private final ExtensionNameGenerator extensionNameGenerator;

    private final Class<T> targetClass;

    private ExtensionLoader(final Class<T> targetClass) {
        this.targetClass = targetClass;
        this.extensionNameGenerator = this.initBeanNameGenerator();
    }

    private ExtensionNameGenerator initBeanNameGenerator() {
        return new DefaultExtensionNameGenerator();
    }

    public static Map<Class<?>, ExtensionLoader<?>> loaders() {
        return CLASS_LOADERS;
    }

    public static <T> ExtensionLoader<T> getExtensionLoader(final Class<T> targetClass) {
        if (targetClass == null) {
            throw new NullPointerException("Extension target class is null");
        }
        if (!targetClass.isInterface()) {
            throw new IllegalArgumentException(String.format("Extension target class(%s) is not interface!", targetClass.getName()));
        }

        ExtensionLoader<T> loader = (ExtensionLoader<T>) CLASS_LOADERS.get(targetClass);
        if (null != loader) {
            return loader;
        }

        CLASS_LOADERS.putIfAbsent(targetClass, new ExtensionLoader<>(targetClass));
        return (ExtensionLoader<T>) CLASS_LOADERS.get(targetClass);
    }

    public T load(final ClassLoader loader) {
        return this.loadExtension(loader);
    }

    public T load(final String name, final ClassLoader loader) {
        return this.loadExtension(name, loader, null, null);
    }

    public T load(final String name, final Object[] args, final ClassLoader loader) {
        Class<?>[] types = null;
        if (args != null && args.length > 0) {
            types = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                types[i] = args[i].getClass();
            }
        }

        return this.loadExtension(name, loader, types, args);
    }

    public T load(final String name, final Class<?>[] types, final Object[] args, final ClassLoader loader) {
        return this.loadExtension(name, loader, types, args);
    }

    public List<T> loads(final ClassLoader loader) {
        return this.loads(null, null, loader);
    }

    public void stop() {
        for (Map.Entry<String, TypeHolder<T>> entry : this.cachedSingletonInstances.entrySet()) {
            TypeHolder<T> holder = entry.getValue();
            T target = holder.getValue();
            if (target instanceof InitializeLifeCycle) {
                ((InitializeLifeCycle) target).stop();
            }
        }
    }

    private List<T> loads(final Class<?>[] types, final Object[] args, final ClassLoader loader) {
        List<Class<?>> all = this.getAllExtensionClass(loader);
        if (all.isEmpty()) {
            return Collections.emptyList();
        }
        return all.stream().map(targetClass -> {
            ExtensionEntity entity = this.classToEntityMap.get(targetClass);
            return this.getExtensionInstance(entity, types, args);
        }).collect(Collectors.toList());
    }

    private List<Class<?>> getAllExtensionClass(final ClassLoader loader) {
        return this.loadAllExtensionClass(loader);
    }

    private T loadExtension(final ClassLoader loader) {
        this.loadAllExtensionClass(loader);
        ExtensionEntity extensionEntity = this.getDefaultExtensionEntity();
        return this.getExtensionInstance(extensionEntity, null, null);
    }

    private T loadExtension(final String name, final ClassLoader loader, final Class<?>[] types, final Object[] args) {
        this.loadAllExtensionClass(loader);
        ExtensionEntity entity = this.getCachedExtensionEntity(name);
        return this.getExtensionInstance(entity, types, args);
    }

    private T getExtensionInstance(final ExtensionEntity entity, final Class<?>[] types, final Object[] args) {
        if (entity == null) {
            log.error("Not found target service implements for class:[{}]", this.targetClass.getName());
            return null;
        }

        if (Scoped.SINGLETON.equals(entity.getScope())) {
            String entityName = entity.getName();
            if (null == entityName) {
                entityName = this.extensionNameGenerator.generate(entity.getTargetClass());
            }

            TypeHolder<T> holder = this.cachedSingletonInstances.get(entityName);
            if (holder == null) {
                this.cachedSingletonInstances.putIfAbsent(entityName, new TypeHolder<>());
                holder = this.cachedSingletonInstances.get(entityName);
            }

            T instance = holder.getValue();
            if (instance == null) {
                synchronized (this.cachedSingletonInstances) {
                    instance = holder.getValue();
                    if (instance == null) {
                        instance = this.newInstance(entity, types, args);
                        holder.setValue(instance);
                    }
                }
            }

            return instance;
        }

        return this.newInstance(entity, types, args);
    }

    private T newInstance(final ExtensionEntity entity, final Class<?>[] types, final Object[] args) {
        try {
            return this.initInstance(entity.getTargetClass(), types, args);
        } catch (Exception e) {
            throw new IllegalStateException("Extension new instance(entity: " + entity + ", class: " + this.targetClass + ")  could not be instantiated", e);
        }
    }

    private ExtensionEntity getDefaultExtensionEntity() {
        return this.entitiesHolder.getValue().stream().findFirst().orElse(null);
    }

    private ExtensionEntity getCachedExtensionEntity(final String name) {
        return this.nameToEntityMap.get(name);
    }

    private List<Class<?>> loadAllExtensionClass(final ClassLoader loader) {
        List<ExtensionEntity> entities = this.entitiesHolder.getValue();
        if (null == entities) {
            synchronized (this.entitiesHolder) {
                entities = this.entitiesHolder.getValue();
                if (null == entities) {
                    entities = this.findAllExtensionEntity(loader);
                    this.entitiesHolder.setValue(entities);
                }
            }
        }

        return entities.stream()
                .map(ExtensionEntity::getTargetClass)
                .collect(Collectors.toList());
    }

    private List<ExtensionEntity> findAllExtensionEntity(final ClassLoader loader) {
        List<ExtensionEntity> entityList = new ArrayList<>();
        this.loadDirectory(EXTENSION_DIRECTORY + this.targetClass.getName(), loader, entityList);
        return entityList.stream()
                .sorted(Comparator.comparing(ExtensionEntity::getOrder))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private void loadDirectory(final String dir, final ClassLoader classLoader, final List<ExtensionEntity> entities) {
        try {
            Enumeration<URL> urls = classLoader != null ? classLoader.getResources(dir) : ClassLoader.getSystemResources(dir);
            if (null != urls) {
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    this.loadResources(entities, url, classLoader);
                }
            }
        } catch (IOException e) {
            log.error("Load @SPI extension class error, dir:[{}]", dir, e);
        }
    }

    private void loadResources(final List<ExtensionEntity> entities, final URL url, final ClassLoader classLoader) {
        try (InputStream inputStream = url.openStream()) {
            Properties properties = new Properties();
            properties.load(inputStream);
            properties.forEach((k, v) -> {
                String name = (String) k;
                if (null != name && !name.isEmpty()) {
                    try {
                        this.loadClass(entities, name, classLoader);
                    } catch (ClassNotFoundException e) {
                        log.warn("Load @SPI extension:[{}] class failed:[{}]", name, e.getMessage());
                    }
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("Load @SPI extension resources error", e);
        }
    }

    private void loadClass(final List<ExtensionEntity> entities, final String className, final ClassLoader loader) throws ClassNotFoundException {
        if (this.notContainsClazz(className, loader)) {
            Class<?> implClass = Class.forName(className, true, loader);
            if (!this.targetClass.isAssignableFrom(implClass)) {
                throw new IllegalStateException("Load @SPI extension class: " + implClass + " failed, subtype is not of " + this.targetClass);
            }

            String entityName = this.extensionNameGenerator.generate(implClass);
            SPI spi = implClass.getAnnotation(SPI.class);
            ExtensionEntity.ExtensionEntityBuilder builder = ExtensionEntity.builder()
                    .name(entityName)
                    .order(0)
                    .scope(Scoped.SINGLETON)
                    .targetClass(implClass);

            if (null != spi) {
                entityName = spi.value();
                builder.name(entityName).order(spi.order()).scope(spi.scope());
            }

            ExtensionEntity ext = builder.build();
            entities.add(ext);

            this.classToEntityMap.put(implClass, ext);

            if (null != spi) {
                this.nameToEntityMap.put(entityName, ext);
            } else {
                this.nameToEntityMap.put(entityName, ext);
            }
        }
    }

    private boolean notContainsClazz(final String className, final ClassLoader loader) {
        return !this.containsClazz(className, loader);
    }

    private boolean containsClazz(final String className, final ClassLoader loader) {
        return this.classToEntityMap.entrySet().stream()
                .filter(entry -> entry.getKey().getName().equals(className))
                .anyMatch(entry -> Objects.equals(entry.getValue().getTargetClass().getClassLoader(), loader));
    }

    private T initInstance(final Class<?> implClass, final Class<?>[] types, final Object[] args)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        T entity;

        if (null != types && null != args) {
            Constructor<?> constructor = implClass.getDeclaredConstructor(types);
            entity = this.targetClass.cast(constructor.newInstance(args));
        } else {
            entity = this.targetClass.cast(implClass.getDeclaredConstructor().newInstance());
        }

        if (entity instanceof LifeCycle) {
            ((LifeCycle) entity).start();
            if (entity instanceof InitializeLifeCycle) {
                ((InitializeLifeCycle) entity).init();
            }
        }

        return entity;
    }

    private static class TypeHolder<T> {

        private volatile T value;

        public static <T> TypeHolderBuilder<T> builder() {
            return new TypeHolderBuilder();
        }

        public T getValue() {
            return this.value;
        }

        public void setValue(final T value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TypeHolder)) return false;
            TypeHolder<?> that = (TypeHolder<?>) o;
            return Objects.equals(getValue(), that.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getValue());
        }

        public TypeHolder() {
        }

        public TypeHolder(final T value) {
            this.value = value;
        }

        public static class TypeHolderBuilder<T> {
            private T value;

            TypeHolderBuilder() {
            }

            public TypeHolderBuilder<T> value(final T value) {
                this.value = value;
                return this;
            }

            public TypeHolder<T> build() {
                return new TypeHolder(this.value);
            }

            public String toString() {
                return "ExtensionLoader.TypeHolder.TypeHolderBuilder(value=" + this.value + ")";
            }
        }
    }
}
