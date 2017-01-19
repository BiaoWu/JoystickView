/*
 * Copyright (C) 2017 BiaoWu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.biao.joystick;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Joystick View for Android
 *
 * public api:
 *
 * # Scale
 * 1. 设置操作盘的比例    {@link #setPanelScale(float)}
 * 2. 设置内部区域的比例   {@link #setInnerAreaScale}
 * 3. 设置摇杆的比例      {@link #setJoystickScale(float)}
 *
 * # guide line degree
 * 1. 设置guide line的角度 {@link #setDegrees}
 *
 * # listener
 * 1. 设置监听  {@link #setOnJoystickMoveListener}
 *
 * # color
 * 1. 设置操作盘的颜色      {@link #setPanelColor}
 * 2. 设置内部区域线的颜色   {@link #setInnerAreaStrokeColor}
 * 3. 设置guide line的颜色  {@link #setGuideLineColor}
 * 4. 设置摇杆的颜色         {@link #setJoystickColor}
 *
 * @author biaowu.
 */
public class JoystickView extends View {
  private static final String TAG = "JoystickView";

  /**
   * 提供机制，而非解决方案
   */
  public interface OnJoystickMoveListener {
    /**
     * * ----- angel ------
     * *
     * *         90
     * *         ^
     * *         |
     * *         |
     * * 180 ------------> 0
     * *         |
     * *         |
     * *        270
     *
     * @param angle see above
     * @param power [0,1]
     */
    void onChanged(int angle, double power);
  }

  private OnJoystickMoveListener onJoystickMoveListener;

  private PointF joystickPoint = new PointF();
  private PointF centerPoint = new PointF();

  private Paint panelPaint;
  private Paint innerAreaPaint;
  private Paint joystickPaint;
  private Paint guideLinePaint;

  private int panelRadius;        // 操作面板的半径
  private int innerAreaRadius;    // 内部区域半径，即power = 0
  private int joystickRadius;     // 操作球的半径

  // Config 可配置的
  private float panelScale = 0.9f;      // 0 ~ 1 之间
  private float innerAreaScale = 0.4f;  // 0 ~ 1 之间
  private float joystickScale = 0.2f;   // 0 ~ 1 之间

  private int panelColor = Color.WHITE;
  private int innerAreaStrokeColor = Color.TRANSPARENT;
  private int guideLineColor = Color.TRANSPARENT;
  private int joystickColor = Color.BLUE;

  // Debug 辅助线
  private int[] degrees = new int[0];
  private float[] pts = new float[0];

  public JoystickView(Context context) {
    super(context);
    setupPaint();
  }

  public JoystickView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setupPaint();
  }

  public JoystickView(Context context, AttributeSet attrs, int defaultStyle) {
    super(context, attrs, defaultStyle);
    setupPaint();
  }

  protected void setupPaint() {
    panelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    panelPaint.setColor(panelColor);
    panelPaint.setStyle(Paint.Style.FILL_AND_STROKE);

    innerAreaPaint = new Paint();
    innerAreaPaint.setColor(innerAreaStrokeColor);
    innerAreaPaint.setStyle(Paint.Style.STROKE);

    guideLinePaint = new Paint();
    guideLinePaint.setStrokeWidth(2);
    guideLinePaint.setColor(guideLineColor);

    joystickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    joystickPaint.setColor(joystickColor);
    joystickPaint.setStyle(Paint.Style.FILL);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
        MeasureSpec.getSize(heightMeasureSpec));
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    centerPoint.set(getWidth() / 2, getHeight() / 2);

    resetJoystickPoint();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (centerPoint.x <= 0 || centerPoint.y <= 0) return;

    int smallOne = Math.min(getWidth() / 2, getHeight() / 2);
    joystickRadius = (int) (smallOne * joystickScale);
    panelRadius = (int) (smallOne * panelScale);
    innerAreaRadius = (int) (panelRadius * innerAreaScale);
    setupPts();

    // panel
    canvas.drawCircle(centerPoint.x, centerPoint.y, panelRadius, panelPaint);
    // 内环辅助线
    if (innerAreaScale > 0) {
      canvas.drawCircle(centerPoint.x, centerPoint.y, innerAreaRadius, innerAreaPaint);
    }
    // 方向辅助线
    if (pts.length > 0) {
      canvas.drawLines(pts, guideLinePaint);
    }
    // joystick
    canvas.drawCircle(joystickPoint.x, joystickPoint.y, joystickRadius, joystickPaint);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    joystickPoint.set(event.getX(), event.getY());

    double touchDis2Center = Math.sqrt(
        (joystickPoint.x - centerPoint.x) * (joystickPoint.x - centerPoint.x)
            + (joystickPoint.y - centerPoint.y) * (joystickPoint.y - centerPoint.y));

    if (touchDis2Center > panelRadius) {
      joystickPoint.x = (float) ((joystickPoint.x - centerPoint.x) * panelRadius / touchDis2Center
          + centerPoint.x);
      joystickPoint.y = (float) ((joystickPoint.y - centerPoint.y) * panelRadius / touchDis2Center
          + centerPoint.y);

      touchDis2Center = panelRadius;
    }

    int action = event.getAction();
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        break;
      case MotionEvent.ACTION_MOVE:
        break;
      case MotionEvent.ACTION_UP:
        resetJoystickPoint();
        break;
    }

    invalidate();

    if (onJoystickMoveListener != null) {
      int degree = (int) Math.toDegrees(
          Math.asin(Math.abs(centerPoint.y - joystickPoint.y) / touchDis2Center));
      if (joystickPoint.x > centerPoint.x) {
        if (joystickPoint.y < centerPoint.y) {
          //第一象限 do nothing
        } else {
          //第四象限
          degree = 360 - degree;
        }
      } else {
        if (joystickPoint.y < centerPoint.y) {
          //第二象限
          degree = 180 - degree;
        } else {
          //第三象限
          degree = 180 + degree;
        }
      }

      double power;
      if (action == MotionEvent.ACTION_UP || touchDis2Center <= innerAreaRadius) {
        power = 0;
      } else {
        power = (touchDis2Center - innerAreaRadius) / (panelRadius - innerAreaRadius);
      }
      onJoystickMoveListener.onChanged(degree, power);
    }

    return true;
  }

  public void setDegrees(int... degrees) {
    if (degrees == null || degrees.length < 2) {
      Log.e(TAG, "degrees's length must greater than 2 !");
      return;
    }

    this.degrees = degrees;
    this.pts = new float[4 * degrees.length];
    invalidate();
  }

  public void setJoystickScale(float joystickScale) {
    checkScale(innerAreaScale, "panelScale");

    this.joystickScale = joystickScale;
    invalidate();
  }

  public void setPanelScale(float panelScale) {
    checkScale(innerAreaScale, "panelScale");

    this.panelScale = panelScale;
    invalidate();
  }

  public void setInnerAreaScale(float innerAreaScale) {
    checkScale(innerAreaScale, "innerAreaScale");

    this.innerAreaScale = innerAreaScale;
    invalidate();
  }

  public void setOnJoystickMoveListener(OnJoystickMoveListener listener) {
    this.onJoystickMoveListener = listener;
  }

  public void setPanelColor(@ColorInt int panelColor) {
    this.panelColor = panelColor;
    panelPaint.setColor(panelColor);
    invalidate();
  }

  public void setInnerAreaStrokeColor(@ColorInt int innerAreaStrokeColor) {
    this.innerAreaStrokeColor = innerAreaStrokeColor;
    innerAreaPaint.setColor(innerAreaStrokeColor);
    invalidate();
  }

  public void setGuideLineColor(@ColorInt int guideLineColor) {
    this.guideLineColor = guideLineColor;
    guideLinePaint.setColor(guideLineColor);
    invalidate();
  }

  public void setJoystickColor(@ColorInt int joystickColor) {
    this.joystickColor = joystickColor;
    joystickPaint.setColor(joystickColor);
    invalidate();
  }

  // --- private area ---
  private void checkScale(float scale, String name) {
    if (scale < 0 || scale > 1) {
      throw new IllegalArgumentException(name + "must be [0,1]");
    }
  }

  private void resetJoystickPoint() {
    joystickPoint.x = centerPoint.x;
    joystickPoint.y = centerPoint.y;
  }

  private void setupPts() {
    for (int i = 0; i < degrees.length; i++) {
      double radian = Math.toRadians(degrees[i]);
      pts[i * 4] = centerPoint.x;
      pts[i * 4 + 1] = centerPoint.y;
      pts[i * 4 + 2] = (float) (centerPoint.x + Math.cos(radian) * panelRadius);
      pts[i * 4 + 3] = (float) (centerPoint.y - Math.sin(radian) * panelRadius);
    }
  }
}
