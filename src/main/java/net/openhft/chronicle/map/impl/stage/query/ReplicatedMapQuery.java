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

package net.openhft.chronicle.map.impl.stage.query;

import net.openhft.chronicle.hash.Data;
import net.openhft.chronicle.hash.replication.ReplicableEntry;
import net.openhft.chronicle.map.MapAbsentEntry;
import net.openhft.chronicle.map.impl.stage.data.DummyValueZeroData;
import net.openhft.chronicle.map.impl.stage.entry.ReplicatedMapEntryStages;
import net.openhft.chronicle.map.impl.stage.replication.ReplicationUpdate;
import net.openhft.chronicle.map.impl.stage.ret.BytesReturnValue;
import net.openhft.chronicle.map.replication.MapRemoteQueryContext;
import net.openhft.chronicle.map.replication.MapReplicableEntry;
import net.openhft.sg.StageRef;
import net.openhft.sg.Staged;
import org.jetbrains.annotations.Nullable;

import static net.openhft.chronicle.hash.impl.stage.query.KeySearch.SearchState.DELETED;

@Staged
public abstract class ReplicatedMapQuery<K, V, R> extends MapQuery<K, V, R>
        implements MapRemoteQueryContext<K, V, R>, ReplicableEntry {

    @StageRef ReplicatedMapEntryStages<K, V, ?> e;
    @StageRef ReplicationUpdate ru;

    @StageRef BytesReturnValue<V> bytesReturnValue;

    @StageRef ReplicatedMapAbsentDelegating<K, V> absentDelegating;

    @Nullable
    @Override
    public MapAbsentEntry<K, V> absentEntry() {
        checkOnEachPublicOperation.checkOnEachPublicOperation();
        if (entryPresent()) {
            return null;
        } else {
            if (!ks.searchStatePresent()) {
                return absentDelegating;
            } else {
                assert e.entryDeleted();
                return absent.absent();
            }
        }
    }

    @Override
    public boolean entryPresent() {
        return super.entryPresent() && !e.entryDeleted();
    }
    
    @StageRef DummyValueZeroData<V> dummyValue;

    @Override
    public MapReplicableEntry<K, V> entry() {
        return (MapReplicableEntry<K, V>) super.entry();
    }

    @Override
    public void doRemove() {
        checkOnEachPublicOperation.checkOnEachPublicOperation();
        s.innerUpdateLock.lock();
        if (entryPresent()) {
            if (e.valueSize > dummyValue.size())
                e.innerDefaultReplaceValue(dummyValue);
            e.updatedReplicationStateOnPresentEntry();
            e.writeEntryDeleted();
            ru.updateChange();
            s.deleted(s.deleted() + 1);
        } else {
            throw new IllegalStateException("Entry is absent in the map when doRemove() is called");
        }
    }

    @Override
    public void doRemoveCompletely() {
        boolean wasDeleted = e.entryDeleted();
        super.doRemove();
        if (wasDeleted)
            s.deleted(s.deleted() - 1L);
    }

    @Override
    public void doReplaceValue(Data<V> newValue) {
        super.doReplaceValue(newValue);
        ru.updateChange();
        e.updatedReplicationStateOnPresentEntry();
    }
}
