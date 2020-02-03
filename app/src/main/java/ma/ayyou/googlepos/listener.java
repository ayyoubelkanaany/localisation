package ma.ayyou.googlepos;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class listener implements RecognitionListener
{
    public listener(Context context){
        this.context=context;
    }
    public speaker parleur;
   public Context context;
    private static final String TAG = "MyStt3Activity";
    MapsActivity mapsActivity;
    public void onReadyForSpeech(Bundle params){ }
    public void onBeginningOfSpeech(){ }
    public void onRmsChanged(float rmsdB){ }
    public void onBufferReceived(byte[] buffer) { }
    public void onEndOfSpeech(){ }
    public void onError(int error)
    {
        Log.i("error ", String.valueOf(error));
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onResults(Bundle results)
    {

        //results : the result that the person have said
        //recuperer sous forme de tableau
        String str = new String();
        Log.d(TAG, "onResults " + results);
        ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
         /*   for (int i = 0; i < data.size(); i++)
            {
                Log.d(TAG, "result " + data.get(i));
                str += data.get(i);
            }
         */
        //  mText.setText("results: "+str+" "+String.valueOf(data.size()));
        Log.i("results: ", (String) data.get(0));
        mapsActivity=new MapsActivity();
        parleur=new speaker(MapsActivity.parleur);
        parleur.speake((String) data.get(0));

    }
    public void onPartialResults(Bundle partialResults)
    {
        Log.d(TAG, "onPartialResults");
    }
    public void onEvent(int eventType, Bundle params)
    {
        Log.d(TAG, "onEvent " + eventType);
    }
}