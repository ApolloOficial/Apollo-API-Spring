package org.apollo.api.sungrow;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnExpression("${apollo.sungrow.enabled:false} and '${apollo.sungrow.plant-mode:live}'.toLowerCase() == 'simulated'")
public @interface ConditionalOnSungrowSimulated {
}
