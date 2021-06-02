package com.buschmais.jqassistant.plugin.java.test.set.scanner.generics;

import java.util.List;
import java.util.Map;

public class GenericFields<X> {

    private X typeVariable;

    private Map<String, X> parameterizedType;

    private List<List<X>> nestedParameterizedType;

    private List<? extends X> upperBoundWildcard;

    private List<? super X> lowerBoundWildcard;

    private List<?> unboundWildcard;

}
