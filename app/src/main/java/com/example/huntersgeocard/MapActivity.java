package com.example.huntersgeocard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.content.Intent;
import android.view.LayoutInflater;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.FolderOverlay;


public class MapActivity extends AppCompatActivity implements LocationListener {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private MapView mMapView;
    private IMapController mapController;
    private Marker currentLocationMarker;
    private LocationManager locationManager;
    private ImageButton btnLocate;
    private ImageButton btnLayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context ctx = getApplicationContext();
        //Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(String.valueOf(ctx));
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
        btnLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                requestLocationUpdates();
            }
        });
        btnLayer = findViewById(R.id.btnLayer);
        btnLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater li = LayoutInflater.from(MapActivity.this);
                View promptView = li.inflate(R.layout.tool_back_layer, null);
                AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(MapActivity.this);
                mDialogBuilder.setTitle("Выберите необходимые слои");
                mDialogBuilder.setView(promptView);
                mDialogBuilder.show();
                final CheckBox CheckA =promptView.findViewById(R.id.checkBoxA);
                final CheckBox CheckB =promptView.findViewById(R.id.checkBoxB);
                final CheckBox CheckV =promptView.findViewById(R.id.checkBoxV);
                final CheckBox CheckG =promptView.findViewById(R.id.checkBoxG);
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
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        GeoPoint startPoint = new GeoPoint(latitude, longitude);
        if (currentLocationMarker == null) {
            currentLocationMarker = new Marker(mMapView);
            mMapView.getOverlays().add(currentLocationMarker);
        }
        currentLocationMarker.setPosition(startPoint);
        mapController.setCenter(startPoint);
        mMapView.invalidate();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private void requestLocationUpdates() {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    onLocationChanged(location);
                }
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }


   // private void attachExternalDB() {
     //   File extStore = Environment.getExternalStorageDirectory();
      //  File extDbFile = new File(extStore.getAbsolutePath() + "/" + "parma-dxf.sqlite");
      //  if (!extDbFile.exists()) {
      //      throw new RuntimeException("Cannot find external DB " + extDbFile.getPath() + ". Probably external SD card is not mounted");
     //   }
  //  }
    private void loadGeoData(int intraw) {
        FolderOverlay OverlayBuf = null;
        int resint = intraw;
        KmlDocument kmlDocument = new KmlDocument();
        Drawable defaultMarker = getResources().getDrawable(org.osmdroid.bonuspack.R.drawable.marker_default);
        Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
        Style defaultStyle = new Style(defaultBitmap, 0x901010AA, 5f, 0x20AA1010);
        if (resint == 1){
          //  mMapView.getOverlays().remove(OverlayBuf);
            kmlDocument.parseKMLStream(getResources().openRawResource(R.raw.amo), null);
            FolderOverlay geoJsonOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(mMapView, defaultStyle, null, kmlDocument);
            mMapView.getOverlays().add(geoJsonOverlay);
            mMapView.invalidate();
            OverlayBuf = geoJsonOverlay;
        }
        if (resint == 2){
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
        if (resint == 0){
            mMapView.getOverlays().remove(OverlayBuf);
            mMapView.invalidate();
                }
            }

    public void onBackPressed(){
        Intent exit_intent = new Intent(MapActivity.this, MainActivity.class);
        startActivity(exit_intent);
    }
}