package com.template.contracts.clauses;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts. clauses.Clause;

public interface JCTClauses {
    class Move extends Clause {
        @NotNull
        @Override
        public Set<Class<? extends CommandData>> getRequiredCommands() {
            return Collections.singleton(Commands.Move.class);
        }

        @NotNull
        @Override
        public Set<Commands> verify(@NotNull TransactionForContract tx,
                                    @NotNull List<? extends State> inputs,
                                    @NotNull List<? extends State> outputs,
                                    @NotNull List<? extends AuthenticatedObject<? extends Commands>> commands,
                                    @NotNull State groupingKey) {
            AuthenticatedObject<Commands.Move> cmd = requireSingleCommand(tx.getCommands(), Commands.Move.class);
            // There should be only a single input due to aggregation above
            State input = single(inputs);

            if (!cmd.getSigners().contains(input.getOwner()))
                throw new IllegalStateException("Failed requirement: the transaction is signed by the owner of the CP");

            // Check the output CP state is the same as the input state, ignoring the owner field.
            if (outputs.size() != 1) {
                throw new IllegalStateException("the state is propagated");
            }
            // Don't need to check anything else, as if outputs.size == 1 then the output is equal to
            // the input ignoring the owner field due to the grouping.
            return Collections.singleton(cmd.getValue());
        }
    }

    class Redeem implements JCTClauses {
        @Override
        public boolean equals(Object obj) { return obj instanceof Redeem; }
    }

    class Issue implements JCTClauses {
        @Override
        public boolean equals(Object obj) { return obj instanceof Issue; }
    }
}
