package com.yollpoll.compiler.annotationprocessor.proxy;


import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;

/**
 * Created by spq on 2021/3/31
 */
public abstract class CreatorProxy {
    protected String mPackageName;
    protected PackageElement packageElement;
    protected Element mTypeElement;
    protected Messager messager;
    protected Elements elementUtils;

    public CreatorProxy(Elements elementUtils, Element classElement, Messager messager) {
        this.mTypeElement = classElement;
        this.messager = messager;
        this.elementUtils = elementUtils;
        initPackage(classElement);
//        PackageElement packageElement = elementUtils.getPackageOf(mTypeElement);
//        String packageName = packageElement.getQualifiedName().toString();
//        this.mPackageName = packageName;
    }

    private void initPackage(Element element) {
        Element enclosing = element;
        while (enclosing.getKind() != ElementKind.PACKAGE) {
            enclosing = enclosing.getEnclosingElement();
        }
        PackageElement packageElement = (PackageElement) enclosing;
        this.packageElement = packageElement;

        String[] pn = packageElement.getQualifiedName().toString().split("\\.");
        //只取前三层的包名
        if (pn.length >= 3) {
            mPackageName = pn[0] + "." + pn[1] + "." + pn[2];
        } else {
            mPackageName = packageElement.getQualifiedName().toString();
        }
    }

    /**
     * 创建Java代码
     *
     * @return 生成java代码
     */
    public abstract TypeSpec generateJavaCode();

    /**
     * @return 包名
     */
    public String getPackageName() {
        return mPackageName;
    }

    /**
     * @return 类名
     */
    public abstract String getClassName();
}
