package com.example.S2435823HS;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class TrafficAdapter extends ArrayAdapter<PlannedEvents> {

    public TrafficAdapter(Context context, List<PlannedEvents> events) {
        super(context, 0, events);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_item_traffic, parent, false);
        }

        PlannedEvents event = getItem(position);

        TextView tvSource = convertView.findViewById(R.id.tvSource);
        TextView tvType = convertView.findViewById(R.id.tvType);
        TextView tvComment = convertView.findViewById(R.id.tvComment);
        TextView tvDelay = convertView.findViewById(R.id.tvDelay);
        ImageView ivTypeIcon = convertView.findViewById(R.id.ivTypeIcon);
/**
 * Set the text and icon for each list item
 * set text colour based on found delay type
 */

        if (event != null) {
            tvSource.setText(event.getSource());
            tvType.setText(event.getType());

            if ("Current Roadworks".equals(event.getSource())) {
                String delay = event.getDelayType();

                if ("delays".equalsIgnoreCase(delay)) {
                    tvDelay.setText("Delays reported");
                    tvDelay.setTextColor(Color.rgb(210, 140, 0));
                    tvDelay.setVisibility(View.VISIBLE);
                } else if ("longDelays".equalsIgnoreCase(delay)) {
                    tvDelay.setText("Long delays reported");
                    tvDelay.setTextColor(Color.RED);
                    tvDelay.setVisibility(View.VISIBLE);

                } else if ("veryLongDelays".equalsIgnoreCase(delay)) {
                    tvDelay.setText("Very long delays reported");
                    tvDelay.setTextColor(Color.rgb(170, 0, 0));
                    tvDelay.setVisibility(View.VISIBLE);

                } else {
                    tvDelay.setText("No delay information provided");
                    tvDelay.setTextColor(Color.GRAY);
                    tvDelay.setVisibility(View.VISIBLE);
                }
            } else {
                tvDelay.setVisibility(View.GONE);
            }

            String comment = event.getComment();

            if (comment == null || comment.trim().isEmpty()) {
                comment = "No comment provided";
            }
            if (comment.length() > 100) {
                comment = comment.substring(0, 100) + "...";
            }
            tvComment.setText(comment);

            ivTypeIcon.setImageResource(getTypeIcon(event.getType(), event.getSource()));

            if ("Unplanned Events".equals(event.getSource())) {
                tvSource.setTextColor(Color.rgb(190, 55, 45));
                convertView.setBackgroundColor(Color.rgb(255, 235, 232));
            } else {
                tvSource.setTextColor(Color.rgb(35, 105, 165));
                convertView.setBackgroundColor(Color.rgb(232, 243, 255));
            }

        }

        return convertView;


    }

    /**
     * Get the appropriate icon for the event type
     * using keyterms, in not applicable use fallback icon
     */
    private int getTypeIcon(String type, String source) {
        if (type == null || type.trim().isEmpty()) {
            if ("Current Roadworks".equals(source)) {
                return R.drawable.ic_roadworks;
            }
            return R.drawable.ic_warning;
        }
        String value = type.toLowerCase(Locale.ROOT);
        if (value.contains("accident")
                || value.contains("collision")
                || value.contains("crash")) {
            return R.drawable.ic_accident;
        }
        if (value.contains("roadClosed")
                || value.contains("closure")
                || value.contains("closed")
                || value.contains("blocked")) {
            return R.drawable.ic_closure;
        }
        if (value.contains("roadworks")
                || value.contains("roadwork")
                || value.contains("maintenance")) {
            return R.drawable.ic_roadworks;
        }
        if (value.contains("trafficlight")
                || value.contains("traffic lights")
                || value.contains("signal")) {
            return R.drawable.ic_traffic_light;
        }
        if (value.contains("lane")) {
            return R.drawable.ic_lane_restriction;
        }
        return R.drawable.ic_warning;
    }
}

