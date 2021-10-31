package com.whaleal.icefrog.core.annotation;

import java.lang.annotation.*;

/**
 * 别名注解，使用此注解的字段、方法、参数等会有一个别名，用于Bean拷贝、Bean转Map等
 * 一个通用的别名注解  用于反射当中
 *
 * @author Looly
 * @author wh
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface Alias {

    /**
     * 别名值，即使用此注解要替换成的别名名称
     *
     * @return 别名值
     */
    String value();
}
