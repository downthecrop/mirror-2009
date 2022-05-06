import core.game.node.entity.player.link.IronmanMode
import core.game.node.item.Item
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.rs09.consts.Items
import rs09.game.content.global.shops.Shop
import rs09.game.content.global.shops.Shops
import kotlin.math.min
import kotlin.math.roundToInt

class ShopTests {
    companion object {
        init {TestUtils.preTestSetup()}
    }

    private val testPlayer = TestUtils.getMockPlayer("test")
    private val testIronman = TestUtils.getMockPlayer("test2", IronmanMode.STANDARD)
    var nonGeneral = TestUtils.getMockShop("Not General", false, false, Item(4151, 1))
    var general = TestUtils.getMockShop("General", true, false, Item(4151, 1))
    var highAlch = TestUtils.getMockShop("High(af) Alch", true, true, Item(4151, 1))

    @BeforeEach fun beforeEach() {
        val testPlayers = arrayOf(testPlayer, testIronman)
        for (player in testPlayers) {
            player.inventory.clear()
            player.attributes.remove("shop-cont")
        }
        nonGeneral.playerStock.clear()
        general.playerStock.clear()
        highAlch.playerStock.clear()
    }

    private fun assertTransactionSuccess(status: Shop.TransactionStatus) {
        Assertions.assertEquals(true, status is Shop.TransactionStatus.Success, "Transaction failure: ${if(status is Shop.TransactionStatus.Failure) status.reason else ""}")
    }

    @Test fun shouldSellItemToStore() {
        testPlayer.inventory.add(Item(4151, 1))
        testPlayer.setAttribute("shop-cont", general.getContainer(testPlayer))
        val status = general.sell(testPlayer, 0, 1)
        assertTransactionSuccess(status)
    }

    @Test fun shouldNotSellUnstockedItemToStandardStore() {
        testPlayer.inventory.add(Item(1, 1))
        testPlayer.setAttribute("shop-cont", nonGeneral.getContainer(testPlayer))
        val status = nonGeneral.sell(testPlayer, 0, 1)
        Assertions.assertEquals(true, status is Shop.TransactionStatus.Failure)
    }

    @Test fun shouldSellUnstockedItemToGeneralStore() {
        testPlayer.inventory.add(Item(1, 1))
        testPlayer.setAttribute("shop-cont", general.getContainer(testPlayer))
        val status = general.sell(testPlayer, 0, 1)
        assertTransactionSuccess(status)
    }

    @Test fun shouldSellUnstockedItemToGeneralStoreUsingLowAlchBaseValue() {
        //starts at 40% value - a.k.a. low alch price
        //drops 3% value per item stocked
        //bottoms out at 10% value (25% of the low alch price) after 10 items stocked
        val saleItemId = Items.RUNE_MED_HELM_1147
        val shopContainer = general.getContainer(testPlayer)
        Assertions.assertFalse(shopContainer.containItems(saleItemId), "Pre-assertion, shop container should not have the unstocked item to begin with.")
        testPlayer.setAttribute("shop-cont", shopContainer)
        val playerStock = general.playerStock
        Assertions.assertFalse(playerStock.containItems(saleItemId), "Pre-assertion, player stock should not have the unstocked item to begin with.")
        val saleItem = Item(saleItemId, 1)
        val alchValue = saleItem.definition.getAlchemyValue(false)
        val value = saleItem.definition.value
        Assertions.assertEquals((value * 0.4).roundToInt(), alchValue, "Pre-assertion, low alch value should be 40% value.")

        for (i in 0..14) {
            val expectedCoins = (alchValue.toDouble() - value * min(0.03 * i, 0.30)).roundToInt()
            testPlayer.inventory.add(saleItem.copy())
            Assertions.assertEquals(saleItemId, testPlayer.inventory.getId(0), "Pre-assertion, should have item in inventory slot 0.")

            val status = general.sell(testPlayer, 0, 1)

            assertTransactionSuccess(status)
            val coinItem = testPlayer.inventory[0]
            Assertions.assertEquals(Items.COINS_995, coinItem.id)
            Assertions.assertEquals(
                expectedCoins,
                coinItem.amount,
                "Selling item $i should yield the expected price."
            )
            testPlayer.inventory.clear()
        }
    }

    @Test fun shouldSellUnstockedItemToHighAlchStoreUsingHighAlchBaseValue() {
        //starts at 60% value - a.k.a. high alch price
        //drops 3% value per item stocked
        //bottoms out at 30% value (50% of the high alch price) after 10 items stocked
        val saleItemId = Items.RUNE_MED_HELM_1147
        val shopContainer = highAlch.getContainer(testPlayer)
        Assertions.assertFalse(shopContainer.containItems(saleItemId), "Pre-assertion, shop container should not have the unstocked item to begin with.")
        testPlayer.setAttribute("shop-cont", shopContainer)
        val playerStock = highAlch.playerStock
        Assertions.assertFalse(playerStock.containItems(saleItemId), "Pre-assertion, player stock should not have the unstocked item to begin with.")
        val saleItem = Item(saleItemId, 1)
        val alchValue = saleItem.definition.getAlchemyValue(true)
        val value = saleItem.definition.value
        Assertions.assertEquals((value * 0.6).roundToInt(), alchValue, "Pre-assertion, high alch value should be 60% value.")

        for (i in 0..14) {
            val expectedCoins = (alchValue.toDouble() - value * min(0.03 * i, 0.30)).roundToInt()
            testPlayer.inventory.add(saleItem.copy())
            Assertions.assertEquals(saleItemId, testPlayer.inventory.getId(0), "Pre-assertion, should have item in inventory slot 0.")

            val status = highAlch.sell(testPlayer, 0, 1)

            assertTransactionSuccess(status)
            val coinItem = testPlayer.inventory[0]
            Assertions.assertEquals(Items.COINS_995, coinItem.id)
            Assertions.assertEquals(
                expectedCoins,
                coinItem.amount,
                "Selling item $i should yield the expected price."
            )
            testPlayer.inventory.clear()
        }
    }

    @Test fun shouldSellNotedUnstockedItemForSamePriceAsUnnoted() {
        val saleItemId = Items.RUNE_MED_HELM_1147
        var notedSaleItemId = Items.RUNE_MED_HELM_1148
        val shopContainer = highAlch.getContainer(testPlayer)
        Assertions.assertFalse(shopContainer.containItems(saleItemId), "Pre-assertion, shop container should not have the unstocked item to begin with.")
        testPlayer.setAttribute("shop-cont", shopContainer)
        val playerStock = highAlch.playerStock
        Assertions.assertFalse(playerStock.containItems(saleItemId), "Pre-assertion, player stock should not have the unstocked item to begin with.")
        testPlayer.inventory.add(Item(saleItemId, 1))
        var status = highAlch.sell(testPlayer, 0, 1)
        assertTransactionSuccess(status)
        testPlayer.inventory.clear()
        val notedSaleItem = Item(notedSaleItemId, 1)
        testPlayer.inventory.add(notedSaleItem)
        val alchValue = notedSaleItem.definition.getAlchemyValue(true)
        val value = notedSaleItem.definition.value
        status = highAlch.sell(testPlayer, 0, 1)
        assertTransactionSuccess(status)
        val expectedCoins = (alchValue.toDouble() - value * 0.03).roundToInt()

        val coinItem = testPlayer.inventory[0]
        Assertions.assertEquals(Items.COINS_995, coinItem.id)
        Assertions.assertEquals(
            expectedCoins,
            coinItem.amount,
            "Selling noted item should yield the same devalued price as base item."
        )
    }

    @Test fun minimumSellPriceShouldBe1Coin() {
        testPlayer.inventory.add(Item(Items.EMPTY_POT_1931, 1))
        testPlayer.setAttribute("shop-cont", general.getContainer(testPlayer))
        val status = general.sell(testPlayer, 0, 1)
        assertTransactionSuccess(status)
        val coinItem = testPlayer.inventory[0]
        Assertions.assertEquals(Items.COINS_995, coinItem.id)
        Assertions.assertEquals(
            1,
            coinItem.amount,
            "1 coin should be the minimum selling price."
        )
    }

    @Test fun shouldSellUnstockedItemToGeneralStoreAsIronman() {
        testIronman.inventory.add(Item(1, 1))
        testIronman.setAttribute("shop-cont", general.getContainer(testIronman))
        val status = general.sell(testIronman, 0, 1)
        assertTransactionSuccess(status)
    }

    @Test fun shouldSellStackOfUnstockedItemsToPlayerStock() {
        testPlayer.inventory.add(Item(1, 20))
        testPlayer.setAttribute("shop-cont", general.getContainer(testPlayer))
        val status = general.sell(testPlayer, 0, 20)
        assertTransactionSuccess(status)
    }

    @Test fun shouldPutSoldUnstockedItemsInPlayerStock() {
        testPlayer.inventory.add(Item(2,1))
        testPlayer.setAttribute("shop-cont", general.getContainer(testPlayer))
        val status = general.sell(testPlayer, 0, 1)
        assertTransactionSuccess(status)
        Assertions.assertEquals(1, general.playerStock.getAmount(2))
        Assertions.assertEquals(0, general.getContainer(testPlayer).getAmount(2))
    }

    @Test fun shouldAllowStandardPlayerToBuy() {
        testPlayer.inventory.add(Item(995, 100000))
        testPlayer.setAttribute("shop-cont", general.getContainer(testPlayer))
        testPlayer.setAttribute("shop-main", true)
        val status = general.buy(testPlayer, 0, 1)
        assertTransactionSuccess(status)
    }

    @Test fun shouldAllowStandardPlayerToBuyOverstock() {
        testPlayer.inventory.add(Item(995, 100000))
        testPlayer.setAttribute("shop-cont", general.getContainer(testPlayer))
        testPlayer.setAttribute("shop-main", true)
        general.getContainer(testPlayer).add(Item(4151, 100))
        val status = general.buy(testPlayer, 0, 1)
        assertTransactionSuccess(status)
    }

    @Test fun shouldAllowStandardPlayerToBuyPlayerStock() {
        testPlayer.inventory.add(Item(995, 100000))
        testPlayer.setAttribute("shop-cont", general.getContainer(testPlayer))
        testPlayer.setAttribute("shop-main", false)
        general.playerStock.add(Item(4151, 100))
        val status = general.buy(testPlayer, 0, 1)
        assertTransactionSuccess(status)
    }

    @Test fun shouldNotAllowIronmanToBuyOverstock() {
        testIronman.inventory.add(Item(995, 100000))
        testIronman.setAttribute("shop-cont", general.getContainer(testIronman))
        testIronman.setAttribute("shop-main", true)
        general.getContainer(testIronman).add(Item(4151, 100))
        val status = general.buy(testIronman, 0, 1)
        Assertions.assertEquals(true, status is Shop.TransactionStatus.Failure)
    }

    @Test fun shouldNotAllowIronmanToBuyPlayerStock() {
        testIronman.inventory.add(Item(995, 100000))
        testIronman.setAttribute("shop-cont", general.playerStock)
        testIronman.setAttribute("shop-main", false)
        general.playerStock.add(Item(4151, 1))
        val status = general.buy(testIronman, 0, 1)
        Assertions.assertEquals(true, status is Shop.TransactionStatus.Failure)
    }

    @Test fun openShopShouldNotThrowException() {
        Assertions.assertDoesNotThrow {
            general.openFor(testPlayer)
        }
    }

    @Test fun shouldNotThrowExceptionWhenRestockingStockWithNullSlot() {
        Assertions.assertDoesNotThrow {
            general.getContainer(testPlayer).add(Item(1, 100))
            general.getContainer(testPlayer).add(Item(2, 100))
            general.getContainer(testPlayer).replace(null, 0) //replace item in slot 0 with null
            for ((k,_) in general.stockInstances) general.needsUpdate[k] = true
            general.restock()
        }
    }

    @Test fun playerStockShouldNeverBeNull() {
        Assertions.assertNotNull(general.playerStock)
    }

    @Test fun shouldAllowBuyingFromPlayerStockOnMultipleRows() {
        for(i in 0 until 100) {
            general.playerStock.add(Item(i + 3100, 1)) //make sure we populate several rows of items
        }
        testPlayer.inventory.add(Item(995, 100000))
        testPlayer.setAttribute("shop-cont", general.getContainer(testPlayer))
        testPlayer.setAttribute("shop-main", false)
        var status: Shop.TransactionStatus = Shop.TransactionStatus.Failure("Test did not assign transaction status.")
        Assertions.assertDoesNotThrow({
            status = general.buy(testPlayer, 36, 1)
        }, "Error selling to shop: ${general.playerStock}")
        assertTransactionSuccess(status)
    }

    @Test fun invalidStockJsonShouldNotCauseItemShift() {
        val invalidJson = "{1277,10,100}-{1277,10,100}-{1279,10,100}"
        val stock = Shops.parseStock(invalidJson, -1)
        Assertions.assertEquals(2, stock.size)
        Assertions.assertEquals(20, stock[0].amount)
    }
}