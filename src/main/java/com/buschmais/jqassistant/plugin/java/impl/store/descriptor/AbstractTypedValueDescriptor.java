package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.AbstractValueDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypedValueDescriptor;

/**
 * Abstract base class for value descriptors which provide a type information.
 *
 * @param <V> The value type.
 */
public abstract class AbstractTypedValueDescriptor<V> extends AbstractValueDescriptor<V> implements TypedValueDescriptor<V> {

    private TypeDescriptor type;

    @Override
    public TypeDescriptor getType() {
        return type;
    }

    @Override
    public void setType(TypeDescriptor type) {
        this.type = type;
    }
}
