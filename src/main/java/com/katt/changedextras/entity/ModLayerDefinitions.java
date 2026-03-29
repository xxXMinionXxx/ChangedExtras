package com.katt.changedextras.entity;

import com.katt.changedextras.ChangedExtras;
import com.katt.changedextras.entity.model.ConeKatFemaleEntityModel;
import com.katt.changedextras.entity.model.ConeKatMaleEntityModel;
import com.katt.changedextras.entity.model.KattEntityModel;
import com.katt.changedextras.entity.model.WhiteCatEntityModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ChangedExtras.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModLayerDefinitions {
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ConeKatMaleEntityModel.LAYER_LOCATION, ConeKatMaleEntityModel::createBodyLayer);
        event.registerLayerDefinition(ConeKatFemaleEntityModel.LAYER_LOCATION, ConeKatFemaleEntityModel::createBodyLayer);
        event.registerLayerDefinition(WhiteCatEntityModel.LAYER_LOCATION, WhiteCatEntityModel::createBodyLayer);
        event.registerLayerDefinition(KattEntityModel.LAYER_LOCATION, KattEntityModel::createBodyLayer);
    }
}
