/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.spine3.type;

import com.google.common.base.Objects;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * Implementation of the {@code ParameterizedType} interface.
 *
 * @author Illia Shepilov
 */
public class ParametrizedTypeImpl implements ParameterizedType {

    private final Class<?> rawType;
    private final Type[] typeArguments;
    private final Type ownerType;

    private ParametrizedTypeImpl(Class<?> rawType, Type[] typeArguments, Type ownerType) {
        this.rawType = rawType;
        this.typeArguments = typeArguments;
        this.ownerType = ownerType;
    }

    public static ParameterizedType make(Class<?> rawType, Type[] typeArguments, Type ownerType) {
        return new ParametrizedTypeImpl(rawType, typeArguments, ownerType);
    }

    public static ParameterizedType make(Class<?> rawType, Type[] typeArguments) {
        return new ParametrizedTypeImpl(rawType, typeArguments, rawType);
    }

    @Override
    public Type[] getActualTypeArguments() {
        return typeArguments.clone();
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ParametrizedTypeImpl that = (ParametrizedTypeImpl) o;
        return Objects.equal(rawType, that.rawType) &&
               Arrays.equals(typeArguments, that.typeArguments) &&
               Objects.equal(ownerType, that.ownerType);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rawType, Arrays.hashCode(typeArguments), ownerType);
    }
}
