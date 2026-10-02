package com.dogpound.patches.mixin;

import com.dogpound.patches.ScalaObjects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Method;

/** Same as MixinLambdaLibScalaObjects, for the block/item registry callbacks. */
@Mixin(targets = "cn.lambdalib2.registry.impl.RegistryManager$EventHandler", remap = false)
public abstract class MixinLambdaLibScalaObjectsEvents {
    @Redirect(method = "invokeCallback", at = @At(value = "INVOKE", target = "Ljava/lang/reflect/Method;invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"), remap = false)
    private Object dpp$invoke(Method m, Object target, Object[] args) throws ReflectiveOperationException { return ScalaObjects.invoke(m, target, args); }
}
