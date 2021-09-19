package org.upgrad.upstac.testrequests;

import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.mocked.MockedValues;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.consultation.Consultation;
import org.upgrad.upstac.testrequests.consultation.ConsultationController;
import org.upgrad.upstac.testrequests.consultation.CreateConsultationRequest;
import org.upgrad.upstac.testrequests.consultation.DoctorSuggestion;
import org.upgrad.upstac.testrequests.lab.CreateLabResult;
import org.upgrad.upstac.testrequests.lab.TestStatus;
import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequestQueryService;
import org.upgrad.upstac.users.User;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import java.util.HashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Slf4j
class ConsultationControllerTest {


    @InjectMocks
    ConsultationController consultationController;


    @Mock
    TestRequestQueryService testRequestQueryService;

    @Mock
    TestRequestUpdateService testRequestUpdateService;

    @Mock
    UserLoggedInService userLoggedInService;


    @Test
    @WithUserDetails(value = "doctor")
    public void calling_assignForConsultation_with_valid_test_request_id_should_update_the_request_status() {

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_COMPLETED);
        TestRequest expectedTestRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);
        expectedTestRequest.setConsultation(MockedValues.getMockedConsulation(testRequest, DoctorSuggestion.NO_ISSUES));
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(MockedValues.getMockedUser(MockedValues.getRoles("doctor")));
        Mockito.when(testRequestUpdateService.assignForConsultation(Mockito.anyLong(), Mockito.any())).thenReturn(expectedTestRequest);

        TestRequest updatedTestRequest = consultationController.assignForConsultation(testRequest.getRequestId());
        assertEquals(testRequest.getRequestId(), updatedTestRequest.getRequestId());
        assertEquals(RequestStatus.DIAGNOSIS_IN_PROCESS, updatedTestRequest.getStatus());
        assertNotNull(updatedTestRequest.getConsultation());


    }

    public TestRequest getTestRequestByStatus(RequestStatus status) {
        return MockedValues.getTestRequest(status);
        //  return testRequestQueryService.findBy(status).stream().findFirst().get();
    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_assignForConsultation_with_valid_test_request_id_should_throw_exception() {

        Long InvalidRequestId = -34L;
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(MockedValues.getMockedUser(MockedValues.getRoles("doctor")));
        Mockito.when(testRequestUpdateService.assignForConsultation(Mockito.anyLong(), Mockito.any())).thenThrow(new AppException("Invalid ID"));
        ResponseStatusException actualException = assertThrows(ResponseStatusException.class, () -> {
            consultationController.assignForConsultation(InvalidRequestId);
        });
        assertThat(actualException.getMessage(), containsString("Invalid ID"));
    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_valid_test_request_id_should_update_the_request_status_and_update_consultation_details() {

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);
        testRequest.setLabResult(MockedValues.getMockedLabResult(testRequest));
        testRequest.setConsultation(MockedValues.getMockedConsulation(testRequest, DoctorSuggestion.NO_ISSUES));
        TestRequest expectedTestRequest = getTestRequestByStatus(RequestStatus.COMPLETED);
        CreateConsultationRequest createConsultationRequest = getCreateConsultationRequest(testRequest);
        User user = MockedValues.getMockedUser(MockedValues.getRoles("doctor"));
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);
        Mockito.when(testRequestUpdateService.updateConsultation(testRequest.getRequestId(), createConsultationRequest, user)).thenReturn(expectedTestRequest);

        TestRequest updateTestRequest = consultationController.updateConsultation(testRequest.requestId, createConsultationRequest);

        assertEquals(testRequest.getRequestId(), updateTestRequest.getRequestId());
        assertEquals(updateTestRequest.getStatus(), RequestStatus.COMPLETED);
        assertEquals(createConsultationRequest.getSuggestion(), testRequest.getConsultation().getSuggestion());

    }


    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_invalid_test_request_id_should_throw_exception() {

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);
        testRequest.setLabResult(MockedValues.getMockedLabResult(testRequest));
        CreateConsultationRequest createConsultationRequest = getCreateConsultationRequest(testRequest);
        User user = MockedValues.getMockedUser(MockedValues.getRoles("doctor"));
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);
        Mockito.when(testRequestUpdateService.updateConsultation(Mockito.anyLong(), Mockito.any(), Mockito.any())).thenThrow(new AppException("Invalid ID"));

        ResponseStatusException actualException = assertThrows(ResponseStatusException.class, () -> {
            consultationController.updateConsultation(-10L, createConsultationRequest);
        });
        assertThat(actualException.getMessage(), containsString("Invalid ID"));

    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_invalid_empty_status_should_throw_exception() {

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);
        testRequest.setLabResult(MockedValues.getMockedLabResult(testRequest));
        CreateConsultationRequest createConsultationRequest = getCreateConsultationRequest(testRequest);
        createConsultationRequest.setSuggestion(null);

        User user = MockedValues.getMockedUser(MockedValues.getRoles("doctor"));
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);
        Mockito.when(testRequestUpdateService.updateConsultation(testRequest.getRequestId(), createConsultationRequest, user)).thenThrow(new ConstraintViolationException("suggestion can't be null",new HashSet<>()));

        ResponseStatusException actualException = assertThrows(ResponseStatusException.class, () -> {
            consultationController.updateConsultation(testRequest.requestId, createConsultationRequest);
        });
        assertThat(actualException.getMessage(), containsString("ConstraintViolationException"));
    }

    public CreateConsultationRequest getCreateConsultationRequest(TestRequest testRequest) {
        CreateConsultationRequest createConsultationRequest = new CreateConsultationRequest();
        if (testRequest.getLabResult().getResult() == TestStatus.NEGATIVE) {
            createConsultationRequest.setSuggestion(DoctorSuggestion.NO_ISSUES);
            createConsultationRequest.setComments("ok");
        } else {
            createConsultationRequest.setSuggestion(DoctorSuggestion.HOME_QUARANTINE);
            createConsultationRequest.setComments("stay at home. Take medicine as mentioned in notes and keep monitoring your temperature and oxygen level");
        }
        return createConsultationRequest;

    }

}