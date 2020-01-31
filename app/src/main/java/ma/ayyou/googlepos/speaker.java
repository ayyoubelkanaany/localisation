package ma.ayyou.googlepos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.format.DateUtils;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import static ma.ayyou.googlepos.MapsActivity.latitude;
import static ma.ayyou.googlepos.MapsActivity.longitude;

public class speaker extends AppCompatActivity {
    MapsActivity maps;
    private TextToSpeech myTTs;
    private Context context;
    public static String nom_zone;
    public static boolean get_zone=false;
    public SpeechRecognizer speechRecognizer;
    @RequiresApi(api = Build.VERSION_CODES.M)
    public speaker(Context context) {
        this.context=context;
    }
    ///méthode speak
    public void speake(String message) {
        if(Build.VERSION.SDK_INT>21){
            myTTs.speak(message, TextToSpeech.QUEUE_FLUSH,null,null);
        }
        else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
                myTTs.speak(message,TextToSpeech.QUEUE_FLUSH,null);
            }
        }
    }
    ///methode pour initialiser le recognizer qui gère la voix
    @SuppressLint("NewApi")
    public void initializespeechRecognizer() {
        if(speechRecognizer.isRecognitionAvailable(context)){
            speechRecognizer=SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    // Toast.makeText(context, "ready", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onBeginningOfSpeech() {
                    //Toast.makeText(context, "begenning", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    ///Toast.makeText(context, "bufferreceive", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onEndOfSpeech() {

                }
                @Override
                public void onError(int error) {
                    speake("la zone n'est pas enregistrer");
                }
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onResults(Bundle results) {
                    List<String> result=results.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);
                    nom_zone=result.get(0);
                    create_zone();
                }
                @Override
                public void onPartialResults(Bundle partialResults) {
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                }
            });
        }
    }
    ///methode appellé lorsque le speetchRecognizer est initialisé
    public void initializeTextToSpeech(final String menu) {
        myTTs=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(myTTs.getEngines().size()==0){
                    Toast.makeText(context, "no tts", Toast.LENGTH_SHORT).show();
                }
                else{
                    myTTs.setLanguage(Locale.FRENCH);
                    speake(menu);
                }

            }
        });
    }
    ////méhode poiur gérer les commandes
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void processresult(String command) throws InterruptedException {
        command=command.toLowerCase();
        if(command.indexOf("heure")!=-1){
            Date now=new Date();
            String time= DateUtils.formatDateTime(this,now.getTime(),DateUtils.FORMAT_SHOW_TIME);
            Toast.makeText(context, "time : "+time, Toast.LENGTH_SHORT).show();
            //speake("c'est"+time);
        }
    }
public void create_zone(){
    maps=new MapsActivity();
    if(latitude!=0.0 && longitude!=0.0) {
        maps.drawingCercle(latitude, longitude,nom_zone);
    }
}
}
