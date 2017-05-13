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
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.raqun.PiriActivity")
public final class PiriProcessor extends AbstractProcessor {

    private static final ClassName intentClass = ClassName.get("android.content", "Intent");
    private static final ClassName contextClass = ClassName.get("android.content", "Context");
    private static final String METHOD_PREFIX_NEW_INTENT = "newIntentFor";

    private List<MethodSpec> newIntentMethodSpecs;
    private boolean HALT = false;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.newIntentMethodSpecs = new ArrayList<>();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        final Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(PiriActivity.class);
        for (Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "PiriActivity can only use for classes!");
                return HALT;
            }
            generateNewIntentMethod((TypeElement) element);
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

    private void generateNewIntentMethod(TypeElement element) {
        final MethodSpec navigationMethodSpec = MethodSpec
                .methodBuilder(METHOD_PREFIX_NEW_INTENT + element.getSimpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(contextClass, "context")
                .returns(intentClass)
                .addStatement("return new $T($L, $L)", intentClass, "context", element.getQualifiedName() + ".class")
                .build();

        newIntentMethodSpecs.add(navigationMethodSpec);
    }

    private void generateNavigator() throws IOException {
        final TypeSpec.Builder builder = TypeSpec.classBuilder("Piri");
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        for (MethodSpec methodSpec : newIntentMethodSpecs) {
            builder.addMethod(methodSpec);
        }

        final TypeSpec piriSpec = builder.build();
        JavaFile.builder("com.raqun.piri.sample", piriSpec)
                .build()
                .writeTo(processingEnv.getFiler());
    }
}
