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

package net.openhft.chronicle.hash.replication;

import net.openhft.chronicle.hash.ChronicleHash;
import net.openhft.chronicle.hash.ChronicleHashBuilder;
import net.openhft.chronicle.hash.HashAbsentEntry;
import net.openhft.chronicle.hash.HashEntry;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.MapEntry;
import net.openhft.chronicle.map.Replica;
import net.openhft.chronicle.map.replication.MapRemoteOperations;
import net.openhft.chronicle.set.replication.SetRemoteOperations;

/**
 * Abstracts a replicable entry in the replicated {@link ChronicleHash}. A {@link HashAbsentEntry}
 * might be a {@code ReplicableEntry} as well as {@link HashEntry}.
 */
public interface ReplicableEntry {
    /**
     * The identifier, associated with this entry. Originally, and by default, this identifier means
     * the identifier of the {@code ChronicleHash} node on which this entry was updated last.
     *
     * <p>When the entry is replicated, this identifier comes to remote nodes as {@link
     * RemoteOperationContext#remoteIdentifier()}.
     *
     * <p>On {@code ChronicleHash} local operations with entries, like {@link ChronicleMap#put(
     * Object, Object)}, this identifier is overwritten to the own {@code ChronicleHash} {@link
     * Replica#identifier()}. On remote operations, proxied through {@link MapRemoteOperations} or
     * {@link SetRemoteOperations}, you are free to overwrite this identifier to any value, using
     * {@link #updateOrigin(byte, long)} method.
     *
     * @return the identifier, associated with this entry
     */
    byte originIdentifier();

    /**
     * The timestamp, associated with this entry. Originally, and by default, this timestamp means
     * the time of the last.
     *
     * <p>When the entry is replicated, this timestamp comes to remote nodes as {@link
     * RemoteOperationContext#remoteTimestamp()}.
     *
     * <p>On {@code ChronicleHash} local operations with entries, like {@link ChronicleMap#put(
     * Object, Object)}, this timestamp is overwritten using {@linkplain
     * ChronicleHashBuilder#timeProvider(TimeProvider) the configured time provider}. On remote
     * operations, proxied through {@link MapRemoteOperations} or {@link SetRemoteOperations},
     * you are free to overwrite this timestamp by any value, using {@link #updateOrigin(byte, long)
     * } method.
     *
     * @return the timestamp, associated with this entry
     * @see TimeProvider
     */
    long originTimestamp();

    /**
     * Overwrite the entry's associated identifier and timestamp. After this call {@link
     * #originIdentifier()} returns the given {@code newIdentifier} and {@link #originTimestamp()}
     * returns the given {@code newTimestamp} respectively.
     *
     * @param newIdentifier the new identifier for the entry
     * @param newTimestamp the new timestamp for the entry
     */
    void updateOrigin(byte newIdentifier, long newTimestamp);

    /**
     * Suppress the entry, if it was scheduled to be replicated over to remote Chronicle nodes.
     */
    void dropChanged();

    /**
     * Propagate the entry, schedule it to be replicated over to remote Chronicle nodes.
     */
    void raiseChanged();

    /**
     * Check is the entry is scheduled to be replicated to the remote Chronicle nodes, to which
     * the connection is currently established.
     *
     * @return {@code true} is the entry is "dirty" locally, i. e. should be replicated to remote
     * Chronicle nodes, {@code false} otherwise
     */
    boolean isChanged();

    /**
     * Completely remove the entry from the Chronicle hash, not just mark is as deleted, what
     * {@link MapEntry#doRemove()} does for Replicated Chronicle hash. After calling this method
     * the entry won't be replicated over to remote Chronicle nodes, as if {@link #dropChanged()}
     * was called, and incoming entry replication event with the key if this entry will be
     * processed, as the entry have never been present in the Chronicle hash. After calling this
     * method any subsequent call of any method of {@code ReplicableEntry} class throws {@code
     * IllegalStateException}.
     */
    void doRemoveCompletely();
}
