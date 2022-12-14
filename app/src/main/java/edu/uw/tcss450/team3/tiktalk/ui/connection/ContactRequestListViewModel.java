package edu.uw.tcss450.team3.tiktalk.ui.connection;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.uw.tcss450.team3.tiktalk.R;

public class ContactRequestListViewModel extends AndroidViewModel {

    private MutableLiveData<List<Contact>> mContactRequestList;

    public ContactRequestListViewModel(@NonNull Application application) {
        super(application);
        mContactRequestList = new MutableLiveData<>();
        mContactRequestList.setValue(new ArrayList<>());
    }

    public void addContactRequestListObserver(@NonNull LifecycleOwner owner,
                                       @NonNull Observer<? super List<Contact>> observer) {
        mContactRequestList.observe(owner, observer);
    }

    public void connectGet(final String jwt) {
        String url = getApplication().getResources().getString(R.string.base_url) + "contacts/" + "request";
        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null, //no body for this get request
                this::handleResult,
                this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // add headers <key,value>
                headers.put("Authorization", jwt);
                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }

    public void removeRequest(final String jwt, int friendID) {
        String url = getApplication().getResources().getString(R.string.base_url) + "contacts/" + friendID;
        Request request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null, //no body for this get request
                null, //do nothing with the response
                this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // add headers <key,value>
                headers.put("Authorization", jwt);
                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }

    public void addRequest(final String jwt, final int friendID) {
        String url = getApplication().getResources().getString(R.string.base_url) + "contacts/" + "accept/";
        JSONObject body = new JSONObject();
        try {
            body.put("memberid", friendID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Request request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                null, //do nothing with the response
                this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // add headers <key,value>
                headers.put("Authorization", jwt);
                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }

    private void handleResult(final JSONObject response) {
        List<Contact> list;
        try {
            list = new ArrayList<>();
            JSONArray contacts = response.getJSONArray("rows");
            for(int i = 0; i < contacts.length(); i++) {
                JSONObject contact = contacts.getJSONObject(i);
                Contact cContact = new Contact(
                        contact.getString("firstname"),
                        contact.getString("lastname"),
                        contact.getString("nickname"),
                        contact.getString("email"),
                        contact.getInt("memberid_a")
                );
                if (!list.contains(cContact)) {
                    // don't add a duplicate
                    list.add(0, cContact);
                } else {
                    // this shouldn't happen but could with the asynchronous
                    // nature of the application
                    Log.wtf("Contact already received",
                            "Or duplicate id:" + cContact.getMemberID());
                }

            }
            //inform observers of the change (setValue)
            //getOrCreateMapEntry(response.getInt("chatId")).setValue(list);
            mContactRequestList.setValue(list);
        }catch (JSONException e) {
            Log.e("JSON PARSE ERROR", "Found in handle Success ChatViewModel");
            Log.e("JSON PARSE ERROR", "Error: " + e.getMessage());
        }

    }

    private void handleError(final VolleyError error) {
        //you should add much better error handling in a production release.
        //i.e. YOUR PROJECT
        if (Objects.isNull(error.networkResponse)) {
            Log.e("NETWORK ERROR", error.getMessage());
        }else {
            String data = new String(error.networkResponse.data, Charset.defaultCharset())
                    .replace('\"', '\'');
            Log.d("CONNECTION", data);
            //throw new IllegalStateException(error.getMessage());
        }
    }
}
