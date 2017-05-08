package com.raqun;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.raqun.piri.PiriActivity")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class PiriProcessor extends AbstractProcessor {
    private List<MethodSpec> mNavigationMethodSpecs;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.mNavigationMethodSpecs = new ArrayList<>();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        final Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(PiriActivity.class);
        for (Element element : elements) {
            if (element.getKind() == ElementKind.CLASS) {
                generateMethods(element);
            }
        }

        try {
            if (roundEnvironment.processingOver()) {
                generateNavigator();
            }
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.toString());
        }

        return true;
    }

    private <T extends Element> void generateMethods(T element) {
        if (!(element instanceof TypeElement)) {
            return;
        }
        
        final TypeElement typeElement = (TypeElement) element;
        final MethodSpec navigationMethodSpec = MethodSpec
                .methodBuilder("newIntentFor" + typeElement.getSimpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .build();

        mNavigationMethodSpecs.add(navigationMethodSpec);
    }

    private void generateNavigator() throws IOException {
        final TypeSpec.Builder builder = TypeSpec.classBuilder("Piri");
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        for (MethodSpec methodSpec : mNavigationMethodSpecs) {
            builder.addMethod(methodSpec);
        }

        final TypeSpec piriSpec = builder.build();
        final JavaFile javaFile = JavaFile.builder("com.raqun.piri.sample", piriSpec)
                .build();
        javaFile.writeTo(processingEnv.getFiler());
    }
}
