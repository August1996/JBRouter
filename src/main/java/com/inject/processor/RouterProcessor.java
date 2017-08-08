package com.inject.processor;

import com.google.auto.service.AutoService;
import com.inject.Binder;
import com.inject.annotation.Extra;
import com.inject.annotation.Router;
import com.inject.util.Entity;
import com.inject.util.Logger;
import com.inject.util.Utils;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Created by august on 2017/8/05.
 */
@AutoService(Processor.class)
public class RouterProcessor extends AbstractProcessor {

    public static final String PACKAGE_NAME = "com.mimi.auto";
    public static final String CLASS_NAME = "JBRouter";

    private static final List<Entity> sList = new ArrayList<>();

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {


        Messager messager = processingEnv.getMessager();
        Filer filer = processingEnv.getFiler();
        Elements elementUtils = processingEnv.getElementUtils();
        Logger logger = new Logger(messager);

        Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(Router.class);
        if (elementsAnnotatedWith == null || elementsAnnotatedWith.size() == 0) {
            logger.warning("Router注解检索：检索不到任何Router注解");
            return true;
        }


        // 构造JBRouter类
        TypeSpec.Builder tbJBRouter = TypeSpec.classBuilder(CLASS_NAME)
                .addJavadoc("此类由apt自动生成")
                .addModifiers(Modifier.PUBLIC)
                .addField(FieldSpec.builder(ClassName.get(Map.class), "sMap", Modifier.STATIC, Modifier.PRIVATE).build());


        CodeBlock.Builder initCodeBuilder = CodeBlock.builder()
                .addStatement("new $T<String,String>()", ClassName.get(HashMap.class));

        logger.note(CLASS_NAME + "类生成：开始");


        // 添加所有类
        for (Element elementAnnotatedWith : elementsAnnotatedWith) {
            logger.note("Router注解检索：" + elementAnnotatedWith.toString());
            sList.add(new Entity(elementAnnotatedWith, new ArrayList<Element>()));
        }

        // Extra注解分类
        elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(Extra.class);
        for (Element elementAnnotatedWith : elementsAnnotatedWith) {
            if (elementAnnotatedWith.getModifiers().contains(Modifier.PRIVATE) || elementAnnotatedWith.getModifiers().contains(Modifier.PROTECTED)) {
                throw new IllegalStateException("Extra注解检索：Activity属性可见性不能为private或者protected");
            }
            Utils.addToListIfNeed(logger, sList, elementAnnotatedWith);
        }


        ClassName ctxClassName = ClassName.bestGuess("android.content.Context");
        ClassName actClassName = ClassName.bestGuess("android.app.Activity");
        ClassName intentClassName = ClassName.bestGuess("android.content.Intent");
        ClassName integerClassName = ClassName.get(Integer.class);

        AnnotationSpec nullableAnnotation = AnnotationSpec.builder(ClassName.bestGuess("android.support.annotation.Nullable")).build();
        AnnotationSpec nonnullAnnotation = AnnotationSpec.builder(ClassName.bestGuess("android.support.annotation.NonNull")).build();


        // 生成start方法
        for (int i = 0; i < sList.size(); i++) {
            Entity entity = sList.get(i);
            Router router = entity.clz.getAnnotation(Router.class);
            String routerName = Utils.firstLetterToUpper("".equals(router.name()) ? entity.clz.getSimpleName().toString() : router.name());
            String methodName = "start" + routerName;
            List<Element> fieldList = entity.fieldList;

            logger.note("start方法生成：开始->" + methodName);

            MethodSpec.Builder startMethodBuilder = MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ParameterSpec.builder(ctxClassName, "context").addAnnotation(nonnullAnnotation).build());


            ClassName actClz = ClassName.bestGuess(entity.clz.toString());

            // 生成binder类
            String binderName = Utils.getBinderName(routerName);
            TypeSpec.Builder binderTypeBudiler = TypeSpec.classBuilder(binderName)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(Binder.class);
            MethodSpec.Builder bindMethodBuilder = MethodSpec.methodBuilder("bind")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addParameter(Object.class, "activity");
            CodeBlock.Builder binderCodeBuilder = CodeBlock.builder();


            logger.note("Binder类生成：开始->" + binderName);

            logger.note("bind方法生成：开始->" + binderName + ".bind");

            initCodeBuilder.addStatement(String.format("sMap.put(\"%s\",\"%s\")", entity.clz.toString(), entity.clz.getEnclosingElement().toString() + "." + binderName));

            // 生成start方法
            CodeBlock.Builder cbb = CodeBlock.builder();


            cbb.addStatement("$T intent = new Intent(context,$T.class)", intentClassName, actClz);

            // 每个Activity的属性遍历
            for (int j = 0; j < fieldList.size(); j++) {


                logger.note("Activity的属性遍历：开始->" + entity.clz.toString());

                Element field = fieldList.get(j);


                Extra extra = field.getAnnotation(Extra.class);
                String fieldName = field.getSimpleName().toString();
                TypeName paramsClz = ClassName.get(field.asType()).box();
                AnnotationSpec paramsAnnotation = AnnotationSpec.builder(extra.require() ? ClassName.bestGuess("android.support.annotation.NonNull") :
                        ClassName.bestGuess("android.support.annotation.Nullable")).build();
                String paramsName = "".equals(extra.name()) ? fieldName : extra.name();
                logger.note("参数注解添加：" + paramsName + "->" + paramsAnnotation.toString());
                startMethodBuilder.addParameter(ParameterSpec.builder(paramsClz, paramsName).addAnnotation(extra.require() ? nonnullAnnotation : nullableAnnotation).build());
                cbb.addStatement(String.format("intent.putExtra(\"%s\",%s)", paramsName, paramsName));


                String paramsType = paramsClz.toString();

                if ("java.lang.Boolean".equals(paramsType)) {
                    binderCodeBuilder.addStatement(String.format("(($T)(activity)).%s=(($T)(activity)).getIntent().getBooleanExtra(\"%s\",false)", fieldName, paramsName), actClz, actClz);
                } else if ("android.os.Bundle".equals(paramsType)) {
                    binderCodeBuilder.addStatement(String.format("(($T)(activity)).%s=(($T)(activity)).getIntent().getBundleExtra(\"%s\")", fieldName, paramsName), actClz, actClz);
                } else if ("java.lang.Byte".equals(paramsType)) {
                    binderCodeBuilder.addStatement(String.format("(($T)(activity)).%s=(($T)(activity)).getIntent().getByteExtra(\"%s\",(byte) 0)", fieldName, paramsName), actClz, actClz);
                } else if ("java.lang.Character".equals(paramsType)) {
                    binderCodeBuilder.addStatement(String.format("(($T)(activity)).%s=(($T)(activity)).getIntent().getCharExtra(\"%s\", (char) 0)", fieldName, paramsName), actClz, actClz);
                } else if ("java.lang.Double".equals(paramsType)) {
                    binderCodeBuilder.addStatement(String.format("(($T)(activity)).%s=(($T)(activity)).getIntent().getDoubleExtra(\"%s\", 0)", fieldName, paramsName), actClz, actClz);
                } else if ("java.lang.Float".equals(paramsType)) {
                    binderCodeBuilder.addStatement(String.format("(($T)(activity)).%s=(($T)(activity)).getIntent().getFloatExtra(\"%s\", 0f)", fieldName, paramsName), actClz, actClz);
                } else if ("java.lang.Integer".equals(paramsType)) {
                    binderCodeBuilder.addStatement(String.format("(($T)(activity)).%s=(($T)(activity)).getIntent().getIntExtra(\"%s\", 0)", fieldName, paramsName), actClz, actClz);
                } else if ("java.lang.Long".equals(paramsType)) {
                    binderCodeBuilder.addStatement(String.format("(($T)(activity)).%s=(($T)(activity)).getIntent().getLongExtra(\"%s\", 0)", fieldName, paramsName), actClz, actClz);
                } else if ("java.lang.String".equals(paramsType)) {
                    binderCodeBuilder.addStatement(String.format("(($T)(activity)).%s=(($T)(activity)).getIntent().getStringExtra(\"%s\")", fieldName, paramsName), actClz, actClz);
                } else {
                    throw new IllegalArgumentException("不支持的类型:" + paramsType);
                }

                logger.note("bind方法生成：添加参数->" + fieldName + "->" + paramsName);


            }

            cbb.addStatement("context.startActivity(intent)");
            startMethodBuilder.addCode(cbb.build());
            tbJBRouter.addMethod(startMethodBuilder.build());
            logger.note("start方法生成：完成->" + methodName);

            logger.note("Binder方法生成：完成->" + binderName + ".bind");
            bindMethodBuilder.addCode(binderCodeBuilder.build());
            binderTypeBudiler.addMethod(bindMethodBuilder.build());
            // 写到Binder类文件
            JavaFile javaFile = JavaFile.builder(entity.clz.getEnclosingElement().toString(), binderTypeBudiler.build()).build();
            try {
                javaFile.writeTo(filer);
                logger.note("Binder类生成：完成->" + binderName);
            } catch (IOException e) {
                logger.error(e.getMessage());
                logger.note("Binder类生成：失败->" + binderName);
            }
        }

        // 生成startForResult方法
        for (int i = 0; i < sList.size(); i++) {
            Entity entity = sList.get(i);
            Router router = entity.clz.getAnnotation(Router.class);
            String methodName = "start" + Utils.firstLetterToUpper("".equals(router.name()) ? entity.clz.getSimpleName().toString() : router.name()) + "ForResult";
            List<Element> fieldList = entity.fieldList;


            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ParameterSpec.builder(actClassName, "activity").addAnnotation(nonnullAnnotation).build());


            // 生成方法
            CodeBlock.Builder cbb = CodeBlock.builder();

            ClassName actClz = ClassName.bestGuess(entity.clz.toString());

            cbb.addStatement("$T intent = new Intent(activity,$T.class)", intentClassName, actClz);
            for (int j = 0; j < fieldList.size(); j++) {
                Element field = fieldList.get(j);
                Extra extra = field.getAnnotation(Extra.class);
                TypeName paramsClz = ClassName.get(field.asType()).box();
                AnnotationSpec paramsAnnotation = AnnotationSpec.builder(extra.require() ? ClassName.bestGuess("android.support.annotation.NonNull") :
                        ClassName.bestGuess("android.support.annotation.Nullable")).build();
                String paramsName = "".equals(extra.name()) ? field.getSimpleName().toString() : extra.name();
                logger.note(paramsName + "添加注解:" + paramsAnnotation.toString());
                methodBuilder.addParameter(ParameterSpec.builder(paramsClz, paramsName).addAnnotation(extra.require() ? nonnullAnnotation : nullableAnnotation).build());

                cbb.addStatement(String.format("intent.putExtra(\"%s\",%s)", paramsName, paramsName));
            }
            cbb.addStatement("activity.startActivityForResult(intent,requestCode)");
            methodBuilder.addCode(cbb.build());
            methodBuilder.addParameter(ParameterSpec.builder(integerClassName, "requestCode").addAnnotation(nonnullAnnotation).build());

            tbJBRouter.addMethod(methodBuilder.build());
        }
        tbJBRouter.addStaticBlock(initCodeBuilder.build());
        tbJBRouter.addMethod(MethodSpec.methodBuilder("bind").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(Object.class, "object")
                .addCode("try {Object binderName = sMap.get(object.getClass().getName());if (binderName == null) {return;}(($T) (Class.forName((String) binderName).newInstance())).bind(object);} catch (Exception e) {e.printStackTrace();}", Binder.class).build());
        // 写Router类文件
        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, tbJBRouter.build()).build();
        try {
            javaFile.writeTo(filer);
            logger.note(CLASS_NAME + "类生成：完成");
        } catch (IOException e) {
            logger.note(CLASS_NAME + "类生成：失败");
            logger.error(e.getMessage());
        }


        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(Router.class.getName());
        set.add(Extra.class.getName());
        return set;
    }


}
