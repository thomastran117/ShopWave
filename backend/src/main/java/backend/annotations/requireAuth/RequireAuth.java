package backend.annotations.requireAuth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller method (or all methods on a class) as requiring an authenticated user,
 * optionally restricted to specific roles. Use on routes that need a valid Bearer token;
 * all other routes are public by default.
 * <p>
 * Examples:
 * <pre>
 * &#64;RequireAuth
 * &#64;GetMapping("/me")
 * public UserResponse me() { ... }
 *
 * &#64;RequireAuth(roles = {"ADMIN"})
 * &#64;DeleteMapping("/{id}")
 * public void deleteUser(@PathVariable long id) { ... }
 *
 * &#64;RequireAuth(roles = {"ADMIN", "MODERATOR"})
 * &#64;GetMapping("/moderate")
 * public void moderate() { ... }
 * </pre>
 * Role names are checked against authorities (Spring adds "ROLE_" prefix, so use "ADMIN" not "ROLE_ADMIN").
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAuth {

    /**
     * Allowed roles (any one of). Empty means any authenticated user.
     * Values are used with hasAnyRole (e.g. "ADMIN" checks for ROLE_ADMIN).
     */
    String[] roles() default {};
}
