package net.corda.traderdemo.api

import com.google.common.net.HostAndPort
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.DOLLARS
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.serialization.OpaqueBytes
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.loggerFor
import net.corda.flows.IssuerFlow.IssuanceRequester
import net.corda.node.services.messaging.CordaRPCClient
import net.corda.traderdemo.sslConfigFor
import java.util.*

class BuyersClientApi(val rpc: CordaRPCOps) {
    private companion object {
        val logger = loggerFor<BuyersClientApi>()
    }
    fun findAllCommercialPapers(issuers: List<Map<String, Any>>, certsPath: String?): ArrayList<ContractState> {
        val papers = ArrayList<ContractState>()
        issuers.forEach { issuer ->
            logger.info("Connecting to ${issuer["name"]}.")
            val host = HostAndPort.fromString(issuer["host"] as String)
            CordaRPCClient(host, sslConfigFor(issuer["name"] as String, certsPath)).use("demo", "demo") {
                val allLoans = SellersClientApi(this).allLoans()
                papers.addAll(allLoans)
            }
        }

        return papers
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