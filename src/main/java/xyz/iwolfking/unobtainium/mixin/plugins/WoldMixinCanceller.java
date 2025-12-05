package xyz.iwolfking.unobtainium.mixin.plugins;

import com.bawnorton.mixinsquared.api.MixinCanceller;
import net.minecraftforge.fml.loading.LoadingModList;

import java.util.List;

public class WoldMixinCanceller implements MixinCanceller {
    @Override
    public boolean shouldCancel(List<String> list, String s) {
        /*
            The Ars elytra mixin conflicts with Vault Hunters, this disables that mixin.
        */
        if(LoadingModList.get().getModFileById("ars_nouveau") != null) {
            if(s.equals("com.hollingsworth.arsnouveau.common.mixin.elytra.ClientElytraMixin")) {
                return true;
            }
        }

        if(LoadingModList.get().getModFileById("the_vault") != null) {
            /*
                The vanilla Vault Hunters World Chunk mixin has issues, we cancel it and re-implement our own.
            */
            if(s.equals("iskallia.vault.mixin.MixinWorldChunk")) {
                return true;
            }
        }
        final String CIT_PREFIX = "shcm.shsupercm.fabric.citresewn.mixin.";
        if (   s.equals(CIT_PREFIX + "citarmor.ItemStackMixin")
            || s.equals(CIT_PREFIX + "citelytra.ElytraFeatureRendererMixin")
            || s.equals(CIT_PREFIX + "citelytra.ItemStackMixin")
            || s.equals(CIT_PREFIX + "citenchantment.ItemStackMixin")
            || s.equals(CIT_PREFIX + "cititem.ItemStackMixin")){
            return true;
        }
        return false;
    }
}
