/*
 *      Copyright (C) 2015  higherfrequencytrading.com
 *
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Lesser General Public License as published by
 *      the Free Software Foundation, either version 3 of the License.
 *
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public License
 *      along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.chronicle.map;

import net.openhft.chronicle.hash.ChronicleHashInstanceBuilder;
import net.openhft.chronicle.hash.replication.ReplicationChannel;
import net.openhft.chronicle.hash.replication.SingleChronicleHashReplication;
import net.openhft.chronicle.hash.replication.TcpTransportAndNetworkConfig;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

final class MapInstanceBuilder<K, V>
        implements ChronicleHashInstanceBuilder<ChronicleMap<K, V>>, Serializable {

    final ChronicleMapBuilder<K, V> mapBuilder;
    transient SingleChronicleHashReplication singleHashReplication;
    transient ReplicationChannel channel;
    transient File file;
    String name;

    final AtomicBoolean used;

    MapInstanceBuilder(ChronicleMapBuilder<K, V> mapBuilder,
                       SingleChronicleHashReplication singleHashReplication,
                       ReplicationChannel channel,
                       File file, String name, AtomicBoolean used) {
        this.mapBuilder = mapBuilder;
        this.singleHashReplication = singleHashReplication;
        this.channel = channel;
        this.file = file;
        this.name = name;
        this.used = used;
    }

    @Override
    public MapInstanceBuilder<K, V> replicated(
            byte identifier, TcpTransportAndNetworkConfig tcpTransportAndNetwork) {
        return replicated(SingleChronicleHashReplication.builder()
                .tcpTransportAndNetwork(tcpTransportAndNetwork).createWithId(identifier));
    }

    @Override
    public MapInstanceBuilder<K, V> replicated(SingleChronicleHashReplication replication) {
        singleHashReplication = replication;
        channel = null;
        return this;
    }

    @Override
    public MapInstanceBuilder<K, V> replicatedViaChannel(ReplicationChannel channel) {
        singleHashReplication = null;
        this.channel = channel;
        return this;
    }

    @Override
    public MapInstanceBuilder<K, V> persistedTo(File file) {
        this.file = file;
        return this;
    }

    @Override
    public MapInstanceBuilder<K, V> name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public synchronized ChronicleMap<K, V> create() throws IOException {
        if (!used.getAndSet(true)) {
            return mapBuilder.create(this);
        } else {
            throw new IllegalStateException(
                    "A ChronicleMap has already been created using this instance config chain. " +
                            "Create a new instance config (builder.instance()) to create a new " +
                            "ChronicleMap instance");
        }
    }

}
