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

package net.openhft.chronicle.hash.serialization.internal;

import net.openhft.chronicle.hash.hashing.LongHashFunction;
import net.openhft.chronicle.hash.serialization.BytesInterop;
import net.openhft.chronicle.hash.serialization.BytesReader;
import net.openhft.chronicle.hash.serialization.SizeMarshaller;
import net.openhft.lang.io.Bytes;
import org.jetbrains.annotations.NotNull;

import static net.openhft.chronicle.hash.serialization.internal.DummyValue.DUMMY_VALUE;

public enum DummyValueMarshaller
        implements BytesInterop<DummyValue>, BytesReader<DummyValue>, SizeMarshaller {
    INSTANCE;

    @Override
    public boolean startsWith(@NotNull Bytes bytes, @NotNull DummyValue dummyValue) {
        return true;
    }

    @Override
    public boolean equivalent(@NotNull DummyValue a, @NotNull DummyValue b) {
        return true;
    }

    @Override
    public long hash(@NotNull LongHashFunction hashFunction, @NotNull DummyValue dummyValue) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public DummyValue read(@NotNull Bytes bytes, long size) {
        return DUMMY_VALUE;
    }

    @NotNull
    @Override
    public DummyValue read(@NotNull Bytes bytes, long size, DummyValue toReuse) {
        return DUMMY_VALUE;
    }

    @Override
    public long size(@NotNull DummyValue dummyValue) {
        return 0L;
    }

    @Override
    public void write(@NotNull Bytes bytes, @NotNull DummyValue dummyValue) {
        // do nothing
    }

    @Override
    public int sizeEncodingSize(long size) {
        return 0;
    }

    @Override
    public long minEncodableSize() {
        return 0L;
    }

    @Override
    public int minSizeEncodingSize() {
        return 0;
    }

    @Override
    public int maxSizeEncodingSize() {
        return 0;
    }

    @Override
    public void writeSize(Bytes bytes, long size) {
        assert size == 0L;
        // do nothing
    }

    @Override
    public long readSize(Bytes bytes) {
        return 0L;
    }
}
