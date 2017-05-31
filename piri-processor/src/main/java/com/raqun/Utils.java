package com.raqun;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.tools.Diagnostic;

/**
 * Created by tyln on 19/05/2017.
 */

public final class Utils {
    private static ProcessingEnvironment processingEnvironment;

    private Utils() {
        // Empty private constructor
    }

    public static void init(ProcessingEnvironment environment) {
        processingEnvironment = environment;
    }

    public static void logError(String message) {
        processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
    }

    public static void logWarning(String message) {
        processingEnvironment.getMessager().printMessage(Diagnostic.Kind.WARNING, message);
    }

    public static void generateFile(final TypeSpec typeSpec, String packageName) throws IOException {
        JavaFile.builder(packageName, typeSpec)
                .build()
                .writeTo(processingEnvironment.getFiler());
    }
}
