package org.upgrad.upstac.common;

import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.consultation.CreateConsultationRequest;
import org.upgrad.upstac.testrequests.consultation.DoctorSuggestion;
import org.upgrad.upstac.testrequests.lab.CreateLabResult;
import org.upgrad.upstac.testrequests.lab.TestStatus;
import org.upgrad.upstac.users.User;

public class MockedValues {
    public static User createUser() {
        User user = new User();
        user.setId(1L);
        user.setUserName("someuser");
        return user;
    }

    public static CreateLabResult getCreateLabResult(TestRequest testRequest) {
        CreateLabResult createLabResult = new CreateLabResult();
        createLabResult.setBloodPressure("100");
        createLabResult.setComments("Normal");
        createLabResult.setHeartBeat("78");
        createLabResult.setOxygenLevel("96");
        createLabResult.setTemperature("37.6C");
        createLabResult.setResult(testRequest.getLabResult().getResult());

        return createLabResult;
    }
    public static CreateConsultationRequest getCreateConsultationRequest(TestRequest testRequest) {
        CreateConsultationRequest content = new CreateConsultationRequest();
        if (testRequest.getLabResult().getResult() == TestStatus.NEGATIVE) {
            content.setSuggestion(DoctorSuggestion.NO_ISSUES);
            content.setComments("Ok");
        } else if (testRequest.getLabResult().getResult() == TestStatus.POSITIVE) {
            content.setSuggestion(DoctorSuggestion.HOME_QUARANTINE);
            content.setComments("Stay at home all the time, wear the mask and take medicine on time and check oxygen level all the time");
        }
        return content;

    }
}
