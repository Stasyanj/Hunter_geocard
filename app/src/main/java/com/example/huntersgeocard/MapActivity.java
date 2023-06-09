package com.example.huntersgeocard;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;


import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.LocationListener;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;
import android.view.LayoutInflater;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.app.AlarmManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.Distance;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.FolderOverlay;

public class MapActivity extends AppCompatActivity {
    private MapView mMapView;
    private LocationManager locationManager;
    private IMapController mapController;
    private ImageButton btnLocate;
    private ImageButton btnLayer;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context ctx = getApplicationContext();
        Configuration.getInstance().setUserAgentValue(String.valueOf(ctx));
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView = findViewById(R.id.mapView);
        mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mMapView.setMultiTouchControls(true);
        mapController = mMapView.getController();
        mapController.setZoom(11);
        btnLocate = findViewById(R.id.btnLocate);
        GeoPoint PrimaryPoint = new GeoPoint(53.91081, 27.58667);
        mapController.setCenter(PrimaryPoint);
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        btnLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    getCurrentLocation();
            }
        });
        btnLayer = findViewById(R.id.btnLayer);
        btnLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checking();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,100, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 100, locationListener);
        checkEnabled();
    }

    private void checkEnabled() {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private void loadGeoData(int intraw) {
        FolderOverlay OverlayBuf = null;
        int resint = intraw;
        KmlDocument kmlDocument = new KmlDocument();
        Drawable defaultMarker = getResources().getDrawable(org.osmdroid.bonuspack.R.drawable.marker_default);
        Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
        Style defaultStyle = new Style(defaultBitmap, 0x901010AA, 5f, 0x20AA1010);
        if (resint == 1) {
            //  mMapView.getOverlays().remove(OverlayBuf);
            kmlDocument.parseKMLStream(getResources().openRawResource(R.raw.amo), null);
            FolderOverlay geoJsonOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(mMapView, defaultStyle, null, kmlDocument);
            mMapView.getOverlays().add(geoJsonOverlay);
            mMapView.invalidate();
            OverlayBuf = geoJsonOverlay;
        }
        if (resint == 2) {
            kmlDocument.parseKMLStream(getResources().openRawResource(R.raw.bmo), null);
            FolderOverlay geoJsonOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(mMapView, defaultStyle, null, kmlDocument);
            mMapView.getOverlays().add(geoJsonOverlay);
            mMapView.invalidate();
        }
        // if (resint == 3){
        // kmlDocument.parseKMLStream(getResources().openRawResource(R.raw.vmo), null);
        //FolderOverlay geoJsonOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(mMapView, defaultStyle, null, kmlDocument);
        // mMapView.getOverlays().add(geoJsonOverlay);
        // mMapView.invalidate();
        //  }
        //  if (resint == 4){
        //  kmlDocument.parseKMLStream(getResources().openRawResource(R.raw.gmo), null);
        // FolderOverlay geoJsonOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(mMapView, defaultStyle, null, kmlDocument);
        //  mMapView.getOverlays().add(geoJsonOverlay);
        //  mMapView.invalidate();
        // }
        if (resint == 0) {
            mMapView.getOverlays().remove(OverlayBuf);
            mMapView.invalidate();
        }
    }

    public void onBackPressed() {
        Intent exit_intent = new Intent(MapActivity.this, MainActivity.class);
        startActivity(exit_intent);
    }
    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            Marker currentLocationMarker = null;
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            GeoPoint LocPoint = new GeoPoint(latitude, longitude);
            currentLocationMarker = new Marker(mMapView);
            mMapView.getOverlays().add(currentLocationMarker);
            currentLocationMarker.setPosition(LocPoint);
            mapController.setCenter(LocPoint);
            mapController.setZoom(16);
            mMapView.invalidate();
        } else {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null){
                Marker currentLocationMarker = null;
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                GeoPoint LocPoint = new GeoPoint(latitude, longitude);
                currentLocationMarker = new Marker(mMapView);
                mMapView.getOverlays().add(currentLocationMarker);
                currentLocationMarker.setPosition(LocPoint);
                mapController.setCenter(LocPoint);
                mapController.setZoom(16);
                mMapView.invalidate();
            } else{
                // error message
            }
        }
    }
    private void checking(){
        LayoutInflater li = LayoutInflater.from(MapActivity.this);
        View promptView = li.inflate(R.layout.tool_back_layer, null);
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(MapActivity.this);
        mDialogBuilder.setTitle("Выберите необходимые слои");
        mDialogBuilder.setView(promptView);
        mDialogBuilder.show();
        final CheckBox CheckA = promptView.findViewById(R.id.checkBoxA);
        final CheckBox CheckB = promptView.findViewById(R.id.checkBoxB);
        final CheckBox CheckV = promptView.findViewById(R.id.checkBoxV);
        final CheckBox CheckG = promptView.findViewById(R.id.checkBoxG);
        final Button btnSet = promptView.findViewById(R.id.button3);
        final Button btnDel = promptView.findViewById(R.id.button6);
        CheckA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    loadGeoData(1);
            }
        });
        CheckB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    loadGeoData(2);
            }
        });
        CheckV.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    loadGeoData(3);
            }
        });
        CheckG.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    loadGeoData(4);
            }
        });
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadGeoData(0);
            }
        });

    }
}
