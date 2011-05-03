/**
 * See the file "LICENSE" for the full license governing this code.
 */
package org.weloveastrid.rmilk;

import org.weloveastrid.rmilk.data.MilkListFields;
import org.weloveastrid.rmilk.data.MilkListService;
import org.weloveastrid.rmilk.data.MilkTaskFields;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import com.timsu.astridsquid.R;
import com.todoroo.andlib.service.Autowired;
import com.todoroo.andlib.service.ContextManager;
import com.todoroo.andlib.service.DependencyInjectionService;
import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.Join;
import com.todoroo.andlib.sql.QueryTemplate;
import com.todoroo.astrid.api.AstridApiConstants;
import com.todoroo.astrid.api.Filter;
import com.todoroo.astrid.api.FilterCategory;
import com.todoroo.astrid.api.FilterListHeader;
import com.todoroo.astrid.api.FilterListItem;
import com.todoroo.astrid.data.Metadata;
import com.todoroo.astrid.data.StoreObject;
import com.todoroo.astrid.data.Task;
import com.todoroo.astrid.data.MetadataApiDao.MetadataCriteria;
import com.todoroo.astrid.data.TaskApiDao.TaskCriteria;

/**
 * Exposes filters based on RTM lists
 *
 * @author Tim Su <tim@todoroo.com>
 *
 */
public class MilkFilterExposer extends BroadcastReceiver {

    @Autowired private MilkListService milkListService;

    static {
        MilkDependencyInjector.initialize();
    }

    private Filter filterFromList(Context context, StoreObject list) {
        String listName = list.getValue(MilkListFields.NAME);
        String title = context.getString(R.string.rmilk_FEx_list_title,
                listName);
        ContentValues values = new ContentValues();
        values.put(Metadata.KEY.name, MilkTaskFields.METADATA_KEY);
        values.put(MilkTaskFields.LIST_ID.name, list.getValue(MilkListFields.REMOTE_ID));
        values.put(MilkTaskFields.TASK_SERIES_ID.name, 0);
        values.put(MilkTaskFields.TASK_ID.name, 0);
        values.put(MilkTaskFields.REPEATING.name, 0);
        Filter filter = new Filter(listName, title, new QueryTemplate().join(
                Join.left(Metadata.TABLE, Task.ID.eq(Metadata.TASK))).where(Criterion.and(
                        MetadataCriteria.withKey(MilkTaskFields.METADATA_KEY),
                        TaskCriteria.activeAndVisible(),
                        MilkTaskFields.LIST_ID.eq(list.getValue(MilkListFields.REMOTE_ID)))),
                values);

        return filter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ContextManager.setContext(context);

        // if we aren't logged in, don't expose features
        if(!MilkUtilities.INSTANCE.isLoggedIn())
            return;

        DependencyInjectionService.getInstance().inject(this);

        StoreObject[] lists = milkListService.getLists();

        // If user does not have any tags, don't show this section at all
        if(lists.length == 0)
            return;

        Filter[] listFilters = new Filter[lists.length];
        for(int i = 0; i < lists.length; i++)
            listFilters[i] = filterFromList(context, lists[i]);

        FilterListHeader rtmHeader = new FilterListHeader(context.getString(R.string.rmilk_FEx_header));
        FilterCategory rtmLists = new FilterCategory(context.getString(R.string.rmilk_FEx_list),
                listFilters);

        // transmit filter list
        FilterListItem[] list = new FilterListItem[2];
        list[0] = rtmHeader;
        list[1] = rtmLists;
        Intent broadcastIntent = new Intent(AstridApiConstants.BROADCAST_SEND_FILTERS);
        broadcastIntent.putExtra(AstridApiConstants.EXTRAS_ADDON, MilkUtilities.IDENTIFIER);
        broadcastIntent.putExtra(AstridApiConstants.EXTRAS_RESPONSE, list);
        context.sendBroadcast(broadcastIntent, AstridApiConstants.PERMISSION_READ);
    }

}
