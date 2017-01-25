package net.corda.traderdemo

import com.esotericsoftware.kryo.Kryo
import com.google.common.net.HostAndPort
import net.corda.contracts.CommercialPaper
import net.corda.contracts.testing.calculateRandomlySizedAmounts
import net.corda.core.contracts.*
import net.corda.core.crypto.CompositeKey
import net.corda.core.crypto.Party
import net.corda.core.crypto.composite
import net.corda.core.crypto.generateKeyPair
import net.corda.core.days
import net.corda.core.flows.FlowLogic
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.node.NodeInfo
import net.corda.core.seconds
import net.corda.core.serialization.OpaqueBytes
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.Emoji
import net.corda.core.utilities.loggerFor
import net.corda.flows.IssuerFlow.IssuanceRequester
import net.corda.flows.NotaryFlow
import net.corda.node.services.messaging.CordaRPCClient
import net.corda.testing.BOC
import net.corda.testing.http.HttpApi
import net.corda.traderdemo.flow.IssueFlow
import net.corda.traderdemo.flow.SellerFlow
import java.time.Instant
import java.util.*
import net.corda.traderdemo.flow.*
import kotlin.test.assertEquals

/**
 * Interface for communicating with nodes running the trader demo.
 */
class TraderDemoClientApi(val rpc: CordaRPCOps) {
    private companion object {
        val logger = loggerFor<TraderDemoClientApi>()
    }

    fun runBuyer(amount: Amount<Currency> = 30000.0.DOLLARS, notary: String = "Notary"): Boolean {
        return false;
    }

    fun runBuyer2(amount: Amount<Currency> = 30000.0.DOLLARS, notary: String = "Notary"): Boolean {
        val bankOfCordaParty = rpc.partyFromName(BOC.name)
                ?: throw Exception("Unable to locate ${BOC.name} in Network Map Service")
        val me = rpc.nodeIdentity()
        // TODO: revert back to multiple issue request amounts (3,10) when soft locking implemented
        val amounts = calculateRandomlySizedAmounts(amount, 1, 1, Random())
        val handles = amounts.map {
            rpc.startFlow(::IssuanceRequester, amount, me.legalIdentity, OpaqueBytes.of(1), bankOfCordaParty)
        }

        handles.forEach {
            require(it.returnValue.toBlocking().first() is SignedTransaction)
        }

        return true
    }
}
