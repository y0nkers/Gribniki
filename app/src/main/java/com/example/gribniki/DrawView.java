package com.example.gribniki;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class DrawView extends View {
    private static int width, height;
    private float clickX = 0, clickY = 0;
    final private Paint paint;

    final private Game game;
    Point[] coordinates;

    MediaPlayer music;
    SoundPool soundPool;

    SharedPreferences settings;
    SharedPreferences.Editor editor;

    int click_sound = 0, pickup_sound = 0, bonus_sound = 0, trap_sound = 0, bear_sound = 0, nothing_sound = 0, gameover_sound = 0;
    RectF panel, heartImage, mushroomImage, soundButton, musicButton, restartButton;

    public DrawView(Context context) {
        super(context);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;

        paint = new Paint();
        paint.setAntiAlias(true);

        game = new Game(width, height);
        coordinates = game.getCoordinates();

        AudioAttributes attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        soundPool = new SoundPool.Builder().setAudioAttributes(attributes).build();
        click_sound = soundPool.load(context, R.raw.click, 1);
        pickup_sound = soundPool.load(context, R.raw.pickup, 1);
        bonus_sound = soundPool.load(context, R.raw.bonus, 1);
        trap_sound = soundPool.load(context, R.raw.trap, 1);
        bear_sound = soundPool.load(context, R.raw.bear, 1);
        nothing_sound = soundPool.load(context, R.raw.nothing, 1);
        gameover_sound = soundPool.load(context, R.raw.gameover, 1);

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
        mushroomImage = new RectF(2 * width / 7f + 10, 0, 3 * width / 7f - 10, height / 20f);
        soundButton = new RectF(5 * width / 8f - 10, 0, 6 * width / 8f - 10, height / 20f);
        musicButton = new RectF(6 * width / 8f - 10, 0, 7 * width / 8f - 10, height / 20f);
        restartButton = new RectF(7 * width / 8f, 0, width, height / 20f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        myDrawBitmap(canvas, R.drawable.background, new RectF(0, 0, width, height));
        drawTopPanel(canvas);
        drawObjects(canvas);

        if (!game.isActive()) {
            drawGameOverWindow(canvas);
            soundPool.play(gameover_sound, 1, 1, 1, 0, 1);
        }
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
        String str = "x" + game.getLives();
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.fontSize));
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);
        float textWidth, textHeight;
        //textWidth = paint.measureText(lives);
        textHeight = rect.height();
        canvas.drawText(str, width / 7f, height / 40f + (textHeight / 2f), paint);

        // Mushroom image
        myDrawBitmap(canvas, R.drawable.mushroom, mushroomImage);

        // Mushroom count
        str = "x" + game.getPoints();
        paint.getTextBounds(str, 0, str.length(), rect);
        //textWidth = paint.measureText(lives);
        textHeight = rect.height();
        canvas.drawText(str, 3 * width / 7f + 10, height / 40f + (textHeight / 2f), paint);

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

    private void drawObjects(Canvas canvas) {
        boolean[] opened = game.getOpened();
        int objectsCount = game.OBJECTS_COUNT;
        int sprite_width = game.BUSH_WIDTH, sprite_height = game.BUSH_HEIGHT;
        RectF rectF = new RectF();

        for (int i = 0; i < objectsCount; ++i) {
            if (!opened[i]) {
                rectF.set(coordinates[i].x - sprite_width / 2f, coordinates[i].y - sprite_height / 2f, coordinates[i].x + sprite_width / 2f, coordinates[i].y + sprite_height / 2f);
                myDrawBitmap(canvas, R.drawable.bush, rectF);
            }
        }
    }

    private void drawGameOverWindow(Canvas canvas) {
        // Window
        RectF rectF = new RectF(width / 10f, 2 * height / 6f, 9 * width / 10f, 4 * height / 6f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(rectF, 10, 10, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(rectF, 10, 10, paint);

        // Text
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.fontSize));
        String str = "Конец игры!";
        Rect rect = new Rect();
        float textWidth, textHeight, centerX, centerY;
        centerX = width / 2f;
        centerY = 7 * height / 18f;
        paint.getTextBounds(str, 0, str.length(), rect);
        textWidth = paint.measureText(str);
        textHeight = rect.height();
        canvas.drawText(str, centerX - (textWidth / 2f), centerY + (textHeight / 2f), paint);

        str = "Ваш результат: " + getResources().getQuantityString(R.plurals.points, game.getPoints(), game.getPoints());
        paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.pointsFontSize));
        centerX = width / 2f;
        centerY = 9 * height / 18f;
        paint.getTextBounds(str, 0, str.length(), rect);
        textWidth = paint.measureText(str);
        textHeight = rect.height();
        canvas.drawText(str, centerX - (textWidth / 2f), centerY + (textHeight / 2f), paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX(), y = event.getY();

            if (settings.getBoolean("Sound", true)) {
                clickX = x;
                clickY = y;
                soundPool.play(click_sound, 1, 1, 1, 0, 1);
            }

            // Interaction with objects
            if (game.isActive()) {
                int objectsCount = game.OBJECTS_COUNT;
                int[] objects = game.getObjects();
                boolean[] opened = game.getOpened();
                int sprite_width = game.BUSH_WIDTH, sprite_height = game.BUSH_HEIGHT;
                int id;

                for (int i = 0; i < objectsCount; ++i) {
                    // Check if click on object
                    if (x >= coordinates[i].x - sprite_width / 2f && x <= coordinates[i].x + sprite_width / 2f && y >= coordinates[i].y - sprite_height / 2f && y <= coordinates[i].y + sprite_width / 2f && !opened[i]) {
                        game.openObject(i); // Open the object so as not to draw it in the future
                        game.setLives(game.getLives() - 1); // Decrease lives count

                        ObjectType type = ObjectType.values()[objects[i]];
                        switch (type) {
                            case MUSHROOM:
                                id = R.drawable.mushroom;
                                game.setPoints(game.getPoints() + 1);
                                Toast.makeText(this.getContext(), "Под кустом вы нашли небольшую группу грибов.\n+1 очко!", Toast.LENGTH_LONG).show();
                                if (settings.getBoolean("Sound", true))
                                    soundPool.play(pickup_sound, 1, 1, 1, 0, 1);
                                break;
                            case BONUS:
                                // add bonus: + 5 mushrooms or + 3 lives
                                if (game.getRandom().nextBoolean()) {
                                    game.setPoints(game.getPoints() + 5);
                                    Toast.makeText(this.getContext(), "Под кустом вы нашли огромную группу грибов!\n+5 очков!", Toast.LENGTH_LONG).show();
                                } else {
                                    game.setLives(game.getLives() + 4); // Compensate -1 live when player clicked on object
                                    Toast.makeText(this.getContext(), "Под кустом вы нашли кое-что лучше, чем грибы.\n+3 жизни", Toast.LENGTH_LONG).show();
                                }
                                if (settings.getBoolean("Sound", true))
                                    soundPool.play(bonus_sound, 1, 1, 1, 0, 1);
                                break;
                            case TRAP:
                                id = R.drawable.bear;
                                if (game.getRandom().nextBoolean()) {
                                    Toast.makeText(this.getContext(), "Зайдя за куст, вы наткнулись на бурого медведя.\nRest in Peace...", Toast.LENGTH_LONG).show();
                                    game.setLives(0);
                                    if (settings.getBoolean("Sound", true))
                                        soundPool.play(bear_sound, 1, 1, 1, 0, 1);
                                } else {
                                    Toast.makeText(this.getContext(), "Зайдя за куст, вы наступили на капкан.\n-1 жизнь. Ещё легко отделались!", Toast.LENGTH_LONG).show();
                                    if (game.getLives() != 0) game.setLives(game.getLives() - 1);
                                    if (settings.getBoolean("Sound", true))
                                        soundPool.play(trap_sound, 1, 1, 1, 0, 1);
                                }
                                break;
                            case NOTHING:
                                soundPool.play(nothing_sound, 1, 1, 1, 0, 1);
                                Toast.makeText(this.getContext(), "Под кустом вы ничего не нашли.", Toast.LENGTH_LONG).show();
                                break;
                        }
                        if (game.getLives() == 0) game.setActive(false);
                        break;
                    }
                }
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
                    } catch (Throwable t) {
                        Toast.makeText(this.getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else music.start();
            }

            // Click on Restart button
            else if (x >= restartButton.left && x <= restartButton.right && y >= restartButton.top && y <= restartButton.bottom) {
                // if game is not active close game over window
                game.restart();
                coordinates = game.getCoordinates();
            }

            invalidate();
        }
        return true;
    }
}
