package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("OpenPackage")
public interface OpenPackageDescriptor extends JavaDescriptor, PackageToModuleDescriptor {
}
