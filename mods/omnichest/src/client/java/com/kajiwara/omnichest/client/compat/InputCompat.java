package com.kajiwara.omnichest.client.compat;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;

/**
 * キー押下判定の版差吸収。
 * 26.3 で GLFW → SDL に移行し、 {@code InputConstants.isKeyDown} が {@code Window} 引数を取らなくなった。
 */
public final class InputCompat {
    private InputCompat() {
    }

    public static boolean isKeyDown(Window window, int key) {
        //? if >=26.3 {
        /*return InputConstants.isKeyDown(key);*/
        //?} else {
        return InputConstants.isKeyDown(window, key);
        //?}
    }
}
