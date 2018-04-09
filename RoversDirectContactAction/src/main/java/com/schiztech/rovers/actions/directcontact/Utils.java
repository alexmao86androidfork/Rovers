package com.schiztech.rovers.actions.directcontact;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;

import com.schiztech.roverdirectcontactaction.R;
import com.schiztech.rovers.api.RoversActionBuilder;

/**
 * Created by schiz_000 on 11/25/2014.
 */
public class Utils {

    public enum DirectType{
        Call,
        Sms;

        public Drawable getOverlayDrawable(Context context){
            int resource = this == Call ? R.drawable.ic_phone_overlay : R.drawable.ic_sms_overlay;
            return context.getResources().getDrawable(resource);
        }

        public int getIconResource(Context context){
            int resource = this == Call ? R.drawable.ic_phone_blue : R.drawable.ic_sms_green;
            return resource;
        }
    }

    public static final String EXTRA_CONTACT_URI = "contact_uri";
    public static final String EXTRA_DIRECT_TYPE = "direct_type";

    public static Intent getPickContactIntent(){
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);

        return intent;
    }

    public static Intent contactDataToRoverAction(Context context, DirectType directType, Uri contactData, int iconResource){
        String contactID = contactData.toString();

        //MAKE YOUR CALL .. do whatever... example:
        ContentResolver contentResolver = context.getContentResolver();
        Uri contactUri = Uri.parse(contactID);
        Cursor cur = contentResolver.query(contactUri, null, null, null, null);
        cur.moveToFirst();

        String contactName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        String contactPhone = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        cur.close();

        Intent result = createRoverAction(context, directType, contactName, getContactPhoto(contactPhone, context, directType.getOverlayDrawable(context)), contactID);

        return result;
    }

    private static Intent createRoverAction(Context context, DirectType directType, String contactName, Bitmap contactPhoto, String contactUri) {
        Intent.ShortcutIconResource shortcutIconResource =
                Intent.ShortcutIconResource.fromContext(context, directType.getIconResource(context));

        Intent launchIntent = new Intent(context, DirectContactActionActivity.class);
        launchIntent.putExtra(EXTRA_CONTACT_URI, contactUri);
        launchIntent.putExtra(EXTRA_DIRECT_TYPE, directType.toString());

        Intent result = RoversActionBuilder.build()
                .setColor(context.getResources().getColor(directType == DirectType.Call ? R.color.direct_call_default_background : R.color.direct_sms_default_background))
                .setIconResource(shortcutIconResource)
                .setIcon(contactPhoto)
                .setLabel(contactName + "")//not null
                .setIntent(launchIntent)
                .create();

        return result;
    }

    //region Contact Photo

    private static Bitmap getContactPhoto(String phoneNumber, Context context, Drawable mergeDrawable) {
        if(phoneNumber == null) return null;

        final Integer thumbnailId = fetchThumbnailId(phoneNumber, context);
        if (thumbnailId != null) {
            Bitmap thumbnail = fetchThumbnail(thumbnailId, context);
            return mergeBitmaps(thumbnail, mergeDrawable);
        }
        return null;

    }

    private static final String[] PHOTO_ID_PROJECTION = new String[]{
            ContactsContract.Contacts.PHOTO_ID
    };

    private static final String[] PHOTO_BITMAP_PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Photo.PHOTO
    };

    private static Integer fetchThumbnailId(String phoneNumber, Context context) {

        final Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        final Cursor cursor = context.getContentResolver().query(uri, PHOTO_ID_PROJECTION, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");

        try {
            Integer thumbnailId = null;
            if (cursor.moveToFirst()) {
                thumbnailId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
            }
            return thumbnailId;
        } finally {
            cursor.close();
        }

    }

    final static Bitmap fetchThumbnail(final int thumbnailId, Context context) {

        final Uri uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, thumbnailId);
        final Cursor cursor = context.getContentResolver().query(uri, PHOTO_BITMAP_PROJECTION, null, null, null);

        try {
            Bitmap thumbnail = null;
            if (cursor.moveToFirst()) {
                final byte[] thumbnailBytes = cursor.getBlob(0);
                if (thumbnailBytes != null) {
                    thumbnail = BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.length);
                }
            }
            return thumbnail;
        } finally {
            cursor.close();
        }

    }

    private static float OVERLAY_ICON_ALPHA = 0.75f;
    private static Bitmap mergeBitmaps(Bitmap contactPhoto, Drawable actionIcon){
        if(contactPhoto == null || contactPhoto.getWidth() <=0 || contactPhoto.getHeight() <= 0)
            return contactPhoto;

        Bitmap bitmap = null;
        try {

            bitmap = Bitmap.createBitmap(contactPhoto.getWidth(), contactPhoto.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            canvas.drawBitmap(contactPhoto,0,0,null);
            actionIcon.setAlpha((int)(OVERLAY_ICON_ALPHA * 255));
            actionIcon.setBounds(contactPhoto.getWidth()/4, contactPhoto.getHeight()/2, contactPhoto.getWidth()*3/4, contactPhoto.getHeight());
            actionIcon.draw(canvas);

        } catch (Exception e) {
        }
        return bitmap;

    }

    //endregion Contact Photo

}
