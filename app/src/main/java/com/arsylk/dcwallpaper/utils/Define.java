package com.arsylk.dcwallpaper.utils;

import android.os.Environment;
import com.arsylk.dcwallpaper.BuildConfig;
import com.arsylk.dcwallpaper.R;

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

    public static final int REQUEST_PERMISSION_STORAGE = 997;
    public static final int REQUEST_FILE_UNPACK = 227, REQUEST_FILE_PACK = 228, REQUEST_WALLPAPER_SET = 229, REQUEST_FILE_PATCH = 230;

    public static final String ONLINE_MODELS_URL = "https://arsylk.pythonanywhere.com/api/get_models?offset=%s";
    public static final String ONLINE_MODEL_FILE_URL = "https://arsylk.pythonanywhere.com/api/get_model_file/%s";
    public static final String ONLINE_MODEL_PREVIEW_URL = "https://arsylk.pythonanywhere.com/api/get_model_preview/%s";

    public static final String UPLOAD_COMMUNITY_PATCH = "http://dwatchseries-storage.000webhostapp.com/dctools/post_locale_patch.php";
    public static final String REMOTE_ASSET_COMMUNITY_PATCH = "http://dwatchseries-storage.000webhostapp.com/dctools/assets_community_patch.php";

    //regional assets
    public static final String REMOTE_ASSET_ENGLISH_PATCH = "http://dwatchseries-storage.000webhostapp.com/dctools/assets_english_patch.php?md5=%s";
    public static final File ASSET_ENGLISH_PATCH = new File(ASSETS_DIRECTORY, "english_patch.json");

    public static final String REMOTE_ASSET_RUSSIAN_PATCH = "http://dwatchseries-storage.000webhostapp.com/dctools/assets_russian_patch.php?md5=%s";
    public static final File ASSET_RUSSIAN_PATCH = new File(ASSETS_DIRECTORY, "russian_patch.json");

    //public assets
    public static final String REMOTE_ASSET_CHILD_NAMES = "http://dwatchseries-storage.000webhostapp.com/dctools/assets_child_names.php?md5=%s";
    public static final File ASSET_CHILD_NAMES = new File(ASSETS_DIRECTORY, "child_names.json");

    public static final String REMOTE_ASSET_CHILD_SKILLS = "http://dwatchseries-storage.000webhostapp.com/dctools/assets_child_skills.php?md5=%s";
    public static final File ASSET_CHILD_SKILLS = new File(ASSETS_DIRECTORY, "child_skills.json");

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


    public static final Pattern PATTERN_BANNER_DATE = Pattern.compile(".*(\\d{1,2})월\\D*?(\\d{1,2})일.*?(\\d{1,2})월\\D*?(\\d{1,2})일.*");
    public static final Pattern PATTERN_BANNER_DATE_TIME = Pattern.compile(".*(\\d{1,2})월\\D*?(\\d{1,2})일\\D*?(\\d{1,2})시.*?(\\d{1,2})월\\D*?(\\d{1,2})일\\D*?(\\d{1,2})시.*");
}
