package tech.greedylabs.crowspot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    EditText usernameEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(Helper.SHARED_PREF_KEY, MODE_PRIVATE);
        if(!sharedPreferences.getBoolean(Helper.PREF_EXISTS, false))
        {
            setContentView(R.layout.activity_main);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.TRANSPARENT);
            usernameEt = findViewById(R.id.usernameEt);
        }
        else{
            Intent intent = new Intent(this,ScanActivity.class);
            startActivity(intent);
            finish();
        }

    }

    public void proceed(View view) {
        String username = usernameEt.getText().toString();

        if (username.isEmpty())
        {
            usernameEt.requestFocus();
            usernameEt.setError("Username cannot be empty");
        }
        else{
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Helper.PREF_EXISTS, true);
            editor.putString(Helper.USERNAME, username);
            editor.apply();
            Intent intent = new Intent(this,ScanActivity.class);
            startActivity(intent);
            finish();
        }
    }
}