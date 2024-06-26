package com.buschmais.jqassistant.plugin.java.test.scanner;

import com.buschmais.jqassistant.plugin.java.api.model.ConstructorDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.constructor.ImplicitDefaultConstructor;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.constructor.OverloadedConstructor;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.assertj.MethodDescriptorCondition.constructorDescriptor;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contains test which verify correct scanning of constructors.
 */
class ConstructorIT extends AbstractJavaPluginIT {

    /**
     * Verifies scanning of {@link ImplicitDefaultConstructor}.
     *
     * @throws NoSuchMethodException
     *     If the test fails.
     */
    @Test
    void implicitDefaultConstructor() throws NoSuchMethodException {
        scanClasses(ImplicitDefaultConstructor.class);
        store.beginTransaction();
        assertThat(query("MATCH (c:Method:Constructor) RETURN c").<ConstructorDescriptor>getColumn("c")).haveExactly(1,
            constructorDescriptor(ImplicitDefaultConstructor.class));
        // the constructor of java.lang.Object is not scanned but should be identified as constructor
        assertThat(query("MATCH (c:Method:Constructor) RETURN c").<ConstructorDescriptor>getColumn("c")).haveExactly(1, constructorDescriptor(Object.class));
        store.commitTransaction();
    }

    /**
     * Verifies scanning of {@link OverloadedConstructor}.
     *
     * @throws NoSuchMethodException
     *     If the test fails.
     */
    @Test
    void overloadedConstructors() throws NoSuchMethodException {
        scanClasses(OverloadedConstructor.class);
        store.beginTransaction();
        assertThat(query("MATCH (c:Method:Constructor) RETURN c").<ConstructorDescriptor>getColumn("c")).haveExactly(1,
                constructorDescriptor(OverloadedConstructor.class))
            .haveExactly(1, constructorDescriptor(OverloadedConstructor.class, String.class));
        store.commitTransaction();
    }
}
