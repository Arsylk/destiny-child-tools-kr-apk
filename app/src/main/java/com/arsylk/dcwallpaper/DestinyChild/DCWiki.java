package com.arsylk.dcwallpaper.DestinyChild;

import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Define;
import com.arsylk.dcwallpaper.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class DCWiki {
    public static class Page {
        private String modelId = null, name = null, kname = null, region = null;
        private int stars = 0, element = 0, type = 0;
        private String skillLeader = null, skillAuto = null, skillTap = null, skillSlide = null, skillDrive = null;
        private String portaitImages[] = null;
        private String thumbnailImage = null;

        public Page(JSONObject json) {
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
                    kname = json.getString("kname");
                else
                    kname = "";
                if(json.has("region"))
                    region = json.getString("region");

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

                if(json.has("thumbnail"))
                    thumbnailImage = json.getString("thumbnail");

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

        public String getModelId() {
            return modelId;
        }

        public String getName() {
            return name;
        }

        public String getKname() {
            return kname;
        }

        public String getRegion() {
            return region;
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

        public String getThumbnailImage() {
            return thumbnailImage;
        }
    }
    private Map<String, Page> wikiPages;
    private List<Page> sortedWikiPages = null;

    public DCWiki() {
        this.wikiPages = new HashMap<>();
        loadWiki("kr");
    }

    private void loadWiki(String region) {
        try {
            JSONObject jsonWiki = Utils.fileToJson(Define.ASSET_CHILD_SKILLS);
            JSONArray arrayWiki = jsonWiki.getJSONArray("child_skills");
            for(int i = 0; i < arrayWiki.length(); i++) {
                JSONObject childWiki = arrayWiki.getJSONObject(i);
                if(childWiki.has("region") && childWiki.has("model_id")) {
                    if(childWiki.getString("region").equalsIgnoreCase(region)) {
                        wikiPages.put(childWiki.getString("model_id"), new Page(childWiki));
                    }
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasWikiPage(String modelId) {
        return wikiPages.containsKey(modelId);
    }

    public Page getWikiPage(String modelId) {
        if(wikiPages.containsKey(modelId)) {
            return wikiPages.get(modelId);
        }
        return null;
    }

    public List<Page> getWikiPages() {
        if(sortedWikiPages == null) {
            List<String> keys = new ArrayList<>(wikiPages.keySet());
            Collections.sort(keys);
            sortedWikiPages = new ArrayList<>();
            for(String key : keys) {
                sortedWikiPages.add(wikiPages.get(key));
            }
        }

        return sortedWikiPages;
    }
}

/*
"name": "Naiad",
"kname": "메르핸 나이아스",
"thumbnail": "http://static.inven.co.kr/image_2011/site_image/game/minidata/99/5500030_i.png",
"image1": "http://i.imgur.com/HRJWusA.png",
"image2": "http://i.imgur.com/zEBzXzZ.png",
"image3": "http://i.imgur.com/c0KnLuf.png",
"imageSkin": "",
"starLevel": "5",
"region": "kr",
"element": "water",
"type": "support",
"skillLeader": "Skill gauge charge rate +20% to all allies",
"skillAuto": "Deals 93 damage",
"skillTap": "Deals 314 damage to 1 enemy and skill gauge charge speed +40% for 8s to 2 allies with highest attack",
"skillSlide": "Deals 699 damage to 1 enemy and gives 140% Double-Edge (increase attack and reduce defense) for 14s to 5 water attack type allies                                                                                ",
"skillDrive": "Deals 1840 damage to 3 random enemeis and heals 1526HP to 3 allies with lowest HP and attack +120% for 20s to 3 allies with highest attack",
"tags": [
    "Naias"
],
"tiers": {
    "WORLD_BOSS": "A",
    "PVP": "C",
    "PVE": "C"
},
"props": {}
*/
