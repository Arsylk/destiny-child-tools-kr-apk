package com.arsylk.mammonsmite.DestinyChild;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Pair;
import com.arsylk.mammonsmite.Async.CachedImage;
import com.arsylk.mammonsmite.utils.Define;
import com.arsylk.mammonsmite.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.*;


public class DCNewWiki {
    public static JSONObject CHARACTER_DATA = null;
    public static JSONObject CHARACTER_SKIN_DATA = null;
    public static JSONObject SKILL_ACTIVE_DATA = null;
    public static JSONObject SKILL_BUFF_DATA = null;
    public static JSONObject IGNITION_SKILL_DATA = null;
    public static JSONObject SKILL_TEXT_DATA = null;
    public static JSONObject CHARACTER_TEXT_DATA = null;
    public static List<Child> ALL_CHILDREN = new ArrayList<>();
    public static void load() throws Exception {
        CHARACTER_DATA = Utils.fileToJson(new File(Define.DUMP_DATA_DIRECTORY, String.format("%s.json", Define.DUMP_DATA[0])));
        CHARACTER_SKIN_DATA = Utils.fileToJson(new File(Define.DUMP_DATA_DIRECTORY, String.format("%s.json", Define.DUMP_DATA[1])));
        SKILL_ACTIVE_DATA = Utils.fileToJson(new File(Define.DUMP_DATA_DIRECTORY, String.format("%s.json", Define.DUMP_DATA[2])));
        SKILL_BUFF_DATA = Utils.fileToJson(new File(Define.DUMP_DATA_DIRECTORY, String.format("%s.json", Define.DUMP_DATA[3])));
        IGNITION_SKILL_DATA = Utils.fileToJson(new File(Define.DUMP_DATA_DIRECTORY, String.format("%s.json", Define.DUMP_DATA[4])));

        JSONObject localeJson = Utils.fileToJson(new File(Define.DUMP_DATA_DIRECTORY, String.format("%s.json", Define.DUMP_DATA[5])));
        SKILL_TEXT_DATA = localeJson.getJSONObject("files").getJSONObject("f80a001a49cfda65").getJSONObject("dict");
        CHARACTER_TEXT_DATA = localeJson.getJSONObject("files").getJSONObject("c40e0023a077cb28").getJSONObject("dict");

        ALL_CHILDREN = new ArrayList<>();
        for(String idx : Utils.forloop(CHARACTER_DATA.keys())) {
            try {
                ALL_CHILDREN.add(new Child(idx));
            }catch(Exception e) {
            }
        }
    }
    public static void unload() throws Exception {
        CHARACTER_DATA = null;
        CHARACTER_SKIN_DATA = null;
        SKILL_ACTIVE_DATA = null;
        SKILL_BUFF_DATA = null;
        IGNITION_SKILL_DATA = null;
        SKILL_TEXT_DATA = null;
        ALL_CHILDREN = null;
        System.gc();
    }


    public static class Child {
        public JSONObject json;
        public String idx;
        public String name;
        public int attribute, role, grade;
        public List<Pair<String, Integer>> status;
        public LinkedHashMap<String, Skill> skills;
        public LinkedHashMap<String, Skill> skills_ignited;
        public LinkedHashMap<String, String> skins;
        private CachedImage image = null;

        public Child(String idx) {
            this.idx = idx;
            this.status = new ArrayList<>();
            this.skills = new LinkedHashMap<>();
            this.skills_ignited = new LinkedHashMap<>();
            this.skins = new LinkedHashMap<>();
            try {
                load();
            }catch(JSONException e) {
                e.printStackTrace();
            }
        }

        private void load() throws JSONException {
            json = CHARACTER_DATA.getJSONObject(idx);

            // basic info
            name = json.getString("name");
            attribute = json.getInt("attribute");
            role = json.getInt("role");
            grade = json.getInt("start_grade");

            // view idx
            if(CHARACTER_SKIN_DATA.has(idx)) {
                List<String> skinsList = new ArrayList<>();
                if(CHARACTER_SKIN_DATA.get(idx) instanceof JSONObject) {
                    String key = CHARACTER_SKIN_DATA.getJSONObject(idx).keys().next();

                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(CHARACTER_SKIN_DATA.getJSONObject(idx).getJSONObject(key));

                    CHARACTER_SKIN_DATA.put(idx, jsonArray);
                }

                if(CHARACTER_SKIN_DATA.get(idx) instanceof JSONArray) {
                    JSONArray allSkinsArray = CHARACTER_SKIN_DATA.getJSONArray(idx);
                    for(int i = 0; i < allSkinsArray.length(); i++) {
                        Object skinsObject = allSkinsArray.get(i);

                        if(skinsObject instanceof JSONArray) {
                            JSONArray skinsArray = (JSONArray) skinsObject;
                            for(int s = 0; s < skinsArray.length(); s++) {
                                JSONObject skinJson = skinsArray.getJSONObject(s);
                                skinsList.add(skinJson.getString("view_idx"));
                            }
                        }else if(skinsObject instanceof JSONObject) {
                            JSONObject skinsJson = (JSONObject) skinsObject;
                            for(String key : Utils.forloop(skinsJson.keys())) {
                                JSONObject skinJson = skinsJson.getJSONObject(key);
                                skinsList.add(skinJson.getString("view_idx"));
                            }
                        }
                    }
                    Collections.sort(skinsList);

                    for(String skin : skinsList) {
                        String name = "";
                        if(CHARACTER_TEXT_DATA.has(skin)) {
                            if(CHARACTER_TEXT_DATA.getString(skin).contains("\t")) {
                                name = CHARACTER_TEXT_DATA.getString(skin).split("\t")[0];
                                name = name.replaceAll("_", " ").trim();
                            }
                        }
                        skins.put(skin, name);
                    }
                }

                // start loading image icon
                image = new CachedImage(String.format("http://arsylk.pythonanywhere.com/static/icons/%s.png", getSkin()), getSkin());
            }

            // get status
            for(String stat : new String[] {"hp", "def", "atk", "cri", "agi"})
                status.add(new Pair<>(stat, json.getInt(stat)));

            // get skills
            for(int i = 1; i <= 5; i++) {
                String key = String.format("skill_%d", i);
                String skill_idx = json.getString(key);
                skills.put(Define.SKILL_NUMBER_NAME[i], new Skill(skill_idx));
            }

            // get skills ignited
            if(IGNITION_SKILL_DATA.has(idx)) {
                JSONObject ignitionSkillsJson = IGNITION_SKILL_DATA.getJSONObject(idx);
                List<String> keys = Utils.forloop(ignitionSkillsJson.keys());
                Collections.sort(keys, (o1, o2) -> {
                    try {
                        return Integer.compare(Integer.parseInt(o1), Integer.parseInt(o2));
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                    return o1.compareTo(o2);
                });

                // override with skill upgrades
                for(String ignitionSkillKey : keys) {
                    JSONObject ignitionSkillJson = ignitionSkillsJson.getJSONObject(ignitionSkillKey);
                    for(int i = 1; i <= 5; i++) {
                        String key = String.format("skill_%d", i);
                        String skill_idx = ignitionSkillJson.getString(key);

                        if(SKILL_ACTIVE_DATA.has(skill_idx)) {
                            Skill ignitedSkill = new Skill(skill_idx);
                            ignitedSkill.ignited = true;
                            skills_ignited.put(Define.SKILL_NUMBER_NAME[i], ignitedSkill);
                        }
                    }
                }
            }
        }


        public CachedImage getIcon() {
            return image;
        }

        public String getSkin() {
            String skin = null;
            for(String key : skins.keySet())
                if(skin == null || key.endsWith("_01") || key.endsWith("_02"))
                    skin = key;
            return skin;
        }

        public String getName() {
            String name = null;
            for(String key : skins.keySet())
                if(name == null || key.endsWith("_01") || key.endsWith("_02"))
                    name = skins.get(key);
            return name != null ? name : this.name;
        }

        public Skill getSkill(int number) {
            return skills.get(Define.SKILL_NUMBER_NAME[number]);
        }

        public Skill getIgnitedSkill(int number) {
            return skills_ignited.get(Define.SKILL_NUMBER_NAME[number]);
        }
    }

    public static class Skill {
        public JSONObject json;
        public String idx;
        public int attribute, type, cost, gauge_per_sec, time_cool;
        public List<SkillPart> parts;
        public boolean ignited = false;

        public Skill(String idx) {
            this.idx = idx;
            this.parts = new ArrayList<>();
            try {
                load();
            }catch(JSONException e) {
            }
        }

        private void load() throws JSONException {
            json = SKILL_ACTIVE_DATA.getJSONObject(idx);

            // basic info
            attribute = json.getInt("attribute");
            type = json.getInt("type");
            cost = json.getInt("cost");
            gauge_per_sec = json.getInt("gauge_per_sec");
            time_cool = json.getInt("time_cool");

            // skill parts
            for(int i = 0; i < 5; i++) {
                parts.add(new SkillPart(json, i));
            }
        }

        public String getName() {
            String text = "";
            try {
                text = SKILL_TEXT_DATA.getString(idx);
                text = text.split("\t")[0];
            }catch(Exception e) {
            }
            return text;
        }

        public String getText() {
            String text = null;
            try {
                text = SKILL_TEXT_DATA.getString(idx);
                text = text.replaceFirst(".*\t", "");
                text = text.replace("\\", "\n");
                text = text.replaceAll("<color=.*?>(.*?)</color>", "$1");
            }catch(Exception e) {
            }
            return text;
        }

        public String getFilledText() {
            String text = getText();
            if(text != null) {
                for(SkillPart part : parts) {
                    String valueText = part.value_type == 1 ? String.format("%d", part.value) : String.format("%d%%", part.value/10);
                    if(part.value_type == 2 && text.contains("{value}") && text.contains("{p_value}")) {
                        text = text.replaceAll(String.format("\\{%s\\}", part.makeKey("value")), String.format("%d", part.value));
                    }

                    text = text.replaceAll(String.format("\\{%s\\}", part.makeKey("value")), valueText);
                    text = text.replaceAll(String.format("\\{%s\\}", part.makeKey("p_value")), valueText);
                    text = text.replaceAll(String.format("\\{%s\\}", part.makeKey("h_value")), valueText);

                    String durationText = String.format("%d Seconds", part.duration);
                    text = text.replaceAll(String.format("\\{%s\\}", part.makeKey("duration")), durationText);
                }
            }

            return text != null ? text : "";
        }
    }

    public static class SkillPart {
        public JSONObject buff_json = null;
        public String buff_idx = "", buff_logic = "";
        public String buff_name = "", buff_icon = "", buff_description = "";
        private int part_num;
        private int duration, duration_type, duration_time;
        private String n_attack, target;
        private int target_fit;
        private int value, value_type;

        public SkillPart(JSONObject skillJson, int part_num) {
            this.part_num = part_num;
            try {
                load(skillJson);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        private void load(JSONObject skillJson) throws JSONException {
            // basic info
            if(skillJson.has(makeKey("duration")))
                duration = skillJson.getInt(makeKey("duration"));
            else duration = 0;
            if(skillJson.has(makeKey("duration")+"_type"))
                duration_type = skillJson.getInt(makeKey("duration")+"_type");
            else duration_type = 0;
            if(skillJson.has(makeKey("duration")+"_time"))
                duration_time = skillJson.getInt(makeKey("duration")+"_time");
            else duration_time = 0;

            n_attack = skillJson.getString(makeKey("n_attack"));
            target = skillJson.getString(makeKey("target"));
            target_fit = skillJson.getInt(makeKey("target_fit"));

            // value & type
            value = skillJson.getInt(makeKey("value"));
            value_type = skillJson.getInt(makeKey("value")+"_type");

            // buff info
            if(skillJson.has(makeKey("buff"))) {
                buff_idx = skillJson.getString(makeKey("buff"));
                if(SKILL_BUFF_DATA.has(buff_idx)) {
                    buff_json = SKILL_BUFF_DATA.getJSONObject(buff_idx);
                    buff_logic = buff_json.getString("logic");
                }else if(buff_idx.equals("0")) buff_idx = "";

                if(SKILL_TEXT_DATA.has(buff_idx)) {
                    String[] buff_text_parts = SKILL_TEXT_DATA.getString(buff_idx).split("\t");
                    if(buff_text_parts.length > 0)
                        buff_name = buff_text_parts[0];
                    if(buff_text_parts.length > 1)
                        buff_icon = buff_text_parts[1];
                    if(buff_text_parts.length > 2)
                        buff_description = buff_text_parts[2];
                }
            }
        }

        public String makeKey(String key) {
            if(part_num == 0)
                return key;
            return String.format("%s_%d", key, part_num);
        }

        public Bitmap getBuffIcon() {
            String buffIconPath = String.format("effect/battle/buff/%s/img/value.png", buff_idx);
            File buffIconFile = new File(DCTools.Resources.DC_FILES_DIRECTORY, buffIconPath);

            if(buffIconFile.exists() && buffIconFile.isFile()) {
                try {
                    return BitmapFactory.decodeFile(buffIconFile.getAbsolutePath());
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    public static LinkedHashMap<String, String> getBuffLogicList() {
        LinkedHashMap<String, String> logicList = new LinkedHashMap<>();
        for(String idx : Utils.forloop(SKILL_BUFF_DATA.keys())) {
            try {
                String logic = SKILL_BUFF_DATA.getJSONObject(idx).getString("logic");
                if(logicList.get(logic) == null)
                    logicList.put(logic, idx);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        return logicList;
    }
}
