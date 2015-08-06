/*
 * Copyright (c) 2015 Algolia
 * http://www.algolia.com/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.algolia.search.saas;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
 * Abstract class for Index methods
 */
abstract class BaseIndex {
    private APIClient client;
    private String encodedIndexName;
    private String indexName;
    private final long MAX_TIME_MS_TO_WAIT = 10000L;

    /**
     * Index initialization (You should not call this initialized yourself)
     */
    protected BaseIndex(APIClient client, String indexName) {
        try {
            this.client = client;
            this.encodedIndexName = URLEncoder.encode(indexName, "UTF-8");
            this.indexName = indexName;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getIndexName() {
        return indexName;
    }

    /**
     * Add an object in this index
     *
     * @param obj the object to add. 
     */
    public JSONObject addObject(JSONObject obj) throws AlgoliaException {
        return client.postRequest("/1/indexes/" + encodedIndexName, obj.toString(), false);
    }

    /**
     * Add an object in this index
     *
     * @param obj the object to add. 
     * @param objectID an objectID you want to attribute to this object 
     * (if the attribute already exist the old object will be overwrite)
     */
    public JSONObject addObject(JSONObject obj, String objectID) throws AlgoliaException {
        try {
            return client.putRequest("/1/indexes/" + encodedIndexName + "/" + URLEncoder.encode(objectID, "UTF-8"), obj.toString());
        } catch (UnsupportedEncodingException e) {
            throw new AlgoliaException(e.getMessage());
        }
    }

    /**
     * Custom batch
     *
     * @param actions the array of actions
     * @throws AlgoliaException
     */
    public JSONObject batch(JSONArray actions) throws AlgoliaException {
        try {
            JSONObject content = new JSONObject();
            content.put("requests", actions);
            return client.postRequest("/1/indexes/" + encodedIndexName + "/batch", content.toString(), false);
        } catch (JSONException e) {
            throw new AlgoliaException(e.getMessage());
        }
    }

    /**
     * Custom batch
     *
     * @param actions the array of actions
     * @throws AlgoliaException
     */
    public JSONObject batch(List<JSONObject> actions) throws AlgoliaException {
        try {
            JSONObject content = new JSONObject();
            content.put("requests", actions);
            return client.postRequest("/1/indexes/" + encodedIndexName + "/batch", content.toString(), false);
        } catch (JSONException e) {
            throw new AlgoliaException(e.getMessage());
        }
    }

    /**
     * Add several objects
     *
     * @param objects contains an array of objects to add.
     */
    public JSONObject addObjects(List<JSONObject> objects) throws AlgoliaException {
        try {
            JSONArray array = new JSONArray();
            for (JSONObject obj : objects) {
                JSONObject action = new JSONObject();
                action.put("action", "addObject");
                action.put("body",obj);
                array.put(action);
            }
            return batch(array);
        } catch (JSONException e) {
            throw new AlgoliaException(e.getMessage());
        }
    }

    /**
     * Add several objects
     *
     * @param inputArray contains an array of objects to add.
     */
    public JSONObject addObjects(JSONArray inputArray) throws AlgoliaException {
        try {
            JSONArray array = new JSONArray();
            for(int n = 0; n < inputArray.length(); n++)
            {
                JSONObject action = new JSONObject();
                action.put("action", "addObject");
                action.put("body", inputArray.getJSONObject(n));
                array.put(action);
            }
            return batch(array);
        } catch (JSONException e) {
            throw new AlgoliaException(e.getMessage());
        }
    }

    /**
     * Get an object from this index
     *
     * @param objectID the unique identifier of the object to retrieve
     */
    public JSONObject getObject(String objectID) throws AlgoliaException {
        try {
            return client.getRequest("/1/indexes/" + encodedIndexName + "/" + URLEncoder.encode(objectID, "UTF-8"), false);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get an object from this index
     *
     * @param objectID the unique identifier of the object to retrieve
     * @param attributesToRetrieve, contains the list of attributes to retrieve.
     */
    public JSONObject getObject(String objectID,  List<String> attributesToRetrieve) throws AlgoliaException {
        try {
            StringBuilder params = new StringBuilder();
            params.append("?attributes=");
            for (int i = 0; i < attributesToRetrieve.size(); ++i) {
                if (i > 0)
                    params.append(",");
                params.append(URLEncoder.encode(attributesToRetrieve.get(i), "UTF-8"));
            }
            return client.getRequest("/1/indexes/" + encodedIndexName + "/" + URLEncoder.encode(objectID, "UTF-8") + params.toString(), false);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get several objects from this index
     *
     * @param objectIDs the array of unique identifier of objects to retrieve
     * @throws AlgoliaException
     */
    public JSONObject getObjects(List<String> objectIDs) throws AlgoliaException {
        try {
            JSONArray requests = new JSONArray();
            for (String id : objectIDs) {
                JSONObject request = new JSONObject();
                request.put("indexName", this.indexName);
                request.put("objectID", id);
                requests.put(request);
            }
            JSONObject body = new JSONObject();
            body.put("requests", requests);
            return client.postRequest("/1/indexes/*/objects", body.toString(), true);
        } catch (JSONException e){
            throw new AlgoliaException(e.getMessage());
        }
    }

    /**
     * Update partially an object (only update attributes passed in argument)
     *
     * @param partialObject the object attributes to override
     */
    public JSONObject partialUpdateObject(JSONObject partialObject, String objectID) throws AlgoliaException {
        try {
            return client.postRequest("/1/indexes/" + encodedIndexName + "/" + URLEncoder.encode(objectID, "UTF-8") + "/partial", partialObject.toString(), false);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Partially Override the content of several objects
     *
     * @param inputArray the array of objects to update (each object must contains an objectID attribute)
     */
    public JSONObject partialUpdateObjects(JSONArray inputArray) throws AlgoliaException {
        try {
            JSONArray array = new JSONArray();
            for(int n = 0; n < inputArray.length(); n++)
            {
                JSONObject obj = inputArray.getJSONObject(n);
                JSONObject action = new JSONObject();
                action.put("action", "partialUpdateObject");
                action.put("objectID", obj.getString("objectID"));
                action.put("body", obj);
                array.put(action);
            }
            return batch(array);
        } catch (JSONException e) {
            throw new AlgoliaException(e.getMessage());
        }
    }

    /**
     * Partially Override the content of several objects
     *
     * @param objects the array of objects to update (each object must contains an objectID attribute)
     */
    public JSONObject partialUpdateObjects(List<JSONObject> objects) throws AlgoliaException {
        try {
            JSONArray array = new JSONArray();
            for (JSONObject obj : objects) {
                JSONObject action = new JSONObject();
                action.put("action", "partialUpdateObject");
                action.put("objectID", obj.getString("objectID"));
                action.put("body",obj);
                array.put(action);
            }
            return batch(array);
        } catch (JSONException e) {
            throw new AlgoliaException(e.getMessage());
        }
    }

    /**
     * Override the content of object
     *
     * @param object the object to save
     */
    public JSONObject saveObject(JSONObject object, String objectID) throws AlgoliaException {
        try {
            return client.putRequest("/1/indexes/" + encodedIndexName + "/" + URLEncoder.encode(objectID, "UTF-8"), object.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Override the content of several objects
     *
     * @param objects an array of objects to update (each object must contains an objectID attribute)
     */
    public JSONObject saveObjects(List<JSONObject> objects) throws AlgoliaException {
        try {
            JSONArray array = new JSONArray();
            for (JSONObject obj : objects) {
                JSONObject action = new JSONObject();
                action.put("action", "updateObject");
                action.put("objectID", obj.getString("objectID"));
                action.put("body",obj);
                array.put(action);
            }
            return batch(array);
        } catch (JSONException e) {
            throw new AlgoliaException(e.getMessage());
        }
    }

    /**
     * Override the content of several objects
     *
     * @param inputArray contains an array of objects to update (each object must contains an objectID attribute)
     */
    public JSONObject saveObjects(JSONArray inputArray) throws AlgoliaException {
        try {
            JSONArray array = new JSONArray();
            for(int n = 0; n < inputArray.length(); n++)
            {
                JSONObject obj = inputArray.getJSONObject(n);
                JSONObject action = new JSONObject();
                action.put("action", "updateObject");
                action.put("objectID", obj.getString("objectID"));
                action.put("body", obj);
                array.put(action);
            }
            return batch(array);
        } catch (JSONException e) {
            throw new AlgoliaException(e.getMessage());
        }
    }

    /**
     * Delete an object from the index 
     *
     * @param objectID the unique identifier of object to delete
     */
    public JSONObject deleteObject(String objectID) throws AlgoliaException {
        if (objectID.length() == 0)
            throw new AlgoliaException("Invalid objectID");
        try {
            return client.deleteRequest("/1/indexes/" + encodedIndexName + "/" + URLEncoder.encode(objectID, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Delete several objects
     *
     * @param objects the array of objectIDs to delete
     */
    public JSONObject deleteObjects(List<String> objects) throws AlgoliaException {
        try {
            JSONArray array = new JSONArray();
            for (String id : objects) {
                JSONObject obj = new JSONObject();
                obj.put("objectID", id);
                JSONObject action = new JSONObject();
                action.put("action", "deleteObject");
                action.put("body",obj);
                array.put(action);
            }
            return batch(array);
        } catch (JSONException e) {
            throw new AlgoliaException(e.getMessage());
        }
    }

    /**
     * Delete several objects
     *
     * @param objects the array of objectIDs to delete
     */
    public JSONObject deleteObjects2(List<JSONObject> objects) throws AlgoliaException {
        try {
            JSONArray array = new JSONArray();
            for (JSONObject obj : objects) {
                JSONObject action = new JSONObject();
                action.put("action", "deleteObject");
                action.put("body", obj);
                array.put(action);
            }
            return batch(array);
        } catch (JSONException e) {
            throw new AlgoliaException(e.getMessage());
        }
    }

    static class IndexBrower implements Iterator<JSONObject> {

        IndexBrower(APIClient client, String encodedIndexName, Query params, String startingCursor) throws AlgoliaException {
            this.client = client;
            this.params = params;
            this.encodedIndexName = encodedIndexName;

            doQuery(startingCursor);
            this.pos = 0;
        }

        @Override
        public boolean hasNext() {
            try {
                do {
                    if (pos < answer.getJSONArray("hits").length()) {
                        hit = answer.getJSONArray("hits").getJSONObject(pos);
                        ++pos;
                        return true;
                    }
                    if (answer.has("cursor") && !answer.getString("cursor").isEmpty()) {
                        pos = 0;
                        doQuery(getCursor());
                        continue;
                    }
                    return false;
                } while (true);
            } catch (JSONException e) {
                throw new IllegalStateException(e);
            } catch (AlgoliaException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public JSONObject next() {
            return hit;
        }

        public String getCursor() {
            try {
                return answer != null && answer.has("cursor") ? answer.getString("cursor") : null;
            } catch (JSONException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void remove() {
            throw new IllegalStateException("Cannot remove while browsing");
        }

        private void doQuery(String cursor) throws AlgoliaException {
            String paramsString = params.getQueryString();
            if (cursor != null) {
                try {
                    paramsString += (paramsString.length() > 0 ? "&" : "") + "cursor=" +  URLEncoder.encode(cursor, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalStateException(e);
                }
            }
            this.answer = client.getRequest("/1/indexes/" + encodedIndexName + "/browse" + ((paramsString.length() > 0) ? ("?" + paramsString) : ""), true);
        }

        final APIClient client;
        final Query params;
        final String encodedIndexName;
        JSONObject answer;
        JSONObject hit;
        int pos;
    }

    /**
     * Browse all index content
     */
    public Iterator<JSONObject> browse(Query params) throws AlgoliaException {
        return new IndexBrower(client, encodedIndexName, params, null);
    }

    /**
     * Browse all index content starting from a cursor
     */
    public Iterator<JSONObject> browseFrom(Query params, String cursor) throws AlgoliaException {
        return new IndexBrower(client, encodedIndexName, params, cursor);
    }

    /**
     * Delete all objects matching a query
     *
     * @param query the query string
     * @throws AlgoliaException
     */
    public void deleteByQuery(Query query) throws AlgoliaException {
        List<String> attributesToRetrieve = new ArrayList<String>();
        attributesToRetrieve.add("objectID");
        query.setAttributesToRetrieve(attributesToRetrieve);
        query.setHitsPerPage(100);

        JSONObject results = this.search(query);
        try {
            while (results.getInt("nbHits") != 0) {
                List<String> objectIDs = new ArrayList<String>();
                for (int i = 0; i < results.getJSONArray("hits").length(); ++i) {
                    JSONObject hit = results.getJSONArray("hits").getJSONObject(i);
                    objectIDs.add(hit.getString("objectID"));
                }
                JSONObject task = this.deleteObjects(objectIDs);
                this.waitTask(task.getString("taskID"));
                results = this.search(query);
            }
        } catch (JSONException e) {
            throw new AlgoliaException(e.getMessage());
        }
    }

    /**
     * Search inside the index
     */
    public JSONObject search(Query query) throws AlgoliaException {
        String paramsString = query.getQueryString();
        if (paramsString.length() > 0)
            return client.getRequest("/1/indexes/" + encodedIndexName + "?" + paramsString, true);
        else
            return client.getRequest("/1/indexes/" + encodedIndexName, true);
    }

    /**
     * Wait the publication of a task on the server. 
     * All server task are asynchronous and you can check with this method that the task is published.
     *
     * @param taskID the id of the task returned by server
     * @param timeToWait time to sleep seed
     */
    public void waitTask(String taskID, long timeToWait) throws AlgoliaException {
        try {
            while (true) {
                JSONObject obj = client.getRequest("/1/indexes/" + encodedIndexName + "/task/" + URLEncoder.encode(taskID, "UTF-8"), false);
                if (obj.getString("status").equals("published"))
                    return;
                try {
                    Thread.sleep(timeToWait >= MAX_TIME_MS_TO_WAIT ? MAX_TIME_MS_TO_WAIT : timeToWait);
                } catch (InterruptedException e) {
                }
                timeToWait *= 2;
            }
        } catch (JSONException e) {
            throw new AlgoliaException(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wait the publication of a task on the server.
     * All server task are asynchronous and you can check with this method that the task is published.
     *
     * @param taskID the id of the task returned by server
     */
    public void waitTask(String taskID) throws AlgoliaException {
        waitTask(taskID, MAX_TIME_MS_TO_WAIT);
    }

    /**
     * Get settings of this index
     */
    public JSONObject getSettings() throws AlgoliaException {
        return client.getRequest("/1/indexes/" + encodedIndexName + "/settings", false);
    }

    /**
     * Set settings for this index
     *
     * @param settings the settings object that can contains :
     * - minWordSizefor1Typo: (integer) the minimum number of characters to accept one typo (default = 3).
     * - minWordSizefor2Typos: (integer) the minimum number of characters to accept two typos (default = 7).
     * - hitsPerPage: (integer) the number of hits per page (default = 10).
     * - attributesToRetrieve: (array of strings) default list of attributes to retrieve in objects. 
     *   If set to null, all attributes are retrieved.
     * - attributesToHighlight: (array of strings) default list of attributes to highlight. 
     *   If set to null, all indexed attributes are highlighted.
     * - attributesToSnippet**: (array of strings) default list of attributes to snippet alongside the number of words to return (syntax is attributeName:nbWords).
     *   By default no snippet is computed. If set to null, no snippet is computed.
     * - attributesToIndex: (array of strings) the list of fields you want to index.
     *   If set to null, all textual and numerical attributes of your objects are indexed, but you should update it to get optimal results.
     *   This parameter has two important uses:
     *     - Limit the attributes to index: For example if you store a binary image in base64, you want to store it and be able to 
     *       retrieve it but you don't want to search in the base64 string.
     *     - Control part of the ranking*: (see the ranking parameter for full explanation) Matches in attributes at the beginning of 
     *       the list will be considered more important than matches in attributes further down the list. 
     *       In one attribute, matching text at the beginning of the attribute will be considered more important than text after, you can disable 
     *       this behavior if you add your attribute inside `unordered(AttributeName)`, for example attributesToIndex: ["title", "unordered(text)"].
     * - attributesForFaceting: (array of strings) The list of fields you want to use for faceting. 
     *   All strings in the attribute selected for faceting are extracted and added as a facet. If set to null, no attribute is used for faceting.
     * - ranking: (array of strings) controls the way results are sorted.
     *   We have six available criteria: 
     *    - typo: sort according to number of typos,
     *    - geo: sort according to decreassing distance when performing a geo-location based search,
     *    - proximity: sort according to the proximity of query words in hits,
     *    - attribute: sort according to the order of attributes defined by attributesToIndex,
     *    - exact: sort according to the number of words that are matched identical to query word (and not as a prefix),
     *    - custom: sort according to a user defined formula set in **customRanking** attribute.
     *   The standard order is ["typo", "geo", "proximity", "attribute", "exact", "custom"]
     * - customRanking: (array of strings) lets you specify part of the ranking.
     *   The syntax of this condition is an array of strings containing attributes prefixed by asc (ascending order) or desc (descending order) operator.
     *   For example `"customRanking" => ["desc(population)", "asc(name)"]`  
     * - queryType: Select how the query words are interpreted, it can be one of the following value:
     *   - prefixAll: all query words are interpreted as prefixes,
     *   - prefixLast: only the last word is interpreted as a prefix (default behavior),
     *   - prefixNone: no query word is interpreted as a prefix. This option is not recommended.
     * - highlightPreTag: (string) Specify the string that is inserted before the highlighted parts in the query result (default to "<em>").
     * - highlightPostTag: (string) Specify the string that is inserted after the highlighted parts in the query result (default to "</em>").
     * - optionalWords: (array of strings) Specify a list of words that should be considered as optional when found in the query.
     */
    public JSONObject setSettings(JSONObject settings) throws AlgoliaException {
        return client.putRequest("/1/indexes/" + encodedIndexName + "/settings", settings.toString());
    }

    /**
     * Delete the index content without removing settings and index specific API keys.
     */
    public JSONObject clearIndex() throws AlgoliaException {
        return client.postRequest("/1/indexes/" + encodedIndexName + "/clear", "", false);
    }

    /**
     * List all existing user keys with their associated ACLs
     */
    public JSONObject listUserKeys() throws AlgoliaException {
        return client.getRequest("/1/indexes/" + encodedIndexName + "/keys", false);
    }

    /**
     * Get ACL of a user key
     */
    public JSONObject getUserKeyACL(String key) throws AlgoliaException {
        return client.getRequest("/1/indexes/" + encodedIndexName + "/keys/" + key, false);
    }

    /**
     * Delete an existing user key
     */
    public JSONObject deleteUserKey(String key) throws AlgoliaException {
        return client.deleteRequest("/1/indexes/" + encodedIndexName + "/keys/" + key);
    }

    /**
     * Create a new user key
     *
     * @param params the list of parameters for this key. Defined by a JSONObject that 
     * can contains the following values:
     *   - acl: array of string
     *   - indices: array of string
     *   - validity: int
     *   - referers: array of string
     *   - description: string
     *   - maxHitsPerQuery: integer
     *   - queryParameters: string
     *   - maxQueriesPerIPPerHour: integer
     */
    public JSONObject addUserKey(JSONObject params) throws AlgoliaException {
        return client.postRequest("/1/indexes/" + encodedIndexName + "/keys", params.toString(), false);
    }

    /**
     * Create a new user key
     *
     * @param acls the list of ACL for this key. Defined by an array of strings that 
     * can contains the following values:
     *   - search: allow to search (https and http)
     *   - addObject: allows to add/update an object in the index (https only)
     *   - deleteObject : allows to delete an existing object (https only)
     *   - deleteIndex : allows to delete index content (https only)
     *   - settings : allows to get index settings (https only)
     *   - editSettings : allows to change index settings (https only)
     */
    public JSONObject addUserKey(List<String> acls) throws AlgoliaException {
        return addUserKey(acls, 0, 0, 0);
    }

    /**
     * Update a new user key
     *
     * @param params the list of parameters for this key. Defined by a JSONObject that 
     * can contains the following values:
     *   - acl: array of string
     *   - indices: array of string
     *   - validity: int
     *   - referers: array of string
     *   - description: string
     *   - maxHitsPerQuery: integer
     *   - queryParameters: string
     *   - maxQueriesPerIPPerHour: integer
     */
    public JSONObject updateUserKey(String key, JSONObject params) throws AlgoliaException {
        return client.putRequest("/1/indexes/" + encodedIndexName + "/keys/" + key, params.toString());
    }

    /**
     * Update a user key
     *
     * @param acls the list of ACL for this key. Defined by an array of strings that 
     * can contains the following values:
     *   - search: allow to search (https and http)
     *   - addObject: allows to add/update an object in the index (https only)
     *   - deleteObject : allows to delete an existing object (https only)
     *   - deleteIndex : allows to delete index content (https only)
     *   - settings : allows to get index settings (https only)
     *   - editSettings : allows to change index settings (https only)
     */
    public JSONObject updateUserKey(String key, List<String> acls) throws AlgoliaException {
        return updateUserKey(key, acls, 0, 0, 0);
    }

    /**
     * Create a new user key
     *
     * @param acls the list of ACL for this key. Defined by an array of strings that 
     * can contains the following values:
     *   - search: allow to search (https and http)
     *   - addObject: allows to add/update an object in the index (https only)
     *   - deleteObject : allows to delete an existing object (https only)
     *   - deleteIndex : allows to delete index content (https only)
     *   - settings : allows to get index settings (https only)
     *   - editSettings : allows to change index settings (https only)
     * @param validity the number of seconds after which the key will be automatically removed (0 means no time limit for this key)
     * @param maxQueriesPerIPPerHour Specify the maximum number of API calls allowed from an IP address per hour.  Defaults to 0 (no rate limit).
     * @param maxHitsPerQuery Specify the maximum number of hits this API key can retrieve in one call. Defaults to 0 (unlimited) 
     */
    public JSONObject addUserKey(List<String> acls, int validity, int maxQueriesPerIPPerHour, int maxHitsPerQuery) throws AlgoliaException {
        JSONArray array = new JSONArray(acls);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("acl", array);
            jsonObject.put("validity", validity);
            jsonObject.put("maxQueriesPerIPPerHour", maxQueriesPerIPPerHour);
            jsonObject.put("maxHitsPerQuery", maxHitsPerQuery);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return addUserKey(jsonObject);
    }

    /**
     * Update a user key
     *
     * @param acls the list of ACL for this key. Defined by an array of strings that 
     * can contains the following values:
     *   - search: allow to search (https and http)
     *   - addObject: allows to add/update an object in the index (https only)
     *   - deleteObject : allows to delete an existing object (https only)
     *   - deleteIndex : allows to delete index content (https only)
     *   - settings : allows to get index settings (https only)
     *   - editSettings : allows to change index settings (https only)
     * @param validity the number of seconds after which the key will be automatically removed (0 means no time limit for this key)
     * @param maxQueriesPerIPPerHour Specify the maximum number of API calls allowed from an IP address per hour.  Defaults to 0 (no rate limit).
     * @param maxHitsPerQuery Specify the maximum number of hits this API key can retrieve in one call. Defaults to 0 (unlimited) 
     */
    public JSONObject updateUserKey(String key, List<String> acls, int validity, int maxQueriesPerIPPerHour, int maxHitsPerQuery) throws AlgoliaException {
        JSONArray array = new JSONArray(acls);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("acl", array);
            jsonObject.put("validity", validity);
            jsonObject.put("maxQueriesPerIPPerHour", maxQueriesPerIPPerHour);
            jsonObject.put("maxHitsPerQuery", maxHitsPerQuery);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return updateUserKey(key, jsonObject);
    }

    /**
     * Browse all index content
     *
     * @param page Pagination parameter used to select the page to retrieve.
     *             Page is zero-based and defaults to 0. Thus, to retrieve the 10th page you need to set page=9
     */
    public JSONObject browse(int page) throws AlgoliaException {
        return client.getRequest("/1/indexes/" + encodedIndexName + "/browse?page=" + page, false);
    }

    /**
     * Browse all index content
     *
     * @param page Pagination parameter used to select the page to retrieve.
     *             Page is zero-based and defaults to 0. Thus, to retrieve the 10th page you need to set page=9
     * @param hitsPerPage: Pagination parameter used to select the number of hits per page. Defaults to 1000.
     */
    public JSONObject browse(int page, int hitsPerPage) throws AlgoliaException {
        return client.getRequest("/1/indexes/" + encodedIndexName + "/browse?page=" + page + "&hitsPerPage=" + hitsPerPage, false);
    }
}