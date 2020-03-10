package info.guardianproject.ripple;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.service.quicksettings.TileService;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.annotation.RequiresApi;
import info.guardianproject.panic.PanicTrigger;

@RequiresApi(api = Build.VERSION_CODES.N)
public class PanicTileService extends TileService {
    public static final String TAG = "PanicTileService";
    private static final boolean DEBUG = false;
    private final Handler handler = new Handler();

    @Override
    public void onClick() {
        super.onClick();

        Intent intent = new Intent(this, CountDownActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (!isSecure()) {
            showDialog();
        } else {
            unlockAndRun(new Runnable() {
                @Override
                public void run() {
                    showDialog();
                }
            });
        }
    }

    private void showDialog() {
        setTheme(R.style.Theme_MaterialComponents_Light);

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_panic, null, false);

        Dialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        // avoid leaking the handler
                        handler.removeCallbacksAndMessages(null);
                    }
                })
                .create();

        showDialog(dialog);
        load(dialog);
    }

    private void load(final Dialog dialog) {
        boolean mTestRun = DEBUG;

        if (mTestRun) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                    Toast.makeText(getBaseContext(), R.string.done, Toast.LENGTH_LONG).show();
                }
            }, 20000);
        } else {
            PanicTrigger.sendTrigger(this);
            Toast.makeText(this, R.string.done, Toast.LENGTH_LONG).show();

            /* This app needs to stay running for a while to make sure that it sends
             * all of the Intents to Activities, Services, and BroadcastReceivers. If
             * it exits too soon, they will not get sent. */
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                }
            }, 10000); // 10 second delay
        }
    }
}
