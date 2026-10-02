package com.dogpound.patches.mixin;

import com.dogpound.patches.ScalaObjects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Method;

/**
 * AcademyCraft (Scala) on Cleanroom: LambdaLib2 finds its @StateEventCallback methods on the Scala object class
 * (AbilityInterf$) and throws "must be static". Treat object methods as static and call them on MODULE$.
 */
@Mixin(targets = "cn.lambdalib2.registry.impl.RegistryManager", remap = false)
public abstract class MixinLambdaLibScalaObjects {
    @Redirect(method = {"lambda$checkInit$0", "lambda$checkInit$1"}, at = @At(value = "INVOKE", target = "Ljava/lang/reflect/Method;getModifiers()I"), remap = false)
    private int dpp$scalaStatic(Method m) { return ScalaObjects.modifiers(m); }

    @Redirect(method = "onStateEvent", at = @At(value = "INVOKE", target = "Ljava/lang/reflect/Method;invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"), remap = false)
    private Object dpp$invoke(Method m, Object target, Object[] args) throws ReflectiveOperationException { return ScalaObjects.invoke(m, target, args); }
}
