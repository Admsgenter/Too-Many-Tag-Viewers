package admsgenter.too_many_tag_viewers.mixin;

import admsgenter.too_many_tag_viewers.标签查看器;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Screen.class)
public class ScreenMixin {
    /**
     * @author Admsgenter
     * @reason hasAltDown Mixin
     */
    @Overwrite
    public static boolean hasAltDown() { return 标签查看器.Alt.getAsBoolean(); }

    /**
     * @author Admsgenter
     * @reason hasControlDown Mixin
     */
    @Overwrite
    public static boolean hasControlDown() { return 标签查看器.Ctrl.getAsBoolean(); }

    /**
     * @author Admsgenter
     * @reason hasShiftDown Mixin
     */
    @Overwrite
    public static boolean hasShiftDown() { return 标签查看器.Shift.getAsBoolean(); }
}