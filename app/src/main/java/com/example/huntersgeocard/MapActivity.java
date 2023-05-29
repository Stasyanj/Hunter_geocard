package com.example.huntersgeocard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;

public class MapActivity extends AppCompatActivity implements LocationListener {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private MapView mMapView;
    private IMapController mapController;
    private Marker currentLocationMarker;
    private LocationManager locationManager;
    private ImageButton btnLocate;
    private Button buttoncheckmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        mMapView = findViewById(R.id.mapView);
        mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);
        mapController = mMapView.getController();
        mapController.setZoom(10);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        btnLocate = findViewById(R.id.btnLocate);
        btnLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLocationUpdates();
            }
        });
        buttoncheckmap = findViewById(R.id.checkbox);
        buttoncheckmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCheckboxDialog();
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

    private void showCheckboxDialog() {
        final String[] items = {"Зона А", "Зона Б", "Зона В", "Зона Г"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите пункты").setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                    }
                })
                .setPositiveButton("Готово", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadGeoJsonData();
                        loadGeoJsonData2();
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void loadGeoJsonData(){
        KmlDocument kmlDocument = new KmlDocument();
        Drawable defaultMarker = getResources().getDrawable(org.osmdroid.bonuspack.R.drawable.marker_default);
        Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
        Style defaultStyle = new Style(defaultBitmap, 0x901010AA, 5f, 0x20AA1010);
        kmlDocument.parseKMLStream(getResources().openRawResource(R.raw.bmo), null);
        FolderOverlay geoJsonOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(mMapView, defaultStyle, null, kmlDocument);
        mMapView.getOverlays().add(geoJsonOverlay);
        mMapView.invalidate();
    }

    private void loadGeoJsonData2(){
        KmlDocument kmlDocument = new KmlDocument();
        Drawable defaultMarker = getResources().getDrawable(org.osmdroid.bonuspack.R.drawable.marker_default);
        Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
        Style defaultStyle = new Style(defaultBitmap, 0x901010AA, 5f, 0x20AA1010);
        kmlDocument.parseKMLStream(getResources().openRawResource(R.raw.amo), null);
        FolderOverlay geoJsonOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(mMapView, defaultStyle, null, kmlDocument);
        mMapView.getOverlays().add(geoJsonOverlay);
        mMapView.invalidate();
    }

}