package com.arsylk.dcwallpaper.DestinyChild;

import com.arsylk.dcwallpaper.Async.CachedImage;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Define;
import com.arsylk.dcwallpaper.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class DCWiki {
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
                    return R.drawable.ic_element_wind;
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
                    return R.drawable.frame_element_wind;
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
        private int type;
        private String name = null, description = null, comment = null;
        private int health = 0, attack = 0, defense = 0, agility = 0, critical = 0;
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
                    health = json.getInt("health");
                    Stat hp = new Stat("HP", "Health", health, R.id.search_stat_hp);
                    statsAll.add(hp);
                    if(health > 0) {
                        statsActive.add(hp);
                    }
                }
                if(json.has("attack")) {
                    attack = json.getInt("attack");
                    Stat atk = new Stat("ATK", "Attack", attack, R.id.search_stat_atk);
                    statsAll.add(atk);
                    if(attack > 0) {
                        statsActive.add(atk);
                    }
                }
                if(json.has("defense")) {
                    defense = json.getInt("defense");
                    Stat def = new Stat("DEF", "Defense", defense, R.id.search_stat_def);
                    statsAll.add(def);
                    if(defense > 0) {
                        statsActive.add(def);
                    }
                }
                if(json.has("agility")) {
                    agility = json.getInt("agility");
                    Stat agi = new Stat("AGI", "Agility", agility, R.id.search_stat_agi);
                    statsAll.add(agi);
                    if(agility > 0) {
                        statsActive.add(agi);
                    }
                }
                if(json.has("critical")) {
                    critical = json.getInt("critical");
                    Stat crit = new Stat("CRIT", "Critical", critical, R.id.search_stat_crit);
                    statsAll.add(crit);
                    if(critical > 0) {
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
    private List<Equipment> sortedWikiEquipment = null;

    public DCWiki() {
        wikiChildrenPages = new HashMap<>();
        loadChildrenWiki();

        sortedWikiEquipment = new ArrayList<>();
        loadEquipmentStats();

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
}