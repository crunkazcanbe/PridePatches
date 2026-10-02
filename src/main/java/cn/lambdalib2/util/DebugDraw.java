package cn.lambdalib2.util;

/**
 * COMPILE-ONLY STUB of LambdaLib2's DebugDraw so the mixin can resolve its target at compile time.
 * The real class lives inside AcademyCraft at runtime; this stub is excluded from the built jar
 * (see the `exclude 'cn/**'` rule in build.gradle). Only the members the mixin references need to
 * exist here, with matching signatures.
 */
public class DebugDraw {
    // real signature is postRenderEntity(RenderAllEntityEvent); the mixin targets it by name and
    // takes no event arg, so an Object param here is enough to give the method a body to inject into.
    public void postRenderEntity(Object event) {
    }
}
