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

package net.openhft.chronicle.map.impl.stage.iter;

import net.openhft.chronicle.hash.Data;
import net.openhft.chronicle.hash.impl.stage.iter.HashSegmentIteration;
import net.openhft.chronicle.map.MapContext;
import net.openhft.chronicle.map.MapEntry;
import net.openhft.chronicle.map.impl.IterationContextInterface;
import net.openhft.chronicle.map.impl.stage.entry.MapEntryStages;
import net.openhft.chronicle.map.impl.stage.map.WrappedValueInstanceValueHolder;
import net.openhft.sg.StageRef;
import net.openhft.sg.Staged;
import org.jetbrains.annotations.NotNull;

@Staged
public abstract class MapSegmentIteration<K, V, R> extends HashSegmentIteration<K, MapEntry<K, V>>
        implements MapEntry<K, V>, IterationContextInterface<K, V, R> {
    
    @StageRef MapEntryStages<K, V> entry;
    @StageRef WrappedValueInstanceValueHolder<K, V, ?> wrappedValueInstanceValueHolder;

    @Override
    public void doReplaceValue(Data<V> newValue) {
        checkOnEachPublicOperation.checkOnEachPublicOperation();
        try {
            entry.innerDefaultReplaceValue(newValue);
        } finally {
            s.innerWriteLock.unlock();
        }
    }

    @NotNull
    @Override
    public MapContext<K, V, ?> context() {
        return wrappedValueInstanceValueHolder;
    }
}
