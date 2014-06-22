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
package com.facebook.presto.connector.system;

import com.facebook.presto.spi.HostAddress;
import com.facebook.presto.spi.Split;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class SystemSplit
        implements Split
{
    private final SystemTableHandle tableHandle;
    private final Map<String, Object> filters;
    private final List<HostAddress> addresses;

    public SystemSplit(SystemTableHandle tableHandle, Map<String, Object> filters, HostAddress address)
    {
        this(tableHandle, filters, ImmutableList.of(checkNotNull(address, "address is null")));
    }

    @JsonCreator
    public SystemSplit(
            @JsonProperty("tableHandle") SystemTableHandle tableHandle,
            @JsonProperty("filters") Map<String, Object> filters,
            @JsonProperty("addresses") List<HostAddress> addresses)
    {
        this.tableHandle = checkNotNull(tableHandle, "tableHandle is null");
        this.filters = checkNotNull(filters, "filters is null");

        checkNotNull(addresses, "hosts is null");
        checkArgument(!addresses.isEmpty(), "hosts is empty");
        this.addresses = ImmutableList.copyOf(addresses);
    }

    @Override
    public boolean isRemotelyAccessible()
    {
        return false;
    }

    @Override
    @JsonProperty
    public List<HostAddress> getAddresses()
    {
        return addresses;
    }

    @JsonProperty
    public SystemTableHandle getTableHandle()
    {
        return tableHandle;
    }

    @JsonProperty
    public Map<String, Object> getFilters()
    {
        return filters;
    }

    @Override
    public Object getInfo()
    {
        return this;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("tableHandle", tableHandle)
                .add("filters", filters)
                .add("addresses", addresses)
                .toString();
    }
}
