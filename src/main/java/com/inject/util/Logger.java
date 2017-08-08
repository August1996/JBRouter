package com.inject.util;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * Created by august on 2017/8/6.
 */

public class Logger {
    private Messager messager;

    public Logger(Messager messager) {
        this.messager = messager;
    }

    public void note(CharSequence text) {
        messager.printMessage(Diagnostic.Kind.NOTE, "----------" + text + "---------");
    }

    public void note(CharSequence text, Element element) {
        messager.printMessage(Diagnostic.Kind.NOTE, "----------" + text + "---------", element);
    }

    public void note(CharSequence text, Element element, AnnotationMirror annotationMirror) {
        messager.printMessage(Diagnostic.Kind.NOTE, "----------" + text + "---------", element, annotationMirror);
    }

    public void note(CharSequence text, Element element, AnnotationMirror annotationMirror, AnnotationValue annotationValue) {
        messager.printMessage(Diagnostic.Kind.NOTE, "----------" + text + "---------", element, annotationMirror, annotationValue);
    }

    public void error(CharSequence text) {
        messager.printMessage(Diagnostic.Kind.ERROR, "----------" + text + "---------");
    }

    public void error(CharSequence text, Element element) {
        messager.printMessage(Diagnostic.Kind.ERROR, "----------" + text + "---------", element);
    }

    public void error(CharSequence text, Element element, AnnotationMirror annotationMirror) {
        messager.printMessage(Diagnostic.Kind.ERROR, "----------" + text + "---------", element, annotationMirror);
    }

    public void error(CharSequence text, Element element, AnnotationMirror annotationMirror, AnnotationValue annotationValue) {
        messager.printMessage(Diagnostic.Kind.ERROR, "----------" + text + "---------", element, annotationMirror, annotationValue);
    }

    public void warning(CharSequence text) {
        messager.printMessage(Diagnostic.Kind.WARNING, "----------" + text + "---------");
    }

    public void warning(CharSequence text, Element element) {
        messager.printMessage(Diagnostic.Kind.WARNING, "----------" + text + "---------", element);
    }

    public void warning(CharSequence text, Element element, AnnotationMirror annotationMirror) {
        messager.printMessage(Diagnostic.Kind.WARNING, "----------" + text + "---------", element, annotationMirror);
    }

    public void warning(CharSequence text, Element element, AnnotationMirror annotationMirror, AnnotationValue annotationValue) {
        messager.printMessage(Diagnostic.Kind.WARNING, "----------" + text + "---------", element, annotationMirror, annotationValue);
    }
}
