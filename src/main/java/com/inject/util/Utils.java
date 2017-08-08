package com.inject.util;

import java.lang.reflect.Field;
import java.util.List;

import javax.lang.model.element.Element;

/**
 * Created by august on 2017/8/6.
 */

public class Utils {
    public static String firstLetterToUpper(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }


    public static void addToListIfNeed(Logger logger, List<Entity> sList, Element elementAnnotatedWith) {
        for (int i = 0; i < sList.size(); i++) {
            Entity entity = sList.get(i);
            if (entity.clz.toString().equals(getOwner(elementAnnotatedWith).toString())) {
                logger.note("Extra注解检索：" + elementAnnotatedWith.toString() + "->" + entity.clz.toString());
                entity.fieldList.add(elementAnnotatedWith);

            }
        }
    }

    public static Field getField(Object object, String field) {
        Class<?> aClass = object.getClass();
        try {
            return aClass.getField(field);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getValue(Object object, String field) {
        Field field1 = getField(object, field);
        if (field1 == null) {
            return null;
        }
        try {
            return field1.get(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Element getOwner(Element element) {
        return (Element) getValue(element, "owner");
    }

    public static final String BINDER_SUFFIX = "ExtraBinder";

    public static String getBinderName(String routerName) {
        return routerName + BINDER_SUFFIX;
    }
}
