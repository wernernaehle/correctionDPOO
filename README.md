# CorrectionDPOO

Herramienta de corrección automática para entregas Java. Ejecuta las pruebas configuradas para cada ejercicio, calcula las notas, genera un informe HTML por estudiante y crea un ZIP cifrado con el informe.

## Requisitos

- JDK instalado. Se necesita el compilador del JDK para analizar Javadoc y calidad del código.
- Acceso a los proyectos de entregas y a los proyectos de pruebas configurados.
- Los proyectos evaluados deben incluir el wrapper de Gradle (`gradlew` o `gradlew.bat`).

## Configuración

La configuración se lee desde `src/main/resources/config.properties`. Las cuatro primeras líneas indican las rutas de entregas, informes y pruebas, y el nombre de la PAC. Ajusta esas rutas a tu entorno antes de ejecutar la herramienta.

Las líneas siguientes contienen la puntuación y opciones por ejercicio. Los nombres de las propiedades distinguen mayúsculas y minúsculas.

Para añadir a `PASSWORD.txt` la nota de evaluación continua para Canvas, se puede configurar `canvas.grade.multiplier`. La nota se calcula como `nota total * multiplier / 10`; por ejemplo, con `canvas.grade.multiplier = 20`, una nota total de `9.11` se muestra como `18.22`. Si se omite la propiedad, se usa `10` y el campo de Canvas coincide con la nota total. Esta propiedad sólo afecta al mensaje de `PASSWORD.txt`, no a la nota calculada por la herramienta.

### Puntuación y requisitos mínimos

Las definiciones de ejercicios siguen el formato existente `Ejercicio = ClaseTest:puntuación#metodo:puntuación, ...`. La propiedad opcional `<Ejercicio>.minimumRequirements` define pruebas que deben superarse para obtener nota en ese ejercicio:

```properties
PAC2Ex1.minimumRequirements = MuseumArtifactTest#testConstructor(), MuseumArtifactPrivateTest#testFieldsArePrivate()
```

Si una prueba obligatoria falla, se omite o está ausente, la nota de tests del ejercicio es cero. Los resultados individuales siguen apareciendo en el informe. Si no se configura esta propiedad para un ejercicio, se conserva su puntuación habitual.

### Evaluación de Javadoc

La evaluación Javadoc se configura por ejercicio y está desactivada si no se activa explícitamente. Requiere título para la sección separada del informe:

```properties
javadoc.exercise.title = PAC2Ex3
PAC2Ex2.javadoc.enabled = true
PAC2Ex2.javadoc.classes = MuseumArtifact
PAC2Ex2.javadoc.presenceScore = 0.5
PAC2Ex2.javadoc.qualityMaxScore = 0.5
PAC2Ex2.javadoc.include = class, fields, constructors, publicMethods, privateMethods
PAC2Ex2.javadoc.referencePath = reference-solutions/DPOO_PAC2_sol/PAC2Ex2_sol
```

La puntuación Javadoc se calcula independientemente de los requisitos mínimos de tests. Si el ejercicio no compila, la evaluación Javadoc obtiene cero.

### Penalización opcional por calidad del código

La calidad se activa por ejercicio. La penalización se resta de la nota de tests más Javadoc del ejercicio, sin permitir que su nota final sea negativa. La herramienta no evalúa calidad cuando el ejercicio no compila o no cumple sus requisitos mínimos. Sin `quality.enabled = true`, la nota y el informe del ejercicio conservan el comportamiento habitual.

Configuración mínima:

```properties
PAC2Ex1.quality.enabled = true
PAC2Ex1.quality.maxPenalty = 1
```

Configuración completa de ejemplo:

```properties
PAC2Ex1.quality.enabled = true
PAC2Ex1.quality.maxPenalty = 1
PAC2Ex1.quality.classes = MuseumArtifact, ArtifactUtils
PAC2Ex1.quality.referencePath = reference-solutions/DPOO_PAC2_sol/PAC2Ex1_sol
PAC2Ex1.quality.weights = duplication:1, deadCode:1, complexity:1, javaIdioms:1, naming:1
PAC2Ex1.quality.violationsForFullCategoryPenalty = 3
PAC2Ex1.quality.complexityThreshold = 10
PAC2Ex1.quality.nestingThreshold = 3
PAC2Ex1.quality.methodLengthThreshold = 30
PAC2Ex1.quality.disabledRules = UNUSED_IMPORT
```

`classes` y `referencePath` son opcionales: por defecto se analizan las clases Java de la referencia del ejercicio. Se reconocen las rutas `reference-solutions/<PAC>/ExN` y `reference-solutions/DPOO_<PAC>_sol/<Ejercicio>_sol`. La referencia y las clases configuradas se validan antes de corregir las entregas.

Los pesos son opcionales y por defecto valen `1` para cada categoría. Se pueden definir globalmente (`quality.weights`) y sobrescribir por ejercicio. La penalización de cada categoría crece proporcionalmente al número de incumplimientos hasta alcanzar el máximo asignado a esa categoría; `violationsForFullCategoryPenalty` fija cuántos incumplimientos alcanzan ese máximo (por defecto, `3`). Los umbrales de complejidad, anidamiento y longitud de método tienen valores por defecto `10`, `3` y `30`, respectivamente, y se elevan si la referencia supera dichos valores. Las reglas incumplidas por la referencia se desactivan automáticamente para ese ejercicio y se registran en el log. También se pueden desactivar explícitamente mediante `disabledRules`.

Las categorías y sus reglas son:

| Categoría | Qué revisa | Reglas |
|---|---|---|
| `duplication` | Código repetido y reutilización | Bloques duplicados; constructores que repiten lógica o no delegan con `this(...)`. |
| `deadCode` | Código que no se utiliza | Métodos/campos privados, variables locales o parámetros privados e imports sin uso; código Java comentado. |
| `complexity` | Complejidad y legibilidad de métodos | Complejidad ciclomática, anidamiento y longitud excesivos; condicionales innecesarios; múltiples `break`/`continue` en un bucle. |
| `javaIdioms` | Uso claro y adecuado de Java | Comparaciones de `String` con `==`/`!=`, variables devueltas inmediatamente, literales numéricos repetidos, atributos inicializados en la declaración y saltos de línea explícitos. |
| `naming` | Claridad y convenciones de nombres | Nombres fuera de camelCase, constantes fuera de UPPER_SNAKE_CASE y nombres de una letra o poco descriptivos. |

Cada categoría incluye una explicación en el informe. Los incumplimientos se muestran agrupados por categoría con fichero, línea, identificador de regla y mensaje breve.

## Ejecución

Desde la raíz del repositorio:

```sh
./gradlew test
./gradlew shadowJar
java -cp build/libs/correctionDPOO-1.0-SNAPSHOT-all.jar edu.uoc.correction.Main
```

En Windows, usa `gradlew.bat` para ejecutar Gradle. La aplicación no recibe argumentos de línea de comandos; utiliza las rutas de `config.properties`.

Los informes se generan dentro del directorio configurado para informes. También se crea un ZIP AES cifrado por estudiante y `PASSWORD.txt`, que asocia cada archivo con su contraseña y nota.
