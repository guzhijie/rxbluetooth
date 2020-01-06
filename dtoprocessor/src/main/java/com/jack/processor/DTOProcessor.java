package com.jack.processor;

import com.google.auto.service.AutoService;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.jack.dto.annotation.DTO;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;


/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/12/23 0023
 */
@AutoService(value = {Processor.class})
@SupportedAnnotationTypes({"com.jack.dto.annotation.DTO"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class DTOProcessor extends AbstractProcessor {
    private Messager m_messager;
    private Filer  m_filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        m_messager = processingEnvironment.getMessager();
        m_filer =  processingEnvironment.getFiler();
        m_messager.printMessage(Diagnostic.Kind.NOTE, m_filer.toString());
        super.init(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        for (TypeElement typeElement : set) {
            m_messager.printMessage(Diagnostic.Kind.NOTE, typeElement.getSimpleName().toString());
        }
        try {
            generatorDTOBean();
        } catch (FormatterException | IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> stringSet = new HashSet<>();
        stringSet.add(DTO.class.getName());
        return stringSet;
    }

    private void generatorDTOBean() throws FormatterException, IOException {
        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.      println($S)", System.class, "Hello, JavaPoet!")
                .build();

        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(main)
                .build();

        JavaFile javaFile = JavaFile.builder("com.example.helloworld", helloWorld)
                .build();
//        CharSource source = new CharSource() {
//            @Override
//            public Reader openStream() throws IOException {
//                return new StringReader(javaFile.toString());
//            }
//        };
//        CharSink output = new CharSink() {
//            @Override
//            public Writer openStream() throws IOException {
//                return new OutputStreamWriter(System.out);
//            }
//        };
//        new Formatter().formatSource(source, output);
//        javaFile.writeTo(System.out);
        javaFile.writeTo(m_filer);
    }
}
