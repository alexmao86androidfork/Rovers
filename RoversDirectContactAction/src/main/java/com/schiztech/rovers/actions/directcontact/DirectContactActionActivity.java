package com.schiztech.rovers.actions.directcontact;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;


public class DirectContactActionActivity extends Activity {
    public static final String TAG = "DialActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getIntent().getExtras() != null){
            String contactUri = getContactUri();
            if(contactUri != null){
                directContact(contactUri);
            }
            else{
                Log.e(TAG, "contactUri is missing");
                announceError(null);
            }
        }
        else{
            Log.e(TAG, "no intent extras found");
            announceError(null);
        }

        finish();
    }

    private void directContact(String contactUri){

        Cursor cur = getContentResolver().query(Uri.parse(contactUri), null, null, null, null);
        if(cur != null && cur.getCount() > 0) {
            cur.moveToFirst();
            String dialNumber = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if(dialNumber != null) {

                Utils.DirectType directType = getDirectType();
                makeDirectAction(dialNumber, directType);
            }
            else{
                Log.e(TAG, "no phone number found");
                announceError("no phone number found.");
            }
        }
        else{
            Log.e(TAG, "cursor results are empty");
            announceError(null);
        }
    }

    private void makeDirectAction(String dialNumber, Utils.DirectType directType){

        if(directType != null) {
            Intent launchIntent = null;
            if(directType == Utils.DirectType.Call) {
                Uri telNumber = Uri.parse("tel:" + dialNumber);
                launchIntent = new Intent(Intent.ACTION_CALL, telNumber);
            }
            else if (directType == Utils.DirectType.Sms) {
                Uri smsNumber = Uri.parse("sms:" + dialNumber);
                launchIntent = new Intent(Intent.ACTION_VIEW);
                launchIntent.setData(smsNumber);

            }

            if(launchIntent != null) {
                try {
                    startActivity(launchIntent);
                }
                catch (Exception e){
                    Log.e(TAG, "Error launching direct action intent: " + e.getMessage());
                    announceError(null);
                }
            }
            else{
                Log.e(TAG, "no launch intent found");
                announceError(null);
            }
        }
        else{
            Log.e(TAG, "no direct type found");
            announceError(null);
        }

    }

    private void announceError(String extraInfo){
        Toast.makeText(getApplicationContext(), "Could not dial to contact" +
                ((extraInfo != null) ? ": " + extraInfo : "" ),Toast.LENGTH_SHORT).show();
    }

    private String getContactUri(){
        return getIntent().getExtras().getString(Utils.EXTRA_CONTACT_URI, null);
    }

    private Utils.DirectType getDirectType(){
        String directTypeString = getIntent().getExtras().getString(Utils.EXTRA_DIRECT_TYPE, null);
        if(directTypeString != null)
            return Utils.DirectType.valueOf(directTypeString);

        return null;
    }
}
