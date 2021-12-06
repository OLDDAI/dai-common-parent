package com.dai.common.util.validations.validator;

import com.dai.common.util.tools.RegUtils;
import com.dai.common.util.tools.lang3.StringUtils;
import com.dai.common.util.validations.annotate.Phone;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 手机号码约束逻辑判断
 */
public class PhoneValidator implements ConstraintValidator<Phone, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        return RegUtils.checkPhone(value);
    }

}
