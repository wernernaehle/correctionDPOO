package edu.uoc.correction.service;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.DocTrees;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import edu.uoc.correction.model.Configuration;
import edu.uoc.correction.model.ExerciceConfiguration;
import edu.uoc.correction.model.Exercise;
import edu.uoc.correction.model.JavadocClassConfiguration;
import edu.uoc.correction.model.JavadocClassResult;
import edu.uoc.correction.model.JavadocElementType;
import edu.uoc.correction.model.JavadocEvaluation;
import edu.uoc.correction.model.JavadocExerciseConfiguration;
import com.sun.source.doctree.AuthorTree;
import com.sun.source.doctree.BlockTagTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.doctree.VersionTree;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class JavadocEvaluator {

    private static final Logger LOGGER = Logger.getLogger(JavadocEvaluator.class.getName());
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]*>");
    private static final Pattern INLINE_TAG = Pattern.compile("\\{@(?:code|link|linkplain|literal)\\s+([^}]*)}");
    private static final Pattern NON_WORD = Pattern.compile("[^\\p{L}\\p{N}]+");
    private final Configuration configuration;
    private final Map<String, Map<String, ParsedClass>> referenceClasses = new LinkedHashMap<>();
    private boolean validated;

    public JavadocEvaluator(Configuration configuration) {
        this.configuration = configuration;
    }

    public void validateConfiguration() {
        if (validated) {
            return;
        }
        Map<Path, Map<String, List<ParsedClass>>> parsedReferenceDirectories = new LinkedHashMap<>();
        for (Map.Entry<String, ExerciceConfiguration> entry
                : configuration.getMapExercisesTests().entrySet()) {
            JavadocExerciseConfiguration exerciseConfiguration =
                    entry.getValue().getJavadocConfiguration();
            if (exerciseConfiguration == null || !exerciseConfiguration.isEnabled()) {
                continue;
            }
            Map<String, ParsedClass> exerciseReferences = new LinkedHashMap<>();
            for (JavadocClassConfiguration classConfiguration : exerciseConfiguration.getClasses()) {
                Path referenceDirectory = resolveReferenceDirectory(entry.getKey(), classConfiguration);
                Map<String, List<ParsedClass>> parsed = parsedReferenceDirectories.get(referenceDirectory);
                if (parsed == null) {
                    try {
                        parsed = parseSourceDirectory(referenceDirectory);
                        parsedReferenceDirectories.put(referenceDirectory, parsed);
                    } catch (IOException | IllegalArgumentException e) {
                        throw invalidReference(entry.getKey(), classConfiguration, referenceDirectory,
                                "could not parse reference sources: " + e.getMessage(), e);
                    }
                }
                List<ParsedClass> matchingClasses = parsed.getOrDefault(
                        classConfiguration.getClassName(), Collections.emptyList());
                if (matchingClasses.isEmpty()) {
                    throw invalidReference(entry.getKey(), classConfiguration, referenceDirectory,
                            "class does not exist in the reference source");
                }
                if (matchingClasses.size() > 1) {
                    throw invalidReference(entry.getKey(), classConfiguration, referenceDirectory,
                            "class name is ambiguous in the reference source");
                }
                ParsedClass referenceClass = matchingClasses.get(0);
                if (!hasAnyJavadoc(referenceClass)) {
                    throw invalidReference(entry.getKey(), classConfiguration, referenceDirectory,
                            "reference class has no Javadoc with content");
                }
                exerciseReferences.put(classConfiguration.getClassName(), referenceClass);
            }
            referenceClasses.put(entry.getKey(), exerciseReferences);
        }
        validated = true;
    }

    public JavadocEvaluation evaluate(Exercise exercise) {
        ExerciceConfiguration exerciseConfiguration =
                configuration.getMapExercisesTests().get(exercise.getName());
        JavadocExerciseConfiguration javadocConfiguration = exerciseConfiguration == null
                ? null : exerciseConfiguration.getJavadocConfiguration();
        if (javadocConfiguration == null || !javadocConfiguration.isEnabled()) {
            exercise.setJavadocEvaluation(null);
            return null;
        }
        validateConfiguration();

        List<JavadocClassResult> classResults = new ArrayList<>();
        if (exercise.getTestsuiteList() == null) {
            for (JavadocClassConfiguration classConfiguration : javadocConfiguration.getClasses()) {
                classResults.add(new JavadocClassResult(classConfiguration.getClassName(),
                        0, classConfiguration.getPresenceScore(), 0,
                        classConfiguration.getQualityMaxScore(), false, false,
                        "No se evalúa la Javadoc porque el ejercicio no compila.", Collections.emptyList()));
            }
            JavadocEvaluation evaluation = new JavadocEvaluation(classResults);
            exercise.setJavadocEvaluation(evaluation);
            return evaluation;
        }

        Path sourceDirectory = exercise.getSourceDirectory() == null
                ? null : Paths.get(exercise.getSourceDirectory());
        Map<String, List<ParsedClass>> studentClasses;
        try {
            studentClasses = sourceDirectory != null && Files.isDirectory(sourceDirectory)
                    ? parseSourceDirectory(sourceDirectory) : Collections.emptyMap();
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.warning("Could not parse Javadoc source for exercise " + exercise.getName()
                    + ": " + e.getMessage());
            throw new IllegalStateException("Could not parse Javadoc source for exercise "
                    + exercise.getName(), e);
        }

        Map<String, ParsedClass> exerciseReferences = referenceClasses.getOrDefault(
                exercise.getName(), Collections.emptyMap());
        for (JavadocClassConfiguration classConfiguration : javadocConfiguration.getClasses()) {
            List<ParsedClass> matches = studentClasses.getOrDefault(
                    classConfiguration.getClassName(), Collections.emptyList());
            if (matches.isEmpty()) {
                String message = "No se ha encontrado la clase " + classConfiguration.getClassName()
                        + " en la entrega.";
                LOGGER.warning(message + " Exercise: " + exercise.getName());
                classResults.add(new JavadocClassResult(classConfiguration.getClassName(),
                        0, classConfiguration.getPresenceScore(), 0,
                        classConfiguration.getQualityMaxScore(), false, false,
                        message, Collections.emptyList()));
                continue;
            }
            if (matches.size() > 1) {
                String message = "La clase " + classConfiguration.getClassName()
                        + " aparece más de una vez en la entrega; no se puede evaluar.";
                LOGGER.warning(message + " Exercise: " + exercise.getName());
                classResults.add(new JavadocClassResult(classConfiguration.getClassName(),
                        0, classConfiguration.getPresenceScore(), 0,
                        classConfiguration.getQualityMaxScore(), false, false,
                        message, Collections.emptyList()));
                continue;
            }

            ParsedClass studentClass = matches.get(0);
            if (!hasAnyJavadoc(studentClass)) {
                classResults.add(new JavadocClassResult(classConfiguration.getClassName(),
                        0, classConfiguration.getPresenceScore(), 0,
                        classConfiguration.getQualityMaxScore(), true, false,
                        "No se han encontrado comentarios Javadoc.", Collections.emptyList()));
                continue;
            }

            ParsedClass referenceClass = exerciseReferences.get(classConfiguration.getClassName());
            List<String> details = new ArrayList<>();
            double qualityRatio = calculateQuality(referenceClass, studentClass,
                    classConfiguration.getInclude(), details);
            double qualityScore = roundToTwoDecimals(
                    classConfiguration.getQualityMaxScore() * qualityRatio);
            classResults.add(new JavadocClassResult(classConfiguration.getClassName(),
                    classConfiguration.getPresenceScore(), classConfiguration.getPresenceScore(),
                    qualityScore, classConfiguration.getQualityMaxScore(), true, true,
                    "", details));
        }
        JavadocEvaluation evaluation = new JavadocEvaluation(classResults);
        exercise.setJavadocEvaluation(evaluation);
        return evaluation;
    }

    private double calculateQuality(ParsedClass reference, ParsedClass student,
                                    Set<JavadocElementType> include, List<String> details) {
        List<Double> elementScores = new ArrayList<>();
        if (include.contains(JavadocElementType.CLASS) && reference.documentation != null) {
            elementScores.add(scoreClass(reference, student, details));
        }
        if (include.contains(JavadocElementType.FIELDS)) {
            for (ParsedField referenceField : reference.fields) {
                if (!hasContent(referenceField.documentation)) {
                    continue;
                }
                ParsedField studentField = student.fieldsByName.get(referenceField.name);
                if (studentField == null) {
                    details.add(referenceField.name + ": no evaluado: no existe en la entrega");
                    continue;
                }
                elementScores.add(scoreReferenceElement(referenceField.name,
                        referenceField.documentation, Collections.emptyList(), studentField.documentation,
                        Collections.emptyList(), details));
            }
        }
        if (include.contains(JavadocElementType.CONSTRUCTORS)) {
            for (ParsedMethod referenceMethod : reference.methods) {
                if (!referenceMethod.constructor || !hasContent(referenceMethod.documentation)) {
                    continue;
                }
                addMethodScore(referenceMethod, student.methodsBySignature, details, elementScores);
            }
        }
        if (include.contains(JavadocElementType.PUBLIC_METHODS)) {
            for (ParsedMethod referenceMethod : reference.methods) {
                if (referenceMethod.constructor || !referenceMethod.isPublic
                        || !hasContent(referenceMethod.documentation)) {
                    continue;
                }
                addMethodScore(referenceMethod, student.methodsBySignature, details, elementScores);
            }
        }
        if (include.contains(JavadocElementType.PRIVATE_METHODS) && reference.hasDocumentedPrivateMethods()) {
            for (ParsedMethod studentMethod : student.methods) {
                if (!studentMethod.isPrivate) {
                    continue;
                }
                elementScores.add(scorePrivateMethod(studentMethod, details));
            }
        }
        if (elementScores.isEmpty()) {
            return 0;
        }
        return elementScores.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private void addMethodScore(ParsedMethod referenceMethod,
                                Map<String, ParsedMethod> studentMethods,
                                List<String> details, List<Double> elementScores) {
        String signature = referenceMethod.signature;
        ParsedMethod studentMethod = studentMethods.get(signature);
        if (studentMethod == null) {
            details.add(signature + ": no evaluado: no existe en la entrega");
            return;
        }
        elementScores.add(scoreReferenceElement(signature, referenceMethod.documentation,
                referenceMethod.parameterNames, studentMethod.documentation,
                studentMethod.parameterNames, details));
    }

    private double scoreClass(ParsedClass reference, ParsedClass student, List<String> details) {
        List<Criterion> criteria = new ArrayList<>();
        if (hasText(reference.documentation.description)) {
            criteria.add(new Criterion("descripción", isMeaningfulDescription(
                    student.documentation == null ? "" : student.documentation.description,
                    student.name)));
        }
        if (reference.documentation.hasAuthorTag()) {
            criteria.add(new Criterion("@author", student.documentation != null
                    && student.documentation.hasUsefulAuthor()));
        }
        if (reference.documentation.hasVersionTag()) {
            criteria.add(new Criterion("@version", student.documentation != null
                    && student.documentation.hasUsefulVersion()));
        }
        return finishElementScore(reference.name, criteria, details);
    }

    private double scoreReferenceElement(String label, JavadocDoc referenceDocumentation,
                                         List<String> referenceParameterNames,
                                         JavadocDoc studentDocumentation,
                                         List<String> studentParameterNames,
                                         List<String> details) {
        List<Criterion> criteria = new ArrayList<>();
        if (hasText(referenceDocumentation.description)) {
            criteria.add(new Criterion("descripción", isMeaningfulDescription(
                    studentDocumentation == null ? "" : studentDocumentation.description,
                    elementName(label))));
        }
        for (Map.Entry<String, String> referenceParameter
                : referenceDocumentation.parameters.entrySet()) {
            int position = referenceParameterNames.indexOf(referenceParameter.getKey());
            String studentParameter = position >= 0 && position < studentParameterNames.size()
                    ? studentParameterNames.get(position) : referenceParameter.getKey();
            boolean documented = studentDocumentation != null
                    && hasUsefulText(studentDocumentation.parameters.get(studentParameter));
            criteria.add(new Criterion("@param " + studentParameter, documented));
        }
        if (referenceDocumentation.returnTag) {
            criteria.add(new Criterion("@return", studentDocumentation != null
                    && hasUsefulText(studentDocumentation.returnDescription)));
        }
        for (Map.Entry<String, String> referenceException : referenceDocumentation.exceptions.entrySet()) {
            String exceptionName = simpleTypeName(referenceException.getKey());
            boolean documented = studentDocumentation != null
                    && studentDocumentation.exceptions.entrySet().stream().anyMatch(actual ->
                    exceptionName.equals(simpleTypeName(actual.getKey())) && hasUsefulText(actual.getValue()));
            criteria.add(new Criterion("@throws " + exceptionName, documented));
        }
        return finishElementScore(label, criteria, details);
    }

    private double scorePrivateMethod(ParsedMethod method, List<String> details) {
        JavadocDoc documentation = method.documentation;
        boolean hasUsableDocumentation = documentation != null
                && (isMeaningfulDescription(documentation.description, method.name)
                || documentation.hasUsefulTagText());
        List<Criterion> criteria = new ArrayList<>();
        criteria.add(new Criterion("descripción o etiqueta", hasUsableDocumentation));
        for (String parameterName : method.parameterNames) {
            criteria.add(new Criterion("@param " + parameterName,
                    documentation != null && hasUsefulText(documentation.parameters.get(parameterName))));
        }
        if (!"void".equals(method.returnType)) {
            criteria.add(new Criterion("@return", documentation != null
                    && hasUsefulText(documentation.returnDescription)));
        }
        for (String exceptionType : method.thrownTypes) {
            String exceptionName = simpleTypeName(exceptionType);
            boolean documented = documentation != null
                    && documentation.exceptions.entrySet().stream().anyMatch(actual ->
                    exceptionName.equals(simpleTypeName(actual.getKey())) && hasUsefulText(actual.getValue()));
            criteria.add(new Criterion("@throws " + exceptionName, documented));
        }
        return finishElementScore(method.signature, criteria, details);
    }

    private double finishElementScore(String label, List<Criterion> criteria, List<String> details) {
        if (criteria.isEmpty()) {
            return 0;
        }
        int passed = 0;
        for (Criterion criterion : criteria) {
            if (criterion.passed) {
                passed++;
            } else {
                details.add(label + ": falta " + criterion.label);
            }
        }
        return (double) passed / criteria.size();
    }

    private boolean hasAnyJavadoc(ParsedClass parsedClass) {
        if (hasContent(parsedClass.documentation)) {
            return true;
        }
        if (parsedClass.fields.stream().anyMatch(field -> hasContent(field.documentation))) {
            return true;
        }
        return parsedClass.methods.stream().anyMatch(method -> hasContent(method.documentation));
    }

    private boolean hasContent(JavadocDoc documentation) {
        return documentation != null && documentation.hasContent();
    }

    private boolean hasText(String value) {
        return JavadocDoc.hasRawText(value);
    }

    private boolean hasUsefulText(String value) {
        String cleaned = visibleText(value).trim();
        if (cleaned.isEmpty()) {
            return false;
        }
        String lower = cleaned.toLowerCase(Locale.ROOT);
        return !lower.matches("(?s).*(\\btodo\\b|\\bfixme\\b|\\btbd\\b|not implemented|documentation pending).*");
    }

    private boolean isMeaningfulDescription(String description, String elementName) {
        if (description == null || description.contains("{@inheritDoc}")) {
            return description != null && description.contains("{@inheritDoc}");
        }
        String cleaned = visibleText(description).toLowerCase(Locale.ROOT).trim();
        if (!hasUsefulText(cleaned)) {
            return false;
        }
        String words = NON_WORD.matcher(cleaned).replaceAll(" ").trim();
        String normalizedName = NON_WORD.matcher(elementName.toLowerCase(Locale.ROOT))
                .replaceAll(" ").trim();
        if (normalizedName.isEmpty()) {
            return true;
        }
        if (words.equals(normalizedName) || words.equals("the " + normalizedName)
                || words.equals("a " + normalizedName) || words.equals("an " + normalizedName)) {
            return false;
        }
        String descriptiveWords = Arrays.stream(words.split("\\s+"))
                .filter(word -> !Arrays.asList("this", "is", "the", "a", "an", "gets",
                        "get", "sets", "set", "returns", "return", "value", "of", "class",
                        "method", "field", "constructor", "description", "documentation",
                        "property", "generated", "default", "for").contains(word))
                .collect(Collectors.joining(" "));
        if (descriptiveWords.isEmpty() || descriptiveWords.equals(normalizedName)) {
            return false;
        }
        String compact = words.replace(" ", "");
        String compactName = normalizedName.replace(" ", "");
        if (compact.equals("descriptionof" + compactName)
                || compact.equals("documentationfor" + compactName)
                || compact.equals("the" + compactName + "method")
                || compact.equals("the" + compactName + "class")
                || compact.equals("the" + compactName + "field")) {
            return false;
        }
        return true;
    }

    private String visibleText(String value) {
        if (value == null) {
            return "";
        }
        String withInlineText = INLINE_TAG.matcher(value).replaceAll("$1");
        return HTML_TAG.matcher(withInlineText.replace("{@inheritDoc}", "inheritDoc"))
                .replaceAll(" ").replaceAll("\\s+", " ");
    }

    private String elementName(String label) {
        int openingParenthesis = label.indexOf('(');
        return openingParenthesis > 0 ? label.substring(0, openingParenthesis) : label;
    }

    private Path resolveReferenceDirectory(String exerciseName,
                                          JavadocClassConfiguration classConfiguration) {
        Path workingDirectory = Paths.get(System.getProperty("user.dir"));
        String configuredPath = classConfiguration.getReferencePath();
        if (configuredPath != null) {
            Path path = Paths.get(configuredPath);
            return (path.isAbsolute() ? path : workingDirectory.resolve(path)).normalize();
        }

        String exerciseSuffix = exerciseName.startsWith(configuration.getName())
                ? exerciseName.substring(configuration.getName().length()) : exerciseName;
        List<Path> candidates = Arrays.asList(
                workingDirectory.resolve(Paths.get("reference-solutions",
                        configuration.getName(), exerciseSuffix)),
                workingDirectory.resolve(Paths.get("reference-solutions",
                        "DPOO_" + configuration.getName() + "_sol", exerciseName + "_sol")));
        for (Path candidate : candidates) {
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }
        return candidates.get(0);
    }

    private IllegalArgumentException invalidReference(String exerciseName,
                                                       JavadocClassConfiguration classConfiguration,
                                                       Path referenceDirectory, String reason) {
        return invalidReference(exerciseName, classConfiguration, referenceDirectory, reason, null);
    }

    private IllegalArgumentException invalidReference(String exerciseName,
                                                       JavadocClassConfiguration classConfiguration,
                                                       Path referenceDirectory, String reason,
                                                       Exception cause) {
        String property = exerciseName + ".javadoc.classes";
        String configuredReferencePath = classConfiguration.getReferencePath();
        String referenceProperty = configuredReferencePath == null ? ""
                : ", property '" + exerciseName + ".javadoc.referencePath' with value '"
                + configuredReferencePath + "'";
        String message = "Invalid Javadoc configuration for exercise " + exerciseName
                + ", property '" + property + "' with value '" + classConfiguration.getClassName()
                + "': " + reason + referenceProperty + " (reference path: " + referenceDirectory + ")";
        return cause == null ? new IllegalArgumentException(message)
                : new IllegalArgumentException(message, cause);
    }

    private Map<String, List<ParsedClass>> parseSourceDirectory(Path sourceDirectory) throws IOException {
        if (!Files.isDirectory(sourceDirectory)) {
            return Collections.emptyMap();
        }
        List<Path> sourceFiles;
        try (Stream<Path> paths = Files.walk(sourceDirectory)) {
            sourceFiles = paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .sorted(Comparator.comparing(Path::toString))
                    .collect(Collectors.toList());
        }
        if (sourceFiles.isEmpty()) {
            return Collections.emptyMap();
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("A JDK with the Java compiler is required to evaluate Javadoc");
        }
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        Map<String, List<ParsedClass>> parsedClasses = new TreeMap<>();
        try (StandardJavaFileManager fileManager =
                     compiler.getStandardFileManager(diagnostics, Locale.ROOT, null)) {
            Iterable<? extends JavaFileObject> javaFiles = fileManager.getJavaFileObjectsFromPaths(sourceFiles);
            JavacTask task = (JavacTask) compiler.getTask(null, fileManager, diagnostics,
                    Collections.singletonList("-proc:none"), null, javaFiles);
            DocTrees docTrees = DocTrees.instance(task);
            for (CompilationUnitTree unit : task.parse()) {
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitClass(ClassTree classTree, Void unused) {
                        ParsedClass parsedClass = parseClass(classTree, getCurrentPath(), docTrees);
                        parsedClasses.computeIfAbsent(parsedClass.name, ignored -> new ArrayList<>())
                                .add(parsedClass);
                        return super.visitClass(classTree, unused);
                    }
                }.scan(unit, null);
            }
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Java source parsing failed: " + e.getMessage(), e);
        }

        List<Diagnostic<? extends JavaFileObject>> errors = diagnostics.getDiagnostics().stream()
                .filter(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR)
                .collect(Collectors.toList());
        if (!errors.isEmpty()) {
            String diagnosticText = errors.stream().limit(5)
                    .map(diagnostic -> diagnostic.getMessage(Locale.ROOT))
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException("Java source parsing failed: " + diagnosticText);
        }
        return parsedClasses;
    }

    private ParsedClass parseClass(ClassTree classTree, TreePath classPath, DocTrees docTrees) {
        String className = classTree.getSimpleName().toString();
        JavadocDoc documentation = readDocumentation(classPath, docTrees);
        List<ParsedField> fields = new ArrayList<>();
        List<ParsedMethod> methods = new ArrayList<>();
        Map<String, ParsedField> fieldsByName = new LinkedHashMap<>();
        Map<String, ParsedMethod> methodsBySignature = new LinkedHashMap<>();
        for (Tree member : classTree.getMembers()) {
            TreePath memberPath = new TreePath(classPath, member);
            if (member instanceof VariableTree) {
                VariableTree field = (VariableTree) member;
                ParsedField parsedField = new ParsedField(field.getName().toString(),
                        readDocumentation(memberPath, docTrees));
                fields.add(parsedField);
                fieldsByName.put(parsedField.name, parsedField);
            } else if (member instanceof MethodTree) {
                ParsedMethod parsedMethod = parseMethod(className, (MethodTree) member,
                        memberPath, docTrees);
                methods.add(parsedMethod);
                methodsBySignature.put(parsedMethod.signature, parsedMethod);
            }
        }
        return new ParsedClass(className, documentation, fields, fieldsByName,
                methods, methodsBySignature);
    }

    private ParsedMethod parseMethod(String className, MethodTree methodTree,
                                     TreePath methodPath, DocTrees docTrees) {
        boolean constructor = methodTree.getReturnType() == null;
        String methodName = constructor ? className : methodTree.getName().toString();
        List<String> parameterNames = methodTree.getParameters().stream()
                .map(parameter -> parameter.getName().toString()).collect(Collectors.toList());
        List<String> parameterTypes = methodTree.getParameters().stream()
                .map(parameter -> normalizeType(parameter.getType())).collect(Collectors.toList());
        String signature = methodName + "(" + String.join(",", parameterTypes) + ")";
        String returnType = constructor ? "void" : normalizeType(methodTree.getReturnType());
        List<String> thrownTypes = methodTree.getThrows().stream()
                .map(this::normalizeType).collect(Collectors.toList());
        Set<javax.lang.model.element.Modifier> modifiers = methodTree.getModifiers().getFlags();
        return new ParsedMethod(methodName, signature, constructor,
                modifiers.contains(javax.lang.model.element.Modifier.PUBLIC),
                modifiers.contains(javax.lang.model.element.Modifier.PRIVATE),
                parameterNames, returnType, thrownTypes,
                readDocumentation(methodPath, docTrees));
    }

    private JavadocDoc readDocumentation(TreePath path, DocTrees docTrees) {
        DocCommentTree docTree = docTrees.getDocCommentTree(path);
        if (docTree == null) {
            return null;
        }
        String description = docTree.getFullBody().stream()
                .map(DocTree::toString).collect(Collectors.joining());
        JavadocDoc documentation = new JavadocDoc(description);
        for (DocTree docTag : docTree.getBlockTags()) {
            BlockTagTree blockTag = (BlockTagTree) docTag;
            if (blockTag instanceof ParamTree) {
                ParamTree parameter = (ParamTree) blockTag;
                if (!parameter.isTypeParameter()) {
                    documentation.parameters.put(parameter.getName().getName().toString(),
                            parameter.getDescription().stream().map(DocTree::toString)
                                    .collect(Collectors.joining()));
                }
            } else if (blockTag instanceof ReturnTree) {
                ReturnTree returnTag = (ReturnTree) blockTag;
                documentation.returnTag = true;
                documentation.returnDescription = returnTag.getDescription().stream()
                        .map(DocTree::toString).collect(Collectors.joining());
            } else if (blockTag instanceof ThrowsTree) {
                ThrowsTree throwsTag = (ThrowsTree) blockTag;
                documentation.exceptions.put(throwsTag.getExceptionName().toString(),
                        throwsTag.getDescription().stream().map(DocTree::toString)
                                .collect(Collectors.joining()));
            } else if (blockTag instanceof AuthorTree) {
                documentation.authors.add(((AuthorTree) blockTag).getName().stream()
                        .map(DocTree::toString).collect(Collectors.joining()));
            } else if (blockTag instanceof VersionTree) {
                documentation.versions.add(((VersionTree) blockTag).getBody().stream()
                        .map(DocTree::toString).collect(Collectors.joining()));
            } else {
                documentation.otherTags.add(blockTag.toString().replaceFirst("^@[A-Za-z]+\\s*", ""));
            }
        }
        return documentation;
    }

    private String normalizeType(Tree type) {
        if (type instanceof AnnotatedTypeTree) {
            return normalizeType(((AnnotatedTypeTree) type).getUnderlyingType());
        }
        if (type instanceof ArrayTypeTree) {
            return normalizeType(((ArrayTypeTree) type).getType()) + "[]";
        }
        if (type instanceof ParameterizedTypeTree) {
            ParameterizedTypeTree parameterized = (ParameterizedTypeTree) type;
            return normalizeType(parameterized.getType()) + "<"
                    + parameterized.getTypeArguments().stream().map(this::normalizeType)
                    .collect(Collectors.joining(",")) + ">";
        }
        if (type instanceof WildcardTree) {
            WildcardTree wildcard = (WildcardTree) type;
            if (wildcard.getBound() == null) {
                return "?";
            }
            String bound = wildcard.getKind()
                    == com.sun.source.tree.Tree.Kind.EXTENDS_WILDCARD ? "extends" : "super";
            return "? " + bound
                    + " " + normalizeType(wildcard.getBound());
        }
        if (type instanceof PrimitiveTypeTree) {
            return type.toString();
        }
        if (type instanceof IdentifierTree) {
            return ((IdentifierTree) type).getName().toString();
        }
        if (type instanceof MemberSelectTree) {
            return ((MemberSelectTree) type).getIdentifier().toString();
        }
        String typeName = type.toString();
        int lastDot = typeName.lastIndexOf('.');
        return lastDot >= 0 ? typeName.substring(lastDot + 1) : typeName;
    }

    private String simpleTypeName(String typeName) {
        String normalized = typeName.trim();
        int genericStart = normalized.indexOf('<');
        if (genericStart >= 0) {
            normalized = normalized.substring(0, genericStart);
        }
        int lastDot = normalized.lastIndexOf('.');
        return lastDot >= 0 ? normalized.substring(lastDot + 1) : normalized;
    }

    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static final class Criterion {
        private final String label;
        private final boolean passed;

        private Criterion(String label, boolean passed) {
            this.label = label;
            this.passed = passed;
        }
    }

    private static final class JavadocDoc {
        private final String description;
        private final Map<String, String> parameters = new LinkedHashMap<>();
        private final Map<String, String> exceptions = new LinkedHashMap<>();
        private final List<String> authors = new ArrayList<>();
        private final List<String> versions = new ArrayList<>();
        private final List<String> otherTags = new ArrayList<>();
        private boolean returnTag;
        private String returnDescription;

        private JavadocDoc(String description) {
            this.description = description;
        }

        private boolean hasContent() {
            if (description != null && !description.trim().isEmpty()) {
                return true;
            }
            return parameters.values().stream().anyMatch(JavadocDoc::hasRawText)
                    || exceptions.values().stream().anyMatch(JavadocDoc::hasRawText)
                    || hasRawText(returnDescription)
                    || authors.stream().anyMatch(JavadocDoc::hasRawText)
                    || versions.stream().anyMatch(JavadocDoc::hasRawText)
                    || otherTags.stream().anyMatch(JavadocDoc::hasRawText);
        }

        private boolean hasAuthorTag() {
            return !authors.isEmpty();
        }

        private boolean hasVersionTag() {
            return !versions.isEmpty();
        }

        private boolean hasUsefulAuthor() {
            return authors.stream().anyMatch(JavadocDoc::hasRawText);
        }

        private boolean hasUsefulVersion() {
            return versions.stream().anyMatch(JavadocDoc::hasRawText);
        }

        private boolean hasUsefulTagText() {
            return parameters.values().stream().anyMatch(JavadocDoc::hasRawText)
                    || exceptions.values().stream().anyMatch(JavadocDoc::hasRawText)
                    || hasRawText(returnDescription)
                    || authors.stream().anyMatch(JavadocDoc::hasRawText)
                    || versions.stream().anyMatch(JavadocDoc::hasRawText)
                    || otherTags.stream().anyMatch(JavadocDoc::hasRawText);
        }

        private static boolean hasRawText(String text) {
            if (text == null) {
                return false;
            }
            String inlineText = INLINE_TAG.matcher(text).replaceAll("$1")
                    .replace("{@inheritDoc}", "inheritDoc");
            String visible = HTML_TAG.matcher(inlineText).replaceAll("").trim();
            return !visible.isEmpty();
        }
    }

    private static final class ParsedClass {
        private final String name;
        private final JavadocDoc documentation;
        private final List<ParsedField> fields;
        private final Map<String, ParsedField> fieldsByName;
        private final List<ParsedMethod> methods;
        private final Map<String, ParsedMethod> methodsBySignature;

        private ParsedClass(String name, JavadocDoc documentation, List<ParsedField> fields,
                            Map<String, ParsedField> fieldsByName, List<ParsedMethod> methods,
                            Map<String, ParsedMethod> methodsBySignature) {
            this.name = name;
            this.documentation = documentation;
            this.fields = fields;
            this.fieldsByName = fieldsByName;
            this.methods = methods;
            this.methodsBySignature = methodsBySignature;
        }

        private boolean hasDocumentedPrivateMethods() {
            return methods.stream().anyMatch(method -> method.isPrivate
                    && method.documentation != null && method.documentation.hasContent());
        }
    }

    private static final class ParsedField {
        private final String name;
        private final JavadocDoc documentation;

        private ParsedField(String name, JavadocDoc documentation) {
            this.name = name;
            this.documentation = documentation;
        }
    }

    private static final class ParsedMethod {
        private final String name;
        private final String signature;
        private final boolean constructor;
        private final boolean isPublic;
        private final boolean isPrivate;
        private final List<String> parameterNames;
        private final String returnType;
        private final List<String> thrownTypes;
        private final JavadocDoc documentation;

        private ParsedMethod(String name, String signature, boolean constructor,
                             boolean isPublic, boolean isPrivate, List<String> parameterNames,
                             String returnType, List<String> thrownTypes, JavadocDoc documentation) {
            this.name = name;
            this.signature = signature;
            this.constructor = constructor;
            this.isPublic = isPublic;
            this.isPrivate = isPrivate;
            this.parameterNames = parameterNames;
            this.returnType = returnType;
            this.thrownTypes = thrownTypes;
            this.documentation = documentation;
        }
    }
}
