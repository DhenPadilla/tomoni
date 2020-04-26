package com.template.contracts;
import com.template.states.JCTJob;
import com.template.states.JCTJobStatus;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class JobExamples {

    List<String> jobReferences = Arrays.asList("J1","J2","J3");

    public List<JCTJob> getJobExamples() {
        return Arrays.asList(layFoundationJob(), fitFirstFloorJob());
    }

    private JCTJob layFoundationJob(){
        return layFoundationsJobWith(JCTJobStatus.PENDING);
    }

    private JCTJob fitFirstFloorJob() {
        return fitDoorsJobWith(JCTJobStatus.PENDING);
    }

    private JCTJob layFoundationsJobWith(JCTJobStatus status) {
        return new JCTJob(
                jobReferences.get(0),
                "Laying foundation",
                50.0,
                LocalDate.of(2021, 1, 1),
                0.0,
                status);
    }

    private JCTJob fitDoorsJobWith(JCTJobStatus status){
        return new JCTJob(
                jobReferences.get(1),
                "Fitting doors into first floor",
                50.0,
                LocalDate.of(2021, 5, 1),
                0.0,
                status);
    }

}

