package com.buschmais.jqassistant.plugin.java.test.rules;

import java.io.IOException;
import java.util.List;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.virtualdependson.*;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for the concept java:VirtualDependsOn.
 */
class VirtualDependsOnIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java:VirtualDependsOn" for a class implementing an
     * interface.
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void invokeInterfaceMethod() throws Exception {
        scanClasses(ClassType.class, InterfaceType.class, InterfaceTypeClient.class);
        assertThat(applyConcept("java:VirtualDependsOn").getStatus()).isEqualTo(SUCCESS);
        store.beginTransaction();
        List<TypeDescriptor> types = query("MATCH (client:Type)-[:VIRTUAL_DEPENDS_ON]->(type:Type) WHERE client.name='InterfaceTypeClient' RETURN type")
                .getColumn("type");
        assertThat(types.size()).isEqualTo(1);
        assertThat(types.get(0), typeDescriptor(ClassType.class));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java:VirtualInvokes" for a sub class extending a class.
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void invokeClassMethod() throws Exception {
        scanClasses(ClassType.class, SubClassType.class, ClassTypeClient.class);
        assertThat(applyConcept("java:VirtualDependsOn").getStatus()).isEqualTo(SUCCESS);
        store.beginTransaction();
        List<TypeDescriptor> types = query("MATCH (client:Type)-[:VIRTUAL_DEPENDS_ON]->(type:Type) WHERE client.name='ClassTypeClient' RETURN type")
                .getColumn("type");
        assertThat(types.size()).isEqualTo(1);
        assertThat(types.get(0), typeDescriptor(SubClassType.class));
        store.commitTransaction();
    }
}
