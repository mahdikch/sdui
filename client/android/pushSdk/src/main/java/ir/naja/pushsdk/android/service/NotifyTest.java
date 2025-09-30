/*******************************************************************************
 * Copyright (c) 1999, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 */
package ir.naja.pushsdk.android.service;

import static androidx.core.content.ContextCompat.startForegroundService;

import static java.security.AccessController.getContext;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import ir.naja.pushsdk.R;
import ir.naja.pushsdk.android.PersianDate;


/**
 * Provides static methods for creating and showing notifications to the user.
 */
public class NotifyTest {

    /**
     * Message ID Counter
     **/
    private static int MessageID = 0;

    /**
     * Displays a notification in the notification area of the UI
     *
     * @param context           Context from which to create the notification
     * @param messageString     The string to display to the user as a message
     * @param intent            The intent which will start the activity when the user clicks the notification
     * @param notificationTitle The resource reference to the notification title
     */
    public static final String TAG = "push_naji_Channel";


    public static Notification notifcation(Activity context, String title, String senderTitle, Intent intent, int notificationTitle, Intent mainIntent) {

        //Get the notification manage which we will use to display the notification
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);

        Calendar.getInstance().getTime().toString();

        long when = System.currentTimeMillis();

        //get the notification title from the application's strings.xml file
        CharSequence contentTitle = context.getString(notificationTitle);

        //the message that will be displayed as the ticker
//        String ticker = contentTitle + " " + messageString;

        //build the pending intent that will start the appropriate activity
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                3, intent, PendingIntent.FLAG_IMMUTABLE);
        Uri parse = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notif);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(TAG, "najification", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("najification");
            channel.setImportance(NotificationManager.IMPORTANCE_MIN);
            AudioAttributes audioAttributes=new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_NOTIFICATION).build();
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),audioAttributes);
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        //build the notification
        Notification notification = new NotificationCompat.Builder(context, TAG)
                .setContentTitle(contentTitle + " : " + senderTitle)
                .setContentText(title)
                .setAutoCancel(true)
                .setChannelId(TAG)
//                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSound(parse)
//                .setSound(Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
                .setSmallIcon(ir.naja.pushsdk.R.mipmap.naja)
                .setWhen(when)
                .setShowWhen(true)
                .setColor(Color.GREEN)
                .setPriority(NotificationCompat.PRIORITY_LOW)
//                .setLocalOnly(true)
                .build();

//        Notification.Builder notificationCompat = new Notification.Builder(context);
//        notificationCompat.setAutoCancel(true)
//                .setContentTitle(contentTitle)
//                .setContentIntent(pendingIntent)
//                .setContentText(messageString)
//                .setTicker(ticker)
//                .setWhen(when)
//                .setSmallIcon(ir.naja.pushsdk.R.mipmap.naja);

//        Notification notification = notificationCompat.build();
        //display the notification
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.FOREGROUND_SERVICE}, 100);
        } else {
            startForegroundService(context, mainIntent);
//            context.startService( mainIntent);
        }
        NotificationManagerCompat.from(context).notify(new Random().nextInt(), notification);

//        mNotificationManager.notify(MessageID, notification);
        MessageID++;

        return notification;
    }

    public static Notification notifcation(Context context, boolean isconnect) {

        //Get the notification manage which we will use to display the notification
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);

        Calendar.getInstance().getTime().toString();
        PersianDate persianDate = new PersianDate();
        String shamsiDate = persianDate.dayName()+" "+persianDate.getShDay() + " " +persianDate.getMonthName()+" "+" سال "+persianDate.getShYear();
//                persianDate.getShMonth() + " " +
//                persianDate.getShYear();
        long when = System.currentTimeMillis();
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notification_custom);
        views.setTextViewText(R.id.text_shamsi, shamsiDate);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat gregorianFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
        String gregorianDate = gregorianFormat.format(calendar.getTime());
        views.setTextViewText(R.id.text_miladi_ghamari, gregorianDate);
        Bitmap dayBitmap = createDayBitmap(persianDate.getShDay(), 50);
//        views.setImageViewResource(R.id.image_day, dayBitmap); // آیکون عدد روز
        views.setImageViewBitmap(R.id.image_day, dayBitmap); // آیکون عدد روز

        //build the pending intent that will start the appropriate activity
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                3, new Intent(), PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("101", "najification", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(null);
            channel.setShowBadge(false);
            channel.setSound(null,null);
            channel.setImportance(NotificationManager.IMPORTANCE_MIN);
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        String messageString = isconnect ? "is Running Naji Push" : "Naja push Disconnect";
        int color = isconnect ? Color.GREEN : Color.RED;
        //build the notification
        Notification notification = new NotificationCompat.Builder(context, "101")
//                .setContentTitle("najification")
//                .setContentTitle("دوشنبه 23 تیر ماه 1404")
//                .setContentText(messageString)
//                .setContentText("18 محرم 14 1447")
                .setCustomContentView(views)
                .setOngoing(true)
                .setSilent(true)
                .setAutoCancel(true)
                .setChannelId("101")
//                .setSound(null)
                .setContentIntent(pendingIntent)
//                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setSmallIcon(ir.naja.pushsdk.R.mipmap.naja)
//                .setSmallIcon(R.mipmap.naja)
                .setWhen(when)
                .setShowWhen(true)
                .setColor(color)
//                .setLocalOnly(true)
                .build();

//        Notification.Builder notificationCompat = new Notification.Builder(context);
//        notificationCompat.setAutoCancel(true)
//                .setContentTitle(contentTitle)
//                .setContentIntent(pendingIntent)
//                .setContentText(messageString)
//                .setTicker(ticker)
//                .setWhen(when)
//                .setSmallIcon(ir.naja.pushsdk.R.mipmap.naja);

//        Notification notification = notificationCompat.build();
        //display the notification
//        int i = ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS);
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return pushNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
//        }
//        if (ContextCompat.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.FOREGROUND_SERVICE}, 100);
//        } else {
//            startForegroundService(context,null);
//        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }
        NotificationManagerCompat.from(context).notify(101, notification);

//        mNotificationManager.notify(MessageID, notification);
        MessageID++;

        return notification;
    }
    public static Bitmap createDayBitmap(int dayNumber, int size) {
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // دایره آبی
        Paint paintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCircle.setColor(Color.parseColor("#3F51B5"));
        float radius = size / 2f - size * 0.04f; // مثلا 4% padding
        canvas.drawCircle(size / 2f, size / 2f, radius, paintCircle);

        // عدد سفید وسط دایره
        Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(size * 0.44f); // تقریبا 42 در اندازه 96
        paintText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paintText.setTextAlign(Paint.Align.CENTER);

        float textY = size / 2f - (paintText.descent() + paintText.ascent()) / 2f;
        canvas.drawText(String.valueOf(dayNumber), size / 2f, textY, paintText);

        return bitmap;
    }

    private boolean checkAndRequestPermissions(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.FOREGROUND_SERVICE}, 1);
            return false;
        }
        return true;
    }

    /**
     * Display a toast notification to the user
     *
     * @param context  Context from which to create a notification
     * @param text     The text the toast should display
     * @param duration The amount of time for the toast to appear to the user
     */
    static void toast(Context context, CharSequence text, int duration) {
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

}
