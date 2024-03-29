/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.client;

import com.google.common.base.Objects;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

public class ClientSession
{
    private final URI server;
    private final String user;
    private final String source;
    private final String catalog;
    private final String schema;
    private final String language;
    private final boolean debug;

    public ClientSession(URI server, String user, String source, String catalog, String schema, boolean debug)
    {
        this.server = checkNotNull(server, "server is null");
        this.user = user;
        this.source = source;
        this.catalog = catalog;
        this.schema = schema;
        this.language = "";
        this.debug = debug;
    }
    public ClientSession(URI server, String user, String source, String catalog, String schema, boolean debug, String language)
    {
        this.server = checkNotNull(server, "server is null");
        this.user = user;
        this.source = source;
        this.catalog = catalog;
        this.schema = schema;
        this.language = language;
        this.debug = debug;
    }

    public URI getServer()
    {
        return server;
    }

    public String getUser()
    {
        return user;
    }

    public String getSource()
    {
        return source;
    }

    public String getCatalog()
    {
        return catalog;
    }

    public String getSchema()
    {
        return schema;
    }

    public String getLanguage() { return language; }

    public boolean isDebug()
    {
        return debug;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("server", server)
                .add("user", user)
                .add("catalog", catalog)
                .add("schema", schema)
                .add("debug", debug)
                .add("language", language)
                .toString();
    }
}
