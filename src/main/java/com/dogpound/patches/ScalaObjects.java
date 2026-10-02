package com.dogpound.patches;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/** Scala `object` methods are instance methods on Foo$ with the singleton in a static MODULE$ field. */
public final class ScalaObjects {
    private ScalaObjects() {}

    public static Object module(Class<?> c) {
        try {
            Field f = c.getDeclaredField("MODULE$");
            return Modifier.isStatic(f.getModifiers()) ? f.get(null) : null;
        } catch (ReflectiveOperationException e) { return null; }
    }

    /** modifiers, plus STATIC when the method lives on a Scala object (so "must be static" checks pass) */
    public static int modifiers(Method m) {
        int mod = m.getModifiers();
        return !Modifier.isStatic(mod) && module(m.getDeclaringClass()) != null ? mod | Modifier.STATIC : mod;
    }

    /** Method.invoke that supplies the Scala singleton when the caller passes null for an object method */
    public static Object invoke(Method m, Object target, Object... args) throws ReflectiveOperationException {
        if (target == null && !Modifier.isStatic(m.getModifiers())) target = module(m.getDeclaringClass());
        return m.invoke(target, args);
    }
}
