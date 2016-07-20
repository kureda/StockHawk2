package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by Serg on 7/8/2016.
 */
public class WidgetService extends RemoteViewsService {

    static final int INDEX_STOCKS_ID = 0;
    static final int INDEX_STOCKS_SYMBOL = 1;
    static final int INDEX_STOCKS_BIDPRICE = 2;
    static final int INDEX_STOCKS_ISUP = 3;
    static final String STOCK_POSITION = "item";
    private static final String[] COLUMNS = {
            QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.BIDPRICE,
            QuoteColumns.ISUP
    };

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new RemoteViewsService.RemoteViewsFactory() {

            private Cursor cursor;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                closeCursor();
                final long identityToken = Binder.clearCallingIdentity();
                Uri uri = QuoteProvider.Quotes.CONTENT_URI;
                String selection = QuoteColumns.ISCURRENT + " = ?";
                String[] selectionArgs = {"1"};
                cursor = getContentResolver().query(uri, COLUMNS, selection, selectionArgs, null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                closeCursor();
            }

            @Override
            public int getCount() {
                return cursor == null ? 0 : cursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int cursorPosition) {
                if (wrongCursor(cursorPosition))
                    return null;
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list_item);
                String symbol = cursor.getString(INDEX_STOCKS_SYMBOL);
                String bid_price = cursor.getString(INDEX_STOCKS_BIDPRICE);
                boolean up = cursor.getInt(INDEX_STOCKS_ISUP) == 1;
                views.setTextViewText(R.id.stock_symbol, symbol);
                views.setTextViewText(R.id.bid_price, bid_price);
                views.setTextColor(R.id.bid_price, up ? Color.GREEN : Color.RED); //up or down
                Intent intent = new Intent();
                intent.putExtra(STOCK_POSITION, symbol);
                views.setOnClickFillInIntent(R.id.widget_list_item, intent);
                return views;
            }


            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int cursorPosition) {
                if (cursor.moveToPosition(cursorPosition)) {
                    return cursor.getLong(INDEX_STOCKS_ID);
                } else {
                    return cursorPosition;
                }
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

            private boolean wrongCursor(int position) {
                return cursor == null
                        || position == AdapterView.INVALID_POSITION
                        || !cursor.moveToPosition(position);
            }

            private void closeCursor() {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }
        };
    }
}
