package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("ExportedPackage")
public interface ExportedPackageDescriptor extends JavaDescriptor, PackageToModuleDescriptor {
}
