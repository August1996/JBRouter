package com.inject.util;

import java.util.List;

import javax.lang.model.element.Element;

/**
 * Created by august on 2017/8/6.
 */

public class Entity {
    public Element clz;
    public List<Element> fieldList;

    public Entity(Element clz, List<Element> fieldList) {
        this.clz = clz;
        this.fieldList = fieldList;
    }
}
