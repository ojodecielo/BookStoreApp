package com.example.android.bookstoreapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookstoreapp.data.BookContract.BookEntry;


public class ProductDetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_BOOK_LOADER = 0;
    private Uri mCurrentBookUri;
    private TextView mNameTextView;
    private TextView mPriceTextView;
    double price;
    private TextView mQuantityTextView;
    int quantity;
    private TextView mSupplierTextView;
    private TextView mPhoneTextView;

    private boolean mBookHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.product_details);

        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();
        getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);

        mNameTextView = (TextView) findViewById(R.id.product_details_name);
        mPriceTextView = (TextView) findViewById(R.id.product_details_price);
        mQuantityTextView = (TextView) findViewById(R.id.product_details_quantity);
        mSupplierTextView = (TextView) findViewById(R.id.product_details_supplier);
        mPhoneTextView = (TextView) findViewById(R.id.product_details_phone);

        Button increaseButton = (Button) findViewById(R.id.button_increase);
        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increment();
            }
        });

        Button decreaseButton = (Button) findViewById(R.id.button_decrease);
        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decrement();
            }
        });

        Button editButton = (Button) findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEdit();
            }
        });

        Button orderButton = (Button) findViewById(R.id.order_button);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callSupplier();
            }
        });

        Button deleteButton = (Button) findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteBook() {
        if (mCurrentBookUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_book_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_book_successful), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
        Intent returnIntent = new Intent(getApplicationContext(), CatalogActivity.class);
        startActivity(returnIntent);
    }

    public void openEdit() {
        if (mCurrentBookUri != null) {
            Intent editIntent = new Intent(ProductDetailsActivity.this, EditorActivity.class);
            editIntent.setData(mCurrentBookUri);
            startActivity(editIntent);
        }
        finish();
    }

    private void increment() {
        ContentResolver resolver = mQuantityTextView.getContext().getContentResolver();
        ContentValues values = new ContentValues();
        String quantityString = mQuantityTextView.getText().toString().trim();
        int quantityInteger = Integer.parseInt(quantityString);
        quantityInteger = quantityInteger + 1;
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantityInteger);
        resolver.update(mCurrentBookUri, values, null, null);
    }

    private void decrement() {
        ContentResolver resolver = mQuantityTextView.getContext().getContentResolver();
        ContentValues values = new ContentValues();
        String quantityString = mQuantityTextView.getText().toString().trim();
        int quantityInteger = Integer.parseInt(quantityString);
        quantityInteger = quantityInteger - 1;
        if (quantityInteger < 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.book_out_of_stock), Toast.LENGTH_SHORT).show();
        } else {
            values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantityInteger);
            resolver.update(mCurrentBookUri, values, null, null);
        }
    }

    public void callSupplier() {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        String phoneString = mPhoneTextView.getText().toString().trim();
        callIntent.setData(Uri.parse("tel:" + phoneString));
        startActivity(callIntent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_SUPPLIER,
                BookEntry.COLUMN_BOOK_PHONE};
        return new CursorLoader(this,
                mCurrentBookUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER);
            int phoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PHONE);

            String name = cursor.getString(nameColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);

            mNameTextView.setText(name);
            mPriceTextView.setText(Double.toString(price));
            mQuantityTextView.setText(Integer.toString(quantity));
            mSupplierTextView.setText(supplier);
            mPhoneTextView.setText(phone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameTextView.setText("");
        mPriceTextView.setText("");
        mQuantityTextView.setText("");
        mSupplierTextView.setText("");
        mPhoneTextView.setText("");
    }
}
