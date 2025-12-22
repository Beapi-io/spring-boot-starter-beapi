package io.beapi.api.utils

import java.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.AliasFor;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface WebSocket {
	@AliasFor(annotation = org.springframework.stereotype.Component.class)
	String value() default "";
}