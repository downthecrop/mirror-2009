package core.game.node.entity.skill.magic;

import org.rs09.consts.Items;

import java.util.ArrayList;
import java.util.List;

public enum CombinationRune {
    LAVA_RUNE(4699, Runes.FIRE_RUNE,Runes.EARTH_RUNE),
    STEAM_RUNE(4694,Runes.FIRE_RUNE,Runes.WATER_RUNE),
    MIST_RUNE(4695,Runes.WATER_RUNE,Runes.AIR_RUNE),
    DUST_RUNE(4696, Runes.AIR_RUNE,Runes.EARTH_RUNE),
    SMOKE_RUNE(4697,Runes.FIRE_RUNE,Runes.AIR_RUNE),
    MUD_RUNE(4698,Runes.EARTH_RUNE,Runes.WATER_RUNE),
    COMBO_ELEMENTAL(Items.ELEMENTAL_RUNE_12850, Runes.FIRE_RUNE, Runes.AIR_RUNE, Runes.EARTH_RUNE, Runes.WATER_RUNE),
    COMBO_CATALYTIC(Items.CATALYTIC_RUNE_12851, Runes.MIND_RUNE, Runes.BODY_RUNE, Runes.DEATH_RUNE, Runes.CHAOS_RUNE);


    public Runes[] types;
    public int id;
    CombinationRune(int id, Runes... types){
        this.id = id;
        this.types = types;
    }

    public static List<CombinationRune> eligibleFor(Runes rune){
        List<CombinationRune> runes = new ArrayList<>(20);
        for(CombinationRune r : CombinationRune.values()){
            for(Runes ru : r.types){
                if(ru == rune){
                    runes.add(r);
                }
            }
        }
        return runes;
    }
}
