package com.arsylk.mammonsmite.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import com.arsylk.mammonsmite.Async.AsyncWithDialog;
import com.koushikdutta.async.future.FutureCallback;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {
    /*yappy start*/
    private static int[][] yappy_maps = new int[32][16];
    private static int[] yappy_info = new int[256];
    private static boolean yappy_mapped = false;

    private static void yappy_fill() {
        long step = 1 << 16;
        for(int i = 0; i < 16; ++i) {
            int value = 65535;
            step = ((step * 67537) >> 16);
            while(value < (29L << 16)) {
                yappy_maps[value >> 16][i] = 1;
                value = (int) ((value * step) >> 16);
            }
        }

        int cntr = 0;
        for(int i = 0; i < 29; ++i) {
            for(int j = 0; j < 16; ++j) {
                if(yappy_maps[i][j] != 0) {
                    yappy_info[32 + cntr] = i + 4 + (j << 8);
                    yappy_maps[i][j] = 32 + cntr;
                    cntr += 1;
                }else {
                    if(i == 0)
                        throw new EmptyStackException();
                    yappy_maps[i][j] = yappy_maps[i - 1][j];
                }
            }
        }
        if(cntr != 256 - 32) {
            throw new EmptyStackException();
        }
        yappy_mapped = true;
    }

    public static byte[] yappy_uncompress(byte[] data, int size) {
        if(!yappy_mapped)
            yappy_fill();

        ArrayList<Byte> to = new ArrayList<>();
        int data_p = 0;
        int to_p = 0;
        while(to.size() < size) {
            if(!(data_p + 1 < data.length))
                return data;

            int index = data[data_p] & 0xFF;
            if(index < 32) {
                byte[] copy = Arrays.copyOfRange(data, data_p+1, (data_p+1)+(index+1));
                for(byte byte_copy : copy) {
                    to.add(byte_copy);
                }
                to_p += index + 1;
                data_p += index + 2;
            }else {
                int info = yappy_info[index];
                int length = info & 0x00ff;
                int offset = (info & 0xff00) + (data[data_p+1] & 0xFF);
                List<Byte> copy = to.subList((to_p - offset), Math.min((to_p - offset)+length, to.size()));
                to.addAll(copy);
                to_p += length;
                data_p += 2;
            }
        }

        byte[] to_byte = new byte[to.size()];
        for(int i = 0; i < to.size(); i++) {
            to_byte[i] = to.get(i);
        }
        return to_byte;
    }
    /*yappy end*/

    /*aes start*/
    private static Cipher[] cipher = {null, null};
    private static boolean[] cipher_made = {false, false};

    private static void make_cipher(int key) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        if(key == 0) {
            byte[] key0 = new byte[] {(byte) 0x37, (byte) 0xea, (byte) 0x79, (byte) 0x85, (byte) 0x86, (byte) 0x29, (byte) 0xec, (byte) 0x94, (byte) 0x85, (byte) 0x20, (byte) 0x7c, (byte) 0x1a, (byte) 0x62, (byte) 0xc3, (byte) 0x72, (byte) 0x4f, (byte) 0x72, (byte) 0x75, (byte) 0x25, (byte) 0x0b, (byte) 0x99, (byte) 0x99, (byte) 0xbd, (byte) 0x7f, (byte) 0x0b, (byte) 0x24, (byte) 0x9a, (byte) 0x8d, (byte) 0x85, (byte) 0x38, (byte) 0x0e, (byte) 0x39};
            cipher[key] = Cipher.getInstance("AES/ECB/NoPadding");
            cipher[key].init(Cipher.DECRYPT_MODE, new SecretKeySpec(key0, "AES"));
            cipher_made[key] = true;
        }else if(key == 1) {
            byte[] key1 = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, (byte) 0xEC, (byte) 0x8B, (byte) 0x9C, (byte) 0xED, (byte) 0x94, (byte) 0x84, (byte) 0xED, (byte) 0x8A, (byte) 0xB8, (byte) 0xEC, (byte) 0x97, (byte) 0x85, (byte) 0xEA, (byte) 0xB3, (byte) 0xBC, (byte) 0xEB, (byte) 0x9D, (byte) 0xBC, (byte) 0xEC, (byte) 0x9D, (byte) 0xB8, (byte) 0xEA, (byte) 0xB2, (byte) 0x8C, (byte) 0xEC, (byte) 0x9E, (byte) 0x84, (byte) 0xEC, (byte) 0xA6};
            cipher[key] = Cipher.getInstance("AES/ECB/NoPadding");
            cipher[key].init(Cipher.DECRYPT_MODE, new SecretKeySpec(key1, "AES"));
            cipher_made[key] = true;
        }
        
    }

    public static byte[] aes_decrypt(byte[] data, int key) throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        if(!cipher_made[key])
            make_cipher(key);

        //16 byte blocks
        data = Arrays.copyOf(data, data.length+(16 - (data.length % 16)));

        return cipher[key].doFinal(data);
    }
    /*aes end*/

    /*directories start*/
    public static File rename(File file, String newName) {
        File rename = new File(file.getParent(), newName);
        if(file.renameTo(rename)) {
            file = rename;
        }
        return file;
    }

    public static File getUnpackPath() {
        File dir = Define.UNPACKER_DIRECTORY;
        if(dir.isFile()) {
            dir.delete();
        }
        if(!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getUnpackPath(String file) {
        return new File(getUnpackPath(), file);
    }

    public static File getUnpackPath(String subdir, String file) {
        File dir = getUnpackPath(subdir);
        if(!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, file);
    }
    /*directories end*/

    /*hex start*/
    public static String md5(String seed) {
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            md.update(seed.getBytes());
            return Utils.bytesToHex(md.digest());
        }catch(Exception e) {
            return "";
        }
    }

    public static String md5(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            md.update(FileUtils.readFileToByteArray(file));
            return Utils.bytesToHex(md.digest());
        }catch(Exception e) {
            return "";
        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        for(int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        if(len % 2 == 0) {
            byte[] data = new byte[len/2];
            for(int i = 0; i < len; i+=2) {
                data[i/2] = ((byte)((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i+1), 16)));
            }
            return data;
        }
        return null;
    }
    /*hex end*/

    /*json start*/
    @SuppressLint("NewApi")
    public static JSONObject fileToJson(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder content = new StringBuilder();
            String line;
            while((line = br.readLine()) != null)
                content.append(line);
            br.close();
            return new JSONObject(content.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject fileToJson(File file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder();
            String line;
            while((line = br.readLine()) != null)
                content.append(line);
            br.close();
            return new JSONObject(content.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /*json end*/

    /*buffer start*/
    public static FloatBuffer createFloatBuffer(int floatCount) {
        ByteBuffer data = ByteBuffer.allocateDirect(floatCount * 4);
        data.order(ByteOrder.nativeOrder());
        return data.asFloatBuffer();
    }

    public static FloatBuffer setupFloatBuffer(FloatBuffer preBuffer, float[] array) {
        if (preBuffer == null || preBuffer.capacity() < array.length) {
            preBuffer = createFloatBuffer(array.length * 2);
        } else {
            preBuffer.clear();
        }
        preBuffer.put(array);
        preBuffer.position(0);
        return preBuffer;
    }

    public static ShortBuffer createShortBuffer(int shortCount) {
        ByteBuffer data = ByteBuffer.allocateDirect(shortCount * 4);
        data.order(ByteOrder.nativeOrder());
        return data.asShortBuffer();
    }

    public static ShortBuffer setupShortBuffer(ShortBuffer preBuffer, short[] array) {
        if (preBuffer == null || preBuffer.capacity() < array.length) {
            preBuffer = createShortBuffer(array.length * 2);
        } else {
            preBuffer.clear();
        }
        preBuffer.clear();
        preBuffer.put(array);
        preBuffer.position(0);
        return preBuffer;
    }

    public static ByteBuffer createByteBuffer(int count) {
        ByteBuffer data = ByteBuffer.allocateDirect(count * 4);
        data.order(ByteOrder.nativeOrder());
        return data;
    }

    public static ByteBuffer setupByteBuffer(ByteBuffer preBuffer, byte[] array) {
        if (preBuffer == null || preBuffer.capacity() < array.length) {
            preBuffer = createByteBuffer(array.length * 2);
        } else {
            preBuffer.clear();
        }
        preBuffer.put(array);
        preBuffer.position(0);
        return preBuffer;
    }

    public static IntBuffer setupIntBuffer(IntBuffer preBuffer, int[] array) {
        if (preBuffer == null || preBuffer.capacity() < array.length) {
            preBuffer = createIntBuffer(array.length * 2);
        } else {
            preBuffer.clear();
        }
        preBuffer.clear();
        preBuffer.put(array);
        preBuffer.position(0);
        return preBuffer;
    }

    public static IntBuffer createIntBuffer(int count) {
        ByteBuffer data = ByteBuffer.allocateDirect(count * 4);
        data.order(ByteOrder.nativeOrder());
        return data.asIntBuffer();
    }
    /*buffer end*/

    /*byte search start*/
    public static int findBytePatternPosition(MappedByteBuffer mbb, byte[] pattern) {
        for(int i = 0; i < mbb.limit(); i++) {
            if(mbb.get(i) == pattern[0] && mbb.limit() - i  >= pattern.length) {
                boolean matches = true;
                for(int j = 0; j < pattern.length; j++) {
                    if(mbb.get(i + j) != pattern[j]) {
                        matches = false;
                        break;
                    }
                }
                if(matches) {
                    return i;
                }
            }
        }
        return -1;
    }
    /*byte search end*/

    /*permissions start*/
    public static boolean requestPermission(Context context) {
        //get write permission all SDK
        if((ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Define.REQUEST_PERMISSION_STORAGE);
            return false;
        }

        //get read permission all SDK
        if(Build.VERSION.SDK_INT >= 16) {
            if((ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Define.REQUEST_PERMISSION_STORAGE);
                return false;
            }
        }

        return true;
    }
    /*permissions end*/

    /*files start*/
    public static File uriToFile(Uri uri) {
        File file = null;
        try {
            String uriPath = uri.getPath();
            String[] parts = uriPath.split(":");
            if(parts.length > 1) {
                uriPath = uriPath.substring(parts[0].length()+1);
                uriPath = Environment.getExternalStorageDirectory() + File.separator + uriPath;
            }
            file = new File(uriPath);
        }catch(Exception e) {
            e.printStackTrace();
        }

        return file;
    }

    public static void initDirectories() {
        //setup directories
        try {
            FileUtils.forceMkdir(Define.ASSETS_DIRECTORY);
            FileUtils.forceMkdir(Define.BITMAP_CACHE_DIRECTORY);
            FileUtils.forceMkdir(Define.DUMP_DATA_DIRECTORY);
            FileUtils.forceMkdir(Define.BASE_DIRECTORY);
            FileUtils.forceMkdir(Define.UNPACKER_DIRECTORY);
            FileUtils.forceMkdir(Define.MODELS_DIRECTORY);
            FileUtils.forceMkdir(Define.ONLINE_DIRECTORY);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    /*files end*/

    /*image start*/
    public static Bitmap trim(Bitmap source) {
        int firstX = 0, firstY = 0;
        int lastX = source.getWidth();
        int lastY = source.getHeight();
        int[] pixels = new int[source.getWidth() * source.getHeight()];
        source.getPixels(pixels, 0, source.getWidth(), 0, 0, source.getWidth(), source.getHeight());
        loop:
        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                if (pixels[x + (y * source.getWidth())] != Color.TRANSPARENT) {
                    firstX = x;
                    break loop;
                }
            }
        }
        loop:
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = firstX; x < source.getWidth(); x++) {
                if (pixels[x + (y * source.getWidth())] != Color.TRANSPARENT) {
                    firstY = y;
                    break loop;
                }
            }
        }
        loop:
        for (int x = source.getWidth() - 1; x >= firstX; x--) {
            for (int y = source.getHeight() - 1; y >= firstY; y--) {
                if (pixels[x + (y * source.getWidth())] != Color.TRANSPARENT) {
                    lastX = x;
                    break loop;
                }
            }
        }
        loop:
        for (int y = source.getHeight() - 1; y >= firstY; y--) {
            for (int x = source.getWidth() - 1; x >= firstX; x--) {
                if (pixels[x + (y * source.getWidth())] != Color.TRANSPARENT) {
                    lastY = y;
                    break loop;
                }
            }
        }
        return Bitmap.createBitmap(source, firstX, firstY, lastX - firstX, lastY - firstY);
    }

    public static void bitmapToFile(Bitmap bitmap, File file) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            FileUtils.writeByteArrayToFile(file, stream.toByteArray());
            stream.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    /*image end*/

    /*callback start*/
    public interface Callback {
        void onCall();
    }
    public interface OnProgressUpdate<Progress> {
        void onProgressUpdate(Progress progress);
    }
    public interface OnPostExecute<Result> {
        void onPostExecute(Result result);
    }
    /*callback end*/

    /*date start*/
    public static String getDateLabel(boolean shortLabel) {
        return new SimpleDateFormat(shortLabel ? "dd-MM-yyyy" : "HH-mm_dd-MM-yyyy", Locale.ENGLISH).format(Calendar.getInstance().getTime());
    }

    public static String betweenDates(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();
        if(different < 0) return "Already finished!";


        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        StringBuilder sb = new StringBuilder();
        if(elapsedDays > 0) {
            sb.append(elapsedDays);
            sb.append(" days ");
        }
        if(elapsedHours > 0) {
            sb.append(elapsedHours);
            sb.append(" hours ");
        }
        if(elapsedMinutes > 0) {
            sb.append(elapsedMinutes);
            sb.append(" minutes ");
        }
        if(sb.length() == 0) {
            sb.append("Less than an hour!");
        }
        sb.trimToSize();
        return sb.toString();
    }
    /*date end*/

    /*sounds start*/
    public static void playSoundFile(Context context, File file) {
        try {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if(am != null)
                if(am.isMusicActive())
                    return;
            MediaPlayer mp = MediaPlayer.create(context, Uri.fromFile(file));
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
        }catch(Exception e) {
            e.printStackTrace();
        }

    }
    /*sounds end*/

    /*input start*/
    public static void dismissKeyboard(Context context) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = ((Activity) context).getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(context);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    /*input end*/

    /*translate start*/
    public static void translate(Context context, String textInKr, final FutureCallback<String> callback) {
        new AsyncWithDialog<String, Void, JSONObject>(context, true, "Loading..."){
            @Override
            protected JSONObject doInBackground(String... strings) {
                try {
                    Document doc = Jsoup.connect("https://openapi.naver.com/v1/papago/n2mt")
                            .header("Content-Type", "application/x-www-form-urlencoded; charset = UTF-8")
                            .header("X-Naver-Client-Id", "1jiJsHzsjLWhrVlhILSr")
                            .header("X-Naver-Client-Secret", "QSRF3gsLOi")
                            .postDataCharset("utf-8")
                            .data("source", "ko")
                            .data("target", "en")
                            .data("text", strings[0])
                            .ignoreContentType(true)
                            .ignoreHttpErrors(true)
                            .post();
                    return new JSONObject(doc.body().text());
                }catch(Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONObject response) {
                super.onPostExecute(response);
                try {
                    callback.onCompleted(null, response.getJSONObject("message").getJSONObject("result").getString("translatedText"));
                }catch(Exception e) {
                    callback.onCompleted(e, null);
                }
            }
        }.execute(textInKr);
    }
    /*translate end*/

    /*preference start*/
    public static String getDeviceToken(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs.contains("device_token")) {
            return prefs.getString("device_token", "");
        }
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        while(sb.length() < 32){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        String s = sb.toString().substring(0, 32);
        prefs.edit().putString("device_token", s).commit();
        return s;
    }

    public static SharedPreferences widgetPref(Context context, int widgetId) {
        return context.getSharedPreferences("appwidget_" + widgetId, 0);
    }

    public static void setWidgetPref(Context context, int widgetId, String key, String value) {
        SharedPreferences.Editor prefs = widgetPref(context, widgetId).edit();
        prefs.putString(key, value);
        prefs.commit();
    }

    public static String getWidgetPref(Context context, int widgetId, String key) {
        SharedPreferences prefs = widgetPref(context, widgetId);
        return prefs.getString(key, null);
    }

    public static void removeWidgetPref(Context context, int widgetId) {
        SharedPreferences prefs = widgetPref(context, widgetId);
        prefs.edit().clear().apply();
    }
    /*preference end*/

    /*iterator to for loop*/
    public static <T> List<T> forloop(Iterator<T> iterator) {
        List<T> list = new ArrayList<>();
        while(iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }
    /*iterator end*/
}
