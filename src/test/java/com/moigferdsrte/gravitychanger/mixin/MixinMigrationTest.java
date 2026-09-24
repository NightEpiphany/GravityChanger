package com.moigferdsrte.gravitychanger.mixin;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks class files without initializing Minecraft or applying Mixins.
 * A real runClient smoke test is still needed to validate injection points.
 */
class MixinMigrationTest {
    private static final String MIXIN_PACKAGE = "com/moigferdsrte/gravitychanger/mixin/";

    @Test
    void viewBlockingInjectionTargetsTheLevelExtractor() throws IOException {
        assertInjectionTargets(
            "client/LevelExtractorMixin",
            "net/minecraft/client/renderer/extract/LevelExtractor",
            "gravitychanger$getDirectionalViewBlockingState",
            "Lorg/spongepowered/asm/mixin/injection/Inject;"
        );
    }

    @Test
    void directionalViewInjectionTargetsInstanceMethods() throws IOException {
        assertInjectionTargets(
            "EntityMixin",
            "net/minecraft/world/entity/Entity",
            "gravitychanger$calculateDirectionalViewVector",
            "Lcom/llamalad7/mixinextras/injector/ModifyReturnValue;"
        );
    }

    @Test
    void upwardMovementInjectionTargetsTheExtractedPositionHandler() throws IOException {
        MethodNode target = assertInjectionTargets(
            "ServerGamePacketListenerImplMixin",
            "net/minecraft/server/network/ServerGamePacketListenerImpl",
            "gravitychanger$useLocalUpwardMovement",
            "Lorg/spongepowered/asm/mixin/injection/ModifyVariable;"
        ).getFirst();

        // These names are used by ModifyVariable and MixinExtras @Local.
        Map<String, String> locals = target.localVariables.stream()
            .collect(Collectors.toMap(local -> local.name, local -> local.desc, (first, second) -> first));
        assertEquals("Z", locals.get("movedUpwards"));
        assertEquals("D", locals.get("xDist"));
        assertEquals("D", locals.get("yDist"));
        assertEquals("D", locals.get("zDist"));
    }

    private static List<MethodNode> assertInjectionTargets(
        final String mixinName,
        final String targetName,
        final String handlerName,
        final String annotationDescriptor
    ) throws IOException {
        ClassNode mixin = readClass(MIXIN_PACKAGE + mixinName);
        AnnotationNode mixinAnnotation = findAnnotation(
            mixin.invisibleAnnotations, "Lorg/spongepowered/asm/mixin/Mixin;"
        );
        assertEquals(List.of(Type.getObjectType(targetName)), annotationValue(mixinAnnotation, "value"));

        MethodNode handler = mixin.methods.stream()
            .filter(method -> method.name.equals(handlerName))
            .findFirst().orElseThrow();
        AnnotationNode injection = findAnnotation(handler.visibleAnnotations, annotationDescriptor);
        List<?> selectors = (List<?>)annotationValue(injection, "method");
        assertTrue(!selectors.isEmpty(), "Injection must declare at least one target");

        ClassNode target = readClass(targetName);
        Map<String, MethodNode> targetMethods = target.methods.stream()
            .collect(Collectors.toMap(method -> method.name + method.desc, Function.identity()));
        return selectors.stream().map(selector -> {
            MethodNode method = targetMethods.get((String)selector);
            assertNotNull(method, () -> "Missing target: " + targetName + "." + selector);
            assertEquals(handler.access & Opcodes.ACC_STATIC, method.access & Opcodes.ACC_STATIC,
                () -> "Static/instance mismatch: " + selector);
            return method;
        }).toList();
    }

    private static ClassNode readClass(final String internalName) throws IOException {
        try (var stream = MixinMigrationTest.class.getClassLoader().getResourceAsStream(internalName + ".class")) {
            assertNotNull(stream, () -> "Missing class: " + internalName);
            ClassNode node = new ClassNode();
            new ClassReader(stream).accept(node, ClassReader.SKIP_FRAMES);
            return node;
        }
    }

    private static AnnotationNode findAnnotation(final List<AnnotationNode> annotations, final String descriptor) {
        assertNotNull(annotations, () -> "Missing annotations: " + descriptor);
        return annotations.stream().filter(annotation -> annotation.desc.equals(descriptor))
            .findFirst().orElseThrow(() -> new AssertionError("Missing annotation: " + descriptor));
    }

    private static Object annotationValue(final AnnotationNode annotation, final String name) {
        for (int i = 0; i < annotation.values.size(); i += 2) {
            if (name.equals(annotation.values.get(i))) {
                return annotation.values.get(i + 1);
            }
        }
        throw new AssertionError("Missing annotation value: " + name);
    }
}
