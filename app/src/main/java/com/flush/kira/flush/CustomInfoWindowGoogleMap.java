package com.flush.kira.flush;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.flush.kira.flush.pojo.InfoWindowData;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowGoogleMap implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public CustomInfoWindowGoogleMap(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.map_custom_info_window, null);

        TextView name_tv = view.findViewById(R.id.name);
        TextView details_tv = view.findViewById(R.id.details);
        ImageView img = view.findViewById(R.id.pic);
        TextView price_v = view.findViewById(R.id.price);
        TextView distance_v = view.findViewById(R.id.distance);
        TextView transport_tv = view.findViewById(R.id.transport);
        RatingBar ratingBar = view.findViewById(R.id.ratingBar);

        name_tv.setText(marker.getTitle());
        details_tv.setText(marker.getSnippet());

        InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();

        price_v.setText(infoWindowData.getPrice());
        distance_v.setText(infoWindowData.getDistance());
        transport_tv.setText(infoWindowData.getTransport());
        ratingBar.setRating(Float.parseFloat("2.0"));

        return view;
    }
}
