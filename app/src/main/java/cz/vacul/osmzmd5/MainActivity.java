package cz.vacul.osmzmd5;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    EditText tv;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        tv = (EditText) findViewById(R.id.sample_text);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public static native String crackMD5(String inputHash);

    public void crackIt(View view){
        new CrackTask(this).execute(tv.getText().toString());
    }

    static class CrackTask extends AsyncTask<String, Void, String> {
        Context appContext;

        public CrackTask(Context context){
            appContext = context.getApplicationContext();
        }

        @Override
        protected String doInBackground(String... params) {
            return crackMD5(params[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(appContext, s, Toast.LENGTH_LONG).show();
        }
    }
}
