/*
 * Copyright (c) 2012-2016 Algolia
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

import android.support.annotation.NonNull;

import org.json.JSONObject;

/**
 * Encapsulates the two possible outcomes of an API request: either a JSON object (success), or an error (failure).
 * One and only one is guaranteed to be non-null.
 */
class APIResult {
    /** The content returned (in case of success). */
    public final JSONObject content;

    /** The error encountered (in case of failure). */
    public final AlgoliaException error;

    /**
     * Construct a new success result.
     *
     * @param content The content returned.
     */
    public APIResult(@NonNull JSONObject content) {
        this.content = content;
        this.error = null;
    }

    /**
     * Construct a new failure result.
     *
     * @param error The error that was encountered.
     */
    public APIResult(@NonNull AlgoliaException error) {
        this.content = null;
        this.error = error;
    }

    /** Test whether this is a success (true) or failure (false) result. */
    public boolean isSuccess() {
        return error == null;
    }
}