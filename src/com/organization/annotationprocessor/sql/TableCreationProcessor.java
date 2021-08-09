package com.organization.annotationprocessor.sql;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner9;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class TableCreationProcessor extends AbstractProcessor {
    private String sql = "";
    private StringBuilder sb = new StringBuilder();
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation:
             annotations) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element element:
                 elements) {
                element.accept(new TableCreationVisitor(), null);
            }
        }
                try {
                    sql = sb.substring(0, sb.length()-1) + ");";
                    File file = new File("/Users/serginiho/IdeaProjects/myproject/src/com/homecompany/chapter20/exercise3/SqlGenerated.txt");
                    PrintWriter writer = new PrintWriter(file);
                    writer.println("creation SQL is :");
                    writer.println(sql);
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        return true;
    }
    private class TableCreationVisitor extends ElementScanner9<Void, Void> {
        private String columnName = "";
        public Void visitType (TypeElement e, Void p) {
            if (e.getAnnotation(DBTable.class) != null) {
                DBTable dbTable = e.getAnnotation(DBTable.class);
                sb.insert(0, "CREATE TABLE " + ((dbTable.name().length() < 1)
                        ? e.getSimpleName().toString().toUpperCase()
                        : dbTable.name()) + " (");
            }
            return p;
        }
        public Void visitVariable(VariableElement e, Void p){
             if (e.getAnnotation(SQLInteger.class) != null){
                SQLInteger sInt = e.getAnnotation(SQLInteger.class);
                if(sInt.name().length() < 1)
                    columnName = e.getSimpleName().toString().toUpperCase();
                else
                    columnName = sInt.name();
                sb.append("\n    ").append(columnName).append(" INT").append(getConstraints(sInt.constraints())).append(",");
            }else if (e.getAnnotation(SQLString.class) != null){
                SQLString sString = e.getAnnotation(SQLString.class);
                if(sString.name().length()<1)
                    columnName = e.getSimpleName().toString().toUpperCase();
                else
                    columnName = sString.name();
                sb.append("\n    ").append(columnName).append(" VARCHAR(").append(sString.value()).
                        append(")").append(getConstraints(sString.constraints())).append(",");
            }else if(e.getAnnotation(SQLDouble.class) != null){
                SQLDouble sqlDouble = e.getAnnotation(SQLDouble.class);
                if(sqlDouble.name().length()<1)
                    columnName = e.getSimpleName().toString().toUpperCase();
                else
                    columnName = sqlDouble.name();
                sb.append("\n    ").append(columnName).append(" FLOAT").
                        append(getConstraints(sqlDouble.constraints())).append(",");
            }else if(e.getAnnotation(SQLLong.class) != null){
                SQLLong sqlLong = e.getAnnotation(SQLLong.class);
                if (sqlLong.name().length()<1)
                    columnName = e.getSimpleName().toString().toUpperCase();
                else
                    columnName = sqlLong.name();
                sb.append("\n    ").append(columnName).append(" BIGINT").
                        append(getConstraints(sqlLong.uniqueness().constraints())).append(",");
            }
            return p;
        }
        private String getConstraints(Constraints con) {
            String constraints = "";
            if(!con.allowNull())
                constraints += " NOT NULL";
            if(con.primaryKey())
                constraints += " PRIMARY KEY";
            if(con.unique())
                constraints += " UNIQUE";
            return constraints;
        }
    }
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new TreeSet<>(Arrays.asList(
                "com.organization.annotationprocessor.sql.DBTable",
                "com.organization.annotationprocessor.sql.SQLString",
                "com.organization.annotationprocessor.sql.SQLInteger",
                "com.organization.annotationprocessor.sql.SQLDouble",
                "com.organization.annotationprocessor.sql.SQLLong",
                "com.organization.annotationprocessor.sql.Uniqueness",
                "com.organization.annotationprocessor.sql.Constraints"
        ));
    }
}
