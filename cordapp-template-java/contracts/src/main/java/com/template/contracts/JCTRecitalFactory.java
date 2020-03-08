package com.template.contracts;

import com.google.common.collect.ImmutableMap;
import com.template.contracts.clauses.*;
import net.corda.core.transactions.LedgerTransaction;

import java.util.HashMap;
import java.util.Map;

public class JCTRecitalFactory {
    private Map<Integer, String> recitalMap =
            ImmutableMap.of(1, "The Employer wishes to have the following work carried out, " +
                                "at the following location (respectively) and has had drawings and either a " +
                                "specification or work schedules prepared which show and describe the work to be done",
                            2, "the Contractor has supplied the Employer with a fully priced copy of the bills " +
                                "of quantities, which for identification has been signed or initialled by or on behalf of " +
                                "each Party (‘the Contract Bills’);\n" +
                                "and has provided the Employer with the priced schedule of activities annexed to " +
                                "this Contract (‘the Activity Schedule’);",
                            3, "The drawings are annexed to this Contract ('the Contract Drawings') " +
                                "and have for identification been signed or initialled by or on" +
                                "behalf of each Party." +
                                "They are numbered/listed in the following:",
                            4, "for the purposes of the Construction Industry Scheme (CIS) under the Finance Act 2004, " +
                                "the status of the Employer is, as at the Base Date, that stated in the Contract Particulars;",
                            5, "the Employer has provided the Contractor with a schedule (‘the Information Release Schedule’) " +
                                "which states the information the Architect/Contract Administrator will release and the time of that release;"
                    );

    public JCTRecital getRecital(Integer recitalNo) {
        if(recitalNo == null){
            return null;
        }
        if(recitalNo == 1) {
            return new JCTFirstRecital(recitalMap.get(1), "CREATED");
        } else if(recitalNo == 2) {
            return new JCTSecondRecital(recitalMap.get(2), "CREATED");
        } else if(recitalNo == 3) {
            return new JCTThirdRecital(recitalMap.get(3), "CREATED");
        }
        else if(recitalNo == 4) {
            return new JCTFourthRecital(recitalMap.get(4), "CREATED");
        }
        else if(recitalNo == 5) {
            return new JCTFifthRecital(recitalMap.get(5), "CREATED");
        }
        // TODO - MAKE MORE RECITALS
//        else if(recitalNo == 6) {
//            return new JCTSixthRecital();
//        }
//        else if(recitalNo == 7) {
//            return new JCTSeventhRecital();
//        }

        return null;
    }
}
