package com.example.android.bookstoreapp;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookstoreapp.data.BookContract.BookEntry;


public class BookCursorAdapter extends CursorAdapter {

    Context mContext;

    public BookCursorAdapter(CatalogActivity context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);

        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);

        String bookName = cursor.getString(nameColumnIndex);
        Double bookPrice = cursor.getDouble(priceColumnIndex);
        String bookPriceString = bookPrice.toString();

        final int bookQuantity = cursor.getInt(quantityColumnIndex);

        final String bookQuantityString = cursor.getString(quantityColumnIndex);
        final Integer[] quantity = {Integer.parseInt(bookQuantityString)};

        nameTextView.setText(bookName);
        priceTextView.setText(bookPriceString);
        quantityTextView.setText(bookQuantityString);

        int id = cursor.getInt(cursor.getColumnIndex(BookEntry._ID));

        final Uri currentBookUri = Uri.parse(BookEntry.CONTENT_URI + "/" + id);
        mContext = context;

        Button sellButton = (Button) view.findViewById(R.id.sell_button);
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (quantity[0] == 0) {
                    Toast.makeText(mContext, R.string.book_out_of_stock, Toast.LENGTH_SHORT).show();
                } else {
                    values.put(BookEntry.COLUMN_BOOK_QUANTITY, bookQuantity - 1);
                    resolver.update(currentBookUri, values, null, null);
                    context.getContentResolver().notifyChange(currentBookUri, null);
                }
            }
        });
    }
}
