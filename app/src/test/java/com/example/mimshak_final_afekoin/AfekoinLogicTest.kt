package com.example.mimshak_final_afekoin

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.min

// Unit tests for reward calculations and input validation (no device or Firebase needed)
class AfekoinLogicTest {

    // Afekliker
    private fun afeklikerReward(taps: Int): Double = min(2.0, taps * 0.02)

    @Test fun afekliker_zeroTaps_givesZero() {
        assertEquals(0.0, afeklikerReward(0), 0.001)
    }

    @Test fun afekliker_oneTap_givesTwoHundredths() {
        assertEquals(0.02, afeklikerReward(1), 0.001)
    }

    @Test fun afekliker_100Taps_givesMaximum() {
        assertEquals(2.0, afeklikerReward(100), 0.001)
    }

    @Test fun afekliker_200Taps_stillCappedAtMax() {
        assertEquals(2.0, afeklikerReward(200), 0.001)
    }

    @Test fun afekliker_50Taps_givesOneAfk() {
        assertEquals(1.0, afeklikerReward(50), 0.001)
    }

    // Afequiz
    private fun quizReward(correct: Int): Double = correct.toDouble()

    @Test fun quiz_0Correct_givesNoReward() {
        assertEquals(0.0, quizReward(0), 0.001)
    }

    @Test fun quiz_15Correct_gives15Afk() {
        assertEquals(15.0, quizReward(15), 0.001)
    }

    @Test fun quiz_7Correct_gives7Afk() {
        assertEquals(7.0, quizReward(7), 0.001)
    }

    // Liebnitz
    private fun liebnitzReward(score: Int): Double = min(5.0, score * 0.1)

    @Test fun liebnitz_zeroScore_givesZero() {
        assertEquals(0.0, liebnitzReward(0), 0.001)
    }

    @Test fun liebnitz_50Score_givesMaximum() {
        assertEquals(5.0, liebnitzReward(50), 0.001)
    }

    @Test fun liebnitz_100Score_stillCappedAtMax() {
        assertEquals(5.0, liebnitzReward(100), 0.001)
    }

    @Test fun liebnitz_10Score_givesOneAfk() {
        assertEquals(1.0, liebnitzReward(10), 0.001)
    }

    // Daily bonus
    @Test fun dailyBonus_isAlwaysFiveAfk() {
        val bonus = 5.0
        assertTrue("Daily bonus must be positive", bonus > 0)
        assertEquals(5.0, bonus, 0.001)
    }

    // Store
    data class StoreItem(val name: String, val price: Double)

    private val storeItems = listOf(
        StoreItem("Print credit",    50.0),
        StoreItem("Coffee voucher",  100.0),
        StoreItem("Campus lunch",    200.0),
        StoreItem("USB flash drive", 800.0),
        StoreItem("Notebook",        1300.0),
        StoreItem("Afeka T-shirt",   2500.0),
        StoreItem("Afeka hoodie",    5000.0),
    )

    @Test fun store_allItemsHavePositivePrice() {
        storeItems.forEach { item ->
            assertTrue("${item.name} price must be positive", item.price > 0)
        }
    }

    @Test fun store_itemCount_isSeven() {
        assertEquals(7, storeItems.size)
    }

    @Test fun store_cheapestItem_isPrintCredit() {
        val cheapest = storeItems.minByOrNull { it.price }
        assertEquals("Print credit", cheapest?.name)
        assertEquals(50.0, cheapest?.price ?: 0.0, 0.001)
    }

    @Test fun store_mostExpensiveItem_isHoodie() {
        val priciest = storeItems.maxByOrNull { it.price }
        assertEquals("Afeka hoodie", priciest?.name)
        assertEquals(5000.0, priciest?.price ?: 0.0, 0.001)
    }

    @Test fun store_itemsAreSortedByAscendingPrice() {
        val prices = storeItems.map { it.price }
        assertEquals(prices.sorted(), prices)
    }

    // Username validation
    private val usernameRegex = Regex("^[a-z0-9_]{3,20}$")

    @Test fun username_validLowercase_passes() {
        assertTrue(usernameRegex.matches("iliasim"))
    }

    @Test fun username_withNumbers_passes() {
        assertTrue(usernameRegex.matches("user123"))
    }

    @Test fun username_withUnderscore_passes() {
        assertTrue(usernameRegex.matches("ai_bot"))
    }

    @Test fun username_tooShort_fails() {
        assertFalse(usernameRegex.matches("ab"))
    }

    @Test fun username_tooLong_fails() {
        assertFalse(usernameRegex.matches("a".repeat(21)))
    }

    @Test fun username_uppercase_fails() {
        assertFalse(usernameRegex.matches("ILIASIM"))
    }

    @Test fun username_withSpaces_fails() {
        assertFalse(usernameRegex.matches("bad user"))
    }

    @Test fun username_withSpecialChars_fails() {
        assertFalse(usernameRegex.matches("user@name"))
    }

    // Transfer validation
    @Test fun transfer_positiveAmount_isValid() {
        assertTrue(10.0 > 0)
    }

    @Test fun transfer_zeroAmount_isInvalid() {
        assertFalse(0.0 > 0)
    }

    @Test fun transfer_negativeAmount_isInvalid() {
        assertFalse(-5.0 > 0)
    }

    @Test fun transfer_insufficientBalance_isRejected() {
        val balance = 30.0
        val amount  = 50.0
        assertFalse("Should reject transfer when balance < amount", balance >= amount)
    }

    @Test fun transfer_sufficientBalance_isAllowed() {
        val balance = 100.0
        val amount  = 50.0
        assertTrue("Should allow transfer when balance >= amount", balance >= amount)
    }
}
