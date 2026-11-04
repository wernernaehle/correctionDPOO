package edu.uoc.correction.service;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import edu.uoc.correction.model.Configuration;
import edu.uoc.correction.model.ExerciceConfiguration;
import edu.uoc.correction.model.Exercise;
import edu.uoc.correction.model.QualityCategory;
import edu.uoc.correction.model.QualityCategoryResult;
import edu.uoc.correction.model.QualityEvaluation;
import edu.uoc.correction.model.QualityExerciseConfiguration;
import edu.uoc.correction.model.QualityRule;
import edu.uoc.correction.model.QualityViolation;

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
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CodeQualityEvaluator {
    private static final Logger LOGGER = Logger.getLogger(CodeQualityEvaluator.class.getName());
    private static final Pattern COMMENT = Pattern.compile("(?s)/\\*.*?\\*/|//[^\\r\\n]*");
    private static final Pattern COMMENTED_STATEMENT = Pattern.compile(
            "(?m)^\\s*(?:return\\b|(?:if|for|while|switch|try|throw|new)\\b|"
                    + "(?:\\w+\\s+)+\\w+\\s*=|\\w+\\s*\\([^;]*\\)\\s*;)");

    private final Configuration configuration;
    private final Map<String, ReferenceProfile> references = new LinkedHashMap<>();
    private boolean validated;

    public CodeQualityEvaluator(Configuration configuration) {
        this.configuration = configuration;
    }

    public void validateConfiguration() {
        if (validated) {
            return;
        }
        for (Map.Entry<String, ExerciceConfiguration> entry
                : configuration.getMapExercisesTests().entrySet()) {
            QualityExerciseConfiguration quality = entry.getValue().getQualityConfiguration();
            if (quality == null) {
                continue;
            }
            Map<String, ParsedSource> referenceSources;
            try {
                referenceSources = parseSources(quality.getReferencePath(), entry.getKey());
            } catch (IllegalStateException e) {
                throw new IllegalArgumentException("Invalid code-quality configuration for exercise "
                        + entry.getKey() + " property '" + entry.getKey() + ".quality.referencePath' with value '"
                        + quality.getReferencePath() + "': could not parse reference sources: "
                        + e.getMessage(), e);
            }
            Map<String, List<QualityViolation>> referenceViolations = new LinkedHashMap<>();
            Map<String, Metrics> metrics = new LinkedHashMap<>();
            List<DuplicateSignature> referenceBlocks = new ArrayList<>();
            for (String className : quality.getClasses()) {
                ParsedSource source = referenceSources.get(className);
                if (source == null) {
                    throw configurationError(entry.getKey(), quality, className,
                            "class source could not be parsed");
                }
                Analysis analysis = analyze(source, quality, className);
                referenceViolations.put(className, analysis.violations);
                metrics.put(className, analysis.metrics);
                referenceBlocks.addAll(analysis.duplicateBlocks);
            }
            Set<QualityRule> disabled = quality.getDisabledRules().isEmpty()
                    ? EnumSet.noneOf(QualityRule.class) : EnumSet.copyOf(quality.getDisabledRules());
            for (Map.Entry<String, List<QualityViolation>> classEntry : referenceViolations.entrySet()) {
                Set<QualityRule> violated = classEntry.getValue().stream()
                        .map(QualityViolation::getRule).collect(Collectors.toSet());
                for (QualityRule rule : violated) {
                    if (rule == QualityRule.CYCLOMATIC_COMPLEXITY
                            || rule == QualityRule.NESTING_DEPTH || rule == QualityRule.METHOD_LENGTH) {
                        continue;
                    }
                    if (disabled.add(rule)) {
                        LOGGER.warning("Regla " + rule.getId() + " desactivada en " + entry.getKey()
                                + " porque la solución de referencia la incumple");
                    }
                }
            }
            if (hasDuplicateBlocks(referenceBlocks) && disabled.add(QualityRule.DUPLICATED_BLOCKS)) {
                LOGGER.warning("Regla " + QualityRule.DUPLICATED_BLOCKS.getId() + " desactivada en "
                        + entry.getKey() + " porque la solución de referencia la incumple");
            }
            references.put(entry.getKey(), new ReferenceProfile(referenceViolations, metrics, disabled));
        }
        validated = true;
    }

    QualityEvaluation evaluate(Exercise exercise, double scoreObtained) {
        ExerciceConfiguration exerciseConfiguration =
                configuration.getMapExercisesTests().get(exercise.getName());
        QualityExerciseConfiguration quality = exerciseConfiguration == null
                ? null : exerciseConfiguration.getQualityConfiguration();
        if (quality == null) {
            exercise.setQualityEvaluation(null);
            return null;
        }
        validateConfiguration();
        ReferenceProfile reference = references.get(exercise.getName());
        Path sourceRoot = exercise.getSourceDirectory() == null
                ? null : Paths.get(exercise.getSourceDirectory());
        Map<String, ParsedSource> studentSources = sourceRoot == null
                ? Collections.emptyMap() : parseSources(sourceRoot, exercise.getName());
        List<QualityViolation> violations = new ArrayList<>();
        List<DuplicateSignature> studentBlocks = new ArrayList<>();
        for (String className : quality.getClasses()) {
            ParsedSource source = studentSources.get(className);
            if (source == null) {
                continue;
            }
            Analysis analysis = analyze(source, quality, className);
            Metrics referenceMetrics = reference.metrics.get(className);
            violations.addAll(applyReferenceThresholds(analysis, quality, referenceMetrics,
                    reference.disabledRules));
            studentBlocks.addAll(analysis.duplicateBlocks);
        }
        if (!reference.disabledRules.contains(QualityRule.DUPLICATED_BLOCKS)) {
            violations.addAll(duplicateViolations(studentBlocks));
        }
        violations.sort(Comparator.comparing(QualityViolation::getFile)
                .thenComparingLong(QualityViolation::getLine)
                .thenComparing(violation -> violation.getRule().getId()));

        List<QualityCategoryResult> categories = new ArrayList<>();
        double weightTotal = quality.getWeights().values().stream()
                .mapToDouble(Double::doubleValue).sum();
        double totalPenalty = 0;
        for (QualityCategory category : QualityCategory.values()) {
            int count = 0;
            for (QualityViolation violation : violations) {
                if (category.getKey().equals(violation.getCategory())) {
                    count++;
                }
            }
            double weight = quality.getWeights().getOrDefault(category, 0.0);
            double rawPenalty = quality.getMaxPenalty() * (weight / weightTotal)
                    * Math.min(1.0, count / (double) quality.getViolationsForFullCategoryPenalty());
            categories.add(new QualityCategoryResult(
                    category.getKey(), category.getDescription(), count, round(rawPenalty)));
            totalPenalty += rawPenalty;
        }
        totalPenalty = Math.min(quality.getMaxPenalty(), round(totalPenalty));
        double finalScore = Math.max(0, round(scoreObtained - totalPenalty));
        QualityEvaluation evaluation = new QualityEvaluation(
                scoreObtained, totalPenalty, quality.getMaxPenalty(), finalScore, categories, violations);
        exercise.setQualityEvaluation(evaluation);
        return evaluation;
    }

    private List<QualityViolation> applyReferenceThresholds(Analysis analysis,
                                                              QualityExerciseConfiguration quality,
                                                              Metrics referenceMetrics,
                                                              Set<QualityRule> disabled) {
        List<QualityViolation> result = new ArrayList<>();
        for (QualityViolation violation : analysis.violations) {
            QualityRule rule = violation.getRule();
            if (disabled.contains(rule)) {
                continue;
            }
            if (rule == QualityRule.CYCLOMATIC_COMPLEXITY
                    && violation.getMeasuredValue() <= Math.max(
                    quality.getComplexityThreshold(), referenceMetrics.maxComplexity)) continue;
            if (rule == QualityRule.NESTING_DEPTH
                    && violation.getMeasuredValue() <= Math.max(
                    quality.getNestingThreshold(), referenceMetrics.maxNesting)) continue;
            if (rule == QualityRule.METHOD_LENGTH
                    && violation.getMeasuredValue() <= Math.max(
                    quality.getMethodLengthThreshold(), referenceMetrics.maxMethodLength)) continue;
            result.add(violation);
        }
        return result;
    }

    private Analysis analyze(ParsedSource source, QualityExerciseConfiguration quality, String className) {
        Analyzer analyzer = new Analyzer(source, quality, className);
        analyzer.scan(source.unit, null);
        analyzer.finish();
        return new Analysis(analyzer.violations, analyzer.metrics, analyzer.duplicateSignatures());
    }

    private boolean hasDuplicateBlocks(List<DuplicateSignature> blocks) {
        return blocks.stream().collect(Collectors.groupingBy(block -> block.fingerprint))
                .values().stream().anyMatch(group -> group.size() > 1);
    }

    private List<QualityViolation> duplicateViolations(List<DuplicateSignature> blocks) {
        List<QualityViolation> violations = new ArrayList<>();
        for (List<DuplicateSignature> group : blocks.stream()
                .collect(Collectors.groupingBy(block -> block.fingerprint)).values()) {
            for (int i = 1; i < group.size(); i++) {
                DuplicateSignature duplicate = group.get(i);
                violations.add(new QualityViolation(escapeHtml(duplicate.file), duplicate.line,
                        QualityRule.DUPLICATED_BLOCKS,
                        "se repite un bloque de cinco sentencias."));
            }
        }
        return violations;
    }

    private Map<String, ParsedSource> parseSources(Path directory, String exerciseName) {
        if (!Files.isDirectory(directory)) {
            throw new IllegalStateException("Code-quality source directory does not exist for "
                    + exerciseName + ": " + directory);
        }
        Path sourceRoot = Files.isDirectory(directory.resolve("src/main/java"))
                ? directory.resolve("src/main/java")
                : Files.isDirectory(directory.resolve("src")) ? directory.resolve("src") : directory;
        List<Path> files;
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            files = paths.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.toString().contains("/test/"))
                    .sorted().collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("Could not read code-quality sources for " + exerciseName
                    + " from " + sourceRoot, e);
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("A JDK compiler is required for code-quality analysis");
        }
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        Map<String, ParsedSource> parsed = new LinkedHashMap<>();
        try (StandardJavaFileManager fileManager =
                     compiler.getStandardFileManager(diagnostics, Locale.ROOT, null)) {
            Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromPaths(files);
            JavacTask task = (JavacTask) compiler.getTask(null, fileManager, diagnostics,
                    Arrays.asList("-proc:none"), null, units);
            for (CompilationUnitTree unit : task.parse()) {
                Path path = Paths.get(unit.getSourceFile().toUri());
                ParsedSource source = new ParsedSource(path, unit, Trees.instance(task).getSourcePositions(),
                        Files.readString(path));
                for (Tree declaration : unit.getTypeDecls()) {
                    if (declaration instanceof ClassTree) {
                        parsed.put(((ClassTree) declaration).getSimpleName().toString(), source);
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Could not parse code-quality sources for " + exerciseName, e);
        }
        List<Diagnostic<? extends JavaFileObject>> errors = diagnostics.getDiagnostics().stream()
                .filter(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR)
                .collect(Collectors.toList());
        if (!errors.isEmpty()) {
            String details = errors.stream().limit(5)
                    .map(diagnostic -> diagnostic.getMessage(Locale.ROOT))
                    .collect(Collectors.joining("; "));
            throw new IllegalStateException("Could not parse code-quality sources for " + exerciseName
                    + ": " + details);
        }
        return parsed;
    }

    private IllegalArgumentException configurationError(String exerciseName,
                                                        QualityExerciseConfiguration quality,
                                                        String className, String reason) {
        return new IllegalArgumentException("Invalid code-quality configuration for exercise "
                + exerciseName + " property '" + exerciseName + ".quality.classes' with value '"
                + String.join(",", quality.getClasses()) + "': " + className + " " + reason);
    }

    private static double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private final class Analyzer extends TreePathScanner<Void, Void> {
        private final ParsedSource source;
        private final QualityExerciseConfiguration quality;
        private final String selectedClass;
        private final List<QualityViolation> violations = new ArrayList<>();
        private final Metrics metrics = new Metrics();
        private final Map<String, Integer> identifierUses = new HashMap<>();
        private final Map<String, Integer> literals = new HashMap<>();
        private final List<MethodTree> methods = new ArrayList<>();
        private final List<MethodTree> constructors = new ArrayList<>();
        private final List<VariableTree> fields = new ArrayList<>();
        private final List<VariableTree> localsAndParameters = new ArrayList<>();
        private final List<VariableTree> parameters = new ArrayList<>();
        private final Set<String> stringVariables = new HashSet<>();

        private Analyzer(ParsedSource source, QualityExerciseConfiguration quality, String selectedClass) {
            this.source = source;
            this.quality = quality;
            this.selectedClass = selectedClass;
        }

        @Override
        public Void visitIdentifier(com.sun.source.tree.IdentifierTree node, Void unused) {
            identifierUses.merge(node.getName().toString(), 1, Integer::sum);
            return super.visitIdentifier(node, unused);
        }

        @Override
        public Void visitVariable(VariableTree node, Void unused) {
            if (node.getType() != null && node.getType().toString().equals("String")) {
                stringVariables.add(node.getName().toString());
            }
            if (getCurrentPath() != null && getCurrentPath().getParentPath() != null) {
                Tree parent = getCurrentPath().getParentPath().getLeaf();
                if (parent instanceof MethodTree) {
                    parameters.add(node);
                } else if (!(parent instanceof ClassTree)) {
                    localsAndParameters.add(node);
                }
            }
            return super.visitVariable(node, unused);
        }

        @Override
        public Void visitClass(ClassTree node, Void unused) {
            String className = node.getSimpleName().toString();
            if (!selectedClass.equals(className)) {
                return null;
            }
            for (Tree member : node.getMembers()) {
                if (member instanceof VariableTree) {
                    VariableTree field = (VariableTree) member;
                    fields.add(field);
                    boolean staticFinal = field.getModifiers().getFlags().contains(
                            javax.lang.model.element.Modifier.STATIC)
                            && field.getModifiers().getFlags().contains(javax.lang.model.element.Modifier.FINAL);
                    if (!field.getModifiers().getFlags().contains(javax.lang.model.element.Modifier.STATIC)
                            && field.getInitializer() != null) {
                        add(field, QualityRule.FIELD_INITIALIZER,
                                "inicializa el atributo '" + field.getName() + "' en el constructor.");
                    }
                    if (staticFinal && !field.getName().toString().matches("[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)*")) {
                        add(field, QualityRule.NON_UPPER_SNAKE_CONSTANT,
                                "la constante '" + field.getName() + "' debe usar UPPER_SNAKE_CASE.");
                    } else if (!staticFinal) {
                        checkName(field, field.getName().toString());
                    }
                }
            }
            return super.visitClass(node, unused);
        }

        @Override
        public Void visitMethod(MethodTree node, Void unused) {
            methods.add(node);
            boolean constructor = node.getReturnType() == null;
            if (constructor) {
                constructors.add(node);
            } else {
                checkName(node, node.getName().toString());
            }
            for (VariableTree parameter : node.getParameters()) {
                checkName(parameter, parameter.getName().toString());
            }
            if (node.getBody() != null) analyzeMethod(node);
            return super.visitMethod(node, unused);
        }

        @Override
        public Void visitLiteral(LiteralTree node, Void unused) {
            collectLiteral(node);
            if (node.getValue() instanceof String) {
                String value = (String) node.getValue();
                if (value.contains("\n") || value.contains("\r\n")) {
                    add(node, QualityRule.EXPLICIT_LINE_SEPARATOR,
                            "usa System.lineSeparator() en lugar de un salto de línea literal.");
                }
            }
            return super.visitLiteral(node, unused);
        }

        @Override
        public Void visitBinary(BinaryTree node, Void unused) {
            if (node.getKind() == Tree.Kind.EQUAL_TO || node.getKind() == Tree.Kind.NOT_EQUAL_TO) {
                String left = node.getLeftOperand().toString();
                String right = node.getRightOperand().toString();
            boolean nullComparison = node.getLeftOperand() instanceof LiteralTree
                    && ((LiteralTree) node.getLeftOperand()).getValue() == null
                        || node.getRightOperand() instanceof LiteralTree
                    && ((LiteralTree) node.getRightOperand()).getValue() == null;
            if (!nullComparison && (node.getLeftOperand() instanceof LiteralTree
                    && ((LiteralTree) node.getLeftOperand()).getValue() instanceof String
                    || node.getRightOperand() instanceof LiteralTree
                    && ((LiteralTree) node.getRightOperand()).getValue() instanceof String
                    || stringVariables.contains(left) || stringVariables.contains(right))) {
                add(node, QualityRule.STRING_REFERENCE_COMPARISON,
                        "los String se comparan con " + (node.getKind() == Tree.Kind.EQUAL_TO ? "==" : "!=")
                                + "; usa equals().");
            }
            }
            return super.visitBinary(node, unused);
        }

        @Override
        public Void visitIf(IfTree node, Void unused) {
            boolean thenReturns = exits(node.getThenStatement());
            if (node.getElseStatement() != null
                    && node.getThenStatement() instanceof com.sun.source.tree.ReturnTree
                    && node.getElseStatement() instanceof com.sun.source.tree.ReturnTree) {
                Tree thenExpression = ((com.sun.source.tree.ReturnTree) node.getThenStatement()).getExpression();
                Tree elseExpression = ((com.sun.source.tree.ReturnTree) node.getElseStatement()).getExpression();
                if (thenExpression != null && elseExpression != null
                        && thenExpression.toString().equals("true") && elseExpression.toString().equals("false")) {
                    add(node, QualityRule.REDUNDANT_CONDITIONAL,
                            "devuelve directamente la condición en lugar de usar if/else.");
                }
            } else if (node.getElseStatement() != null && thenReturns) {
                add(node, QualityRule.REDUNDANT_CONDITIONAL,
                        "elimina el else posterior a una instrucción que ya termina el bloque.");
            }
            return super.visitIf(node, unused);
        }

        @Override
        public Void visitImport(com.sun.source.tree.ImportTree node, Void unused) {
            String importName = node.getQualifiedIdentifier().toString();
            String simpleName = importName.substring(importName.lastIndexOf('.') + 1);
            String withoutImport = source.contents.replaceAll("(?m)^\\s*import\\s+[^;]+;\\s*", " ");
            if (!simpleName.equals("*") && !Pattern.compile("\\b" + Pattern.quote(simpleName) + "\\b")
                    .matcher(withoutImport).find()) {
                add(node, QualityRule.UNUSED_IMPORT, "el import '" + importName + "' no se usa.");
            }
            return super.visitImport(node, unused);
        }

        @Override
        public Void visitBreak(BreakTree node, Void unused) {
            return super.visitBreak(node, unused);
        }

        @Override
        public Void visitContinue(ContinueTree node, Void unused) {
            return super.visitContinue(node, unused);
        }

        private void analyzeMethod(MethodTree method) {
            BlockTree body = method.getBody();
            int complexity = 1;
            int nesting = nestingDepth(body);
            int statementCount = countStatements(body);
            MethodMetricsVisitor visitor = new MethodMetricsVisitor();
            visitor.scan(body, null);
            complexity += visitor.decisions;
            metrics.maxComplexity = Math.max(metrics.maxComplexity, complexity);
            metrics.maxNesting = Math.max(metrics.maxNesting, nesting);
            metrics.maxMethodLength = Math.max(metrics.maxMethodLength, statementCount);
            if (complexity > quality.getComplexityThreshold()) {
                add(method, QualityRule.CYCLOMATIC_COMPLEXITY,
                        "la complejidad ciclomática (" + complexity + ") supera el umbral.", complexity);
            }
            if (nesting > quality.getNestingThreshold()) {
                add(method, QualityRule.NESTING_DEPTH,
                        "el anidamiento (" + nesting + ") supera el umbral.", nesting);
            }
            if (statementCount > quality.getMethodLengthThreshold()) {
                add(method, QualityRule.METHOD_LENGTH,
                        "el método contiene " + statementCount + " sentencias y es demasiado largo.",
                        statementCount);
            }
            for (Tree loop : visitor.loopsWithMultipleJumps) {
                add(loop, QualityRule.MULTIPLE_LOOP_JUMPS,
                        "el bucle contiene más de un break o continue.");
            }
            List<? extends StatementTree> statements = body.getStatements();
            if (statements.size() >= 5) {
                for (int start = 0; start + 5 <= statements.size(); start++) {
                    String block = statements.subList(start, start + 5).stream()
                            .map(tree -> normalize(tree.toString())).collect(Collectors.joining("|"));
                    Tree duplicateStart = statements.get(start);
                    long position = source.positions.getStartPosition(source.unit, duplicateStart);
                    long line = position < 0 ? 1 : source.unit.getLineMap().getLineNumber(position);
                    duplicateBlocks.add(new DuplicateBlock(block,
                            source.path.getFileName().toString(), line));
                }
            }
            if (statements.size() >= 2
                    && statements.get(statements.size() - 1) instanceof com.sun.source.tree.ReturnTree
                    && statements.get(statements.size() - 2) instanceof VariableTree) {
                VariableTree variable = (VariableTree) statements.get(statements.size() - 2);
                String returnName = ((com.sun.source.tree.ReturnTree)
                        statements.get(statements.size() - 1)).getExpression() == null ? ""
                        : ((com.sun.source.tree.ReturnTree) statements.get(statements.size() - 1))
                        .getExpression().toString();
                if (variable.getName().toString().equals(returnName)) {
                    add(variable, QualityRule.IMMEDIATE_RETURN_VARIABLE,
                            "devuelve directamente la expresión en lugar de la variable '" + returnName + "'.");
                }
            }
        }

        private final List<DuplicateBlock> duplicateBlocks = new ArrayList<>();

        private void finish() {
            for (VariableTree field : fields) {
                if (field.getModifiers().getFlags().contains(javax.lang.model.element.Modifier.PRIVATE)
                        && identifierUses.getOrDefault(field.getName().toString(), 0) == 0) {
                    add(field, QualityRule.UNUSED_PRIVATE_FIELD,
                            "el atributo privado '" + field.getName() + "' no se utiliza.");
                }
            }
            for (VariableTree variable : localsAndParameters) {
                if (identifierUses.getOrDefault(variable.getName().toString(), 0) == 0) {
                    add(variable, QualityRule.UNUSED_LOCAL_OR_PARAMETER,
                            "la variable local '" + variable.getName() + "' no se utiliza.");
                }
            }
            for (VariableTree parameter : parameters) {
                boolean privateParameter = methods.stream().anyMatch(method ->
                        method.getParameters().contains(parameter)
                                && method.getModifiers().getFlags()
                                .contains(javax.lang.model.element.Modifier.PRIVATE));
                if (privateParameter
                        && identifierUses.getOrDefault(parameter.getName().toString(), 0) == 0) {
                    add(parameter, QualityRule.UNUSED_LOCAL_OR_PARAMETER,
                            "el parámetro '" + parameter.getName() + "' no se utiliza.");
                }
            }
            for (Map.Entry<String, Integer> literal : literals.entrySet()) {
                if (literal.getValue() > 1 && !isCommonLiteral(literal.getKey())) {
                    addRepeatedLiterals(literal.getKey());
                }
            }
            for (MethodTree method : methods) {
                if (method.getModifiers().getFlags().contains(javax.lang.model.element.Modifier.PRIVATE)
                        && identifierUses.getOrDefault(method.getName().toString(), 0) <= 1) {
                    add(method, QualityRule.UNUSED_PRIVATE_METHOD,
                            "el método privado '" + method.getName() + "' no se utiliza.");
                }
            }
            checkConstructorDuplication();
            checkDefaultConstructorAssignments();
            detectCommentedCode();
        }

        private List<DuplicateSignature> duplicateSignatures() {
            return duplicateBlocks.stream().map(block ->
                    new DuplicateSignature(block.fingerprint, block.file, block.line))
                    .collect(Collectors.toList());
        }

        private void checkConstructorDuplication() {
            Map<String, MethodTree> firstByBody = new HashMap<>();
            for (MethodTree constructor : constructors) {
                List<? extends StatementTree> statements = constructor.getBody() == null
                        ? Collections.emptyList() : constructor.getBody().getStatements();
                if (!statements.isEmpty() && isThisInvocation(statements.get(0))) {
                    continue;
                }
                String body = normalize(constructor.getBody() == null ? "" : constructor.getBody().toString());
                MethodTree previous = firstByBody.putIfAbsent(body, constructor);
                if (previous != null) {
                    add(constructor, QualityRule.CONSTRUCTOR_DELEGATION,
                            "este constructor repite la lógica de otro; delega con this(...).");
                }
            }
        }

        private void checkDefaultConstructorAssignments() {
            for (MethodTree constructor : constructors) {
                if (constructor.getParameters().isEmpty() && constructor.getBody() != null
                        && !constructor.getBody().getStatements().isEmpty()
                        && !isThisInvocation(constructor.getBody().getStatements().get(0))) {
                    for (StatementTree statement : constructor.getBody().getStatements()) {
                        if (statement instanceof com.sun.source.tree.ExpressionStatementTree
                                && ((com.sun.source.tree.ExpressionStatementTree) statement).getExpression()
                                instanceof AssignmentTree) {
                            add(statement, QualityRule.DEFAULT_CONSTRUCTOR_DELEGATION,
                                    "el constructor sin parámetros debe delegar con this(...).");
                            break;
                        }
                    }
                }
            }
        }

        private boolean isThisInvocation(StatementTree statement) {
            return statement instanceof com.sun.source.tree.ExpressionStatementTree
                    && ((com.sun.source.tree.ExpressionStatementTree) statement).getExpression()
                    instanceof MethodInvocationTree
                    && ((MethodInvocationTree) ((com.sun.source.tree.ExpressionStatementTree)
                    statement).getExpression()).getMethodSelect().toString().equals("this");
        }

        private void detectCommentedCode() {
            Matcher matcher = COMMENT.matcher(source.contents);
            while (matcher.find()) {
                String comment = matcher.group();
                if (comment.startsWith("/**")) {
                    continue;
                }
                String commentBody = comment.replaceAll("(?m)^\\s*(?://|/\\*+|\\*)\\s?", "")
                        .replace("*/", "");
                Matcher code = COMMENTED_STATEMENT.matcher(commentBody);
                if (code.find()) {
                    long line = source.contents.substring(0, matcher.start()).chars().filter(ch -> ch == '\n').count() + 1;
                    addAt(line, QualityRule.COMMENTED_OUT_CODE,
                            "el comentario contiene código Java que debería eliminarse.");
                }
            }
        }

        private void collectLiteral(LiteralTree literal) {
            Object value = literal.getValue();
            if (value instanceof Number) {
                String key = value.toString();
                literals.merge(key, 1, Integer::sum);
            }
        }

        private boolean isCommonLiteral(String value) {
            try {
                BigDecimal number = new BigDecimal(value).stripTrailingZeros();
                return number.compareTo(BigDecimal.ZERO) == 0
                        || number.compareTo(BigDecimal.ONE) == 0
                        || number.compareTo(BigDecimal.ONE.negate()) == 0;
            } catch (NumberFormatException ignored) {
                return false;
            }
        }

        private void addRepeatedLiterals(String value) {
            new TreePathScanner<Void, Void>() {
                @Override
                public Void visitLiteral(LiteralTree node, Void unused) {
                    if (node.getValue() instanceof Number && value.equals(node.getValue().toString())) {
                        add(node, QualityRule.REPEATED_MAGIC_LITERAL,
                                "el literal numérico '" + value + "' se repite; considera declararlo como constante.");
                    }
                    return super.visitLiteral(node, unused);
                }
            }.scan(source.unit, null);
        }

        private void checkName(Tree node, String name) {
            if (name.length() == 1 && !Set.of("i", "j", "k").contains(name)) {
                add(node, QualityRule.MEANINGLESS_NAME, "el nombre '" + name + "' es demasiado corto.");
            }
            if (!name.matches("[\\p{Ll}][\\p{L}\\p{N}$]*")) {
                add(node, QualityRule.NON_CAMEL_CASE_NAME,
                        "el nombre '" + name + "' debe seguir camelCase.");
            }
        }

        private int nestingDepth(Tree tree) {
            class NestingVisitor extends TreeScanner<Void, Integer> {
                private int maximum;
                @Override public Void scan(Tree node, Integer depth) {
                    if (node == null) return null;
                    int next = depth;
                    if (node instanceof IfTree || node.getKind() == Tree.Kind.FOR_LOOP
                            || node.getKind() == Tree.Kind.ENHANCED_FOR_LOOP
                            || node.getKind() == Tree.Kind.WHILE_LOOP
                            || node.getKind() == Tree.Kind.DO_WHILE_LOOP || node instanceof CaseTree) {
                        next++;
                        maximum = Math.max(maximum, next);
                    }
                    return super.scan(node, next);
                }
            }
            NestingVisitor visitor = new NestingVisitor();
            visitor.scan(tree, 0);
            return visitor.maximum;
        }

        private int countStatements(Tree tree) {
            final int[] count = {0};
            new TreeScanner<Void, Void>() {
                @Override
                public Void scan(Tree node, Void unused) {
                    if (node instanceof StatementTree) {
                        count[0]++;
                    }
                    return super.scan(node, unused);
                }
            }.scan(tree, null);
            return count[0];
        }

        private boolean exits(StatementTree statement) {
            if (statement instanceof BlockTree) {
                List<? extends StatementTree> statements = ((BlockTree) statement).getStatements();
                return !statements.isEmpty() && exits(statements.get(statements.size() - 1));
            }
            if (statement instanceof IfTree) {
                IfTree conditional = (IfTree) statement;
                return conditional.getElseStatement() != null
                        && exits(conditional.getThenStatement()) && exits(conditional.getElseStatement());
            }
            return statement instanceof com.sun.source.tree.ReturnTree
                    || statement instanceof com.sun.source.tree.ThrowTree
                    || statement instanceof BreakTree || statement instanceof ContinueTree;
        }

        private void add(Tree tree, QualityRule rule, String message) {
            long position = source.positions.getStartPosition(source.unit, tree);
            long line = position < 0 ? 1 : source.unit.getLineMap().getLineNumber(position);
            addAt(line, rule, message, -1);
        }

        private void addAt(long line, QualityRule rule, String message) {
            addAt(line, rule, message, -1);
        }

        private void add(Tree tree, QualityRule rule, String message, int measuredValue) {
            long position = source.positions.getStartPosition(source.unit, tree);
            long line = position < 0 ? 1 : source.unit.getLineMap().getLineNumber(position);
            addAt(line, rule, message, measuredValue);
        }

        private void addAt(long line, QualityRule rule, String message, int measuredValue) {
            String file = escapeHtml(source.path.getFileName().toString());
            violations.add(new QualityViolation(file, line, rule, escapeHtml(message), measuredValue));
        }

        private String normalize(String text) {
            return text.replaceAll("\\s+", "").trim();
        }
    }

    private static final class MethodMetricsVisitor extends TreeScanner<Void, Void> {
        private int decisions;
        private final List<Tree> loopsWithMultipleJumps = new ArrayList<>();
        private final java.util.Deque<LoopContext> loopJumpCounts = new java.util.ArrayDeque<>();

        @Override public Void visitIf(IfTree node, Void unused) {
            decisions++;
            return super.visitIf(node, unused);
        }
        @Override public Void visitBreak(BreakTree node, Void unused) {
            countLoopJump();
            return super.visitBreak(node, unused);
        }
        @Override public Void visitContinue(ContinueTree node, Void unused) {
            countLoopJump();
            return super.visitContinue(node, unused);
        }
        @Override public Void visitForLoop(com.sun.source.tree.ForLoopTree node, Void unused) {
            beginLoop(node);
            Void result = super.visitForLoop(node, unused);
            endLoop();
            return result;
        }
        @Override public Void visitEnhancedForLoop(com.sun.source.tree.EnhancedForLoopTree node, Void unused) {
            beginLoop(node);
            Void result = super.visitEnhancedForLoop(node, unused);
            endLoop();
            return result;
        }
        @Override public Void visitWhileLoop(com.sun.source.tree.WhileLoopTree node, Void unused) {
            beginLoop(node);
            Void result = super.visitWhileLoop(node, unused);
            endLoop();
            return result;
        }
        @Override public Void visitDoWhileLoop(com.sun.source.tree.DoWhileLoopTree node, Void unused) {
            beginLoop(node);
            Void result = super.visitDoWhileLoop(node, unused);
            endLoop();
            return result;
        }
        @Override public Void visitCase(CaseTree node, Void unused) {
            decisions++;
            return super.visitCase(node, unused);
        }
        @Override public Void visitBinary(BinaryTree node, Void unused) {
            if (node.getKind() == Tree.Kind.CONDITIONAL_AND
                    || node.getKind() == Tree.Kind.CONDITIONAL_OR) decisions++;
            return super.visitBinary(node, unused);
        }
        @Override public Void scan(Tree node, Void unused) {
            if (node != null && (node.getKind() == Tree.Kind.FOR_LOOP
                    || node.getKind() == Tree.Kind.ENHANCED_FOR_LOOP
                    || node.getKind() == Tree.Kind.WHILE_LOOP
                    || node.getKind() == Tree.Kind.DO_WHILE_LOOP)) {
                decisions++;
            }
            if (node != null && node.getKind() == Tree.Kind.CONDITIONAL_EXPRESSION) {
                decisions++;
            }
            return super.scan(node, unused);
        }

        private void beginLoop(Tree loop) {
            loopJumpCounts.push(new LoopContext(loop));
        }

        private void endLoop() {
            LoopContext context = loopJumpCounts.pop();
            if (context.jumps > 1) loopsWithMultipleJumps.add(context.loop);
        }

        private void countLoopJump() {
            if (!loopJumpCounts.isEmpty()) loopJumpCounts.peek().jumps++;
        }

        private static final class LoopContext {
            private final Tree loop;
            private int jumps;
            private LoopContext(Tree loop) { this.loop = loop; }
        }
    }

    private static final class ParsedSource {
        private final Path path;
        private final CompilationUnitTree unit;
        private final SourcePositions positions;
        private final String contents;
        private ParsedSource(Path path, CompilationUnitTree unit, SourcePositions positions, String contents) {
            this.path = path;
            this.unit = unit;
            this.positions = positions;
            this.contents = contents;
        }
    }

    private static final class Metrics {
        private int maxComplexity;
        private int maxNesting;
        private int maxMethodLength;
    }

    private static final class Analysis {
        private final List<QualityViolation> violations;
        private final Metrics metrics;
        private final List<DuplicateSignature> duplicateBlocks;
        private Analysis(List<QualityViolation> violations, Metrics metrics,
                         List<DuplicateSignature> duplicateBlocks) {
            this.violations = violations;
            this.metrics = metrics;
            this.duplicateBlocks = duplicateBlocks;
        }
    }

    private static final class ReferenceProfile {
        private final Map<String, List<QualityViolation>> violations;
        private final Map<String, Metrics> metrics;
        private final Set<QualityRule> disabledRules;
        private ReferenceProfile(Map<String, List<QualityViolation>> violations,
                                 Map<String, Metrics> metrics, Set<QualityRule> disabledRules) {
            this.violations = violations;
            this.metrics = metrics;
            this.disabledRules = disabledRules;
        }
    }

    private static final class DuplicateBlock {
        private final String fingerprint;
        private final String file;
        private final long line;
        private DuplicateBlock(String fingerprint, String file, long line) {
            this.fingerprint = fingerprint;
            this.file = file;
            this.line = line;
        }
    }

    private static final class DuplicateSignature {
        private final String fingerprint;
        private final String file;
        private final long line;
        private DuplicateSignature(String fingerprint, String file, long line) {
            this.fingerprint = fingerprint;
            this.file = file;
            this.line = line;
        }
    }
}
