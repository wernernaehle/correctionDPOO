package edu.uoc.correction.model;

public enum QualityRule {
    DUPLICATED_BLOCKS("duplication", "Bloques de código duplicados"),
    CONSTRUCTOR_DELEGATION("duplication", "Constructor que no delega en otro constructor"),
    DEFAULT_CONSTRUCTOR_DELEGATION("duplication", "Constructor por defecto que asigna valores"),
    UNUSED_PRIVATE_METHOD("deadCode", "Método privado no utilizado"),
    UNUSED_PRIVATE_FIELD("deadCode", "Atributo privado no utilizado"),
    UNUSED_LOCAL_OR_PARAMETER("deadCode", "Variable local o parámetro no utilizado"),
    UNUSED_IMPORT("deadCode", "Import no utilizado"),
    COMMENTED_OUT_CODE("deadCode", "Código Java comentado"),
    CYCLOMATIC_COMPLEXITY("complexity", "Complejidad ciclomática excesiva"),
    NESTING_DEPTH("complexity", "Anidamiento excesivo"),
    METHOD_LENGTH("complexity", "Método demasiado largo"),
    REDUNDANT_CONDITIONAL("complexity", "Condicional innecesario"),
    MULTIPLE_LOOP_JUMPS("complexity", "Más de un break o continue en un bucle"),
    STRING_REFERENCE_COMPARISON("javaIdioms", "Comparación de String con == o !="),
    IMMEDIATE_RETURN_VARIABLE("javaIdioms", "Variable devuelta inmediatamente"),
    REPEATED_MAGIC_LITERAL("javaIdioms", "Literal mágico repetido"),
    FIELD_INITIALIZER("javaIdioms", "Atributo inicializado en la declaración"),
    EXPLICIT_LINE_SEPARATOR("javaIdioms", "Separador de línea explícito"),
    NON_CAMEL_CASE_NAME("naming", "Nombre que no sigue camelCase"),
    NON_UPPER_SNAKE_CONSTANT("naming", "Constante que no sigue UPPER_SNAKE_CASE"),
    MEANINGLESS_NAME("naming", "Nombre de una sola letra o poco descriptivo");

    private final String category;
    private final String description;

    QualityRule(String category, String description) {
        this.category = category;
        this.description = description;
    }

    public String getId() {
        return name();
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public static QualityRule fromId(String id) {
        for (QualityRule rule : values()) {
            if (rule.name().equalsIgnoreCase(id)) {
                return rule;
            }
        }
        return null;
    }
}
