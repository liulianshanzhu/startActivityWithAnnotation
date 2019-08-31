package com.durian.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要传递的参数
 *
 * @author yuansui
 * @see {@link Creator}
 * @since 2017/8/1
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Extra {
    /**
     * 可选参数, 可以不传, 或者多参数情况下为了方便去使用链式调用
     * 声明的参数不能为private
     */
    boolean value() default false;
}
