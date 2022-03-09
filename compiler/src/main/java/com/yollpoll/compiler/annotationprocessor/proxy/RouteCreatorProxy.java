package com.yollpoll.compiler.annotationprocessor.proxy;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import com.yollpoll.annotation.Route;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * Created by spq on 2022/1/10
 */
public class RouteCreatorProxy extends CreatorProxy {
    public List<Element> list = new ArrayList<>();
    public String host;

    public RouteCreatorProxy(Elements elementUtils, Messager messager, String host, List<Element> list) {
        super(elementUtils, list.get(0), messager);
        this.list = list;
        this.host = host;
    }

    public RouteCreatorProxy(Elements elementUtils, Element classElement, Messager messager) {
        super(elementUtils, classElement, messager);
    }


    public void add(Element element) {
        messager.printMessage(Diagnostic.Kind.NOTE, "add route:" + element.getSimpleName());
        list.add(element);
    }

    //onDispatch接口
    ClassName onDispatchListener = ClassName.get("com.yollpoll.router.dispatch", "OnDispatchListener");
    ClassName override = ClassName.get("java.lang", "Override");

    //hashMap<String,String>
    ParameterizedType hashMapName = new ParameterizedType() {
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
    ClassName contextName = ClassName.get("android.content", "Context");
    ClassName onBackListenerName = ClassName.get("com.yollpoll.router.dispatch", "OnBackListener");
    ClassName activityName = ClassName.get("android.app", "Activity");
    ClassName intentName = ClassName.get("android.content", "Intent");
    ClassName startTypeName = ClassName.get("com.yollpoll.router.dispatch", "StartType");
    ClassName fragmentName = ClassName.get("androidx.fragment.app", "Fragment");

    ParameterSpec map = ParameterSpec.builder(hashMapName, "params").build();
    ParameterSpec context = ParameterSpec.builder(contextName, "context").build();
    ParameterSpec onBackListener = ParameterSpec.builder(onBackListenerName, "onBackListener").build();

    MethodSpec onDispatch = MethodSpec.methodBuilder("onDispatch")
            .addAnnotation(override)
            .returns(String.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(map)
            .addParameter(context)
            .addParameter(onBackListener)
            .addStatement(dispatchCode(), activityName, intentName, intentName,
                    startTypeName, startTypeName, activityName, activityName)
            .build();

    private String dispatchCode() {
        return "        String module = params.get(\"module\");\n" +
                "       final String[] result = {\"\"};" +
                "        map.forEach((key, value) -> {\n" +
                "            if (key.equals(module)) {\n" +
                "                result[0] =value;\n" +
                "                try {\n" +
                "                    Class clz = Class.forName(value);\n" +
                "                    if ($T.class.isAssignableFrom(clz)) {\n" +
                "                        $T intent = new $T(context, clz);\n" +
                "                        params.forEach((pKey, pValue) -> {\n" +
                "                            intent.putExtra(pKey, pValue);\n" +
                "                        });\n" +
                "                        String startType = params.get(\"startType\");\n" +
                "                        if (startType.equals($T.START.name())) {\n" +
                "                            context.startActivity(intent);\n" +
                "                        } else if (startType.equals($T.START_FOR_RESULT.name()) && context instanceof $T && params.containsKey(\"reqCode\")) {\n" +
                "                            int reqCode = Integer.parseInt(params.get(\"reqCode\"));\n" +
                "                            (($T) context).startActivityForResult(intent, reqCode);\n" +
                "                        }\n" +
                "                     }\n" +


                "                } catch (ClassNotFoundException e) {\n" +
                "                    e.printStackTrace();\n" +
                "                }\n" +
                "            }\n" +
                "        });\n" +
                "     if (result.length > 0)\n" +
                "            return result[0];\n" +
                "        return \"\"";
    }

    @Override
    public TypeSpec generateJavaCode() {
        messager.printMessage(Diagnostic.Kind.NOTE, "className: " + host + "_DispatchListener");
        collectUrl();
        return TypeSpec.classBuilder(host + "_DispatchListener")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(onDispatchListener)
                .addMethod(onDispatch)
                .addFields(initMapField()).build();
    }


    private Set<String> schemeSet = new HashSet<>();
    private Set<String> hostSet = new HashSet<>();
    private Map<String, String> moduleMap = new HashMap<>();

    //初始化map
    public List<FieldSpec> initMapField() {
        List<FieldSpec> list = new ArrayList<>();
        FieldSpec fieldSpec = FieldSpec
                .builder(hashMapName, "map")
                .initializer("new HashMap<String,String>(){{" + initMapStr() + "}}")
                .addModifiers(Modifier.PUBLIC)
                .build();

        list.add(fieldSpec);
        return list;
    }

    //初始化map
    private String initMapStr() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : moduleMap.entrySet()) {
            builder.append("put(\"" + entry.getKey() + "\",\"" + entry.getValue() + "\");\n");
        }
        return builder.toString();
    }

    //处理url
    public void collectUrl() {
        for (Element element : list) {
            Route route = element.getAnnotation(Route.class);
//            messager.printMessage(Diagnostic.Kind.NOTE, "url: " + route.url());
            try {
                String url = route.url();
                URI uri = new URI(url);
                String query = uri.getRawQuery();
                HashMap<String, String> param = buildParams(query);
                schemeSet.add(uri.getScheme());
                hostSet.add(uri.getHost());
                if (param.containsKey("module")) {
                    String module = param.get("module");
                    moduleMap.put(module, element.asType().toString());
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
//        for (String scheme : schemeSet) {
//            messager.printMessage(Diagnostic.Kind.NOTE, "scheme: " + scheme);
//        }
//
//        for (String host : hostSet) {
//            messager.printMessage(Diagnostic.Kind.NOTE, "host: " + host);
//        }
//
//        for (Map.Entry<String, String> module : moduleMap.entrySet()) {
//            messager.printMessage(Diagnostic.Kind.NOTE, "module:" + module.getKey() + "_activity:" + module.getValue());
//        }
    }

    @Override
    public String getClassName() {
//        messager.printMessage(Diagnostic.Kind.NOTE, "className: " + host + "_DispatchListener");
        return host + "_DispatchListener";
    }

//    @Override
//    public String getPackageName() {
//        return "com.aispeech.arch";
//    }

    public static HashMap<String, String> buildParams(String query) {
        HashMap<String, String> params = new HashMap<String, String>();
        if (query == null) {
            return params;
        }
        try {
            String[] arrSplit = query.split("[&]");
            for (String strSplit : arrSplit) {
                String[] arrSplitEqual = null;
                arrSplitEqual = strSplit.split("[=]");
                if (arrSplitEqual.length > 1) {
                    params.put(arrSplitEqual[0], URLDecoder.decode(arrSplitEqual[1], "UTF-8"));

                } else {
                    if (arrSplitEqual[0] != "") {
                        params.put(arrSplitEqual[0], "");
                    }
                }
            }
            return params;
        } catch (Exception e) {
            return params;
        }
    }
}
