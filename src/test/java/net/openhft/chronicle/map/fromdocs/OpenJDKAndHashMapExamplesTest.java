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

package net.openhft.chronicle.map.fromdocs;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.chronicle.map.ExternalMapQueryContext;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;

/**
 * These code fragments will appear in an article on OpenHFT. These tests to ensure that the examples compile
 * and behave as expected.
 */
public class OpenJDKAndHashMapExamplesTest {
    private static final SimpleDateFormat YYYYMMDD = new SimpleDateFormat("yyyyMMdd");

    private static final String TMP = System.getProperty("java.io.tmpdir");

    public static long parseYYYYMMDD(String s) {
        try {
            return YYYYMMDD.parse(s).getTime();
        } catch (ParseException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void bondExample() throws IOException, InterruptedException {

        File file = new File(TMP + "/chm-myBondPortfolioCHM-" + System.nanoTime());
        file.deleteOnExit();

        ChronicleMap<String, BondVOInterface> chm = ChronicleMapBuilder
                .of(String.class, BondVOInterface.class)
                .averageKeySize(10)
                .entries(1000)
                .createPersistedTo(file);

        BondVOInterface bondVO = chm.newValueInstance();
        try (net.openhft.chronicle.core.io.Closeable c =
                     chm.acquireContext("369604103", bondVO)) {
            bondVO.setIssueDate(parseYYYYMMDD("20130915"));
            bondVO.setMaturityDate(parseYYYYMMDD("20140915"));
            bondVO.setCoupon(5.0 / 100); // 5.0%

            BondVOInterface.MarketPx mpx930 = bondVO.getMarketPxIntraDayHistoryAt(0);
            mpx930.setAskPx(109.2);
            mpx930.setBidPx(106.9);

            BondVOInterface.MarketPx mpx1030 = bondVO.getMarketPxIntraDayHistoryAt(1);
            mpx1030.setAskPx(109.7);
            mpx1030.setBidPx(107.6);
        }

        ChronicleMap<String, BondVOInterface> chmB = ChronicleMapBuilder
                .of(String.class, BondVOInterface.class)
                .averageKeySize(10)
                .entries(1000)
                .createPersistedTo(file);

        try (ExternalMapQueryContext<String, BondVOInterface, ?> c =
                     chmB.queryContext("369604103")) {
            BondVOInterface bond = c.entry().value().get();
            if (bond != null) {
                assertEquals(5.0 / 100, bond.getCoupon(), 0.0);

                BondVOInterface.MarketPx mpx930B = bond.getMarketPxIntraDayHistoryAt(0);
                assertEquals(109.2, mpx930B.getAskPx(), 0.0);
                assertEquals(106.9, mpx930B.getBidPx(), 0.0);

                BondVOInterface.MarketPx mpx1030B = bond.getMarketPxIntraDayHistoryAt(1);
                assertEquals(109.7, mpx1030B.getAskPx(), 0.0);
                assertEquals(107.6, mpx1030B.getBidPx(), 0.0);
            }
        }

        BondVOInterface bond = chm.newValueInstance();
        // lookup the key and give me a reference I can update in a thread safe way.
        try (net.openhft.chronicle.core.io.Closeable c =
                     chm.acquireContext("369604103", bond)) {
            // found a key and bond has been set
            // get directly without touching the rest of the record.
            long _matDate = bond.getMaturityDate();
            // write just this field, again we need to assume we are the only writer.
            bond.setMaturityDate(parseYYYYMMDD("20440315"));

            //demo of how to do OpenHFT off-heap array[ ] processing
            int tradingHour = 2;  //current trading hour intra-day
            BondVOInterface.MarketPx mktPx = bond.getMarketPxIntraDayHistoryAt(tradingHour);
            if (mktPx.getCallPx() < 103.50) {
                mktPx.setParPx(100.50);
                mktPx.setAskPx(102.00);
                mktPx.setBidPx(99.00);
                // setMarketPxIntraDayHistoryAt is not needed as we are using zero copy,
                // the original has been changed.
            }
        }

        // bond will be full of default values and zero length string the first time.

        // from this point, all operations are completely record/entry local,
        // no other resource is involved.
        // now perform thread safe operations on my reference
        bond.addAtomicMaturityDate(16 * 24 * 3600 * 1000L);  //20440331

        bond.addAtomicCoupon(-1 * bond.getCoupon()); //MT-safe! now a Zero Coupon Bond.

        // cleanup.
        chm.close();
        chmB.close();
        file.delete();
    }
}
