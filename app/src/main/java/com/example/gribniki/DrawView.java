package com.example.gribniki;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class DrawView extends View {
    private static int width, height;
    private float clickX = 0, clickY = 0;
    private Paint paint;

    private Game game;

    MediaPlayer click, music;
    SharedPreferences settings;
    SharedPreferences.Editor editor;

    RectF panel, heartImage, soundButton, musicButton, restartButton;

    public DrawView(Context context) {
        super(context);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;

        paint = new Paint();
        paint.setAntiAlias(true);

        game = new Game();
        click = MediaPlayer.create(context, R.raw.click);
        music = MediaPlayer.create(context, R.raw.background);
        music.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                music.start();
            }
        });

        settings = context.getSharedPreferences("Settings", MODE_PRIVATE);
        editor = settings.edit();
        if (settings.getBoolean("Music", true)) this.music.start();

        // Init coordinates for images and buttons
        panel = new RectF(0, 0, width, height / 20f);
        heartImage = new RectF(0, 0, width / 8f, height / 20f);
        soundButton = new RectF(5 * width / 8f - 10, 0, 6 * width / 8f - 10, height / 20f);
        musicButton = new RectF(6 * width / 8f - 10, 0, 7 * width / 8f - 10, height / 20f);
        restartButton = new RectF(7 * width / 8f, 0, width, height / 20f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw background
        myDrawBitmap(canvas, R.drawable.background, new RectF(0, 0, width, height));
        drawTopPanel(canvas);

        if (clickX > 0 && clickY > 0) {
            myDrawBitmap(canvas, R.drawable.click, new RectF(clickX - 50, clickY - 50, clickX + 50, clickY + 50));
            clickX = 0;
            clickY = 0;
            invalidate();
        }
    }

    private void myDrawBitmap(Canvas canvas, int id, RectF rectF) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
        canvas.drawBitmap(bitmap, null, rectF, paint);
    }

    private void drawTopPanel(Canvas canvas) {
        // Panel
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(panel, 10, 10, paint);

        // Heart image
        myDrawBitmap(canvas, R.drawable.heart, heartImage);

        // Lives count
        String lives = "x" + game.getLives();
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.fontSize));
        Rect rect = new Rect();
        paint.getTextBounds(lives, 0, lives.length(), rect);
        float textWidth, textHeight;
        //textWidth = paint.measureText(lives);
        textHeight = rect.height();
        canvas.drawText(lives, width / 7f, height / 40f + (textHeight / 2f), paint);

        int id = 0;
        // Sound button
        if (settings.getBoolean("Sound", true)) id = R.drawable.sound_on;
        else id = R.drawable.sound_off;
        myDrawBitmap(canvas, id, soundButton);

        // Music button
        if (settings.getBoolean("Music", true)) id = R.drawable.music_on;
        else id = R.drawable.music_off;
        myDrawBitmap(canvas, id, musicButton);

        // Restart button
        myDrawBitmap(canvas, R.drawable.restart, restartButton);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX(), y = event.getY();

            if (settings.getBoolean("Sound", true)) {
                clickX = x; clickY = y;
                click.start();
            }

            // Click on Sound button
            if (x >= soundButton.left && x <= soundButton.right && y >= soundButton.top && y <= soundButton.bottom) {
                // Reverse value in settings
                editor.putBoolean("Sound", !settings.getBoolean("Sound", true));
                editor.apply();
            }

            // Click on Music button
            else if (x >= musicButton.left && x <= musicButton.right && y >= musicButton.top && y <= musicButton.bottom) {
                // Reverse value in settings
                boolean toggleMusic = !settings.getBoolean("Music", true);
                editor.putBoolean("Music", toggleMusic);
                editor.apply();

                if (!toggleMusic) {
                    music.stop();
                    try {
                        music.prepare();
                        music.seekTo(0);
                    }
                    catch (Throwable t) {
                        Toast.makeText(this.getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                else music.start();
            }

            // Click on Restart button
            else if (x >= restartButton.left && x <= restartButton.right && y >= restartButton.top && y <= restartButton.bottom) {
                // Restart game...
            }

            invalidate();
        }
        return true;
    }
}
