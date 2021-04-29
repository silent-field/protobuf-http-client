package com.silent.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProtobufHttpRestfulMeta {
	/**
	 * 接口路径
	 */
	String path() ;

	/**
	 * 接口描述
	 */
	String desc() default "";
}
