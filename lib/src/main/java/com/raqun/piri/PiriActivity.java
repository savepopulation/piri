package com.raqun.piri;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by test on 08/05/17.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface PiriActivity {
}
