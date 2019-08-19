package com.arsylk.mammonsmite.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.arsylk.mammonsmite.Async.AsyncAnnouncements;
import com.arsylk.mammonsmite.Async.interfaces.OnAnnouncementPost;
import com.arsylk.mammonsmite.R;

import java.util.ArrayList;
import java.util.List;

public class DCAnnouncementsAdapter extends BaseAdapter implements OnAnnouncementPost {
    private Context context;
    private List<DCAnnouncementItem> announcementList;
    private View loaderView = null;

    public DCAnnouncementsAdapter(Context context, boolean autoLoad) {
        this.context = context;
        this.announcementList = new ArrayList<>();
        if(autoLoad) loadAnnouncements();
    }

    @SuppressLint("InflateParams")
    public View getLoaderView() {
        if(loaderView == null) {
            loaderView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.footer_progress, null);
            loaderView.findViewById(R.id.footer_progressbar).setVisibility(View.VISIBLE);
        }
        return loaderView;
    }

    public synchronized void loadAnnouncements() {
        if(loaderView != null) loaderView.findViewById(R.id.footer_progressbar).setVisibility(View.VISIBLE);
        new AsyncAnnouncements(context, false){
            @Override
            protected void onPostExecute(List<DCAnnouncementItem> dcAnnouncementItems) {
                super.onPostExecute(dcAnnouncementItems);
                if(loaderView != null) loaderView.findViewById(R.id.footer_progressbar).setVisibility(View.GONE);
                notifyDataSetChanged();
            }
        }.setOnAnnouncementsPost(this).execute();
    }

    @Override
    public void onProgressUpdate(DCAnnouncementItem... announcements) {
        if(announcements.length == 0) {
            announcementList.clear();
            return;
        }

        for(DCAnnouncementItem announcement : announcements) {
            announcementList.add(announcement);
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.item_announcement, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.title = convertView.findViewById(R.id.announcement_title);
            holder.author = convertView.findViewById(R.id.announcement_author);
            holder.date = convertView.findViewById(R.id.announcement_date);
            holder.views = convertView.findViewById(R.id.announcement_views);
            holder.thumb = convertView.findViewById(R.id.announcement_thumb);
            convertView.setTag(holder);
        }

        final DCAnnouncementItem announcement = getItem(position);
        ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.title.setText(announcement.isShowTranslated() ? announcement.getTranslatedTitle() : announcement.getTitle());
        holder.author.setText(announcement.getAuthor());
        holder.date.setText(announcement.getDate());
        holder.views.setText(announcement.getViews());
        holder.thumb.setVisibility(announcement.getThumbBitmap() != null ? View.VISIBLE : View.GONE);
        if(announcement.getThumbBitmap() != null) {
            holder.thumb.setImageBitmap(announcement.getThumbBitmap());
        }

        return convertView;
    }

    static class ViewHolder {
        TextView title, author, date, views;
        ImageView thumb;
    }

    @Override
    public int getCount() {
        return announcementList.size();
    }

    @Override
    public DCAnnouncementItem getItem(int position) {
        return announcementList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return Long.valueOf(getItem(position).getId());
    }
}
