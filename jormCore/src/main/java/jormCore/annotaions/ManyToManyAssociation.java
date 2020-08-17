package jormCore.annotaions;
import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;

@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {FIELD})
public @interface ManyToManyAssociation {
	String name() default "";
	String associatedName() default "";
}

