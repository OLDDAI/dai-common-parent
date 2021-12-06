package com.dai.common.util.validations.annotate;

import com.dai.common.util.validations.validator.UrlValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * url约束 为空时通过  可组合 @NotBlank 校验空
 * @author wangxu
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UrlValidator.class)
public @interface Url {

    String message() default "网址格式不正确";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
