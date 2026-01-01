package me.gb2022.modular.pack;

import me.gb2022.modular.FeatureAvailability;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ApplicationPackageProvider {
    String id();

    FeatureAvailability available() default FeatureAvailability.BOTH;

    String description() default "No information presents.";

    String version() default "N/A";

    boolean internal() default false;

    String author() default "No author specified";
}
