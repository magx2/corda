package net.corda.traderdemo.api

import net.corda.core.contracts.DOLLARS
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.serialization.OpaqueBytes
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.loggerFor
import net.corda.flows.IssuerFlow.IssuanceRequester

class BuyersClientApi(val rpc: CordaRPCOps) {
    private companion object {
        val logger = loggerFor<BuyersClientApi>()
    }
    fun runBuyer(amount: net.corda.core.contracts.Amount<java.util.Currency> = 30000.0.DOLLARS, notary: String = "Notary"): Boolean {
        return false;
    }

    fun runBuyer2(amount: net.corda.core.contracts.Amount<java.util.Currency> = 30000.0.DOLLARS, notary: String = "Notary"): Boolean {
        val bankOfCordaParty = rpc.partyFromName(net.corda.testing.BOC.name)
                ?: throw Exception("Unable to locate ${net.corda.testing.BOC.name} in Network Map Service")
        val me = rpc.nodeIdentity()
        // TODO: revert back to multiple issue request amounts (3,10) when soft locking implemented
        val amounts = net.corda.contracts.testing.calculateRandomlySizedAmounts(amount, 1, 1, java.util.Random())
        val handles = amounts.map {
            rpc.startFlow(::IssuanceRequester, amount, me.legalIdentity, OpaqueBytes.of(1), bankOfCordaParty)
        }

        handles.forEach {
            require(it.returnValue.toBlocking().first() is SignedTransaction)
        }

        return true
    }
}