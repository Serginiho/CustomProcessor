package com.organization.annotationprocessor.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public @interface Uniqueness {
    Constraints constraints() default @Constraints(unique = true, allowNull = false);
}
