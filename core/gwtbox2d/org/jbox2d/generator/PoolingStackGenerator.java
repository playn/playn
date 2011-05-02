package org.jbox2d.generator;

import java.io.PrintWriter;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

public class PoolingStackGenerator extends Generator {

  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName)
      throws UnableToCompleteException {
    
    TypeOracle typeOracle = context.getTypeOracle();
    JClassType classToBeGenerated = typeOracle.findType(typeName);
    JParameterizedType superInterface = classToBeGenerated.getImplementedInterfaces()[0]
        .isParameterized();
    JClassType poolableType = superInterface.getTypeArgs()[0];
    String implName = classToBeGenerated.getSimpleSourceName() + "Impl";
    String generatedImplFullName = classToBeGenerated.getPackage().getName() + "." + implName;
    
    PrintWriter pw = context.tryCreate(logger, classToBeGenerated.getEnclosingType().getPackage()
        .getName(), implName);
    if (pw == null) {
      return generatedImplFullName;
    }
    String poolableTypeName = poolableType.getName();

    pw.append("package " + classToBeGenerated.getEnclosingType().getPackage().getName() + ";\n");
    pw.append("import " + superInterface.getQualifiedSourceName() + ";\n");
    pw.append("import " + classToBeGenerated.getQualifiedSourceName() + ";\n");
    pw.append("import " + poolableType.getQualifiedSourceName() + ";\n");
    pw.append("import org.jbox2d.pooling.PoolingStack.PoolContainer;\n");
    pw.append("public class " + implName + " implements "
        + classToBeGenerated.getSimpleSourceName() + "{\n");

    pw.append("private " + poolableTypeName + "[] pool;\n");
    pw.append("private int index;\n");
    pw.append("private int size;\n");
    pw.append("private PoolContainer<" + poolableTypeName + "> container;\n");

    pw.append("public void initPool(int argStackSize) {\n");
    pw.append("size = argStackSize;\n");
    pw.append("pool = new " + poolableTypeName + "[argStackSize];\n");
    pw.append("for(int i=0; i<argStackSize; i++){\n");
    pw.append("pool[i] = new " + poolableTypeName + "();\n");
    pw.append("}\n");
    pw.append("index = 0;\n");
    pw.append("container = new PoolContainer<" + poolableTypeName + ">();\n");
    pw.append("}\n");

    pw.append("public " + poolableTypeName + " pop() {\n");
    pw.append("return pool[index++];\n");
    pw.append("}\n");

    pw.append("public void push(int argNum) {\n");
    pw.append("index -= argNum;\n");
    pw.append("}\n");

    pw.append("public PoolContainer<" + poolableTypeName + "> pop(int argNum) {\n");
    pw.append("switch(argNum){");
    pw.append("case 9:");
    pw.append("container.p8 = pool[index++];");
    pw.append("case 8:");
    pw.append("container.p7 = pool[index++];");
    pw.append("case 7:");
    pw.append("container.p6 = pool[index++];");
    pw.append("case 6:");
    pw.append("container.p5 = pool[index++];");
    pw.append("case 5:");
    pw.append("container.p4 = pool[index++];");
    pw.append("case 4:");
    pw.append("container.p3 = pool[index++];");
    pw.append("case 3:");
    pw.append("container.p2 = pool[index++];");
    pw.append("case 2:");
    pw.append("container.p1 = pool[index++];");
    pw.append("case 1:");
    pw.append("container.p0 = pool[index++];");
    pw.append("break;");
    pw.append("default:");
    pw.append("assert(false);");
    pw.append("}");
    pw.append("return container;");
    pw.append("}\n");

    pw.append("}\n");
    context.commit(logger, pw);
    return generatedImplFullName;
  }
}
