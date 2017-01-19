package com.biao.joystick.sample;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.biao.joystick.JoystickView;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = "MainActivity";
  private JoystickView joystickView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    joystickView = (JoystickView) findViewById(R.id.joystickView);

    joystickView.setDegrees(0, 60, 120, 180, 240, 300);
    joystickView.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
      @Override
      public void onChanged(int angle, double power) {
        Log.d(TAG, "angle = " + angle + ", power = " + power);
      }
    });
  }

  public void testChangeColor(View view) {
    joystickView.setPanelColor(Color.BLACK);
    joystickView.setGuideLineColor(Color.RED);
    joystickView.setInnerAreaStrokeColor(Color.GREEN);
    joystickView.setJoystickColor(Color.WHITE);
  }

  public void testChangeScale(View view) {
    joystickView.setJoystickScale(0.3f);
    joystickView.setInnerAreaScale(0.5f);
    joystickView.setPanelScale(1.0f);
  }
}
