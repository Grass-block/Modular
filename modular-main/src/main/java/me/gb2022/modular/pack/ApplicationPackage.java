package me.gb2022.modular.pack;

import me.gb2022.modular.FeatureAvailability;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ApplicationPackage {
    String value() default "";

    FeatureAvailability available() default FeatureAvailability.PREMIUM;

    String description() default "";

    String version() default "";

    String author() default "";
}
