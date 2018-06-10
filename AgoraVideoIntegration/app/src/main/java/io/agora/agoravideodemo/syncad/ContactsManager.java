package io.agora.agoravideodemo.syncad;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.SyncStateContract;

import java.util.ArrayList;

public class ContactsManager {
    private static String MIMETYPE = "vnd.android.cursor.item/com.example.ajay.contacts_4";

    public static void addContact(Context context, MyContact contact){
        ContentResolver resolver = context.getContentResolver();
        ArrayList<ContentProviderOperation> ops =
                      new ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation
           .newInsert(addCallerIsSyncAdapterParameter(
            ContactsContract.RawContacts.CONTENT_URI, true))
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, 
                 SyncStateContract.Constants.ACCOUNT_NAME)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, 
                 SyncStateContract.Constants.ACCOUNT_TYPE)
                .withValue(ContactsContract.RawContacts.AGGREGATION_MODE, 
                 ContactsContract.RawContacts.AGGREGATION_MODE_DEFAULT)
                .build());

        ops.add(ContentProviderOperation
           .newInsert(addCallerIsSyncAdapterParameter(
             ContactsContract.Data.CONTENT_URI, true))
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, 
                 ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                 contact.name)
                .build());

        ops.add(ContentProviderOperation
           .newInsert(addCallerIsSyncAdapterParameter(
             ContactsContract.Data.CONTENT_URI, true))
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,0)
                .withValue(ContactsContract.Data.MIMETYPE, MIMETYPE)
                .withValue(ContactsContract.Data.DATA1, 12345)
                .withValue(ContactsContract.Data.DATA2, "user")
                .withValue(ContactsContract.Data.DATA3, "MyData")
                .build());

        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, ops);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Uri addCallerIsSyncAdapterParameter(Uri uri,
                                                       boolean isSyncOperation) {
        if (isSyncOperation) {
            return uri.buildUpon()
            .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER,
             "true").build();
        }
        return uri;
    }
}