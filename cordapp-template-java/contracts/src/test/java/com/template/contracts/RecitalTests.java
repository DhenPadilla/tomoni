package com.template.contracts;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.template.contracts.RecitalMap.getRecitalsFor;

public class RecitalTests {
    @Test
    public void testRecitalMapperFunction() {
        List<Integer> recitals = new ArrayList<>();
        recitals.add(1);
        recitals.add(4);
        recitals.add(6);
        List<JCTRecital> recitalList = getRecitalsFor(recitals);
        // Print the name from the list....
        for(JCTRecital recital : recitalList) {
            System.out.println(recital.getRecitalDetails());
        }
        assert !recitalList.isEmpty();
    }
}