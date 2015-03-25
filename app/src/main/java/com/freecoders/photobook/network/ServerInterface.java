package com.freecoders.photobook.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.freecoders.photobook.CommentListAdapter;
import com.freecoders.photobook.FriendsListAdapter;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.db.FriendEntry;
import com.freecoders.photobook.gson.CommentEntryJson;
import com.freecoders.photobook.gson.ServerResponse;
import com.freecoders.photobook.gson.UserProfile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Alex on 2014-11-27.
 */
public class ServerInterface {

    public static final void postContactsRequest(Context context,
        ArrayList<String> contacts, String userId,
        final Response.Listener<String> responseListener,
        final Response.ErrorListener errorListener) {
        Gson gson = new Gson();
        HashMap<String, String> headers = createHeaders(userId);
        Log.d(Constants.LOG_TAG, "Sending post contacts request for " + gson.toJson(contacts));
        StringRequest request = new StringRequest(Request.Method.POST,
                Constants.SERVER_URL+Constants.SERVER_PATH_CONTACTS,
                gson.toJson(contacts), headers,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        responseListener.onResponse(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorListener.onErrorResponse(error);

                    }
                }
        );

        VolleySingleton.getInstance(context).addToRequestQueue(request);

    }

    public static final void updateProfileRequest(Context context,
                                                 UserProfile profile, String userId,
                                                 final Response.Listener<String> responseListener,
                                                 final Response.ErrorListener errorListener) {
        Gson gson = new Gson();
        HashMap<String, String> headers = createHeaders(userId);
        Log.d(Constants.LOG_TAG, "Update profile request");
        StringRequest request = new StringRequest(Request.Method.PUT,
                Constants.SERVER_URL+Constants.SERVER_PATH_USER ,
                gson.toJson(profile), headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, "Response: " + response);
                        if (responseListener != null) responseListener.onResponse(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (errorListener != null) errorListener.onErrorResponse(error);
                    }
                }
        );

        VolleySingleton.getInstance(context).addToRequestQueue(request);

    }

    public static final void addFriendRequest(final ArrayList<FriendEntry> friendList,
        FriendsListAdapter adapter, int pos, Context context,
        final String[] friendIds) {
        Gson gson = new Gson();
        String userId = Photobook.getPreferences().strUserID;
        if (userId.isEmpty()) return;
        HashMap<String, String> headers = createHeaders(userId);
        final int position = pos;
        final FriendsListAdapter friendsListAdapter = adapter;
        Log.d(Constants.LOG_TAG, "Add friend request");
        StringRequest request = new StringRequest(Request.Method.PUT,
                Constants.SERVER_URL+Constants.SERVER_PATH_FRIENDS ,
                gson.toJson(friendIds), headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject resJson = new JSONObject(response);
                            String strRes = resJson.getString(Constants.RESPONSE_RESULT);
                            if (strRes.equals(Constants.RESPONSE_RESULT_OK)) {
                                friendList.get(position).
                                        setStatus(FriendEntry.INT_STATUS_FRIEND);
                                friendsListAdapter.notifyDataSetChanged();
                                int res = Photobook.getFriendsDataSource().updateFriend(
                                        friendList.get(position));
                                Log.d(Constants.LOG_TAG, "Updated " + res + "friend items");
                            }
                        } catch (JSONException e) {
                            Log.d(Constants.LOG_TAG, "Exception " + e.getLocalizedMessage());
                        }
                        Log.d(Constants.LOG_TAG, "Response: " + response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(Constants.LOG_TAG, "Error: " + error.getLocalizedMessage());
                    }
                }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public static final void removeFriendRequest(final ArrayList<FriendEntry> friendList,
                                              FriendsListAdapter adapter, int pos, Context context,
                                              final String[] friendIds) {
        Gson gson = new Gson();
        String userId = Photobook.getPreferences().strUserID;
        if (userId.isEmpty()) return;
        String idList = "";
        if (friendIds.length > 0) {
            idList = friendIds[0];
            for (int i = 1; i < friendIds.length; i++)
                idList = idList + ", " + friendIds[i];
        }
        HashMap<String, String> headers = createHeaders(userId);
        headers.put(Constants.KEY_ID, idList);
        final int position = pos;
        final FriendsListAdapter friendsListAdapter = adapter;
        Log.d(Constants.LOG_TAG, "Remove friend request");
        StringRequest request = new StringRequest(Request.Method.DELETE,
                Constants.SERVER_URL+Constants.SERVER_PATH_FRIENDS ,
                gson.toJson(friendIds), headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject resJson = new JSONObject(response);
                            String strRes = resJson.getString(Constants.RESPONSE_RESULT);
                            if (strRes.equals(Constants.RESPONSE_RESULT_OK)) {
                                friendList.get(position).
                                        setStatus(FriendEntry.INT_STATUS_DEFAULT);
                                friendsListAdapter.notifyDataSetChanged();
                                Photobook.getFriendsDataSource().updateFriend(
                                        friendList.get(position));
                            }
                        } catch (JSONException e) {
                            Log.d(Constants.LOG_TAG, "Exception " + e.getLocalizedMessage());
                        }
                        Log.d(Constants.LOG_TAG, "Response: " + response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(Constants.LOG_TAG, "Error: " + error.getLocalizedMessage());
            }
        }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public static final void likeRequest(Context context,
                                              String imageId, String userId,
                                              final Response.Listener<String> responseListener,
                                              final Response.ErrorListener errorListener) {
        Gson gson = new Gson();
        HashMap<String, String> headers = createHeaders(userId);
        headers.put(Constants.KEY_ID, imageId);
        Log.d(Constants.LOG_TAG, "Like request");
        StringRequest request = new StringRequest(Request.Method.POST,
                Constants.SERVER_URL+Constants.SERVER_PATH_LIKE ,
                "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, "Response: " + response);
                        if (responseListener != null) responseListener.onResponse(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (errorListener != null) errorListener.onErrorResponse(error);
            }
        }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public static final void unLikeRequest(Context context,
                                         String imageId, String userId,
                                         final Response.Listener<String> responseListener,
                                         final Response.ErrorListener errorListener) {
        Gson gson = new Gson();
        HashMap<String, String> headers = createHeaders(userId);
        headers.put(Constants.KEY_ID, imageId);
        Log.d(Constants.LOG_TAG, "Like request");
        StringRequest request = new StringRequest(Request.Method.DELETE,
                Constants.SERVER_URL+Constants.SERVER_PATH_LIKE ,
                "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, "Response: " + response);
                        if (responseListener != null) responseListener.onResponse(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (errorListener != null) errorListener.onErrorResponse(error);
            }
        }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public static final void getComments (Context context,
                                         String imageId,
                                         final CommentListAdapter adapter) {
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put(Constants.HEADER_USERID, Photobook.getPreferences().strUserID);
        headers.put(Constants.HEADER_IMAGEID, imageId);
        headers.put("Accept", "*/*");
        Log.d(Constants.LOG_TAG, "Load comments request");
        StringRequest getCommentsRequest = new StringRequest(Request.Method.GET,
                Constants.SERVER_URL+Constants.SERVER_PATH_COMMENTS,
                "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, response.toString());
                        try {
                            Log.d(Constants.LOG_TAG, "Response " + response);
                            Gson gson = new Gson();
                            JSONObject resJson = new JSONObject(response);
                            String strRes = resJson.getString(Constants.RESPONSE_RESULT);
                            if ((strRes.equals(Constants.RESPONSE_RESULT_OK))
                                    && (resJson.has(Constants.RESPONSE_DATA))) {
                                Type type = new TypeToken<ArrayList<CommentEntryJson>>(){}.getType();
                                ArrayList<CommentEntryJson> commentList = gson.fromJson(
                                        resJson.get(Constants.RESPONSE_DATA).toString(), type);
                                adapter.mCommentList.clear();
                                adapter.mCommentList.addAll(commentList);
                                adapter.notifyDataSetChanged();
                                Log.d(Constants.LOG_TAG, "Loaded  " + commentList.size()
                                        + " comments");
                            }
                        } catch (Exception e) {
                            Log.d(Constants.LOG_TAG, "Exception " + e.getLocalizedMessage());
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if ((error != null) && (error.networkResponse != null)
                        && (error.networkResponse.data != null))
                    Log.d(Constants.LOG_TAG, "Error: " +
                            new String(error.networkResponse.data));
            }
        }
        );
        VolleySingleton.getInstance(Photobook.getMainActivity()).
                addToRequestQueue(getCommentsRequest);
    }

    public static final void getComments (String imageId,
                                          Boolean withModTime,
                                          final Response.Listener<String> responseListener,
                                          final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put(Constants.HEADER_USERID, Photobook.getPreferences().strUserID);
        if ((imageId != null) && (!imageId.isEmpty()))
            headers.put(Constants.HEADER_IMAGEID, imageId);
        if (withModTime)
            headers.put(Constants.HEADER_MODTIME,
                    Photobook.getPreferences().strCommentsTimestamp);
        headers.put("Accept", "*/*");
        Log.d(Constants.LOG_TAG, "Load comments request with timestamp " +
                Photobook.getPreferences().strCommentsTimestamp);
        StringRequest getCommentsRequest = new StringRequest(Request.Method.GET,
                Constants.SERVER_URL+Constants.SERVER_PATH_COMMENTS,
                "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, "Response: " + response);
                        try {
                            Gson gson = new Gson();
                            JSONObject resJson = null;
                            resJson = new JSONObject(response);
                            String strRes = resJson.getString(Constants.RESPONSE_RESULT);
                            if ((strRes.equals(Constants.RESPONSE_RESULT_OK))
                                    && (resJson.has(Constants.KEY_TIMESTAMP))
                                    && (resJson.has(Constants.RESPONSE_DATA))) {
                                String strTimestamp = resJson.getString(Constants.KEY_TIMESTAMP);
                                Photobook.getPreferences().strCommentsTimestamp = strTimestamp;
                                String strData = resJson.getString(Constants.RESPONSE_DATA);
                                Type type = new TypeToken<ArrayList<CommentEntryJson>>(){}.getType();
                                ArrayList<CommentEntryJson> commentList = gson.fromJson(strData,
                                        type);
                                for (int i = 0; i < commentList.size(); i++) {
                                    if (!Photobook.getPreferences().hsetUnreadImages.
                                            contains(commentList.get(i).image_id))
                                        Photobook.getPreferences().hsetUnreadImages.add(
                                                commentList.get(i).image_id);
                                    int intCommentCount = Photobook.getPreferences().
                                            unreadImagesMap.containsKey(commentList.get(i).
                                            image_id) ? Photobook.getPreferences().
                                            unreadImagesMap.get(commentList.get(i).
                                            image_id) : 0;
                                    Photobook.getPreferences().unreadImagesMap.put(
                                            commentList.get(i).image_id, intCommentCount + 1);
                                }
                                Photobook.getPreferences().savePreferences();
                            }
                        } catch (JSONException e) {
                            Log.d(Constants.LOG_TAG, "JSON parsing error for " + response);
                        }
                        if (responseListener != null) responseListener.onResponse(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                            if (errorListener != null) errorListener.onErrorResponse(error);
                    }
        }
        );
        VolleySingleton.getInstance(Photobook.getMainActivity()).
                addToRequestQueue(getCommentsRequest);
    }

    public static final void postCommentRequest(Context context,
                                         String imageId, String userId,
                                         String strText,
                                         long replyTo,
                                         final Response.Listener<String> responseListener,
                                         final Response.ErrorListener errorListener) {
        Gson gson = new Gson();
        HashMap<String, String> headers = createHeaders(userId);
        CommentEntryJson comment = new CommentEntryJson(Request.Method.POST);
        comment.text = strText;
        comment.image_id = imageId;
        comment.reply_to = replyTo;
        Log.d(Constants.LOG_TAG, "Comment request");
        StringRequest request = new StringRequest(Request.Method.POST,
                Constants.SERVER_URL+Constants.SERVER_PATH_COMMENTS ,
                gson.toJson(comment), headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, "Response: " + response);
                        if (responseListener != null) responseListener.onResponse(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (errorListener != null) errorListener.onErrorResponse(error);
            }
        }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public static final void deleteCommentRequest(Context context,
                                                String commendId, String userId,
                                                final Response.Listener<String> responseListener,
                                                final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_USERID, userId);
        headers.put(Constants.HEADER_COMMENTID, commendId);
        headers.put("Accept", "*/*");
        Log.d(Constants.LOG_TAG, "Comment delete request");
        StringRequest request = new StringRequest(Request.Method.DELETE,
                Constants.SERVER_URL+Constants.SERVER_PATH_COMMENTS ,
                "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, "Response: " + response.toString());
                        if (responseListener != null) responseListener.onResponse(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (errorListener != null) errorListener.onErrorResponse(error);
            }
        }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public static final void getUserProfileRequest (Context context,
            String[] userIds,
            final Response.Listener<HashMap<String, UserProfile>> responseListener,
            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put(Constants.HEADER_USERID, Photobook.getPreferences().strUserID);
        String strIdHeader = userIds.length > 0 ? userIds[0] : "";
        for (int i = 1; i < userIds.length; i++) strIdHeader = strIdHeader + "," + userIds[i];
        headers.put(Constants.KEY_ID, strIdHeader);
        headers.put("Accept", "*/*");
        Log.d(Constants.LOG_TAG, "Get user profile request");
        StringRequest getUserProfileRequest = new StringRequest(Request.Method.GET,
                Constants.SERVER_URL+Constants.SERVER_PATH_USER,
                "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, response.toString());
                        try {
                            Gson gson = new Gson();
                            Type type = new TypeToken<ServerResponse
                                    <HashMap<String, UserProfile>>>(){}.getType();
                            ServerResponse<HashMap<String, UserProfile>> res =
                                    gson.fromJson(response, type);
                            if (res.isSuccess() && res.data != null)
                                if (responseListener != null)
                                    responseListener.onResponse(res.data);
                        } catch (Exception e) {
                            Log.d(Constants.LOG_TAG, "Exception in getUserProfile " +
                                    e.getLocalizedMessage());
                            if (responseListener != null)
                                responseListener.onResponse(new HashMap<String, UserProfile>());
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if ((error != null) && (error.networkResponse != null)
                                && (error.networkResponse.data != null))
                            Log.d(Constants.LOG_TAG, "Error: " +
                                    new String(error.networkResponse.data));
                        if (errorListener != null) errorListener.onErrorResponse(error);
                    }
        }
        );
        VolleySingleton.getInstance(Photobook.getMainActivity()).
                addToRequestQueue(getUserProfileRequest);
    }

    public static final void getImageDetailsRequest (Context context,
                                          String imageId,
                                          final Response.Listener<String> responseListener,
                                          final Response.ErrorListener errorListener) {
        sendImageRequest(imageId, Request.Method.GET, responseListener, errorListener);
    }

    private static void sendImageRequest(String imageId, int httpMethod,
                                         final Response.Listener<String> responseListener,
                                         final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = createHeaders(Photobook.getPreferences().strUserID);
        if ((imageId != null) && !imageId.isEmpty())
            headers.put(Constants.HEADER_IMAGEID, imageId);
        Log.d(Constants.LOG_TAG, "Get image details request");
        StringRequest imageRequest = new StringRequest(httpMethod,
                Constants.SERVER_URL + Constants.SERVER_PATH_IMAGE,
                "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, response.toString());
                        if (responseListener != null) responseListener.onResponse(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                        if (errorListener != null) errorListener.onErrorResponse(error);
            }
        }
        );
        VolleySingleton.getInstance(Photobook.getMainActivity()).
                addToRequestQueue(imageRequest);
    }

    /**
        Implements network request to un-share image
        URL: DELETE /image,
        Headers: 'imageid' - imageId
    */
    public static final void unShareImageRequest(Context context,
                                                  String imageId,
                                                  final Response.Listener<String> responseListener,
                                                  final Response.ErrorListener errorListener) {
        sendImageRequest(imageId, Request.Method.DELETE, responseListener, errorListener);
    }

    public static final void getSMSCodeRequest (Context context,
                                            String strPhoneNumber,
                                            final Response.Listener<String> responseListener,
                                            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put("number", strPhoneNumber);
        headers.put("Accept", "*/*");
        Log.d(Constants.LOG_TAG, "Receive sms code request");
        StringRequest getSMSCodeRequest = new StringRequest(Request.Method.GET,
                Constants.SERVER_URL+Constants.SERVER_PATH_USER+"/code",
                "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, response.toString());
                        if (responseListener != null) responseListener.onResponse(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if ((error != null) && (error.networkResponse != null)
                                && (error.networkResponse.data != null))
                            Log.d(Constants.LOG_TAG, "Error: " +
                                    new String(error.networkResponse.data));
                        if (errorListener != null) errorListener.onErrorResponse(error);
                    }
        }
        );
        VolleySingleton.getInstance(Photobook.getMainActivity()).
                addToRequestQueue(getSMSCodeRequest);
    }

    /**
     * Create hash map with the headers for http request
     * @param userId user id to be included to the headers
     * @return created hash map with the headers
     */
    private static HashMap<String, String> createHeaders(String userId) {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "*/*");
        headers.put(Constants.HEADER_USERID, userId);
        return headers;
    }
}
