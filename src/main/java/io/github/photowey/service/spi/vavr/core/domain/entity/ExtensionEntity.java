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
package io.github.photowey.service.spi.vavr.core.domain.entity;

import io.github.photowey.service.spi.vavr.core.enums.Scoped;

import java.io.Serializable;
import java.util.Objects;

/**
 * {@code ExtensionEntity}
 *
 * @author photowey
 * @date 2023/11/05
 * @since 1.0.0
 */
public class ExtensionEntity implements Serializable {

    private static final long serialVersionUID = -1750778542600305518L;

    private String name;
    private Class<?> targetClass;
    private Integer order;
    private Scoped scope;

    public static ExtensionEntityBuilder builder() {
        return new ExtensionEntityBuilder();
    }

    public String getName() {
        return this.name;
    }

    public Class<?> getTargetClass() {
        return this.targetClass;
    }

    public Integer getOrder() {
        return this.order;
    }

    public Scoped getScope() {
        return this.scope;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setTargetClass(final Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public void setOrder(final Integer order) {
        this.order = order;
    }

    public void setScope(final Scoped scope) {
        this.scope = scope;
    }

    public ExtensionEntity() {
    }

    public ExtensionEntity(final String name, final Class<?> targetClass, final Integer order, final Scoped scope) {
        this.name = name;
        this.targetClass = targetClass;
        this.order = order;
        this.scope = scope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtensionEntity)) return false;
        ExtensionEntity that = (ExtensionEntity) o;
        return Objects.equals(getName(), that.getName())
                && Objects.equals(getTargetClass(), that.getTargetClass())
                && Objects.equals(getOrder(), that.getOrder())
                && getScope() == that.getScope();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getTargetClass(), getOrder(), getScope());
    }

    public static class ExtensionEntityBuilder {

        private String name;
        private Class<?> targetClass;
        private Integer order;
        private Scoped scope;

        ExtensionEntityBuilder() {
        }

        public ExtensionEntityBuilder name(final String name) {
            this.name = name;
            return this;
        }

        public ExtensionEntityBuilder targetClass(final Class<?> targetClass) {
            this.targetClass = targetClass;
            return this;
        }

        public ExtensionEntityBuilder order(final Integer order) {
            this.order = order;
            return this;
        }

        public ExtensionEntityBuilder scope(final Scoped scope) {
            this.scope = scope;
            return this;
        }

        public ExtensionEntity build() {
            return new ExtensionEntity(this.name, this.targetClass, this.order, this.scope);
        }

        public String toString() {
            return "ExtensionEntity.ExtensionEntityBuilder(name=" + this.name + ", targetClass=" + this.targetClass + ", order=" + this.order + ", scope=" + this.scope + ")";
        }
    }
}
