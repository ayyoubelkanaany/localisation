package ma.ayyou.googlepos;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
///////////enable speaker while I'm recording
/////////////////////and speak=true
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
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.BaseColumns;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, View.OnClickListener {

    private GoogleMap mMap;
    private Marker marker;
    private Marker marker1;
    private Circle circle;
    public static listener lis;
    static LatLng blindCoor;
    TextToSpeech txtSpeech;
    public static double latitude, longitude;
    private LatLng latLng;
    private LocationManager locationManager;
    public Location location;
    boolean isenable = false;
    public static speaker parleur;
    private Context context;
    static sqliteDbHelper dbhelper;
    Timer timer;
    SpeechRecognizer sr;
    Button record;

    ///arrays to fill from database see get_all_cercles
    Circle [] allCircles;
    String [] allnames;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
      /*  if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { String[] permission = {Manifest.permission.RECORD_AUDIO};requestPermissions(permission, 3);
        }
*/

      locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        isenable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        parleur = new speaker(this);
        parleur.initializespeechRecognizer();
        parleur.initializeTextToSpeech("welcome I am ready");
        marker=null;
        circle=null;
        etat_gps();
        context=this;
        dbhelper= new sqliteDbHelper(context);


        //record = (Button) findViewById(R.id.record);

        //record.setOnClickListener(this);
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {

        parleur.initializeTextToSpeech("welcome I am ready");
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.setBuildingsEnabled(true);
        ///long insert=insertion("ici",""+31.635248,""+-8.000068);//zoomed map
        //long insert=insertion("FST",""+31.6439666,""+-8.0203603);//zoomed map  31°38'40.6"N 8°01'11.4"
       //Toast.makeText(getApplicationContext(), ""+insert, Toast.LENGTH_SHORT).show();
        blindCoor=my_place(latitude,longitude);             //blindCoordinates while is walking
        get_all_circles();           //get all zones on the map


        /////to get coordinates every 10secs
        Log.i("blind coord", String.valueOf(blindCoor));
       final Handler handler = new Handler();
        timer = new Timer();
        TimerTask doTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            testZone(get_all_circles(),blindCoor);      //this fct check if the person is inside the zone  or not;
                        } catch (Exception e) {
                            Toast.makeText(MapsActivity.this, "erreur", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };
        timer.schedule(doTask, 0, 10000);
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
               /****
                drawingCercle(blindCoor.latitude, blindCoor.longitude,"newZone");         ////attention
                long a=insertion("salle3",""+blindCoor.latitude,""+blindCoor.longitude);
                Log.i("nbr of isertion", String.valueOf(a));
               ****/



                ////vibrating while long click on the map
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //deprecated in API 26
                    vibrator.vibrate(300);
                }

                 //////speaking to the user////////////////
                parleur.speake("what is the name of your new zone");
                speaker.speak=false;     //this for enabling the speaker to speak until the user say what he want
              try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();///
                }

              //intent to get voice's user///////
              Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
              intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
              intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());  //or ar-JO   en-US   ar-MA
              intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.example.keyboard");

              intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);  // 1 is the maximum number of results to be returned.
              sr.startListening(intent);
                speaker.speak=true;    //permission granted commander hh
          }
        });
    }
    /////LES M'ETHODE UTILISER
   public LatLng  my_place(double lat,double lon){
        // Add a marker and move the camera
       if(marker!=null){     //when
           marker.remove();
       }
       latitude=lat;longitude=lon;
       if(latitude!=0.0 && longitude!=0.0){
           latLng = new LatLng(latitude, longitude);
           marker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory
                   .defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                   .title("you are here"));       ////for blind marker with blue color
           mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));     //zoomed map
       }
       return latLng;
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
           /* if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;    /////c'est moi
            }*/
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 1, this);
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
                    parleur.speake("you are in " + knownName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    ///////////fct to test if the user is inside or not
    private boolean  testZone(Circle [] cercles,LatLng Coord)
    {
        for (int i=0;i<cercles.length;i++) {                      //foreach circle I test if the user is inside or not       //Circle oneCircle : cercles
            Log.i("circles", String.valueOf(cercles[i])+" and noun is "+String.valueOf(allnames[i]));
            if (cercles[i] != null) {
                Boolean msg =isCircleContains(cercles[i],Coord);
                if (msg) {
                    //Toast.makeText(getBaseContext(), "Blind person is Inside"+Coord.latitude+" and long "+Coord.longitude, Toast.LENGTH_LONG).show();
                    parleur.speake("you are inside "+allnames[i]);
                    break;    //if we found the right zone we break

                } else {
                    //Toast.makeText(getBaseContext(), "Blind person is Outside"+Coord.latitude+" and long "+Coord.longitude, Toast.LENGTH_LONG).show();
                    parleur.speake("you are not closer to our zone");  //if not we continue
                    continue;

                }
            }
        }
        return false;
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
   public Circle [] get_all_circles(){
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

       allCircles=new Circle[itemIds.size()];
       allnames=new String[itemIds.size()];
       for(int i=0;i<itemIds.size();i++){
           double c_lat=0,c_log=0;
           String c_nom="vide";
           c_nom=zones.get(i);
           c_lat=Double.parseDouble(altitudes.get(i));
           c_log=Double.parseDouble(longitudes.get(i));
           Log.i("cr1", String.valueOf(drawingCercle(c_lat,c_log,c_nom)));
           Circle drawedCircle=drawingCercle(c_lat,c_log,c_nom);
           allCircles[i]=drawedCircle;                 //filling the circle array by circles' database
           allnames[i]=c_nom;                          //filling the name circles array to get noun of places from database
           Log.i("circle "+i, allCircles[i].toString());
           Log.i("circle name "+i, allnames[i]);
       }
       return allCircles;
   }
    public Circle drawingCercle(double lat, double lon,String nom){
        // Add a marker in latLng precised and move the camera
        LatLng place = new LatLng(lat,lon);   /////lag and lat
        marker1=mMap.addMarker(new MarkerOptions().position(place).title(nom));
        circle = mMap.addCircle(new CircleOptions()              //circle created arround home variable
                .center(place)
                .radius(20)      // 20 meters for the zone perimeter
                .fillColor(Color.TRANSPARENT)
                .strokeColor(Color.rgb(34,10,44)));
        // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 14));
         return circle;

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
                    ///si la localisation n'est pas null on change les coordonnées qui sont static
                    longitude=location.getLongitude();
                    latitude=location.getLatitude();
                }
            }
            else{

            }
        }
        if (requestCode == 2) {
            if(resultCode==RESULT_CANCELED){
                parleur.speake("gps is on ");
            }
        }
    }
    public long insertion(String nom,String altitude,String longitude){
      //  Log.i("insert1","hello"+dbhelper);
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        //Log.i("insert2","hello"+dbhelper);
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
      blindCoor=my_place(location.getLatitude(),location.getLongitude());
      getAddress(location.getLatitude(),location.getLongitude());
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    @Override
    public void onProviderEnabled(String provider) {
        parleur.speake("turn on your gps");
    }
    @Override
    public void onProviderDisabled(String provider) {
        parleur.speake("turn on your gps");
        Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent,2);
    }



    /////speake fct to speake a msg
    public void speake(String message) {
        if (Build.VERSION.SDK_INT > 21) {
            txtSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            txtSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null);

        }
    }
    //initialise txtToSpeech to a language
    private void initializeTextToSpeech() {
        txtSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (txtSpeech.getEngines().size() == 0) {
                    Toast.makeText(MapsActivity.this, "no tts", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    txtSpeech.setLanguage(Locale.US);      //you can choose any language
                    speake("Welcome I am ready");
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
