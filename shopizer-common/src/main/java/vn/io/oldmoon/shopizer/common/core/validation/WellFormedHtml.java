package vn.io.oldmoon.shopizer.common.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation backed by Jsoup to ensure submitted HTML strings
 * are parseable and well-formed.
 */
@Documented
@Constraint(validatedBy = WellFormedHtmlValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface WellFormedHtml {

  String message() default "HTML content is not well-formed";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
