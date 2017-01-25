package net.corda.traderdemo.api

import net.corda.contracts.CommercialPaper
import net.corda.core.contracts.Amount
import net.corda.core.contracts.DOLLARS
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.Emoji
import net.corda.core.utilities.loggerFor
import net.corda.traderdemo.TraderDemoClientApi
import net.corda.traderdemo.flow.SellerFlow
import java.util.*
import kotlin.test.assertEquals

import net.corda.traderdemo.flow.*
/**
 * Created by Martin on 2017-01-25.
 */

class SellersClientApi(val rpc: CordaRPCOps) {
    private companion object {
        val logger = loggerFor<TraderDemoClientApi>()
    }

    fun runSeller(amount: Amount<Currency> = 1000.0.DOLLARS, counterparty: String): Boolean {
        val commercialPaperTx = rpc.startFlow(::IssueFlow).returnValue.toBlocking().first()
        val commercialPaper = commercialPaperTx.tx.outRef<CommercialPaper.State>(0)
        return false
    }

    fun runSeller2(amount: Amount<Currency> = 1000.0.DOLLARS, counterparty: String): Boolean {
        val otherParty = rpc.partyFromName(counterparty)
        if (otherParty != null) {
            // The seller will sell some commercial paper to the buyer, who will pay with (self issued) cash.
            //
            // The CP sale transaction comes with a prospectus PDF, which will tag along for the ride in an
            // attachment. Make sure we have the transaction prospectus attachment loaded into our store.
            //
            // This can also be done via an HTTP upload, but here we short-circuit and do it from code.
            if (!rpc.attachmentExists(SellerFlow.PROSPECTUS_HASH)) {
                javaClass.classLoader.getResourceAsStream("bank-of-london-cp.jar").use {
                    val id = rpc.uploadAttachment(it)
                    assertEquals(SellerFlow.PROSPECTUS_HASH, id)
                }
            }

            // The line below blocks and waits for the future to resolve.
            val stx = rpc.startFlow(::SellerFlow, otherParty, amount).returnValue.toBlocking().first()
            logger.info("Sale completed - we have a happy customer!\n\nFinal transaction is:\n\n${Emoji.renderIfSupported(stx.tx)}")
            return true
        } else {
            return false
        }
    }
}
