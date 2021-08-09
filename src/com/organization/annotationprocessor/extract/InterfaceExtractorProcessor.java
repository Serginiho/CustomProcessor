package com.organization.annotationprocessor.extract;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class InterfaceExtractorProcessor extends AbstractProcessor {
    private List<ExecutableElement> interfaceMethods = new ArrayList<>();
    public InterfaceExtractorProcessor () {}
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for(TypeElement typeDecl: annotations) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(typeDecl);
            for (Element cl :
                    elements) {
                ExtractInterface annot = cl.getAnnotation(ExtractInterface.class);
                if (annot == null)
                    break;
                for (ExecutableElement m : ElementFilter.methodsIn(cl.getEnclosedElements()))
                    if (m.getModifiers().contains(Modifier.PUBLIC) &&
                            !(m.getModifiers().contains(Modifier.STATIC)))
                        interfaceMethods.add(m);
                if (interfaceMethods.size() > 0) {
                    try {
                        JavaFileObject fileObject = processingEnv.getFiler().createSourceFile(annot.value());
                        Writer w = fileObject.openWriter();
                        PrintWriter writer = new PrintWriter(w);
                        writer.println("package " +
                                "generated.annotation" + ";");
                        writer.println("public interface " +
                                annot.value() + " {");
                        for (ExecutableElement m : interfaceMethods) {
                            writer.print("  ");
                            writer.print(m.getReturnType() + " ");
                            writer.print(m.getSimpleName() + " (");
                            int i = 0;
                            for (VariableElement parm :
                                    m.getParameters()) {
                                writer.print(parm.asType() + " " +
                                        parm.getSimpleName());
                                if (++i < m.getParameters().size())
                                    writer.print(", ");
                            }
                            writer.println(");");
                        }
                        writer.println("}");
                        writer.close();
                    } catch (IOException ioe) {
                        throw new RuntimeException(ioe);
                    }
                }
            }
        }
        return false;
    }
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton("com.organization.annotationprocessor.extract.ExtractInterface");
    }
}
