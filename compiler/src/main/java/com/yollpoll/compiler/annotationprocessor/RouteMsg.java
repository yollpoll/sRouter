package com.yollpoll.compiler.annotationprocessor;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;

/**
 * Created by spq on 2022/1/11
 */
public class RouteMsg {
    private List<Element> elements = new ArrayList<>();

    public List<Element> getElements() {
        return elements;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

}
