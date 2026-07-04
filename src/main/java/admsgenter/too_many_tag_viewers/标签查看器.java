package admsgenter.too_many_tag_viewers;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.ListenerList;
import net.minecraftforge.eventbus.api.EventListenerHelper;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventListener;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

@Mod(标签查看器.MODID)
public final class 标签查看器 {
    public static final String MODID = "too_many_tag_viewers";
    private static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    private static final long HWND = Minecraft.getInstance().getWindow().getWindow();
    public static BooleanSupplier Alt = () -> true;
    public static BooleanSupplier Ctrl = Alt;
    public static BooleanSupplier Shift = Alt;

    public 标签查看器() throws NoSuchFieldException, IllegalAccessException {
        ForgeConfigSpec.Builder 构建器 = new ForgeConfigSpec.Builder();
        ForgeConfigSpec.BooleanValue Alt显示标签 = 构建器.comment("是否启用Alt显示标签", "Alt to view tags").define("alt_view_tags", false);
        ForgeConfigSpec.BooleanValue Ctrl显示标签 = 构建器.comment("是否启用Ctrl显示标签", "Ctrl to view tags").define("ctrl_view_tags", false);
        ForgeConfigSpec.BooleanValue Shift显示标签 = 构建器.comment("是否启用Shift显示标签", "Shift to view tags").define("shift_view_tags", false);
        ForgeConfigSpec.BooleanValue 总是显示标签 = 构建器.comment("是否启用显示标签", "Always view tags").define("always_view_tags", false);
        ForgeConfigSpec.ConfigValue<List<? extends String>> 卸载黑名单 = 构建器.comment(
            "卸载事件的黑名单",
            "如果Too Many Tag Viewers不应该卸载某个监听器, 将这个监听器的名称填入列表让模组忽略它",
            "在logs/latest.log中搜索\"Unregistered Event Listener\", 在双引号中的内容便是这个监听器的名称",
            "例如, 在 ProjectE 的 tagToolTips = true 时, 模组会卸载 ProjectE 的提示框监听器, 无法显示EMC值",
            "此时在latest.log中搜索得到\"Unregistered Event Listener: \"ASM: class moze_intel.projecte.events.ToolTipEvent tTipEvent(Lnet/minecraftforge/event/entity/player/ItemTooltipEvent;)V\"\"",
            "在列表中填入\"ASM: class moze_intel.projecte.events.ToolTipEvent tTipEvent(Lnet/minecraftforge/event/entity/player/ItemTooltipEvent;)V\"即可",
            "The blacklist of unregistered event listeners",
            "If Too Many Tag Viewers should not unregister an event listener, put its name here to ignore it",
            "Search \"Unregistered Event Listener\" in logs/latest.log, the content in double quotation marks is its name",
            "For example, when the config of ProjectE tagToolTips = true, this mod will unregister the event listener of ProjectE, causing the inability to display EMC value",
            "Found \"Unregistered Event Listener: \"ASM: class moze_intel.projecte.events.ToolTipEvent tTipEvent(Lnet/minecraftforge/event/entity/player/ItemTooltipEvent;)V\"\" in latest.log",
            "Then put \"ASM: class moze_intel.projecte.events.ToolTipEvent tTipEvent(Lnet/minecraftforge/event/entity/player/ItemTooltipEvent;)V\" into this list"
        ).defineList("unregister_black_list", Collections.emptyList(), Predicates.alwaysTrue());
        Field busID = EventBus.class.getDeclaredField("busID");
        busID.setAccessible(true);
        int ID = busID.getInt(MinecraftForge.EVENT_BUS);
        FMLJavaModLoadingContext 信息 = FMLJavaModLoadingContext.get();
        信息.registerConfig(ModConfig.Type.CLIENT, 构建器.build(), MODID+".toml");
        信息.getModEventBus().addListener((final InterModProcessEvent 事件) -> {
            ListenerList 监听列表 = EventListenerHelper.getListenerList(ItemTooltipEvent.class);
            String 提示框名称 = Tags.Items.STONE.location().toString();
            Items.STONE.builtInRegistryHolder().bindTags(Collections.singleton(Tags.Items.STONE));
            ReferenceArrayList<Component> 提示框表 = ReferenceArrayList.of(Items.STONE.getDescription());
            ItemTooltipEvent 提示框事件 = new ItemTooltipEvent(new ItemStack(Items.STONE), null, 提示框表, TooltipFlag.ADVANCED);
            Options 选项 = Minecraft.getInstance().options;
            boolean 原配置 = 选项.advancedItemTooltips;
            选项.advancedItemTooltips = true;
            Set<String> 忽略集 = Set.copyOf(卸载黑名单.get());
            for(IEventListener 监听器 : 监听列表.getListeners(ID)) {
                if(监听器.getClass() == EventPriority.class || 忽略集.contains(监听器.toString())) continue;
                监听器.invoke(提示框事件);
                for(Component 提示框 : 提示框表)
                    if(提示框.getString().contains(提示框名称)) {
                        监听列表.unregister(ID, 监听器);
                        LOGGER.info("Unregistered Event Listener: \"{}\"", 监听器);
                    }
                提示框表.size(1);
            }
            选项.advancedItemTooltips = 原配置;
            Alt = () -> InputConstants.isKeyDown(HWND, 342) || InputConstants.isKeyDown(HWND, 346);
            Ctrl = Minecraft.ON_OSX? () -> InputConstants.isKeyDown(HWND, 343) || InputConstants.isKeyDown(HWND, 347):() -> InputConstants.isKeyDown(HWND, 341) || InputConstants.isKeyDown(HWND, 345);
            Shift = () -> InputConstants.isKeyDown(HWND, 340) || InputConstants.isKeyDown(HWND, 344);
            Style 风格 = Style.EMPTY.applyFormat(ChatFormatting.GRAY);
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, (final ItemTooltipEvent 当前事件) -> {
                if(总是显示标签.get() || Shift显示标签.get() && Screen.hasShiftDown() || Ctrl显示标签.get() && Screen.hasControlDown() || Alt显示标签.get() && Screen.hasAltDown()) {
                    List<Component> 提示框 = 当前事件.getToolTip();
                    当前事件.getItemStack().getTags().forEach(标签 -> 提示框.add(Component.literal('#'+标签.location().toString()).setStyle(风格)));
                }
            });
        });
    }
}