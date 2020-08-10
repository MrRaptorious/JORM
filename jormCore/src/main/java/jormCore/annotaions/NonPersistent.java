package jormCore.annotaions;
import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {FIELD , TYPE })
public @interface NonPersistent{}