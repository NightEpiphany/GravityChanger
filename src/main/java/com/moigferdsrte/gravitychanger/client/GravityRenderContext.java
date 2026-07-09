package com.moigferdsrte.gravitychanger.client;

public final class GravityRenderContext {
    private static final ThreadLocal<Boolean> RENDERING_GUI_ENTITY = ThreadLocal.withInitial(() -> false);

    private GravityRenderContext() {
    }

    public static boolean isRenderingGuiEntity() {
        return RENDERING_GUI_ENTITY.get();
    }

    public static void setRenderingGuiEntity(final boolean renderingGuiEntity) {
        RENDERING_GUI_ENTITY.set(renderingGuiEntity);
    }
}
