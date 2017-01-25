package net.corda.traderdemo

import net.corda.contracts.CommercialPaper
import net.corda.core.contracts.*
import net.corda.core.crypto.Party
import net.corda.core.transactions.TransactionBuilder
import java.time.Instant
import java.util.*

/**
 * Created by Martin on 2017-01-25.
 */
class InvestPaper : CommercialPaper() {
    override fun generateIssue(issuance: PartyAndReference, faceValue: Amount<Issued<Currency>>, maturityDate: Instant, notary: Party): TransactionBuilder {
        val state = TransactionState(State(issuance, issuance.party.owningKey, faceValue, maturityDate), notary)
        return TransactionType.General.Builder(notary = notary).withItems(state, Command(Commands.Issue(), issuance.party.owningKey))
    }
}