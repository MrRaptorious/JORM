package jormCore.Annotaions;
import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {FIELD})
public @interface Association{
	public String name() default "";
}