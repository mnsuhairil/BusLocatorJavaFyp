package com.example.buslocatorsystem;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NotificationUtils {

    private Context context;

    public NotificationUtils(Context context) {
        this.context = context;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        private RemoteViews customNotificationLayout;
        private NotificationCompat.Builder builder;
        private int notificationId;

        public DownloadImageTask(RemoteViews customNotificationLayout, NotificationCompat.Builder builder, int notificationId) {
            this.customNotificationLayout = customNotificationLayout;
            this.builder = builder;
            this.notificationId = notificationId;
        }

        private int imageSize = 55; // Set the desired size here (55x55 pixels)

        @Override
        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            Bitmap imageBitmap = null;
            try {
                // Use Glide to load the image
                imageBitmap = Glide.with(context)
                        .asBitmap()
                        .load(imageUrl)
                        .submit()
                        .get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return imageBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                Bitmap circularBitmap = getCircularBitmap(result);
                customNotificationLayout.setImageViewBitmap(R.id.notification_image, circularBitmap);
                // Issue the notification with the updated custom layout
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (manager != null) {
                    manager.notify(notificationId, builder.setContent(customNotificationLayout).build());
                }
            }
        }
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int size = 150; // Set the desired size here (55x55 pixels)

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true);

        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, size, size);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.BLACK);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(resizedBitmap, rect, rect, paint);

        return output;
    }


    public void sendPushNotification(String passengerId, String name, String imgUrl) {
        // Step 1: Create a custom layout for the notification
        RemoteViews customNotificationLayout = new RemoteViews(context.getPackageName(), R.layout.custom_notification_layout);
        // Step 2: Set the passenger's name in the custom layout
        customNotificationLayout.setTextViewText(R.id.notification_text, name);
        // Step 2: Set the image, title, and text in the custom layout using AsyncTask
        new DownloadImageTask(customNotificationLayout, buildNotificationBuilder(), generateNotificationId()).execute(imgUrl);
    }

    private NotificationCompat.Builder buildNotificationBuilder() {
        return new NotificationCompat.Builder(context, "chat_notifications")
                .setSmallIcon(R.drawable.bus_transit)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
    }

    private int generateNotificationId() {
        // Use a unique notification ID for each notification to avoid replacing previous notifications
        return (int) System.currentTimeMillis();
    }
}
