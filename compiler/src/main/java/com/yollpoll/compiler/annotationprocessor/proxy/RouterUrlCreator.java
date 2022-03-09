package com.yollpoll.compiler.annotationprocessor.proxy;


import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import com.yollpoll.annotation.Route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;

/**
 * Created by spq on 2022/1/21
 */
public class RouterUrlCreator extends CreatorProxy {
    public Map<Element, Route> map = new HashMap<>();

    public RouterUrlCreator(Elements elementUtils, Element classElement, Messager messager) {
        super(elementUtils, classElement, messager);
    }

    /**
     * getEnclosingElement 表示包裹当前element的element
     *
     * @return 变量名
     */
    public List<FieldSpec> generateFieldSpec() {
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        for (Map.Entry<Element, Route> entry : map.entrySet()) {
            String url = entry.getValue().url();
            Element element = entry.getKey();
            String key = element.getEnclosingElement().getSimpleName() + "_" + element.getSimpleName().toString();

            FieldSpec fieldSpec = FieldSpec.builder(String.class, key, Modifier.PUBLIC, Modifier.STATIC,
                    Modifier.FINAL)
                    .initializer("$S", url)
                    .build();
            fieldSpecs.add(fieldSpec);
        }

        return fieldSpecs;
    }

    @Override
    public TypeSpec generateJavaCode() {
        TypeSpec bindingClass = TypeSpec.classBuilder(getClassName())
                .addModifiers(Modifier.PUBLIC,Modifier.FINAL)
                .addFields(generateFieldSpec())
                .build();
        return bindingClass;
    }

    @Override
    public String getClassName() {
        return "RouterUrl";
    }
}
