package com.arsylk.mammonsmite.DestinyChild;

import com.arsylk.mammonsmite.Async.CachedImage;
import com.arsylk.mammonsmite.R;
import com.arsylk.mammonsmite.utils.Define;
import com.arsylk.mammonsmite.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;


public class DCWiki {
    // shared beans
    public static class Stat {
        public final String shortText, fullText;
        public final int value, statId;
        public Stat(String shortText, String fullText, int value, int statId) {
            this.shortText = shortText;
            this.fullText = fullText;
            this.value = value;
            this.statId = statId;
        }
    }

    // children pages
    public static class Child {
        private String modelId = null, name = null, krName = null;
        private int stars = 0, element = 0, type = 0;
        private String skillLeader = null, skillAuto = null, skillTap = null, skillSlide = null, skillDrive = null;
        private String[] portaitImages = null;
        private CachedImage image = null;

        public Child(JSONObject json) {
            load(json);
        }

        private void load(JSONObject json) {
            try {
                if(json.has("model_id"))
                    modelId = json.getString("model_id");
                if(json.has("name"))
                    name = json.getString("name");
                else
                    name = "";
                if(json.has("kname"))
                    krName = json.getString("kname");
                else
                    krName = "";

                if(json.has("starLevel"))
                    stars = Integer.valueOf(json.getString("starLevel"));

                if(json.has("type")) {
                    switch(json.getString("type")) {
                        case "attacker":
                            type = 0;
                            break;
                        case "tank":
                            type = 1;
                            break;
                        case "healer":
                            type = 2;
                            break;
                        case "debuffer":
                            type = 3;
                            break;
                        case "support":
                            type = 4;
                            break;
                    }
                }

                if(json.has("element")) {
                    switch (json.getString("element")) {
                        case "fire":
                            element = 0;
                            break;
                        case "water":
                            element = 1;
                            break;
                        case "forest":
                            element = 2;
                            break;
                        case "light":
                            element = 3;
                            break;
                        case "dark":
                            element = 4;
                            break;
                    }
                }

                if(json.has("skillLeader"))
                    skillLeader = json.getString("skillLeader");
                if(json.has("skillAuto"))
                    skillAuto = json.getString("skillAuto");
                if(json.has("skillTap"))
                    skillTap = json.getString("skillTap");
                if(json.has("skillSlide"))
                    skillSlide = json.getString("skillSlide");
                if(json.has("skillDrive"))
                    skillDrive = json.getString("skillDrive");

                if(json.has("thumbnail")) {
                    image = new CachedImage(json.getString("thumbnail").replace("https://", "http://"), "child_"+modelId);
                }

                portaitImages = new String[3];
                for(int i = 0; i < portaitImages.length; i++) {
                    if(json.has("image"+(i+1))) {
                        portaitImages[i] = json.getString("image"+(i+1));
                    }else {
                        portaitImages[i] = null;
                    }
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        public CachedImage getImage() {
            return image;
        }

        public String getModelId() {
            return modelId;
        }

        public String getName() {
            return name;
        }

        public String getKrName() {
            return krName;
        }

        public int getStars() {
            return stars;
        }

        public int getElement() {
            return element;
        }

        public int getElementDrawable() {
            switch(element) {
                case 0:
                    return R.drawable.ic_element_fire;
                case 1:
                    return R.drawable.ic_element_water;
                case 2:
                    return R.drawable.ic_element_forest;
                case 3:
                    return R.drawable.ic_element_light;
                case 4:
                    return R.drawable.ic_element_dark;
                default:
                    return R.drawable.ic_error_outline_black;
            }
        }

        public int getElementFrame() {
            switch(element) {
                case 0:
                    return R.drawable.frame_element_fire;
                case 1:
                    return R.drawable.frame_element_water;
                case 2:
                    return R.drawable.frame_element_forest;
                case 3:
                    return R.drawable.frame_element_light;
                case 4:
                    return R.drawable.frame_element_dark;
                default:
                    return R.drawable.ic_error_outline_black;
            }
        }

        public int getType() {
            return type;
        }

        public int getTypeDrawable() {
            switch(type) {
                case 0:
                    return R.drawable.ic_type_attacker;
                case 1:
                    return R.drawable.ic_type_tank;
                case 2:
                    return R.drawable.ic_type_healer;
                case 3:
                    return R.drawable.ic_type_debuffer;
                case 4:
                    return R.drawable.ic_type_support;
                default:
                    return R.drawable.ic_error_outline_black;
            }
        }

        public String getSkillLeader() {
            return skillLeader;
        }

        public String getSkillAuto() {
            return skillAuto;
        }

        public String getSkillTap() {
            return skillTap;
        }

        public String getSkillSlide() {
            return skillSlide;
        }

        public String getSkillDrive() {
            return skillDrive;
        }

        public String[] getPortaitImages() {
            return portaitImages;
        }
    }
    private Map<String, Child> wikiChildrenPages;
    private List<Child> sortedWikiChildren = null;

    // equipment pages
    public static class Equipment {
        private int type;
        private String name = null, description = null, comment = null;
        private float power = 0.0f;
        private CachedImage image = null;
        private List<Stat> statsActive = null, statsAll = null;

        public Equipment(JSONObject json) {
            load(json);
        }

        private void load(JSONObject json) {
            try {
                // name & descriptions
                if(json.has("type")) {
                    switch(json.getString("type")) {
                        case "weapon":
                            type = 0;
                            break;
                        case "armor":
                            type = 1;
                            break;
                        case "accessory":
                            type = 2;
                            break;
                    }
                }
                if(json.has("name"))
                    name = json.getString("name");
                if(json.has("description"))
                    description = json.getString("description");
                if(json.has("comment"))
                    comment = json.getString("comment");

                // get item stats
                statsAll = new ArrayList<>();
                statsActive = new ArrayList<>();
                if(json.has("health")) {
                    Stat hp = new Stat("HP", "Health", json.getInt("health"), R.id.search_stat_hp);
                    statsAll.add(hp);
                    if(hp.value > 0) {
                        statsActive.add(hp);
                    }
                }
                if(json.has("attack")) {
                    Stat atk = new Stat("ATK", "Attack", json.getInt("attack"), R.id.search_stat_atk);
                    statsAll.add(atk);
                    if(atk.value > 0) {
                        statsActive.add(atk);
                    }
                }
                if(json.has("defense")) {
                    Stat def = new Stat("DEF", "Defense", json.getInt("defense"), R.id.search_stat_def);
                    statsAll.add(def);
                    if(def.value > 0) {
                        statsActive.add(def);
                    }
                }
                if(json.has("agility")) {
                    Stat agi = new Stat("AGI", "Agility", json.getInt("agility"), R.id.search_stat_agi);
                    statsAll.add(agi);
                    if(agi.value > 0) {
                        statsActive.add(agi);
                    }
                }
                if(json.has("critical")) {
                    Stat crit = new Stat("CRIT", "Critical", json.getInt("critical"), R.id.search_stat_crit);
                    statsAll.add(crit);
                    if(crit.value > 0) {
                        statsActive.add(crit);
                    }
                }
                if(json.has("power"))
                    power = (float) json.getDouble("power");

                // create cached image
                if(json.has("id") && json.has("icon")) {
                    image = new CachedImage(json.getString("icon"), "equipment_"+json.getString("id"));
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        public String getName() {
            return name;
        }

        public CachedImage getImage() {
            return image;
        }

        public List<Stat> getStats(boolean activeOnly) {
            return activeOnly ? statsActive : statsAll;
        }

        public int getType() {
            return type;
        }

        public float getPower() {
            return power;
        }

    }
    private List<Equipment> sortedWikiEquipment;

    // soul carta pages
    public static class SoulCarta {
        private String name, description, skill;
        private float standardValue = 0, prismaValue = 0;
        private String standardString = "", prismaString = "";
        private int element = -1, type = -1, condition = -1;
        private CachedImage icon = null, carta = null;
        private List<Stat> standardStats, prismaStats;

        public SoulCarta(JSONObject json) {
            load(json);
        }

        private void load(JSONObject json) {
            try {
                // details
                if(json.has("name"))
                    name = json.getString("name");
                if(json.has("description"))
                    description = json.getString("description").replace("\\n", "\n");
                if(json.has("skill"))
                    skill = json.getString("skill");

                // conditions
                if(json.has("element") && !json.isNull("element")) {
                    switch (json.getString("element")) {
                        case "fire":
                            element = 0;
                            break;
                        case "water":
                            element = 1;
                            break;
                        case "forest":
                            element = 2;
                            break;
                        case "light":
                            element = 3;
                            break;
                        case "dark":
                            element = 4;
                            break;
                    }
                }
                if(json.has("type") && !json.isNull("type")) {
                    switch (json.getString("type")) {
                        case "attacker":
                            type = 0;
                            break;
                        case "tank":
                            type = 1;
                            break;
                        case "healer":
                            type = 2;
                            break;
                        case "debuffer":
                            type = 3;
                            break;
                        case "support":
                            type = 4;
                            break;
                    }
                }
                if(json.has("condition") && !json.isNull("condition")) {
                    switch (json.getString("condition")) {
                        case "pvp":
                            condition = 0;
                            break;
                        case "pve":
                            condition = 1;
                            break;
                        case "big":
                            condition = 2;
                            break;
                    }
                }


                // images
                if(json.has("id") && json.has("icon")) {
                    icon = new CachedImage(json.getString("icon"), "soul_carta_"+json.getString("id")+"_icon");
                }
                if(json.has("id") && json.has("carta")) {
                    carta = new CachedImage(json.getString("carta"), "soul_carta_"+json.getString("id"));
                }

                // stats standard
                if(json.has("standard")) {
                    standardStats = loadStats(json.getJSONObject("standard"));
                }else {
                    standardStats = new ArrayList<>();
                }

                // stats prisma
                if(json.has("prisma")) {
                    prismaStats = loadStats(json.getJSONObject("prisma"));
                }else {
                    prismaStats = new ArrayList<>();
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        private List<Stat> loadStats(JSONObject json) throws Exception {
            List<Stat> statList = new ArrayList<>();
            if(json.has("value")) {
                if(json.has("prisma"))
                    if(!json.getBoolean("prisma")) {

                        standardValue = (float) json.getDouble("value");
                        standardString = (standardValue == (int) standardValue) ? String.valueOf((int) standardValue) : String.valueOf(standardValue);
                    }else {
                        prismaValue = (float) json.getDouble("value");
                        prismaString = (prismaValue == (int) prismaValue) ? String.valueOf((int) prismaValue) : String.valueOf(prismaValue);
                    }
            }
            if(json.has("health")) {
                Stat hp = new Stat("HP", "Health", json.getInt("health"), R.id.search_stat_hp);
                if(hp.value > 0) {
                    statList.add(hp);
                }
            }
            if(json.has("attack")) {
                Stat atk = new Stat("ATK", "Attack", json.getInt("attack"), R.id.search_stat_atk);
                if(atk.value > 0) {
                    statList.add(atk);
                }
            }
            if(json.has("defense")) {
                Stat def = new Stat("DEF", "Defense", json.getInt("defense"), R.id.search_stat_def);
                if(def.value > 0) {
                    statList.add(def);
                }
            }
            if(json.has("agility")) {
                Stat agi = new Stat("AGI", "Agility", json.getInt("agility"), R.id.search_stat_agi);
                if(agi.value > 0) {
                    statList.add(agi);
                }
            }
            if(json.has("critical")) {
                Stat crit = new Stat("CRIT", "Critical", json.getInt("critical"), R.id.search_stat_crit);
                if(crit.value > 0) {
                    statList.add(crit);
                }
            }

            return statList;
        }

        public CachedImage getCarta() {
            return carta;
        }

        public CachedImage getIcon() {
            return icon;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public List<Stat> getStats(boolean prisma) {
            return prisma ? prismaStats : standardStats;
        }

        public String getTypeText() {
            return (new String[] {
                    "",
                    " Attacker ",
                    " Tank ",
                    " Healer ",
                    " Debuffer ",
                    " Support "
                })[type+1];
        }

        public int getTypeDrawable(){
            return (new int[] {
                    0,
                    R.drawable.ic_type_attacker,
                    R.drawable.ic_type_tank,
                    R.drawable.ic_type_healer,
                    R.drawable.ic_type_debuffer,
                    R.drawable.ic_type_support
            })[type+1];

        }

        public String getElementText() {
            return (new String[] {
                    "",
                    " Fire ",
                    " Water ",
                    " Forest ",
                    " Light ",
                    " Dark "
            })[element+1];
        }

        public int getElementDrawable() {
            return (new int[] {
                    0,
                    R.drawable.ic_element_fire,
                    R.drawable.ic_element_water,
                    R.drawable.ic_element_forest,
                    R.drawable.ic_element_light,
                    R.drawable.ic_element_dark
            })[element+1];
        }

        public String getConditionText() {
            return (new String[] {
                    "",
                    "(PvP Only) ",
                    "(PvE Only) ",
                    "(Boss Only) ",
            })[condition+1];
        }

        public String getSkillFormatted(boolean prisma) {
            return getConditionText() + String.format(skill, (prisma ? prismaString : standardString));
        }
    }
    private List<SoulCarta> wikiSoulCarta;

    // single instance
    private static DCWiki instance = null;
    public static DCWiki getInstance() {
        return getInstance(false);
    }
    public static DCWiki getInstance(boolean force) {
        if(instance == null || force) {
            instance = new DCWiki();
        }

        return instance;
    }

    // constructor
    private DCWiki() {
        wikiChildrenPages = new HashMap<>();
        loadChildrenWiki();

        sortedWikiEquipment = new ArrayList<>();
        loadEquipmentStats();

        wikiSoulCarta = new ArrayList<>();
        loadSoulCartaWiki();

    }

    // child skills wiki
    private void loadChildrenWiki() {
        try {
            JSONObject jsonChildrenWiki = Utils.fileToJson(Define.ASSET_CHILD_SKILLS);
            JSONArray arrayChildrenWiki = jsonChildrenWiki.getJSONArray("child_skills");
            for(int i = 0; i < arrayChildrenWiki.length(); i++) {
                JSONObject childWiki = arrayChildrenWiki.getJSONObject(i);
                if(childWiki.has("region") && childWiki.has("model_id")) {
                    if(childWiki.getString("region").equalsIgnoreCase("kr")) {
                        wikiChildrenPages.put(childWiki.getString("model_id"), new Child(childWiki));
                    }
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasChildWiki(String modelId) {
        return wikiChildrenPages.containsKey(modelId);
    }

    public Child getChildWiki(String modelId) {
        if(wikiChildrenPages.containsKey(modelId)) {
            return wikiChildrenPages.get(modelId);
        }
        return null;
    }

    public List<Child> getChildrenWiki() {
        if(sortedWikiChildren == null) {
            List<String> keys = new ArrayList<>(wikiChildrenPages.keySet());
            Collections.sort(keys);
            sortedWikiChildren = new ArrayList<>();
            for(String key : keys) {
                sortedWikiChildren.add(wikiChildrenPages.get(key));
            }
        }

        return sortedWikiChildren;
    }

    // equipment stats wiki
    private void loadEquipmentStats() {
        try {
            JSONObject jsonEquipmentWiki = Utils.fileToJson(Define.ASSET_EQUIPMENT_STATS);
            JSONArray arrayEquipmentWiki = jsonEquipmentWiki.getJSONArray("equipment_stats");
            for(int i = 0; i < arrayEquipmentWiki.length(); i++) {
                JSONObject equipmentWiki = arrayEquipmentWiki.getJSONObject(i);
                sortedWikiEquipment.add(new Equipment(equipmentWiki));
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public List<Equipment> getEquipmentWiki() {
        return sortedWikiEquipment;
    }

    // soul carta wiki
    private void loadSoulCartaWiki() {
        try {
            JSONObject jsonSoulCartaWiki = Utils.fileToJson(Define.ASSET_SOUL_CARTA);
            JSONArray arraySoulCartaWiki = jsonSoulCartaWiki.getJSONArray("soul_cartas");
            for(int i = 0; i < arraySoulCartaWiki.length(); i++) {
                JSONObject soulCartaWiki = arraySoulCartaWiki.getJSONObject(i);
                wikiSoulCarta.add(new SoulCarta(soulCartaWiki));
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public List<SoulCarta> getSoulCartaWiki() {
        return wikiSoulCarta;
    }
}