/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Implementation of the {@link WebServiceConnection} interface that uses a {@link HttpURLConnection}.
 *
 * @author Arjen Poutsma
 */
public class HttpUrlConnection extends AbstractHttpWebServiceConnection {

    private final HttpURLConnection connection;

    /**
     * Creates a new instance of the <code>HttpUrlConnection</code> with the given <code>HttpURLConnection</code>.
     *
     * @param connection the <code>HttpURLConnection</code>
     */
    public HttpUrlConnection(HttpURLConnection connection) {
        Assert.notNull(connection, "connection must not be null");
        this.connection = connection;
    }

    public void close() {
        connection.disconnect();
    }

    protected void addRequestHeader(String name, String value) throws IOException {
        connection.addRequestProperty(name, value);
    }

    protected OutputStream getRequestOutputStream() throws IOException {
        return connection.getOutputStream();
    }

    protected void open() throws IOException {
        connection.connect();
    }

    protected long getResponseContentLength() throws IOException {
        return connection.getContentLength();
    }

    protected Iterator getResponseHeaderNames() throws IOException {
        List headerNames = new ArrayList();
        // Header field 0 is the status line, so we start at 1
        int i = 1;
        while (true) {
            String headerName = connection.getHeaderFieldKey(i);
            if (!StringUtils.hasLength(headerName)) {
                break;
            }
            headerNames.add(headerName);
            i++;
        }
        return headerNames.iterator();
    }

    protected Iterator getResponseHeaders(String name) throws IOException {
        String headerField = connection.getHeaderField(name);
        if (headerField == null) {
            return Collections.EMPTY_LIST.iterator();
        }
        else {
            Set tokens = StringUtils.commaDelimitedListToSet(headerField);
            return tokens.iterator();
        }
    }

    protected int getResponseCode() throws IOException {
        return connection.getResponseCode();
    }

    protected InputStream getResponseInputStream() throws IOException {
        if (connection.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            return connection.getErrorStream();
        }
        else if (connection.getResponseCode() / 100 == 2) {
            return connection.getInputStream();
        }
        else {
            throw new HttpTransportException("Did not receive successful HTTP response: status code = " +
                    connection.getResponseCode() + ", status message = [" + connection.getResponseMessage() + "]");
        }
    }

}
