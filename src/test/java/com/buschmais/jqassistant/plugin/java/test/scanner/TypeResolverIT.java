package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.shared.map.MapBuilder;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.resolver.A;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.resolver.B;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.assertj.TypeDescriptorCondition.typeDescriptor;
import static org.assertj.core.api.Assertions.assertThat;

class TypeResolverIT extends AbstractJavaPluginIT {

    /**
     * Verify scanning dependent types in one artifact where the dependent type is
     * scanned first.
     */
    @Test
    public void dependentTypeFirst() {
        scanClasses("a1", B.class);
        scanClasses("a1", A.class);
        store.beginTransaction();
        TestResult testResult = query("match (a:Artifact)-[:CONTAINS]->(t:Type) where a.fqn=$artifact return t", MapBuilder.<String, Object>builder()
            .entry("artifact", "a1")
            .build());
        assertThat(testResult.getRows()
            .size()).isEqualTo(2);
        assertThat(testResult.<TypeDescriptor>getColumn("t")).areExactly(1, typeDescriptor(A.class))
            .haveExactly(1, typeDescriptor(B.class));
        testResult = query("match (a:Artifact)-[:REQUIRES]->(t:Type) where a.fqn=$artifact return t", MapBuilder.<String, Object>builder()
            .entry("artifact", "a1")
            .build());
        List<TypeDescriptor> types = testResult.getColumn("t");
        assertThat(types).doNotHave(typeDescriptor(A.class));
        assertThat(types).doNotHave(typeDescriptor(B.class));
        store.commitTransaction();
    }

    /**
     * Verify scanning dependent types in one artifact where the dependency is
     * scanned first.
     */
    @Test
    void dependencyTypeFirst() {
        scanClasses("a1", A.class);
        scanClasses("a1", B.class);
        store.beginTransaction();
        TestResult testResult = query("match (a:Artifact)-[:CONTAINS]->(t:Type) where a.fqn=$artifact return t", MapBuilder.<String, Object>builder()
            .entry("artifact", "a1")
            .build());
        assertThat(testResult.getRows()
            .size()).isEqualTo(2);
        assertThat(testResult.<TypeDescriptor>getColumn("t")).haveExactly(1, typeDescriptor(A.class))
            .haveExactly(1, typeDescriptor(B.class));
        testResult = query("match (a:Artifact)-[:REQUIRES]->(t:Type) where a.fqn=$artifact return t", MapBuilder.<String, Object>builder()
            .entry("artifact", "a1")
            .build());
        List<TypeDescriptor> types = testResult.getColumn("t");
        assertThat(types).doNotHave(typeDescriptor(A.class));
        assertThat(types).doNotHave(typeDescriptor(B.class));
        store.commitTransaction();
    }

    /**
     * Verifies scanning dependent types located in dependent artifacts.
     */
    @Test
    void dependentArtifacts() {
        store.beginTransaction();
        JavaArtifactFileDescriptor a1 = getArtifactDescriptor("a1");
        JavaArtifactFileDescriptor a2 = getArtifactDescriptor("a2");
        store.create(a2, DependsOnDescriptor.class, a1);
        store.commitTransaction();
        scanClasses("a1", A.class);
        scanClasses("a2", B.class);
        store.beginTransaction();
        TestResult testResult = query("match (artifact:Artifact)-[:CONTAINS]->(t:Type) where artifact.fqn=$artifact return t",
            MapBuilder.<String, Object>builder()
                .entry("artifact", "a1")
                .build());
        assertThat(testResult.getRows()
            .size()).isEqualTo(1);
        assertThat(testResult.<TypeDescriptor>getColumn("t")).have(typeDescriptor(A.class));
        testResult = query("match (artifact:Artifact)-[:CONTAINS]->(t:Type) where artifact.fqn=$artifact return t", MapBuilder.<String, Object>builder()
            .entry("artifact", "a2")
            .build());
        assertThat(testResult.getRows()
            .size()).isEqualTo(1);
        assertThat(testResult.<TypeDescriptor>getColumn("t")).have(typeDescriptor(B.class));
        testResult = query(
            "match (artifact2:Artifact)-[:CONTAINS]->(b:Type)-[:DEPENDS_ON]->(a:Type)<-[:CONTAINS]-(artifact1:Artifact) where artifact1.fqn=$a1 and artifact2.fqn=$a2 and b.fqn=$b return a",
            MapBuilder.<String, Object>builder()
                .entry("b", B.class.getName())
                .entry("a1", "a1")
                .entry("a2", "a2")
                .build());
        assertThat(testResult.<TypeDescriptor>getColumn("a")).have(typeDescriptor(A.class));
        // java.lang.Object is only required by artifact A1
        testResult = query("match (a:Artifact)-[:REQUIRES]->(o:Type) where o.fqn=$object return a", MapBuilder.<String, Object>builder()
            .entry("object", Object.class.getName())
            .build());
        List<JavaArtifactFileDescriptor> objects = testResult.getColumn("a");
        assertThat(objects.size()).isEqualTo(1);
        assertThat(objects.get(0)).isEqualTo(a1);
        store.commitTransaction();
    }

    /**
     * Verifies scanning dependent types located in artifacts which are transitively
     * dependent.
     */
    @Test
    void transitiveDependentArtifacts() {
        store.beginTransaction();
        JavaArtifactFileDescriptor a1 = getArtifactDescriptor("a1");
        JavaArtifactFileDescriptor a2 = getArtifactDescriptor("a2");
        JavaArtifactFileDescriptor a3 = getArtifactDescriptor("a3");
        store.create(a2, DependsOnDescriptor.class, a1);
        store.create(a3, DependsOnDescriptor.class, a2);
        store.commitTransaction();
        scanClasses("a1", A.class);
        scanClasses("a3", B.class);
        store.beginTransaction();
        TestResult testResult = query("match (artifact:Artifact)-[:CONTAINS]->(t:Type) where artifact.fqn=$artifact return t",
            MapBuilder.<String, Object>builder()
                .entry("artifact", "a1")
                .build());
        assertThat(testResult.getRows()
            .size()).isEqualTo(1);
        assertThat(testResult.<TypeDescriptor>getColumn("t")).have(typeDescriptor(A.class));
        testResult = query("match (artifact:Artifact)-[:CONTAINS]->(t:Type) where artifact.fqn=$artifact return t", MapBuilder.<String, Object>builder()
            .entry("artifact", "a3")
            .build());
        assertThat(testResult.getRows()
            .size()).isEqualTo(1);
        assertThat(testResult.<TypeDescriptor>getColumn("t")).have(typeDescriptor(B.class));
        testResult = query(
            "match (artifact3:Artifact)-[:CONTAINS]->(b:Type)-[:DEPENDS_ON]->(a:Type)<-[:CONTAINS]-(artifact1:Artifact) where artifact1.fqn=$a1 and artifact3.fqn=$a3 and b.fqn=$b return a",
            MapBuilder.<String, Object>builder()
                .entry("b", B.class.getName())
                .entry("a1", "a1")
                .entry("a3", "a3")
                .build());
        assertThat(testResult.<TypeDescriptor>getColumn("a")).have(typeDescriptor(A.class));
        store.commitTransaction();
    }

    /**
     * Verifies scanning dependent types located in independent artifacts.
     */
    @Test
    void independentArtifacts() {
        scanClasses("a1", A.class);
        scanClasses("a2", B.class);
        store.beginTransaction();
        TestResult testResult = query("match (artifact:Artifact)-[:CONTAINS]->(t:Type) where artifact.fqn=$artifact return t",
            MapBuilder.<String, Object>builder()
                .entry("artifact", "a1")
                .build());
        assertThat(testResult.getRows()
            .size()).isEqualTo(1);
        assertThat(testResult.<TypeDescriptor>getColumn("t")).have(typeDescriptor(A.class));
        testResult = query("match (artifact:Artifact)-[:CONTAINS]->(t:Type) where artifact.fqn=$artifact return t", MapBuilder.<String, Object>builder()
            .entry("artifact", "a2")
            .build());
        assertThat(testResult.getRows()
            .size()).isEqualTo(1);
        assertThat(testResult.<TypeDescriptor>getColumn("t")).have(typeDescriptor(B.class));
        testResult = query(
            "match (artifact2:Artifact)-[:CONTAINS]->(b:Type)-[:DEPENDS_ON]->(a:Type)<-[:CONTAINS]-(artifact1:Artifact) where artifact1.fqn=$a1 and artifact2.fqn=$a2 and b.fqn=$b return a",
            MapBuilder.<String, Object>builder()
                .entry("b", B.class.getName())
                .entry("a1", "a1")
                .entry("a2", "a2")
                .build());
        assertThat(testResult.getRows()
            .size()).isEqualTo(0);
        testResult = query("match (artifact:Artifact)-[:REQUIRES]->(a:Type) where a.fqn=$a return artifact", MapBuilder.<String, Object>builder()
            .entry("a", A.class.getName())
            .build());
        assertThat(testResult.getRows()
            .size()).isEqualTo(1);
        ArtifactFileDescriptor a = (ArtifactFileDescriptor) testResult.getColumn("artifact")
            .get(0);
        assertThat(a.getFullQualifiedName()).isEqualTo("a2");
        store.commitTransaction();
    }

    /**
     * Verifies scanning the same type which exists in two independent artifacts.
     */
    @Test
    void duplicateType() {
        scanClasses("a1", A.class);
        scanClasses("a2", A.class);
        store.beginTransaction();
        TestResult testResult = query("match (:Artifact)-[:CONTAINS]->(t:Type) where t.fqn=$t return t", MapBuilder.<String, Object>builder()
            .entry("t", A.class.getName())
            .build());
        assertThat(testResult.getRows()
            .size()).isEqualTo(2);
        store.commitTransaction();
    }

    /**
     * Verifies scanning a type depending on another type which exists in two
     * independent artifacts.
     */
    @Test
    void ambiguousDependencies() {
        store.beginTransaction();
        JavaArtifactFileDescriptor a1 = getArtifactDescriptor("a1");
        JavaArtifactFileDescriptor a2 = getArtifactDescriptor("a2");
        JavaArtifactFileDescriptor a3 = getArtifactDescriptor("a3");
        store.create(a3, DependsOnDescriptor.class, a1);
        store.create(a3, DependsOnDescriptor.class, a2);
        store.commitTransaction();
        scanClasses("a1", A.class);
        scanClasses("a2", A.class);
        scanClasses("a3", B.class);
        store.beginTransaction();
        TestResult testResult = query("match (:Artifact)-[:CONTAINS]->(t:Type) where t.fqn=$t return t", MapBuilder.<String, Object>builder()
            .entry("t", A.class.getName())
            .build());
        assertThat(testResult.getRows()
            .size()).isEqualTo(2);
        testResult = query(
            "match (artifact3:Artifact)-[:CONTAINS]->(b:Type)-[:DEPENDS_ON]->(a:Type)-[:CONTAINS]-(otherArtifact:Artifact) where b.fqn=$b return otherArtifact",
            MapBuilder.<String, Object>builder()
                .entry("a", A.class.getName())
                .entry("b", B.class.getName())
                .build());
        assertThat(testResult.getRows()
            .size()).isEqualTo(1);
        JavaArtifactFileDescriptor otherArtifact = (JavaArtifactFileDescriptor) testResult.getColumn("otherArtifact")
            .get(0);
        assertThat(otherArtifact).satisfiesAnyOf(arg -> assertThat(arg).isEqualTo(a1), arg -> assertThat(arg).isEqualTo(a2));
        store.commitTransaction();
    }

    /**
     *
     */
    @Test
    void duplicateTypeInSameArtifact() {
        File directory = getClassesDirectory(A.class);
        final String resource = "/" + A.class.getName()
            .replace(".", "/") + ".class";
        final File file = new File(directory, resource);
        scanClasses(B.class);
        List<? extends FileDescriptor> descriptors = execute("a1", new ScanClassPathOperation() {
            @Override
            public List<FileDescriptor> scan(JavaArtifactFileDescriptor artifact, Scanner scanner) {
                List<FileDescriptor> result = new ArrayList<>();
                FileDescriptor fileDescriptor1 = scanner.scan(file, "/1.0" + resource, JavaScope.CLASSPATH);
                FileDescriptor fileDescriptor2 = scanner.scan(file, resource, JavaScope.CLASSPATH);
                result.add(fileDescriptor1);
                result.add(fileDescriptor2);
                return result;
            }
        });
        store.beginTransaction();
        assertThat(descriptors.size()).isEqualTo(2);
        FileDescriptor fileDescriptor1 = descriptors.get(0);
        assertThat(fileDescriptor1.getFileName()).isEqualTo("/1.0" + resource);
        assertThat(fileDescriptor1).isInstanceOf(TypeDescriptor.class);
        assertThat(((TypeDescriptor) fileDescriptor1).getFullQualifiedName()).isEqualTo(A.class.getName());
        FileDescriptor fileDescriptor2 = descriptors.get(1);
        assertThat(fileDescriptor2.getFileName()).isEqualTo(resource);
        assertThat(fileDescriptor2).isInstanceOf(TypeDescriptor.class);
        assertThat(((TypeDescriptor) fileDescriptor2).getFullQualifiedName()).isEqualTo(A.class.getName());
        store.commitTransaction();
    }
}
