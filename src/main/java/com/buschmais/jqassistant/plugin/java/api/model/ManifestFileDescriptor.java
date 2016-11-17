package com.buschmais.jqassistant.plugin.java.api.model;

import static com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Manifest")
public interface ManifestFileDescriptor extends JavaDescriptor, FileDescriptor {

    @Declares
    @Outgoing
    ManifestSectionDescriptor getMainSection();

    void setMainSection(ManifestSectionDescriptor mainSection);

    @Declares
    @Outgoing
    List<ManifestSectionDescriptor> getManifestSections();
}
