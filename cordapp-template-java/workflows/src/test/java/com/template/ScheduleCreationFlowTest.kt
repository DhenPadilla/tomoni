package com.template

import net.corda.core.identity.Party
import net.corda.test
import org.junit.After
import org.junit.Before

class ResolveTransactionsFlowTest {
    private lateinit var mockNet: MockNetwork
    private lateinit var notaryNode: StartedMockNode
    private lateinit var megaCorpNode: StartedMockNode
    private lateinit var miniCorpNode: StartedMockNode
    private lateinit var newNotaryNode: StartedMockNode
    private lateinit var megaCorp: Party
    private lateinit var miniCorp: Party
    private lateinit var notary: Party
    private lateinit var newNotary: Party

    @Before
    fun setup() {
        val mockNetworkParameters = MockNetworkParameters(
                cordappsForAllNodes = listOf(DUMMY_CONTRACTS_CORDAPP, enclosedCordapp()),
                notarySpecs = listOf(
                        MockNetworkNotarySpec(DUMMY_NOTARY_NAME),
                        MockNetworkNotarySpec(DUMMY_BANK_A_NAME)
                )
        )
        mockNet = MockNetwork(mockNetworkParameters)
        notaryNode = mockNet.notaryNodes.first()
        megaCorpNode = mockNet.createPartyNode(CordaX500Name("MegaCorp", "London", "GB"))
        miniCorpNode = mockNet.createPartyNode(CordaX500Name("MiniCorp", "London", "GB"))
        notary = notaryNode.info.singleIdentity()
        megaCorp = megaCorpNode.info.singleIdentity()
        miniCorp = miniCorpNode.info.singleIdentity()
        newNotaryNode = mockNet.notaryNodes[1]
        newNotary = mockNet.notaryNodes[1].info.singleIdentity()
    }

    @After
    fun tearDown() {
        mockNet.stopNodes()
        System.setProperty("net.corda.node.dbtransactionsresolver.InMemoryResolutionLimit", "0")
    }
}