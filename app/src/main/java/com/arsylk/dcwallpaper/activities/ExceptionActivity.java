package com.arsylk.dcwallpaper.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Define;
import com.arsylk.dcwallpaper.views.BigTextDialog;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class ExceptionActivity extends AppCompatActivity {
    private Context context = ExceptionActivity.this;
    static class RedirectExceptionHandler implements Thread.UncaughtExceptionHandler {
        private Activity activity;
        public RedirectExceptionHandler(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void uncaughtException(Thread t, Throwable throwable) {
            StringBuilder stacktrace = new StringBuilder();
            for(String line : Log.getStackTraceString(throwable).split("\n")) {
                int offset = line.length() - line.trim().length();
                String padding = new String(new char[offset*8]).replace("\0", " ");
                stacktrace.append(padding);
                stacktrace.append(line.trim());
                stacktrace.append("\n");
            }

            Intent intent = new Intent(activity, ExceptionActivity.class);
            intent.putExtra("title", String.format("FATAL EXCEPTION: %s", t.getName()));
            intent.putExtra("stacktrace", stacktrace.toString());
            activity.startActivity(intent);

            Process.killProcess(Process.myPid());
            System.exit(2);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get extra data
        Bundle extra = getIntent().getExtras();
        if(extra != null) {
            // set title
            if(extra.containsKey("title")) {
                setTitle(extra.getString("title"));
            }
            // get stack traces
            if(extra.containsKey("stacktrace")) {
                String stacktrace = extra.getString("stacktrace");
                // show stack traces
                if(stacktrace != null) {
                    RecyclerView recyclerView = BigTextDialog.initBigTextView(context, stacktrace);
                    setContentView(recyclerView);
                }
                // save stack traces
                try {
                    File logFile = new File(Define.BASE_DIRECTORY, "exception-stacktrace.log");
                    FileUtils.writeStringToFile(logFile, stacktrace, Charset.forName("utf-8"));
                    Toast.makeText(context, "Log file: "+logFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
