package com.yollpoll.compiler.annotationprocessor.proxy;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;

/**
 * Created by spq on 2022/1/11
 */
public class RouterRegisterMapCreatorProxy {
    private Messager messager;
    private Map<String, String> registerMap = new HashMap<>();

    public RouterRegisterMapCreatorProxy(Messager messager, Map<String, String> registerMap) {
        this.messager = messager;
        this.registerMap = registerMap;
    }

    //hashMap<String,String>
    ParameterizedType mapName = new ParameterizedType() {
        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{
                    String.class, String.class
            };
        }

        @Override
        public Type getRawType() {
            return HashMap.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    };

    //当前注册的map
    public List<FieldSpec> initSetField() {
        List<FieldSpec> list = new ArrayList<>();
        FieldSpec fieldSpec = FieldSpec
                .builder(mapName, "registerMap")
                .initializer("new HashMap<String,String>(){{" + initSetStr() + "}}")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .build();

        list.add(fieldSpec);
        return list;
    }

    //初始化map
    private String initSetStr() {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : registerMap.entrySet()) {
//            builder.append("put(\"").append(entry.getKey()).append("\",").append("");
            builder.append("put(\"" + entry.getKey() + "\",\"" + entry.getValue() + "\");");
        }
        return builder.toString();
    }

    public TypeSpec generateJavaCode() {
        return TypeSpec.classBuilder("RegisterMap")
                .addModifiers(Modifier.PUBLIC)
                .addFields(initSetField()).build();
    }


    public String getClassName() {
        return "RouterMap";
    }

    public String getPackageName() {
        return "com.aispeech.arch";
    }
}
