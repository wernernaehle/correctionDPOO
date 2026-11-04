package edu.uoc.correction.model;

public enum QualityCategory {
    DUPLICATION("duplication", "Código repetido y oportunidades para reutilizar lógica."),
    DEAD_CODE("deadCode", "Código declarado o escrito que no se utiliza y conviene eliminar."),
    COMPLEXITY("complexity", "Métodos difíciles de entender por su longitud, decisiones o anidamiento."),
    JAVA_IDIOMS("javaIdioms", "Uso de patrones y características de Java de forma clara y adecuada."),
    NAMING("naming", "Nombres claros y coherentes con las convenciones habituales de Java.");

    private final String key;
    private final String description;

    QualityCategory(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }
}
