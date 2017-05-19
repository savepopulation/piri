package com.raqun;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.security.Key;
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
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes({
        "com.raqun.PiriActivity",
        "com.raqun.PiriParam"
})
public final class PiriProcessor extends AbstractProcessor {

    //TODO remove static package name implementation
    private static final String PACKAGE_NAME = "com.raqun.piri.sample";

    private static final ClassName intentClass = ClassName.get("android.content", "Intent");
    private static final ClassName contextClass = ClassName.get("android.content", "Context");

    private static final String METHOD_PREFIX_NEW_INTENT = "newIntentFor";
    private static final String PARAM_NAME_CONTEXT = "context";
    private static final String CLASS_SUFFIX = ".class";

    private List<MethodSpec> newIntentMethodSpecs = new ArrayList<>();
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
            Utils.init(processingEnv);
        }

        if (!processAnnotations(roundEnvironment)) {
            return HALT;
        }

        if (roundEnvironment.processingOver()) {
            try {
                generateNavigator();
                HALT = true;
            } catch (IOException ex) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.toString());
            }
        }

        return HALT;
    }

    private boolean processAnnotations(RoundEnvironment roundEnv) {
        final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(PiriActivity.class);
        for (Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) {
                //TODO Type Check
                Utils.logError("PiriActivity can only be used for classes!");
                return false;
            }

            if (!generateNewIntentMethod((TypeElement) element)) {
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
        if (!Validation.isNullOrEmpty(pairs)) {
            navigationMethodSpecBuilder.addStatement("final $T intent = new $T($L, $L)",
                    intentClass,
                    intentClass,
                    PARAM_NAME_CONTEXT,
                    element.getQualifiedName() + CLASS_SUFFIX);
            for (KeyElementPair pair : pairs) {
                navigationMethodSpecBuilder.addParameter(ClassName.get(pair.element.asType()),
                        pair.element.getSimpleName().toString());
                navigationMethodSpecBuilder.addStatement("intent.putExtra(\"" + pair.key + "\", $L)",
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

    private List<KeyElementPair> findPiriParamFields(Element parent) {
        final List<? extends Element> citizens = parent.getEnclosedElements();
        if (Validation.isNullOrEmpty(citizens)) return null;

        final List<KeyElementPair> pairs = new ArrayList<>();
        for (Element citizen : citizens) {
            final PiriParam piriAnnotation = citizen.getAnnotation(PiriParam.class);
            if (piriAnnotation != null) {
                if (Validation.isNullOrEmpty(piriAnnotation.key())) {
                    Utils.logWarning("Using PiriParam Annotation without a Key! Field'll be ignored! " +
                            citizen.getSimpleName() + " in " + parent.getSimpleName());
                    continue;
                }
                pairs.add(new KeyElementPair(piriAnnotation.key(), citizen));
            }
        }

        return pairs;
    }

    private void generateNavigator() throws IOException {
        final TypeSpec.Builder builder = TypeSpec.classBuilder("Piri");
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        for (MethodSpec methodSpec : newIntentMethodSpecs) {
            builder.addMethod(methodSpec);
        }

        final TypeSpec piriSpec = builder.build();
        JavaFile.builder(PACKAGE_NAME, piriSpec)
                .build()
                .writeTo(processingEnv.getFiler());
    }
}
