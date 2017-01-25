package net.corda.traderdemo.plugin

import com.esotericsoftware.kryo.Kryo
import net.corda.contracts.CommercialPaper
import net.corda.core.contracts.Amount
import net.corda.core.crypto.Party
import net.corda.core.node.CordaPluginRegistry
import net.corda.core.serialization.OpaqueBytes
import net.corda.flows.IssuerFlow
import net.corda.traderdemo.flow.BuyerFlow
import net.corda.traderdemo.flow.IssueFlow
import net.corda.traderdemo.flow.SellerFlow
import java.util.function.Function

class TraderDemoPlugin : CordaPluginRegistry() {
    // A list of Flows that are required for this cordapp
    override val requiredFlows: Map<String, Set<String>> = mapOf(
            SellerFlow::class.java.name to setOf(Party::class.java.name, Amount::class.java.name),
            IssueFlow::class.java.name to setOf()
    )

    override fun registerRPCKryoTypes(kryo: Kryo): Boolean {
        // Add classes like this.
        kryo.register(CommercialPaper::class.java)
        kryo.register(CommercialPaper.State::class.java)
        // You should return true, otherwise your plugin will be ignored for registering classes with Kryo.
        return true
    }
    override val servicePlugins = listOf(Function(BuyerFlow::Service))
}
