package com.buschmais.jqassistant.plugin.java.test.rules;

import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.deprecated.DeprecatedType;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

/**
 * Tests for the concept java:Deprecated.
 */
@SuppressWarnings("deprecation")
class DeprecatedIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java:Deprecated".
     *
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    void deprecated() throws Exception {
        scanClasses(DeprecatedType.class);
        String packageInfoName = DeprecatedType.class.getPackage().getName() + ".package-info";
        scanClassPathResource(JavaScope.CLASSPATH, "/" + packageInfoName.replaceAll("\\.", "/") + ".class");
        assertThat(applyConcept("java:Deprecated").getStatus()).isEqualTo(SUCCESS);
        store.beginTransaction();
        assertThat(query("MATCH (element:Type:Class:Deprecated) RETURN element").getColumn("element"), hasItem(typeDescriptor(DeprecatedType.class)));
        assertThat(query("MATCH (element:Type:Interface:Deprecated) RETURN element as element").getColumn("element"), hasItem(typeDescriptor(packageInfoName)));
        assertThat(query("MATCH (element:Field:Deprecated) RETURN element").getColumn("element"), hasItem(fieldDescriptor(DeprecatedType.class, "value")));
        assertThat(query("MATCH (element:Method:Deprecated) RETURN element").getColumn("element"), hasItem(methodDescriptor(DeprecatedType.class, "getValue")));
        assertThat(query("MATCH (element:Method:Deprecated) RETURN element").getColumn("element"),
                hasItem(methodDescriptor(DeprecatedType.class, "setValue", int.class)));
        assertThat(query("MATCH (element:Parameter:Deprecated{index:0}) RETURN element").getRows().size()).isEqualTo(1);
        store.commitTransaction();
    }
}
