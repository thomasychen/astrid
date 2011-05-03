/**
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.astrid.sharing;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.timsu.astridsquid.R;
import com.todoroo.andlib.service.ContextManager;
import com.todoroo.astrid.api.AstridApiConstants;
import com.todoroo.astrid.api.TaskAction;
import com.todoroo.astrid.api.TaskDecoration;

/**
 * Exposes {@link TaskDecoration} for timers
 *
 * @author Tim Su <tim@todoroo.com>
 *
 */
public class SharingActionExposer extends BroadcastReceiver {

    static final String EXTRA_TASK = "task"; //$NON-NLS-1$

    @Override
    public void onReceive(Context context, Intent intent) {
        ContextManager.setContext(context);
        long taskId = intent.getLongExtra(AstridApiConstants.EXTRAS_TASK_ID, -1);
        if(taskId == -1)
            return;

        if(AstridApiConstants.BROADCAST_REQUEST_ACTIONS.equals(intent.getAction())) {
            sendAction(context, taskId);
        } else {
            performAction(context, taskId);
        }
    }

    private void performAction(Context context, long taskId) {
        Intent intent = new Intent(context, SharingLoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void sendAction(Context context, long taskId) {
        final String label = context.getString(R.string.sharing_action);
        final Drawable drawable = context.getResources().getDrawable(R.drawable.tango_share);

        Bitmap icon = ((BitmapDrawable)drawable).getBitmap();
        Intent newIntent = new Intent(context, getClass());
        newIntent.putExtra(AstridApiConstants.EXTRAS_TASK_ID, taskId);
        TaskAction action = new TaskAction(label,
                PendingIntent.getBroadcast(context, (int)taskId, newIntent, 0), icon);

        // transmit
        Intent broadcastIntent = new Intent(AstridApiConstants.BROADCAST_SEND_ACTIONS);
        broadcastIntent.putExtra(AstridApiConstants.EXTRAS_RESPONSE, action);
        broadcastIntent.putExtra(AstridApiConstants.EXTRAS_TASK_ID, taskId);
        context.sendBroadcast(broadcastIntent, AstridApiConstants.PERMISSION_READ);
    }

}
