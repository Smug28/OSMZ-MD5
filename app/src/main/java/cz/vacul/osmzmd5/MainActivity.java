package cz.vacul.osmzmd5;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {
    public static final String PROGRESS_UPDATE = "cz.vacul.osmzmd5.PROGRESS_UPDATE";
    public static char[] inputChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l','m','n','o','p','q','r','s','t','u','v','w','x','y','z', 'A','B','C','D','E','F','G','H','I','J','K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    private EditText tv;
    private TextView log;
    private ScrollView scrollView;
    private MyBroadcastReceiver receiver = new MyBroadcastReceiver();

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
        log = (TextView) findViewById(R.id.log);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(PROGRESS_UPDATE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public static native String stringFromJNI();
    public static native String crackMD5(String inputHash, int inputSize);
    public static native String calcHashCpp(String input);
    public static String calcHashJava(String input){
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(input.getBytes(Charset.forName("UTF8")));
            final byte[] resultByte = messageDigest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : resultByte)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String crackMD5Java(String inputHash, int inputSize){
        for (int length = 1; length <= inputSize; length++){
        int[] positions = new int[length];
            for (int i = 0; i < length; i++)
                positions[i] = 0;

            String str = positions2String(positions);
            if (inputHash.equals(calcHashJava(str))) {
                return str;
            }

            int i = length - 1;
            boolean carry = false;
            while (i >= 0){
                if (i < 0 && carry)
                    break;
                if (positions[i] >= 62) {
                    positions[i] = 0;
                    i--;
                    carry = true;
                    continue;
                }
                else {
                    positions[i]++;
                    if (positions[i] >= 62)
                        continue;
                    else {
                        i = length - 1;
                        carry = false;
                    }
                }

                str = positions2String(positions);
                if (inputHash.equals(calcHashJava(str))) {
                    return str;
                }
            }
        }
        return null;
    }

    private static String positions2String(int[] positions) {
        StringBuilder sb = new StringBuilder();
        for (int i : positions)
            sb.append(inputChars[i]);
        return sb.toString();
    }

    public void crackIt(View view){
        new CrackTask(this).execute(tv.getText().toString());
    }

    static class CrackTask extends AsyncTask<String, String, String> {
        Context appContext;

        public CrackTask(Context context){
            appContext = context.getApplicationContext();
        }

        @SuppressLint("DefaultLocale")
        @Override
        protected String doInBackground(String... params) {
            String hashJava = calcHashJava(params[0]);
            String hashCpp = calcHashCpp(params[0]);
            publishProgress(String.format("Input string: %s\nJava hash: %s\nC++ hash: %s\n\nCracking with C++...\n", params[0], hashJava, hashCpp));

            long startTime = System.currentTimeMillis();
            String cppResult = crackMD5(hashCpp, params[0].length());
            long endTime = System.currentTimeMillis();

            publishProgress(String.format("Password found: %s\nTook %d ms\n\nCracking with Java...\n", cppResult, endTime - startTime));

            startTime = System.currentTimeMillis();
            String javaResult = crackMD5Java(hashJava, params[0].length());
            endTime = System.currentTimeMillis();
            publishProgress(String.format("Password found: %s\nTook %d ms\n", javaResult, endTime - startTime));

            return "Done";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(appContext, s, Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            for (String s : values) {
                Intent update = new Intent(PROGRESS_UPDATE);
                update.putExtra("progress", s + "\n");
                appContext.sendBroadcast(update);
            }
        }
    }

    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (PROGRESS_UPDATE.equals(intent.getAction())){
                log.append(intent.getStringExtra("progress"));
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        }
    }
}
