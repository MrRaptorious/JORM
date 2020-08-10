package jormCore.annotaions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {FIELD})
public @interface Size {
	public  static int DefaultSize = 255;
	public int Size() default DefaultSize;
}