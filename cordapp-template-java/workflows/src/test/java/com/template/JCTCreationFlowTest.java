package com.template;

import com.google.common.collect.ImmutableList;
import com.template.flows.JCTFlow;
import com.template.flows.JCTFlowResponder;
//import com.template.flows.IOUFlow;
//import com.template.flows.IOUFlowResponder;
import net.corda.core.contracts.TransactionVerificationException;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

import static org.hamcrest.core.IsInstanceOf.instanceOf;

public class JCTCreationFlowTest {
    private MockNetwork mockNet;
    private StartedMockNode notaryNode;
    private StartedMockNode employerNode;
    private StartedMockNode contractor1Node;
    private StartedMockNode contractor2Node;
    private StartedMockNode newNotaryNode;
    private Party employer;
    private Party contractor1;
    private Party contractor2;
    private Party notary;
    private Party newNotary;

    public JCTCreationFlowTest() {
//        a.registerInitiatedFlow(JCTCreationFlow.class);
//        b.registerInitiatedFlow(JCTCreationFlow.class);
    }

    @Before
    public void setup() {
        MockNetworkParameters mockNetworkParameters = new MockNetworkParameters(ImmutableList.of(
                TestCordapp.findCordapp("com.template.contracts"),
                TestCordapp.findCordapp("com.template.flows")
        ));
        mockNet = new MockNetwork(mockNetworkParameters);
        notaryNode = mockNet.getNotaryNodes().get(0);
        employerNode = mockNet.createPartyNode(new CordaX500Name("EmployerCorp", "London", "GB"));
        contractor1Node = mockNet.createPartyNode(new CordaX500Name("Contractor1Corp", "London", "GB"));
        contractor2Node = mockNet.createPartyNode(new CordaX500Name("Contractor2Corp", "London", "GB"));
        notary = notaryNode.getInfo().getLegalIdentities().get(0);
        employer = employerNode.getInfo().getLegalIdentities().get(0);
        contractor1 = contractor1Node.getInfo().getLegalIdentities().get(0);
        contractor2 = contractor2Node.getInfo().getLegalIdentities().get(0);

        contractor1Node.registerInitiatedFlow(JCTFlowResponder.class);
        contractor2Node.registerInitiatedFlow(JCTFlowResponder.class);
        mockNet.runNetwork();
    }

    @After
    public void tearDown() {
        mockNet.stopNodes();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test public void flowRequiresListOfParties() throws Exception {
        JCTFlow flow = new JCTFlow("Hello", Arrays.asList(employer), Arrays.asList(contractor1));
        CordaFuture<Void> future = employerNode.startFlow(flow);
        mockNet.runNetwork();

        exception.expectCause(instanceOf(TransactionVerificationException.class));
        future.get();
    }

    @Test
    public void flowRejectsInvalidJCTs() throws Exception {
        JCTFlow flow = new JCTFlow("", Arrays.asList(employer), Arrays.asList(contractor1, contractor2));
        CordaFuture<Void> future = employerNode.startFlow(flow);
        mockNet.runNetwork();

        exception.expectCause(instanceOf(TransactionVerificationException.class));
        future.get();
    }
}
