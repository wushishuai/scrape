package p005io.reactivex.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Retention(RetentionPolicy.RUNTIME)
/* renamed from: io.reactivex.annotations.BackpressureSupport */
/* loaded from: classes.dex */
public @interface BackpressureSupport {
    BackpressureKind value();
}