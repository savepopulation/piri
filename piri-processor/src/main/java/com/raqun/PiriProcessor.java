package com.raqun;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes({
        "com.raqun.PiriActivity",
        "com.raqun.PiriFragment",
        "com.raqun.PiriParam",
})
public final class PiriProcessor extends AbstractProcessor {

    //TODO remove static package name implementation
    private static final String PACKAGE_NAME = "com.raqun.piri.sample";

    private static final String CLASS_NAME_INTENT_FACTORY = "PiriIntentFactory";
    private static final String CLASS_NAME_INSTANCE_FACTORY = "PiriInstanceFactory";

    private static final ClassName intentClass = ClassName.get("android.content", "Intent");
    private static final ClassName contextClass = ClassName.get("android.content", "Context");
    private static final ClassName bundleClass = ClassName.get("android.os", "Bundle");
    //private static final ClassName fragmentClass = ClassName.get("android.app", "Fragment");
    //private static final ClassName supportFragmentClass = ClassName.get("android.support.v4.app", "Fragment");

    private static final String METHOD_PREFIX_NEW_INTENT = "newIntentFor";
    private static final String METHOD_PREFIX_NEW_INSTANCE = "newInstanceOf";

    private static final String PARAM_NAME_CONTEXT = "context";
    private static final String CLASS_SUFFIX = ".class";

    private final List<MethodSpec> newIntentMethodSpecs = new ArrayList<>();
    private final List<MethodSpec> newInstanceMethodSpecs = new ArrayList<>();
    private boolean HALT = false;
    private int round = -1;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        round++;

        if (round == 0) {
            EnvironmentUtil.init(processingEnv);
        }

        if (!processAnnotations(roundEnvironment)) {
            return HALT;
        }

        if (roundEnvironment.processingOver()) {
            try {
                createIntentFactory();
                createInstanceFactory();
                HALT = true;
            } catch (IOException ex) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.toString());
            }
        }

        return HALT;
    }

    private boolean processAnnotations(RoundEnvironment roundEnv) {
        return processActivities(roundEnv) && processFragments(roundEnv);
    }

    private boolean processActivities(RoundEnvironment roundEnv) {
        final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(PiriActivity.class);

        if (ValidationUtil.isNullOrEmpty(elements)) {
            return true;
        }

        for (Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) {
                EnvironmentUtil.logError("PiriActivity can only be used for classes!");
                return false;
            }

            if (!generateNewIntentMethod((TypeElement) element)) {
                return false;
            }
        }

        return true;
    }

    private boolean processFragments(RoundEnvironment roundEnv) {
        final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(PiriFragment.class);

        if (ValidationUtil.isNullOrEmpty(elements)) {
            return true;
        }

        for (Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) {
                EnvironmentUtil.logError("PiriFragment can only be used for classes!");
                return false;
            }

            if (!generateNewInstanceMethod((TypeElement) element)) {
                return false;
            }
        }

        return true;
    }

    private boolean generateNewIntentMethod(TypeElement element) {
        final MethodSpec.Builder navigationMethodSpecBuilder = MethodSpec
                .methodBuilder(METHOD_PREFIX_NEW_INTENT + element.getSimpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(intentClass)
                .addParameter(contextClass, PARAM_NAME_CONTEXT);

        final List<KeyElementPair> pairs = findPiriParamFields(element);
        if (!ValidationUtil.isNullOrEmpty(pairs)) {
            navigationMethodSpecBuilder.addStatement("final $T intent = new $T($L, $L)",
                    intentClass,
                    intentClass,
                    PARAM_NAME_CONTEXT,
                    element.getSimpleName() + CLASS_SUFFIX);
            for (KeyElementPair pair : pairs) {
                navigationMethodSpecBuilder.addParameter(ClassName.get(pair.element.asType()),
                        pair.element.getSimpleName().toString());
                navigationMethodSpecBuilder.addStatement("intent.putExtra($S, $L)",
                        pair.key,
                        pair.element);
            }
            navigationMethodSpecBuilder.addStatement("return intent");
        } else {
            navigationMethodSpecBuilder.addStatement("return new $T($L, $L)",
                    intentClass,
                    PARAM_NAME_CONTEXT,
                    element.getQualifiedName() + CLASS_SUFFIX);
        }

        newIntentMethodSpecs.add(navigationMethodSpecBuilder.build());
        return true;
    }

    private boolean generateNewInstanceMethod(TypeElement element) {
        final MethodSpec.Builder instanceMethodSpecBuilder = MethodSpec
                .methodBuilder(METHOD_PREFIX_NEW_INSTANCE + element.getSimpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.get(element));

        final TypeName returnType = ClassName.get(element);
        final List<KeyElementPair> pairs = findPiriParamFields(element);
        if (!ValidationUtil.isNullOrEmpty(pairs)) {
            instanceMethodSpecBuilder.addStatement("final $T args = new $T()",
                    bundleClass,
                    bundleClass);
            for (KeyElementPair pair : pairs) {
                final String statementSuffix = "($S, $L)";
                final TypeName typeName = ClassName.get(pair.element.asType());
                instanceMethodSpecBuilder.addParameter(typeName,
                        pair.element.getSimpleName().toString());
                instanceMethodSpecBuilder.addStatement(generateStatementByType(typeName) + statementSuffix,
                        pair.key,
                        pair.element);
            }
            instanceMethodSpecBuilder.addStatement("final $T instance = new $T()",
                    returnType,
                    returnType);
            instanceMethodSpecBuilder.addStatement("return instance");
        } else {
            instanceMethodSpecBuilder.addStatement("return new $T()", returnType);
        }

        newInstanceMethodSpecs.add(instanceMethodSpecBuilder.build());
        return true;
    }

    private List<KeyElementPair> findPiriParamFields(Element parent) {
        final List<? extends Element> citizens = parent.getEnclosedElements();
        if (ValidationUtil.isNullOrEmpty(citizens)) return null;

        final List<KeyElementPair> pairs = new ArrayList<>();
        for (Element citizen : citizens) {
            final PiriParam piriAnnotation = citizen.getAnnotation(PiriParam.class);
            if (piriAnnotation != null) {
                if (ValidationUtil.isNullOrEmpty(piriAnnotation.key())) {
                    EnvironmentUtil.logWarning("Using PiriParam Annotation without a Key! Field'll be ignored! " +
                            citizen.getSimpleName() + " in " + parent.getSimpleName());
                    continue;
                }
                pairs.add(new KeyElementPair(piriAnnotation.key(), citizen));
            }
        }

        return pairs;
    }

    private static String generateStatementByType(TypeName typeName) {
        switch (typeName.toString()) {
            case "long":
            case "java.lang.Long":
                return "args.putLong";

            case "java.lang.String":
                return "args.putString";
        }

        throw new IllegalArgumentException("Unsuppoerted parameter type!" + typeName.toString());
    }

    private void createIntentFactory() throws IOException {
        final TypeSpec.Builder builder = TypeSpec.classBuilder(CLASS_NAME_INTENT_FACTORY);
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        for (MethodSpec methodSpec : newIntentMethodSpecs) {
            builder.addMethod(methodSpec);
        }

        EnvironmentUtil.generateFile(builder.build(), PACKAGE_NAME);
    }

    private void createInstanceFactory() throws IOException {
        final TypeSpec.Builder builder = TypeSpec.classBuilder(CLASS_NAME_INSTANCE_FACTORY);
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        for (MethodSpec methodSpec : newInstanceMethodSpecs) {
            builder.addMethod(methodSpec);
        }

        EnvironmentUtil.generateFile(builder.build(), PACKAGE_NAME);
    }
}
