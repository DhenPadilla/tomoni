package com.template;

import com.google.common.collect.ImmutableList;
import com.template.flows.CreateJCTScheduleFlow;
import com.template.flows.CreateJCTScheduleResponder;
import com.template.states.JCTJob;
import com.template.states.JCTJobStatus;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.TransactionVerificationException;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.instanceOf;

public class CreateScheduleFlowTest {
    private MockNetwork mockNet;
    private StartedMockNode notaryNode;
    private StartedMockNode employerNode1;
    private StartedMockNode employerNode2;
    private StartedMockNode unexpectedEmployerNode;
    private StartedMockNode contractorNode1;
    private StartedMockNode contractorNode2;
    private Party employer1;
    private Party employer2;
    private Party unexpectedEmployer;
    private Party contractor1;
    private Party contractor2;
    private Party notary;

    public CreateScheduleFlowTest() { }

    @Before
    public void setup() {
        MockNetworkParameters mockNetworkParameters = new MockNetworkParameters(ImmutableList.of(
                TestCordapp.findCordapp("com.template.contracts"),
                TestCordapp.findCordapp("com.template.flows")
        ));
        mockNet = new MockNetwork(mockNetworkParameters);
        notaryNode = mockNet.getNotaryNodes().get(0);
        employerNode1 = mockNet.createPartyNode(new CordaX500Name("EmployerCorp1", "London", "GB"));
        employerNode2 = mockNet.createPartyNode(new CordaX500Name("EmployerCorp2", "London", "GB"));
        unexpectedEmployerNode = mockNet.createPartyNode(new CordaX500Name("UnexpectedEmployer", "London", "GB"));
        contractorNode1 = mockNet.createPartyNode(new CordaX500Name("ContractorCorp1", "London", "GB"));
        contractorNode2 = mockNet.createPartyNode(new CordaX500Name("ContractorCorp2", "London", "GB"));
        notary = notaryNode.getInfo().getLegalIdentities().get(0);
        employer1 = employerNode1.getInfo().getLegalIdentities().get(0);
        employer2 = employerNode2.getInfo().getLegalIdentities().get(0);
        contractor1 = contractorNode1.getInfo().getLegalIdentities().get(0);
        contractor2 = contractorNode2.getInfo().getLegalIdentities().get(0);

        contractorNode1.registerInitiatedFlow(CreateJCTScheduleResponder.class);
        contractorNode2.registerInitiatedFlow(CreateJCTScheduleResponder.class);
        mockNet.runNetwork();
    }

    JobExamples jobFactory = new JobExamples();

    List<JCTJob> jobs = jobFactory.getJobExamples();

    @After
    public void tearDown() {
        mockNet.stopNodes();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void flowRequiresAllJobsPendingStatus() throws Exception {
        JCTJob inProgressJob1 = new JCTJob(null,null,null,null,null, JCTJobStatus.PENDING);
        JCTJob inProgressJob2 = new JCTJob(null,null,null,null,null,JCTJobStatus.IN_PROGRESS);
        List<JCTJob> inProgressJobs = Arrays.asList(inProgressJob1, inProgressJob2);
        CreateJCTScheduleFlow flow =
                new CreateJCTScheduleFlow(
                        "Project 1",
                        Arrays.asList(employer1, employer2),
                        Arrays.asList(contractor1, contractor2),
                        1000.0,
                        5.0,
                        true,
                        inProgressJobs,
                        notary,
                        "Job Reference");
        CordaFuture<UniqueIdentifier> future = employerNode1.startFlow(flow);
        mockNet.runNetwork();

        exception.expectCause(instanceOf(TransactionVerificationException.class));
        future.get();
    }

    @Test
    public void flowRequiresMoreThanTwoParties() throws Exception {
        System.out.println("Number of jobs:");
        System.out.println(jobs.size());
        CreateJCTScheduleFlow flow =
                new CreateJCTScheduleFlow(
                        "Project 1",
                        Arrays.asList(employer1),
                        Arrays.asList(contractor1),
                        1000.0,
                        5.0,
                        true,
                        jobs,
                        notary,
                        "Job Reference");
        CordaFuture<UniqueIdentifier> future = employerNode1.startFlow(flow);
        mockNet.runNetwork();

        exception.expectCause(instanceOf(TransactionVerificationException.class));
        future.get();
    }


    @Test
    public void contractSumAmountMustNotBeZero() throws Exception {
        System.out.println("Testing Contract Sum == 0...");
        CreateJCTScheduleFlow flow =
                new CreateJCTScheduleFlow(
                        "Project 1",
                        Arrays.asList(employer1, employer2),
                        Arrays.asList(contractor1, contractor2),
                        0.0,
                        5.0,
                        true,
                        jobs,
                        notary,
                        "Job Reference");
        CordaFuture<UniqueIdentifier> future = employerNode1.startFlow(flow);
        mockNet.runNetwork();

        exception.expectCause(instanceOf(TransactionVerificationException.class));
        future.get();
    }
}
