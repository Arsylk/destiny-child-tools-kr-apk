package com.arsylk.mammonsmite.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.arsylk.mammonsmite.Async.AsyncPatch;
import com.arsylk.mammonsmite.DestinyChild.DCLocalePatch;
import com.arsylk.mammonsmite.DestinyChild.DCTools;
import com.arsylk.mammonsmite.DestinyChild.Pck;
import com.arsylk.mammonsmite.utils.Define;
import com.arsylk.mammonsmite.utils.Utils;

public class BootCompleteAutoTranslate extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final PendingResult pendingResult = goAsync();
        System.out.println("[mTag:Boot] Boot Complete! "+intent.getAction());
        Log.d("mTag:Boot", "Boot Complete! "+intent.getAction());
        Toast.makeText(context, "Boot Complete! "+intent.getAction(), Toast.LENGTH_SHORT).show();
        Pck.PckHeader pckHeader = new Pck.PckHeader(DCTools.getDCLocalePath());
        for(Pck.PckHeader.Item item : pckHeader.getItems()) {
            if(item.flag != 0) {
                new AsyncPatch(context, false)
                        .setOnPostExecute(success -> {
                                Toast.makeText(context, success ? "Auto patch successful!" : "Auto patch failed!", Toast.LENGTH_SHORT).show();
                                pendingResult.finish();
                            }
                        ).execute(new DCLocalePatch(Utils.fileToJson(Define.ASSET_ENGLISH_PATCH)));
                break;
            }
        }
    }
}
