package backend.annotations.strongPassword;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class StrongPasswordValidator
        implements ConstraintValidator<StrongPassword, String> {

    private int minLength;

    private static final Pattern UPPERCASE =
            Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE =
            Pattern.compile("[a-z]");
    private static final Pattern DIGIT =
            Pattern.compile("\\d");
    private static final Pattern SPECIAL =
            Pattern.compile("[@$!%*?&._-]");

    @Override
    public void initialize(StrongPassword constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
    }

    @Override
    public boolean isValid(String password,
                           ConstraintValidatorContext context) {

        if (password == null)
            return false;

        if (password.length() < minLength)
            return false;

        if (!UPPERCASE.matcher(password).find())
            return false;

        if (!LOWERCASE.matcher(password).find())
            return false;

        if (!DIGIT.matcher(password).find())
            return false;

        if (!SPECIAL.matcher(password).find())
            return false;

        return true;
    }
}