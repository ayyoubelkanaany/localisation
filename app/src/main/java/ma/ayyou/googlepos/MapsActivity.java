package ma.ayyou.googlepos;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static ma.ayyou.googlepos.speaker.nom_zone;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private Marker marker;
    private Marker marker1;
    private Circle circle;
    public static double latitude, longitude;
    private LatLng latLng;
    private LocationManager locationManager;
    public Location location;
    boolean isenable = false;
    speaker parleur;
    private Context context;
    sqliteDbHelper dbhelper;
    Timer timer;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { String[] permission = {Manifest.permission.RECORD_AUDIO};requestPermissions(permission, 3);
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        isenable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        parleur = new speaker(this);
        parleur.initializespeechRecognizer();
        parleur.initializeTextToSpeech("Bienvenu");
        marker=null;
        circle=null;
        etat_gps();
        context=this;
        dbhelper= new sqliteDbHelper(context);
       /*timer.schedule(new TimerTask() {
           @Override
           public void run() {
               isCircleContains(circle,latLng);
           }
       },0,10000);*/
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.setBuildingsEnabled(true);
        my_place(latitude,longitude);/////////////////
        get_all_circles();
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                parleur.speake("qu'il est le nome de la zone");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                parleur.speechRecognizer.startListening(intent);
            }
        });
    }
    /////LES M'ETHODE UTILISER
   public void  my_place(double lat,double lon){
        // Add a marker in Sydney and move the camera
       if(marker!=null){
           marker.remove();
       }
       latitude=lat;longitude=lon;
       if(latitude!=0.0 && longitude!=0.0){
           latLng = new LatLng(latitude, longitude);
           marker=mMap.addMarker(new MarkerOptions().position(latLng).title("you are here"));
           mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));     //zoomed map
       }
    }
    ////////:
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void etat_gps() {///vérifier état de gps
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED | checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_DENIED) {
                String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE};
                requestPermissions(permission, 3);
            } else {
                /// Toast.makeText(context, "hello1", Toast.LENGTH_SHORT).show();
            }
        }
        if (isenable) {
            ////exécuter si le gps est activer
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                ///si la localisation n'est pas null en change les coordonnées qui sont static
                longitude=location.getLongitude();
                latitude=location.getLatitude();
            } else {

            }
        }
        else {
            int REQUEST_ENABLE_LOCATION = 6;
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.RECORD_AUDIO},REQUEST_ENABLE_LOCATION);
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 3);
        }
    }
    ////////////////////////////////////////////////////////
    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(lat, lng, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addresses != null) {
                String knownName = addresses.get(0).getFeatureName();
                if (knownName.equals("Unnamed Road")) {

                } else {
                    parleur.speake("vous etes sur " + knownName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    ////////////////////////////////////////////////////////////::::
    private boolean isCircleContains(Circle circle, LatLng point) {
        double r = circle.getRadius();
        LatLng center = circle.getCenter();
        double cX = center.latitude;
        double cY = center.longitude;
        double pX = point.latitude;
        double pY = point.longitude;
        float[] results = new float[1];
        Location.distanceBetween(cX, cY, pX, pY, results);
        if (results[0] < r) {
            return true;
        } else {
            return false;
        }
    }
////////////////////////////////////////////////////////////////////////
   public void get_all_circles(){
       Cursor cursor= selection();
       List itemIds = new ArrayList<>();
       List<String> zones=new ArrayList<>();
       List<String> altitudes=new ArrayList<>();
       List<String> longitudes=new ArrayList<>();
       while(cursor.moveToNext()) {
           long itemid = cursor.getLong(
                   cursor.getColumnIndexOrThrow(Contrat.testcontrat._ID));
           itemIds.add(itemid);
           String zone = cursor.getString(
                   cursor.getColumnIndexOrThrow(Contrat.testcontrat.COLUMN_NAME_ZONE));
           zones.add(zone);
           String altitude = cursor.getString(
                   cursor.getColumnIndexOrThrow(Contrat.testcontrat.COLUMN_NAME_ALTTUDE));
           altitudes.add(altitude);
           String logitude = cursor.getString(
                   cursor.getColumnIndexOrThrow(Contrat.testcontrat.COLUMN_NAME_LONGITUDE));
           longitudes.add(logitude);
       }
       cursor.close();
       for(int i=0;i<itemIds.size();i++){
           double c_lat=0,c_log=0;
           String c_nom="";
           c_nom=zones.get(i);
           c_lat=Double.parseDouble(altitudes.get(i));
           c_log=Double.parseDouble(longitudes.get(i));
          drawingCercle(c_lat,c_log,c_nom);
       }
   }
    public void drawingCercle(double lat, double lon,String nom){
        // Add a marker in latLng precised and move the camera
        LatLng place = new LatLng(lat,lon);   /////lag and lat
        marker1=mMap.addMarker(new MarkerOptions().position(place).title(nom));
        circle = mMap.addCircle(new CircleOptions()              //circle created arround home variable
                .center(place)
                .radius(15)
                .fillColor(Color.TRANSPARENT)
                .strokeColor(Color.rgb(34,10,44)));
         mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 14));
        long insert=insertion(nom_zone,""+latitude,""+longitude);//zoomed map
        parleur.speake("la zone est bien ajouter");


    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3) {
            if (resultCode == RESULT_CANCELED) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
                location= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    ///si la localisation n'est pas null en change les coordonnées qui sont static
                    longitude=location.getLongitude();
                    latitude=location.getLatitude();
                }
            }
            else{

            }
        }
        if (requestCode == 2) {
            if(resultCode==RESULT_CANCELED){
                parleur.speake("le gps est ouvert");
            }
        }
    }
    public long insertion(String nom,String altitude,String longitude){
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Contrat.testcontrat.COLUMN_NAME_ZONE, nom);
        values.put(Contrat.testcontrat.COLUMN_NAME_ALTTUDE, altitude);
        values.put(Contrat.testcontrat.COLUMN_NAME_LONGITUDE, longitude);

        long newRowId = db.insert(Contrat.testcontrat.TABLE_NAME, null, values);
        return newRowId;
    }
    public Cursor selection(){
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        String[] projection = {
                BaseColumns._ID,
                Contrat.testcontrat.COLUMN_NAME_ZONE,
                Contrat.testcontrat.COLUMN_NAME_ALTTUDE,
                Contrat.testcontrat.COLUMN_NAME_LONGITUDE,

        };
        ///String selection = Contrat.testcontrat.COLUMN_NAME_PRENOM + " LIKE?";
        ///String[] selectionArgs = { "user" };
        Cursor cursor = db.query(
                Contrat.testcontrat.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        return cursor;
    }
    @Override
    public void onLocationChanged(Location location) {
      my_place(location.getLatitude(),location.getLongitude());
      getAddress(location.getLatitude(),location.getLongitude());
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    @Override
    public void onProviderEnabled(String provider) {
        parleur.speake("ouvrez le gps");
    }
    @Override
    public void onProviderDisabled(String provider) {
        parleur.speake("ouvrez le gps");
        Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent,2);
    }
}
