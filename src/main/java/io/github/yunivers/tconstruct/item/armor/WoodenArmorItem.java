package io.github.yunivers.tconstruct.item.armor;

import io.github.yunivers.tconstruct.events.init.InitListener;
import net.minecraft.item.ArmorItem;
import net.modificationstation.stationapi.api.client.item.ArmorTextureProvider;
import net.modificationstation.stationapi.api.template.item.TemplateArmorItem;
import net.modificationstation.stationapi.api.util.Identifier;

public class WoodenArmorItem extends TemplateArmorItem implements ArmorTextureProvider {
    private static final int[] DURABILITY_BY_SLOT = new int[]{11, 16, 15, 13};

    public WoodenArmorItem(Identifier identifier, int slot) {
        super(identifier, 0, 0, slot);

        this.setMaxDamage(DURABILITY_BY_SLOT[equipmentSlot] * 2);
    }

    @Override
    public Identifier getTexture(ArmorItem armour) {
        return InitListener.NAMESPACE.id("wood");
    }
}
