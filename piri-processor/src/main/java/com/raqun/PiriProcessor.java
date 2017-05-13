package com.raqun;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
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
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.raqun.PiriActivity")
public final class PiriProcessor extends AbstractProcessor {

    //TODO remove static package name implementation
    private static final String PACKAGE_NAME = "com.raqun.piri.sample";

    private static final ClassName intentClass = ClassName.get("android.content", "Intent");
    private static final ClassName contextClass = ClassName.get("android.content", "Context");

    private static final String METHOD_PREFIX_NEW_INTENT = "newIntentFor";
    private static final String PARAM_NAME_CONTEXT = "context";

    private List<MethodSpec> newIntentMethodSpecs = new ArrayList<>();
    private boolean HALT = false;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

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
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "PiriActivity can only be used for classes!");
                return false;
            }

            if (!generateNewIntentMethod((TypeElement) element)) {
                return false;
            }
        }

        return true;
    }

    private boolean generateNewIntentMethod(TypeElement element) {

        final PiriActivity annotation = element.getAnnotation(PiriActivity.class);

        if (annotation == null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Annotation cannot be null!");
            return false;
        }

        Class[] types = null;
        String[] keys = null;

        try {
            types = annotation.types();
            keys = annotation.names();
            
        } catch (MirroredTypesException ex) {

            // TODO write an exception handler for this
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "ops stg went wrong!");
        }

        if (keys.length != types.length) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "You must specify a parameter type for eacy key!");
            return false;
        }

        final MethodSpec.Builder navigationMethodSpecBuilder = MethodSpec
                .methodBuilder(METHOD_PREFIX_NEW_INTENT + element.getSimpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(intentClass)
                .addStatement("return new $T($L, $L)", intentClass, PARAM_NAME_CONTEXT, element.getQualifiedName() + ".class")
                .addParameter(contextClass, PARAM_NAME_CONTEXT);

        for (int i = 0; i < keys.length; i++) {
            navigationMethodSpecBuilder.addParameter(types[i], keys[i]);
        }

        newIntentMethodSpecs.add(navigationMethodSpecBuilder.build());
        return true;
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
