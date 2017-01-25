package net.corda.traderdemo.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.contracts.CommercialPaper
import net.corda.contracts.asset.DUMMY_CASH_ISSUER
import net.corda.core.contracts.DOLLARS
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.TransactionType
import net.corda.core.contracts.`issued by`
import net.corda.core.crypto.CompositeKey
import net.corda.core.crypto.Party
import net.corda.core.crypto.composite
import net.corda.core.crypto.generateKeyPair
import net.corda.core.days
import net.corda.core.flows.FlowLogic
import net.corda.core.node.NodeInfo
import net.corda.core.seconds
import net.corda.core.transactions.SignedTransaction
import net.corda.flows.NotaryFlow
import java.time.Instant


//class IssueFlow : FlowLogic<StateAndRef<CommercialPaper.State>>() {
class IssueFlow : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val notary: NodeInfo = serviceHub.networkMapCache.notaryNodes[0]
        val cpOwnerKey = serviceHub.legalIdentityKey
        val tx = selfIssueSomeCommercialPaper(cpOwnerKey.public.composite, notary)
        return tx
    }

    @Suspendable
    fun selfIssueSomeCommercialPaper(ownedBy: CompositeKey, notaryNode: NodeInfo): SignedTransaction {
        // Make a fake company that's issued its own paper.
        val keyPair = generateKeyPair()
        val party = Party("Bank of London", keyPair.public)

        val issuance: SignedTransaction = run {
            val tx = CommercialPaper().generateIssue(party.ref(1, 2, 3), 1100.DOLLARS `issued by` DUMMY_CASH_ISSUER,
                    Instant.now() + 10.days, notaryNode.notaryIdentity)

            // TODO: Consider moving these two steps below into generateIssue.

            // Attach the prospectus.
//            tx.addAttachment(serviceHub.storageService.attachments.openAttachment(SellerFlow.PROSPECTUS_HASH)!!.id)

            // Requesting timestamping, all CP must be timestamped.
            tx.setTime(Instant.now(), 30.seconds)

            // Sign it as ourselves.
            tx.signWith(keyPair)

            // Get the notary to sign the timestamp
            val notarySig = subFlow(NotaryFlow.Client(tx.toSignedTransaction(false)))
            tx.addSignatureUnchecked(notarySig)

            // Commit it to local storage.
            val stx = tx.toSignedTransaction(true)
            serviceHub.recordTransactions(listOf(stx))

            stx
        }

//         Now make a dummy transaction that moves it to a new key, just to show that resolving dependencies works.
        val move: SignedTransaction = run {
            val builder = TransactionType.General.Builder(notaryNode.notaryIdentity)
            CommercialPaper().generateMove(builder, issuance.tx.outRef(0), ownedBy)
            builder.signWith(keyPair)
            val notarySignature = subFlow(NotaryFlow.Client(builder.toSignedTransaction(false)))
            builder.addSignatureUnchecked(notarySignature)
            val tx = builder.toSignedTransaction(true)
            serviceHub.recordTransactions(listOf(tx))
            tx
        }

//        return move.tx.outRef(0)
        return move
    }
}