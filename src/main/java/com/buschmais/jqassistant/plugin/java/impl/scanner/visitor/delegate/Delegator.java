package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.delegate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;

/**
 * Provides methods for delegating method invocations to delegates.
 *
 * @param <D>
 *     The delegate type.
 */
@RequiredArgsConstructor
class Delegator<D> {

    private final List<D> delegates;

    /**
     * Delegate to a method without return value.
     *
     * @param consumer
     *     The Consumer.
     */
    void accept(Consumer<D> consumer) {
        for (D delegate : delegates) {
            if (delegate != null) {
                consumer.accept(delegate);
            }
        }
    }

    /**
     * Delegate to a {@link Function}.
     *
     * @param function
     *     The {@link Function}.
     * @param <T>
     *     The expected return type.
     * @return A {@link List} of returned values, may include <code>null</code> values.
     */
    <T> List<T> apply(Function<D, T> function) {
        List<T> list = new ArrayList<>(delegates.size());
        for (D delegate : delegates) {
            if (delegate != null) {
                T value = function.apply(delegate);
                list.add(value);
            } else {
                list.add(null);
            }
        }
        return list;
    }

}
