package com.github.bhlangonijr.kengine.alphabeta

import com.github.bhlangonijr.chesslib.Bitboard
import com.github.bhlangonijr.chesslib.Board
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@ExperimentalStdlibApi
class TranspositionTableTest {

    private val keys = longArrayOf(
            -702565069, -651716404, -90772002, 139827217, -818005229, 1941803680, -1748144377, 596276289,
            1921378528, 1368035031, -1425636917, -575780564, 1409290471, 845303300, 1136883922, -1430851484,
            924912986, 92095816, -1065598504, -748528041, 1089599880, -723160118, 598513820, -1833342420,
            -906682537, -1353481056, 1170562732, 879692537, -1717334298, -2107126715, -1800285465, -2019153999,
            -312376001, -1972445292, -1934743398, -1607487485, 923393733, 1080390386, 36105524, 93848421,
            356787436, -660305748, 1469707869, 640989862, 2057833544, -1142459402, 853007415, 1784621122,
            781779923, -1144725482, 273392100, -682150231, -256820337, 1136592960, -942170213, -1018707522,
            1847610686, -1601831344, -1276646715, -346590898, 488806964, 1313067790, -1682129807, -341730302,
            612847047, 1095071911, -445450136, 1920139068, 145389739, -643893363, 1241636357, -1100133313,
            -184365430, 1319278410, 337755483, 1168851636, -2018124883, 1987138910, -130098383, -465363916,
            1261447690, 525217523, -636830992, 1346028279, -956871218, 538206404, 649095846, 1324071018,
            1081035944, -613597020, 1165899758, -1992686127, 775621122, 1788875486, -1115851423, 1026827060,
            -1049617314, -1819399049, 44482663, 1565463311, 967598703, -1176084252, 532537143, -1736176902,
            1968791497, 1942159074, 1553299834, 1540848062, -1077798375, -1957540514, -748422607, -1835034361,
            -781018150, -1237809038, 621622304, -1852382785, -44645642, 1394279158, 1641190693, 1031255177,
            1970948249, -874529702, 363532650, 149151584, -2019253001, 1233445122, -1942132566, -1605421159,
            -1510841015, -1047551057, 1756701190, -1429453207, -391100556, 1575039684, -597592416, -427242900,
            1458115053, -1658987226, -1870818275, 1265802426, 784056402, 398561731, -137806895, -325193710)

    @Test
    fun `Test storage`() {

        val board = Board()
        board.loadFromFen("r1b1kb1r/ppp2ppp/8/4n3/4n3/PPP1P3/6PP/RNBK1BNR w kq - 0 19")
        val tt = TranspositionTable(8)

        tt.put(board.incrementalHashKey, 100, 10, TranspositionTable.NodeType.EXACT, 1)

        val entry = tt.get(board.incrementalHashKey, 1)

        assertEquals(100L, entry?.value)

        for (k in keys) {
            tt.put(k, k % 3, (k % 3).toInt(), TranspositionTable.NodeType.UPPERBOUND, 1)
        }

        for (k in keys) {
            val e = tt.get(k, 1)
            assertEquals(k % 3, e?.value)
            assertEquals((k % 3).toInt(), e?.depth)
            assertEquals(TranspositionTable.NodeType.UPPERBOUND, e?.nodeType)
        }

        tt.clear()

        for (k in keys) {
            val e = tt.get(k, 1)
            assertNull(e)
        }
    }

    @Test
    fun `test numbers`() {

        val r = -5L
        val b = r.and(0xFFFFFFFFL).shl(32)
        println("${Bitboard.bitboardToString(r)}\n = \n${Bitboard.bitboardToString(b)}")

        val c = b.ushr(32).and(0xFFFFFFFFL).toInt()
        assertEquals(r, c.toLong())
    }


}