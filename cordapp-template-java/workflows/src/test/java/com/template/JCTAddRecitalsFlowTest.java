//package com.template;
//
//import com.google.common.collect.ImmutableList;
//import com.template.contracts.JCTContract;
//import com.template.flows.JCTFlow;
//import com.template.flows.JCTFlowResponder;
//import com.template.states.JCTState;
//import net.corda.core.concurrent.CordaFuture;
//import net.corda.core.contracts.TransactionVerificationException;
//import net.corda.core.contracts.UniqueIdentifier;
//import net.corda.core.identity.CordaX500Name;
//import net.corda.core.identity.Party;
//import net.corda.core.transactions.SignedTransaction;
//import net.corda.testing.node.MockNetwork;
//import net.corda.testing.node.MockNetworkParameters;
//import net.corda.testing.node.StartedMockNode;
//import net.corda.testing.node.TestCordapp;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.ExpectedException;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.concurrent.ExecutionException;
//
//import static org.hamcrest.core.IsInstanceOf.instanceOf;
//
////import com.template.flows.IOUFlow;
////import com.template.flows.IOUFlowResponder;
//
//public class JCTAddRecitalsFlowTest {
//    private MockNetwork mockNet;
//    private StartedMockNode notaryNode;
//    private StartedMockNode a;
//    private StartedMockNode b;
//    private StartedMockNode c;
//    private StartedMockNode newNotaryNode;
//    private Party employer;
//    private Party contractor1;
//    private Party contractor2;
//    private Party notary;
//    private Party newNotary;
//
//    @Before
//    public void setup() {
//        MockNetworkParameters mockNetworkParameters = new MockNetworkParameters(ImmutableList.of(
//                TestCordapp.findCordapp("com.template.contracts"),
//                TestCordapp.findCordapp("com.template.flows")
//        ));
//        mockNet = new MockNetwork(mockNetworkParameters);
//        notaryNode = mockNet.getNotaryNodes().get(0);
//        a = mockNet.createPartyNode(new CordaX500Name("EmployerCorp", "London", "GB"));
//        b = mockNet.createPartyNode(new CordaX500Name("Contractor1Corp", "London", "GB"));
//        c = mockNet.createPartyNode(new CordaX500Name("Contractor2Corp", "London", "GB"));
//        notary = notaryNode.getInfo().getLegalIdentities().get(0);
//        employer = a.getInfo().getLegalIdentities().get(0);
//        contractor1 = b.getInfo().getLegalIdentities().get(0);
//        contractor2 = c.getInfo().getLegalIdentities().get(0);
//
//        b.registerInitiatedFlow(JCTFlowResponder.class);
//        c.registerInitiatedFlow(JCTFlowResponder.class);
//        mockNet.runNetwork();
//    }
//
//    @After
//    public void tearDown() {
//        mockNet.stopNodes();
//    }
//
//    protected SignedTransaction createJCT() throws Exception {
//        JCTFlow flow = new JCTFlow("Test", Collections.singletonList(employer), Arrays.asList(contractor1, contractor2));
//        CordaFuture<SignedTransaction> future = a.startFlow(flow);
//
//        mockNet.runNetwork();
//        return future.get();
//    }
//
//    protected SignedTransaction addRecitals(UniqueIdentifier linearId,
//                                            StartedMockNode lender,
//                                            StartedMockNode newLender,
//                                            Boolean anonymous) throws Exception {
//        SignedTransaction initialisedJCT = createJCT();
//        JCTContract.Commands.Create flow = new JCTContract.Commands.Create();
//        .startFlow(flow).get();
//    }
//
//    @Rule
//    public final ExpectedException exception = ExpectedException.none();

//    @Test
//    public void addRecitalsToJCTSuccessfully() throws Exception {
//        // Issue obligation.
//        SignedTransaction createdJCTTransaction = createJCT();
//        mockNet.waitQuiescent();
//        JCTState createdJCT = (JCTState) createdJCTTransaction.getTx().getOutputStates().get(0);
//
//        // Transfer obligation.
//        SignedTransaction addRecitalsTransaction =
//                transferObligation(issuedObligation.getLinearId(), b, c, false);
//        network.waitQuiescent();
//        Obligation transferredObligation = (Obligation) transferTransaction.getTx().getOutputStates().get(0);
//
//        // Check the issued obligation with the new lender is the transferred obligation
//        assertEquals(issuedObligation.withNewLender(chooseIdentity(c.getInfo())), transferredObligation);
//
//        // Check everyone has the transfer transaction.
//        Obligation aObligation = (Obligation) a.getServices().loadState(transferTransaction.getTx().outRef(0).getRef()).getData();
//        Obligation bObligation = (Obligation) b.getServices().loadState(transferTransaction.getTx().outRef(0).getRef()).getData();
//        Obligation cObligation = (Obligation) c.getServices().loadState(transferTransaction.getTx().outRef(0).getRef()).getData();
//        assertEquals(aObligation, bObligation);
//        assertEquals(bObligation, cObligation);
//    }

//}
