package io.agora.agoravideodemo.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.LongSparseArray;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.agora.agoravideodemo.model.ContactModel;

/**
 * Created by saiki on 30-05-2018.
 **/
public class ContactsHelper {
    public static @NotNull
    List<ContactModel> getAllLocalContacts(@Nullable Context ctx) {
        List<ContactModel> list = new ArrayList<>();
        if (ctx == null) return list;//Return Empty
        ContentResolver contentResolver = ctx.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor == null) return list;//Return Empty
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor cursorInfo = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(ctx.getContentResolver(),
                            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(id)));

                    Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(id));
                    Uri pURI = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

                    Bitmap photo = null;
                    if (inputStream != null) {
                        photo = BitmapFactory.decodeStream(inputStream);
                    }
                    while (cursorInfo != null && cursorInfo.moveToNext()) {
                        ContactModel info = new ContactModel();
                        info.id = Integer.parseInt(id);
                        info.name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        info.mobileNumber = cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        info.photo = photo;
                        info.photoURI = pURI;
                        list.add(info);
                    }

                    if (cursorInfo != null) {
                        cursorInfo.close();
                    }
                }
            }
            cursor.close();
        }
        return list;
    }

    //https://stackoverflow.com/a/25676318/2102794
    public static @NotNull
    List<ContactModel> getAllLocalContacts2(@Nullable Context ctx, String countryCode) {
        if (ctx == null) return new ArrayList<>();
        LongSparseArray<ContactModel> map = new LongSparseArray<>();

        final String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor phones = ctx.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null);

        if (phones == null) return new ArrayList<>();
        while (phones.moveToNext()) {
            ContactModel info = new ContactModel();

            Long contact_id = phones.getLong(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String get_Mo = CommonUtilsKt.deleteCountry(phoneNumber, countryCode);
            info.name = name;
            info.mobileNumber = get_Mo.replaceAll("[^0-9]", "");
            map.put(contact_id, info);
        }
        phones.close();

        return convertToList(map);
    }

    private static <C> List<C> convertToList(LongSparseArray<C> sparseArray) {
        if (sparseArray == null) return null;
        List<C> arrayList = new ArrayList<C>(sparseArray.size());

        for (int i = 0; i < sparseArray.size(); i++)
            arrayList.add(sparseArray.valueAt(i));
        return arrayList;
    }

}
