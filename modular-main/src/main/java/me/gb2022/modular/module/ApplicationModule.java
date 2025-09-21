package me.gb2022.modular.module;

import me.gb2022.modular.FeatureAvailability;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ApplicationModule {
    /**
     * recommended to fill but acceptable if empty
     */
    String version() default "1.0";

    String id() default "null";

    boolean beta() default false;

    FeatureAvailability available() default FeatureAvailability.INHERIT;

    boolean internal() default false;

    boolean defaultEnable() default true;

    String description() default "No description provided.";


    /**
     * do not care it unless using record.<br>
     * deprecated, please use @Inject("Name;Format(,)") instead.
     */
    @Deprecated
    String[] recordFormat() default {};
}
