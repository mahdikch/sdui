package ir.naja.pushsdk.android.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import ir.naja.pushsdk.android.NajiPush;

public class BootReceiver extends BroadcastReceiver {
   @Override
   public void onReceive(Context context, Intent intent) {
      System.out.println("@@@@@@reboot4 = " + "is call");
      if ((Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))){
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            new NajiPush().connectNajiPush(context,);
            context.startForegroundService(new Intent(context,MqttService.class));
         }else {
            context.startService(new Intent(context,MqttService.class));
         }
      }

   }
}
