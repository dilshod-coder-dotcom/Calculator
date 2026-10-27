package uz.muhasib.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Base64;

import androidx.activity.result.ActivityResult;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.HashSet;
import java.util.Set;

/**
 * Phone helpers for Muhāsib: recent calls (to pick a client or driver quickly),
 * the system contact picker, and on-device text recognition for scanning card numbers.
 * Nothing leaves the phone.
 */
@CapacitorPlugin(
    name = "PhoneBook",
    permissions = { @Permission(alias = "calllog", strings = { Manifest.permission.READ_CALL_LOG }) }
)
public class PhoneBookPlugin extends Plugin {

    @PluginMethod
    public void recentCalls(PluginCall call) {
        if (getPermissionState("calllog") != PermissionState.GRANTED) {
            requestPermissionForAlias("calllog", call, "callLogPermissionResult");
            return;
        }
        readCalls(call);
    }

    @PermissionCallback
    private void callLogPermissionResult(PluginCall call) {
        if (getPermissionState("calllog") == PermissionState.GRANTED) {
            readCalls(call);
        } else {
            call.reject("Qo'ng'iroqlar jurnaliga ruxsat berilmadi", "DENIED");
        }
    }

    private void readCalls(PluginCall call) {
        int limit = call.getInt("limit", 50);
        JSArray list = new JSArray();
        Set<String> seen = new HashSet<>();
        String[] cols = { CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME, CallLog.Calls.DATE, CallLog.Calls.TYPE };
        try (Cursor c = getContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, cols, null, null, CallLog.Calls.DATE + " DESC")) {
            while (c != null && c.moveToNext() && list.length() < limit) {
                String number = c.getString(0);
                if (number == null || number.trim().isEmpty() || !seen.add(number)) continue;
                JSObject o = new JSObject();
                o.put("number", number);
                o.put("name", c.getString(1) == null ? "" : c.getString(1));
                o.put("date", c.getLong(2));
                o.put("type", c.getInt(3));
                list.put(o);
            }
        } catch (Exception e) {
            call.reject(e.getMessage() == null ? "error" : e.getMessage());
            return;
        }
        JSObject ret = new JSObject();
        ret.put("calls", list);
        call.resolve(ret);
    }

    @PluginMethod
    public void pickContact(PluginCall call) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(call, intent, "pickContactResult");
    }

    @ActivityCallback
    private void pickContactResult(PluginCall call, ActivityResult result) {
        if (call == null) return;
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null || result.getData().getData() == null) {
            call.reject("cancelled", "CANCELLED");
            return;
        }
        Uri uri = result.getData().getData();
        String[] cols = { ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME };
        try (Cursor c = getContext().getContentResolver().query(uri, cols, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                JSObject o = new JSObject();
                o.put("number", c.getString(0) == null ? "" : c.getString(0));
                o.put("name", c.getString(1) == null ? "" : c.getString(1));
                call.resolve(o);
                return;
            }
        } catch (Exception e) {
            call.reject(e.getMessage() == null ? "error" : e.getMessage());
            return;
        }
        call.reject("empty");
    }

    @PluginMethod
    public void recognizeText(PluginCall call) {
        String b64 = call.getString("base64");
        if (b64 == null || b64.isEmpty()) {
            call.reject("no image");
            return;
        }
        Bitmap bmp;
        try {
            byte[] bytes = Base64.decode(b64, Base64.DEFAULT);
            bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            call.reject("bad image");
            return;
        }
        if (bmp == null) {
            call.reject("bad image");
            return;
        }
        TextRecognizer rec = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        rec.process(InputImage.fromBitmap(bmp, 0))
            .addOnSuccessListener(text -> {
                JSObject o = new JSObject();
                o.put("text", text.getText());
                call.resolve(o);
                rec.close();
            })
            .addOnFailureListener(e -> {
                call.reject(e.getMessage() == null ? "ocr error" : e.getMessage());
                rec.close();
            });
    }
}
