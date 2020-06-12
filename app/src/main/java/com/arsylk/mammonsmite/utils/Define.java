package com.arsylk.mammonsmite.utils;

import android.os.Environment;
import com.arsylk.mammonsmite.BuildConfig;
import com.arsylk.mammonsmite.R;

import java.io.File;
import java.util.regex.Pattern;

public final class Define {
    public static final File ASSETS_DIRECTORY = new File(Environment.getExternalStorageDirectory(),
            "/Android/data/"+ BuildConfig.APPLICATION_ID+"/files");
    public static final File BASE_DIRECTORY = new File(Environment.getExternalStorageDirectory(), "DCUnpacker");
    public static final File UNPACKER_DIRECTORY = new File(BASE_DIRECTORY, "Unpacked");
    public static final File MODELS_DIRECTORY = new File(BASE_DIRECTORY, "Models");
    public static final File ONLINE_DIRECTORY = new File(BASE_DIRECTORY, "Online");
    public static final File BITMAP_CACHE_DIRECTORY = new File(ASSETS_DIRECTORY, "bitmap");
    public static final File DUMP_DATA_DIRECTORY = new File(ASSETS_DIRECTORY, "dump");

    public static final int REQUEST_PERMISSION_STORAGE = 997;
    public static final int REQUEST_FILE_UNPACK = 227, REQUEST_FILE_PACK = 228, REQUEST_WALLPAPER_SET = 229, REQUEST_FILE_PATCH = 230;

    public static final String ONLINE_ANNOUNCEMNT_BANNERS = "https://kr-gf.line.games/notice/DC/ANDROID/inGame";

    public static final String ONLINE_MODELS_URL = "https://arsylk.pythonanywhere.com/api/get_models?offset=%s";
    public static final String ONLINE_MODEL_FILE_URL = "https://arsylk.pythonanywhere.com/api/get_model_file/%s";
    public static final String ONLINE_MODEL_PREVIEW_URL = "https://arsylk.pythonanywhere.com/api/get_model_preview/%s";

    public static final String UPLOAD_COMMUNITY_PATCH = "https://arsylk.pythonanywhere.com/api/post_locale_patch";
    public static final String REMOTE_ASSET_COMMUNITY_PATCH = "http://dwatchseries-storage.000webhostapp.com/dctools/assets_community_patch.php";

    public static final String REMOTE_CHECK_VERSION = "https://arsylk.pythonanywhere.com/api/get_apk_version";

    //regional assets
    public static final String REMOTE_ASSET_ENGLISH_PATCH = "https://arsylk.pythonanywhere.com/api/get_english_patch/%s";
    public static final File ASSET_ENGLISH_PATCH = new File(ASSETS_DIRECTORY, "english_patch.json");

    public static final String REMOTE_ASSET_RUSSIAN_PATCH = "https://arsylk.pythonanywhere.com/api/get_russian_patch/%s";
    public static final File ASSET_RUSSIAN_PATCH = new File(ASSETS_DIRECTORY, "russian_patch.json");

    //new game dump data assets
    public static final String REMOTE_ASSET_DUMP_DATA = "https://arsylk.pythonanywhere.com/api/get_file/%s/%s";
    public static final String[] DUMP_DATA = new String[] {
            "CHAR_DATA",
            "CHARACTER_SKIN_DATA",
            "SKILL_ACTIVE_DATA",
            "SKILL_BUFF_DATA",
            "IGNITION_CHARACTER_SKILL_DATA",
            "SKILL_LEVEL_EQUATIONS",
            "english_patch",
    };


    //public assets
    public static final String REMOTE_ASSET_CHILD_SKILLS = "https://arsylk.pythonanywhere.com/api/get_children_skills?md5=%s";
    public static final File ASSET_CHILD_SKILLS = new File(ASSETS_DIRECTORY, "child_skills.json");

    public static final String REMOTE_ASSET_EQUIPMENT_STATS = "https://arsylk.pythonanywhere.com/api/get_equipment_stats?md5=%s";
    public static final File ASSET_EQUIPMENT_STATS = new File(ASSETS_DIRECTORY, "equipment_stats.json");

    public static final String REMOTE_ASSET_SOUL_CARTA = "https://arsylk.pythonanywhere.com/api/get_soul_carta?md5=%s";
    public static final File ASSET_SOUL_CARTA = new File(ASSETS_DIRECTORY, "soul_carta.json");

    public static final File ASSET_EXTRACTED_CHILD_NAMES = new File(ASSETS_DIRECTORY, "extracted_child_names.json");
    public static final File ASSET_EVENT_BANNERS = new File(ASSETS_DIRECTORY, "banner_events.json");


    public static final String REMOTE_TRANSLATE_TEXT = "http://dwatchseries-storage.000webhostapp.com/dctools/translate.php?id=%s&text=%s";

    public static final Integer[] CONVERT_ID_ELEMENT = new Integer[] {
            R.id.search_element_fire,
            R.id.search_element_water,
            R.id.search_element_wind,
            R.id.search_element_light,
            R.id.search_element_dark
    };
    public static final Integer[] CONVERT_ID_TYPE = new Integer[] {
            R.id.search_type_attacker,
            R.id.search_type_tank,
            R.id.search_type_healer,
            R.id.search_type_debuffer,
            R.id.search_type_support
    };

    public static final Integer[] CONVERT_ID_ITEM_TYPE = new Integer[] {
            R.id.search_type_weapon,
            R.id.search_type_armor,
            R.id.search_type_accessory
    };


    public static final Pattern PATTERN_BANNER_DATE = Pattern.compile(".*(\\d{1,2})월\\D*?(\\d{1,2})일.*?(\\d{1,2})월\\D*?(\\d{1,2})일.*");
    public static final Pattern PATTERN_BANNER_DATE_TIME = Pattern.compile(".*(\\d{1,2})월\\D*?(\\d{1,2})일\\D*?(\\d{1,2})시.*?(\\d{1,2})월\\D*?(\\d{1,2})일\\D*?(\\d{1,2})시.*");
    public static final Pattern PATTERN_LOCALE_DATE = Pattern.compile("^locale_(\\d{2}-\\d{2}-\\d{4})\\.pck\\.bak$");


    public static final String[] CHILD_ATTRIBUTE_NAME = new String[] {
            "None", "Water", "Fire", "Forest", "Light", "Dark"
    };
    public static final String[] CHILD_ROLE_NAME = new String[] {
            "None", "Attacker", "Defencer", "Healer", "Balancer", "Supporter", "Exp", "Upgrade", "Over Limit", "Max Exp"
    };

    public static final int SKILL_AUTO = 1, SKILL_TAP = 2, SKILL_SLIDE = 3, SKILL_DRIVE = 4, SKILL_LEADER = 5;
    public static final String[] SKILL_NUMBER_NAME = new String[] {
            "None", "Auto", "Tap", "Slide", "Drive", "Leader"
    };

    public final static Integer[] CHILD_ATTRIBUTE_ICON = new Integer[] {
            android.R.color.transparent,
            R.drawable.ic_element_water,
            R.drawable.ic_element_fire,
            R.drawable.ic_element_forest,
            R.drawable.ic_element_light,
            R.drawable.ic_element_dark,
    };

    public final static Integer[] CHILD_ROLE_ICON = new Integer[] {
            android.R.color.transparent,
            R.drawable.ic_type_attacker,
            R.drawable.ic_type_tank,
            R.drawable.ic_type_healer,
            R.drawable.ic_type_debuffer,
            R.drawable.ic_type_support,
            android.R.color.transparent,
            android.R.color.transparent,
            android.R.color.transparent,
            android.R.color.transparent,
    };


    public final static Integer[] CHILD_ATTRIBUTE_OVERLAY = new Integer[] {
            android.R.drawable.stat_notify_error,
            R.drawable.frame_element_water,
            R.drawable.frame_element_fire,
            R.drawable.frame_element_forest,
            R.drawable.frame_element_light,
            R.drawable.frame_element_dark,
    };
}