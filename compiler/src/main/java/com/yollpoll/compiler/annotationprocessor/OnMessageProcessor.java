package com.yollpoll.compiler.annotationprocessor;


import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.yollpoll.annotation.Route;
import com.yollpoll.compiler.annotationprocessor.proxy.RouteCreatorProxy;
import com.yollpoll.compiler.annotationprocessor.proxy.RouterRegisterMapCreatorProxy;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class OnMessageProcessor extends AbstractProcessor {
    private Messager mMessage;
    private Elements mElementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMessage = processingEnv.getMessager();
        mElementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //得到所有的注解
        Set<? extends Element> elements3 = roundEnvironment.getElementsAnnotatedWith(Route.class);

        List<RouteCreatorProxy> routeCreatorProxies = getCreators(elements3);
        Map<String, String> registerMap = new HashMap<>();
        for (RouteCreatorProxy creatorProxy : routeCreatorProxies) {
            if (null == creatorProxy.list || creatorProxy.list.size() == 0) continue;

            registerMap.put(creatorProxy.host, creatorProxy.getPackageName() + "." + creatorProxy.getClassName());
            JavaFile routMapFile = JavaFile.builder(creatorProxy.getPackageName(),
                    creatorProxy.generateJavaCode()).build();
            try {
                routMapFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
                mMessage.printMessage(Diagnostic.Kind.ERROR, "error:" + e.getMessage());
            }
        }


        //路由注册表
        if (!registerMap.isEmpty()) {
            RouterRegisterMapCreatorProxy routerRegisterMapCreatorProxy
                    = new RouterRegisterMapCreatorProxy(mMessage, registerMap);
            //根据注解元素获取包名
            AtomicReference<String> packageName = new AtomicReference<>("");
            if (elements3.size() > 0) {
                elements3.forEach((element) -> {
                    packageName.set(getPackageName(element));
                });
            } else {
                packageName.set(routerRegisterMapCreatorProxy.getPackageName());
            }
            JavaFile routMapFile = JavaFile.builder(packageName.get(),
                    routerRegisterMapCreatorProxy.generateJavaCode()).build();
            try {
                routMapFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
                mMessage.printMessage(Diagnostic.Kind.ERROR, "error:" + e.getMessage());
            }
        }


        mMessage.printMessage(Diagnostic.Kind.NOTE, "process finish ...");
        return true;
    }

    private List<RouteCreatorProxy> getCreators(Set<? extends Element> elements) {
        if (null == elements || elements.isEmpty()) {
            return new ArrayList<>();
        }
//        if (null == routerUrlCreator) {
//            routerUrlCreator = new RouterUrlCreator(mElementUtils, elements.toArray(new Element[elements.size()])[0], mMessage);
//        }
        List<RouteCreatorProxy> res = new ArrayList<>();
        HashMap<String, RouteMsg> cache = new HashMap<>();
        for (Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) {
                error(element, "Only method can be annotated with @Route",
                        Route.class.getSimpleName());
                continue;
            }
            try {
                Route route = element.getAnnotation(Route.class);
//                //注册route url
//                if(null!=routerUrlCreator){
//                    routerUrlCreator.map.put(element,route);
//                }
                String url = route.url();
                URI uri = new URI(url);
                String host = uri.getHost();
                if (cache.containsKey(host)) {
                    cache.get(host).getElements().add(element);
                } else {
                    RouteMsg routeMsg = new RouteMsg();
                    routeMsg.getElements().add(element);
                    cache.put(host, routeMsg);
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        cache.forEach((key, value) -> {
            RouteCreatorProxy creatorProxy = new RouteCreatorProxy(mElementUtils, mMessage, key, value.getElements());
            res.add(creatorProxy);
        });
        return res;
    }

    //获取包名
    private String getPackageName(Element element) {
        Element enclosing = element;
        while (enclosing.getKind() != ElementKind.PACKAGE) {
            enclosing = enclosing.getEnclosingElement();
        }
        PackageElement packageElement = (PackageElement) enclosing;

        String[] pn = packageElement.getQualifiedName().toString().split("\\.");
        //只取前三层的包名
        if (pn.length >= 3) {
            return pn[0] + "." + pn[1] + "." + pn[2];
        } else {
            return packageElement.getQualifiedName().toString();
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(Route.class.getCanonicalName());
        return supportTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void error(Element e, String msg, Object... args) {
        mMessage.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }
}
