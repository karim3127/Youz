package com.youz.android.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.youz.android.fragment.HomeRecentFriendsFragment;
import com.youz.android.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class UtilContact {

    private Context _context;
    String codeCountry;

    public UtilContact(Context context, String codeCountry){
        this._context = context;
        this.codeCountry = codeCountry;
    }

    public Cursor getContactCursor(ContentResolver contactHelper) {
        String[] projection = { ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER };
        Cursor cur = null;
        try {
            cur = contactHelper.query (ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return cur;
    }

    public List<Contact> getContactList(){


        List<Contact> contactList = new ArrayList<>();



        ContentResolver contactResolver = _context.getContentResolver();
        Cursor cursorContact = getContactCursor(contactResolver);

        if (cursorContact != null && cursorContact.moveToFirst()) {

            while (cursorContact.moveToNext()) {
                String phone = cursorContact.getString(cursorContact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String displayName = cursorContact.getString(cursorContact.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String contactId = cursorContact.getString(cursorContact.getColumnIndex(ContactsContract.Contacts._ID));

                Log.e("UtilContact", phone+"");

                if(phone != null){
                    if (!phone.contains("#")) {
                        phone = phone.replaceAll(" ", "");
                        String phoneNumber;
                        if (phone.startsWith("00")) {
                            phoneNumber = phone.replaceFirst("00", "+");
                            phoneNumber = phoneNumber.substring(1, phoneNumber.length());
                        } else if (phone.startsWith("+")) {
                            phoneNumber = phone.substring(1, phone.length());
                        } else {
                            phoneNumber = /*"+" + */codeCountry + phone;
                        }



                         if(phoneNumber.startsWith("972") && !phoneNumber.startsWith("9720") && !phoneNumber.startsWith("972 0")){
                            phoneNumber = phoneNumber.replace("972","9720");
                        }

                        Contact contact = new Contact();
                        contact.setContactId(contactId);
                        contact.setName(displayName);
                        contact.setNumber(phoneNumber);


                        boolean exist = false;
                        for (int i = 0; i < contactList.size() && !exist; i++) {
                            if (contactList.get(i).getNumber().equals(phoneNumber)) {
                                exist = true;
                            }
                        }
                        if (!exist && !phoneNumber.equals(HomeRecentFriendsFragment.MyNumber.replace(" ",""))) {
                            contactList.add(contact);
                        }
                    }
                }
            }

            cursorContact.close();
        }

        return contactList;
    }

}