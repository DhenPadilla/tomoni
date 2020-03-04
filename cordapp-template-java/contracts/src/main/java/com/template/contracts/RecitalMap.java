package com.template.contracts;

import java.util.*;
import java.util.stream.Collectors;

public class RecitalMap {
    private final static HashMap<Integer, Recital> hm = new HashMap<>();

    public RecitalMap() {
        // Recital 1
        String recital1 = "The Employer wishes to have the following work carried out, " +
                        "at the following location (respectively) and has had drawings and either a " +
                        "specification or work schedules prepared which show and describe the work to be done";
        hm.put(1, Recital.createWithNoDetails(1, recital1));

        // Recital 2
        String recital2 = "the Contractor has supplied the Employer with a fully priced copy of the bills " +
                        "of quantities, which for identification has been signed or initialled by or on behalf of " +
                        "each Party (‘the Contract Bills’);\n" +
                        "and has provided the Employer with the priced schedule of activities annexed to " +
                        "this Contract (‘the Activity Schedule’);";
        hm.put(2, Recital.createWithNoDetails(2, recital2));

        // Recital 3
        String recital3 = "The drawings are annexed to this Contract ('the Contract Drawings') " +
                        "and have for identification been signed or initialled by or on" +
                        "behalf of each Party." +
                        "They are numbered/listed in the following:";
        hm.put(3, Recital.createWithNoDetails(3, recital3));

        // Recital 4
        String recital4 = "for the purposes of the Construction Industry Scheme (CIS) under the Finance Act 2004, " +
                        "the status of the Employer is, as at the Base Date, that stated in the Contract Particulars;";
        hm.put(4, Recital.createWithNoDetails(4, recital4));

        // Recital 5
        String recital5 = "the Employer has provided the Contractor with a schedule (‘the Information Release Schedule’) " +
                        "which states the information the Architect/Contract Administrator will release and the time of that release;";
        hm.put(5, Recital.createWithNoDetails(5, recital5));

        // Recital 6 - Division into sections
        String recital6 = "the division of Works into Sections is shown in the Contract Bills and/or the Contract Drawings " +
                        "or in such other documents as are identified in the Contract Particulars;";
        hm.put(6, Recital.createWithNoDetails(6, recital6));

        // Recital 7
        String recital7 = "where so stated in the Contract Particulars, this Contract is supplemented by the Framework Agreement" +
                        " identified in those particulars;";
        hm.put(7, Recital.createWithNoDetails(7, recital7));

        // Recital 8
        String recital8 = "whether any of Supplemental Provisions 1 to 6 and 9 apply is stated in the Contract Particulars;";
        hm.put(8, Recital.createWithNoDetails(8, recital8));

        // TODO - Recitals 9 to 12.


        // Returns Set view
        Set<Map.Entry<Integer, Recital>> st = hm.entrySet();

        for(Map.Entry<Integer, Recital> me:st) {
            System.out.print(me.getKey() + ":");
            System.out.println(me.getValue());
        }
    }


    public static List<Recital> getRecitalsFor(List<Integer> recitals) {
        List<Recital> result = recitals.stream()
                .map(hm::get) // or map(personMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return result;
    }
}
