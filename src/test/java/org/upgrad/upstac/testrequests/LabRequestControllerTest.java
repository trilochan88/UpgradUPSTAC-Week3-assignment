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
import org.upgrad.upstac.testrequests.lab.CreateLabResult;
import org.upgrad.upstac.testrequests.lab.LabRequestController;
import org.upgrad.upstac.testrequests.lab.LabResult;
import org.upgrad.upstac.testrequests.lab.TestStatus;
import org.upgrad.upstac.users.User;

import javax.validation.ConstraintViolationException;

import java.util.HashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
@Slf4j
class LabRequestControllerTest {


    @InjectMocks
    LabRequestController labRequestController;

    @Mock
    TestRequestUpdateService testRequestUpdateService;


    @Mock
    TestRequestQueryService testRequestQueryService;
    @Mock
    UserLoggedInService userLoggedInService;

    @Test
    @WithUserDetails(value = "tester")
    public void calling_assignForLabTest_with_valid_test_request_id_should_update_the_request_status() {

        TestRequest testRequest = MockedValues.getTestRequest(RequestStatus.INITIATED);
        User mockedUser = MockedValues.getMockedUser(MockedValues.getRoles("tester"));
        when(userLoggedInService.getLoggedInUser()).thenReturn(mockedUser);

        TestRequest expectedRequest = MockedValues.getTestRequest(RequestStatus.LAB_TEST_IN_PROGRESS);
        expectedRequest.setLabResult(MockedValues.getMockedLabResult(testRequest));
        when(testRequestUpdateService.assignForLabTest(testRequest.getRequestId(), mockedUser)).thenReturn(expectedRequest);

        TestRequest updatedTestRequest = labRequestController.assignForLabTest(testRequest.getRequestId());

        assertThat(updatedTestRequest.getRequestId(), equalTo(testRequest.getRequestId()));
        assertThat(updatedTestRequest.getStatus(), equalTo(RequestStatus.LAB_TEST_IN_PROGRESS));
        assertNotNull(updatedTestRequest.getLabResult());


    }

    public TestRequest getTestRequestByStatus(RequestStatus status) {
        return testRequestQueryService.findBy(status).stream().findFirst().get();
    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_assignForLabTest_with_valid_test_request_id_should_throw_exception() {

        Long InvalidRequestId = -34L;
      /*  when(testRequestUpdateService.assignForLabTest(anyLong(), any(User.class))).thenThrow(new AppException("Invalid ID"));
        User mockedUser = MockedValues.getMockedUser(MockedValues.getRoles("tester"));
        when(userLoggedInService.getLoggedInUser()).thenReturn(mockedUser);

        ResponseStatusException actualException = assertThrows(ResponseStatusException.class, () -> {
            labRequestController.assignForLabTest(InvalidRequestId);
        });
        assertThat(actualException.getMessage(), containsString("Invalid ID"));*/

        User mockedUser = MockedValues.getMockedUser(MockedValues.getRoles("tester"));

        when(userLoggedInService.getLoggedInUser()).thenReturn(mockedUser);

        when(testRequestUpdateService.assignForLabTest(anyLong(), any(User.class))).thenThrow(new AppException("Invalid ID"));

        ResponseStatusException actualEx = assertThrows(ResponseStatusException.class, () -> {
            labRequestController.assignForLabTest(InvalidRequestId);
        });

        assertThat(actualEx.getMessage(), containsString("Invalid ID"));

    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_valid_test_request_id_should_update_the_request_status_and_update_test_request_details() {

        TestRequest testRequest = MockedValues.getTestRequest(RequestStatus.LAB_TEST_IN_PROGRESS);
        LabResult mockedLabResult = MockedValues.getMockedLabResult(testRequest);
        User mockedUser = MockedValues.getMockedUser(MockedValues.getRoles("tester"));
        when(userLoggedInService.getLoggedInUser()).thenReturn(mockedUser);
        CreateLabResult createLabResult = getCreateLabResult(testRequest);

        TestRequest expectedTestRequest = MockedValues.getTestRequest(RequestStatus.LAB_TEST_COMPLETED);
        expectedTestRequest.setLabResult(mockedLabResult);
        when(testRequestUpdateService.updateLabTest(testRequest.getRequestId(), createLabResult, mockedUser)).thenReturn(expectedTestRequest);

        TestRequest actualTestReq = labRequestController.updateLabTest(testRequest.getRequestId(), createLabResult);

        assertEquals(testRequest.getRequestId(), actualTestReq.getRequestId());
        assertEquals(RequestStatus.LAB_TEST_COMPLETED, actualTestReq.getStatus());
        assertEquals(expectedTestRequest.getLabResult(), actualTestReq.getLabResult());
    }


    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_invalid_test_request_id_should_throw_exception() {
        //getTestRequestByStatus
        TestRequest testRequest = MockedValues.getTestRequest(RequestStatus.LAB_TEST_IN_PROGRESS);
        CreateLabResult createLabResult = getCreateLabResult(testRequest);

        User mockedUser = MockedValues.getMockedUser(MockedValues.getRoles("tester"));

        when(userLoggedInService.getLoggedInUser()).thenReturn(mockedUser);

        when(testRequestUpdateService.updateLabTest(anyLong(), any(CreateLabResult.class), any(User.class))).thenThrow(new AppException("Invalid ID"));

        ResponseStatusException actualEx = assertThrows(ResponseStatusException.class, () -> {
            labRequestController.updateLabTest(-100L, createLabResult);
        });

        assertThat(actualEx.getMessage(), containsString("Invalid ID"));


    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_invalid_empty_status_should_throw_exception() {

       // TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
TestRequest testRequest = MockedValues.getTestRequest(RequestStatus.LAB_TEST_IN_PROGRESS);
        CreateLabResult createLabResult = getCreateLabResult(testRequest);

        User mockedUser = MockedValues.getMockedUser(MockedValues.getRoles("tester"));

        when(userLoggedInService.getLoggedInUser()).thenReturn(mockedUser);
        createLabResult.setResult(null);

        when(testRequestUpdateService.updateLabTest(anyLong(), any(CreateLabResult.class), any(User.class))).thenThrow(new ConstraintViolationException("ConstraintViolationException",new HashSet<>()));
        ResponseStatusException actualEx = assertThrows(ResponseStatusException.class, () -> {
            labRequestController.updateLabTest(100L, createLabResult);
        });

        assertThat(actualEx.getMessage(), containsString("ConstraintViolationException"));
        //Implement this method

        //Create an object of CreateLabResult and call getCreateLabResult() to create the object. Pass the above created object as the parameter
        // Set the result of the above created object to null.

        // Create an object of ResponseStatusException . Use assertThrows() method and pass updateLabTest() method
        // of labRequestController with request Id of the testRequest object and the above created object as second parameter
        //Refer to the TestRequestControllerTest to check how to use assertThrows() method


        //Use assertThat() method to perform the following comparison
        //  the exception message should be contain the string "ConstraintViolationException"

    }

    public CreateLabResult getCreateLabResult(TestRequest testRequest) {

        //Create an object of CreateLabResult and set all the values
        // Return the object
        LabResult labResult = MockedValues.getMockedLabResult(testRequest);
        CreateLabResult createLabResult = new CreateLabResult();
        createLabResult.setResult(labResult.getResult());
        createLabResult.setComments(labResult.getComments());
        createLabResult.setTemperature(labResult.getTemperature());
        createLabResult.setHeartBeat(labResult.getHeartBeat());
        createLabResult.setBloodPressure(labResult.getBloodPressure());
        createLabResult.setOxygenLevel(labResult.getOxygenLevel());
        return createLabResult;
    }

}