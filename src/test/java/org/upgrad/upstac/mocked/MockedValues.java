package org.upgrad.upstac.mocked;

import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.consultation.Consultation;
import org.upgrad.upstac.testrequests.consultation.DoctorSuggestion;
import org.upgrad.upstac.testrequests.lab.LabResult;
import org.upgrad.upstac.testrequests.lab.TestStatus;
import org.upgrad.upstac.users.User;
import org.upgrad.upstac.users.models.AccountStatus;
import org.upgrad.upstac.users.models.Gender;
import org.upgrad.upstac.users.roles.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/*
    Consultation consultation;

    @OneToOne(mappedBy="request")
    LabResult labResult;
 */
public class MockedValues {
    public static TestRequest getTestRequest(RequestStatus status) {
        TestRequest request = new TestRequest();
        request.setRequestId(232323L);
        request.setCreated(LocalDate.now());
        request.setName("Test");
        request.setGender(Gender.MALE);
        request.setAge(34);
        request.setAddress("DL,IND");
        request.setEmail("t@g.com");
        request.setPhoneNumber("2323232323");
        request.setPinCode(123232);
        request.setStatus(status);
        request.setCreatedBy(getMockedUser(getRoles("user")));
        return request;
    }

    public static Set<Role> getRoles(String userType) {
        Role doctor = new Role();
        doctor.setName("DOCTOR");
        doctor.setId(123L);
        Role user = new Role();
        user.setName("USER");
        user.setId(124L);
        Role tester = new Role();
        tester.setName("TESTER");
        tester.setId(125L);
        Role authority = new Role();
        authority.setName("AUTHORITY");
        authority.setId(126L);
        switch (userType.toLowerCase(Locale.ROOT)) {
            case "doctor":
                Set<Role> doctorRole = new HashSet<Role>();
                doctorRole.add(doctor);
                doctorRole.add(user);
                return doctorRole;
            case "user":
                Set<Role> userRole = new HashSet<Role>();
                userRole.add(user);
                return userRole;
            case "tester":
                Set<Role> testerRole = new HashSet<Role>();
                testerRole.add(tester);
                testerRole.add(user);
                return testerRole;
            case "authority":
                Set<Role> authorityRole = new HashSet<Role>();
                authorityRole.add(authority);
                authorityRole.add(user);
                return authorityRole;
            default:
                Set<Role> defaultRole = new HashSet<Role>();
                defaultRole.add(user);
                return defaultRole;
        }
    }

    public static User getMockedUser(Set<Role> roles) {
        User user = new User();
        user.setId(100L);
        user.setUserName("test123");
        user.setPassword("12344");
        user.setEmail("test@gmail.com");
        user.setAddress("DL,IND");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setStatus(AccountStatus.APPROVED);
        user.setPhoneNumber("123434343");
        user.setCreated(LocalDateTime.now());
        user.setDateOfBirth(LocalDate.of(1988, 05, 05));
        user.setGender(Gender.MALE);
        user.setPinCode(234567);
        user.setRoles(roles);
        return user;
    }

    public static Consultation getMockedConsulation(TestRequest testRequest, DoctorSuggestion suggestion) {
        Consultation consultation = new Consultation();
        consultation.setId(22434L);
        consultation.setDoctor(getMockedUser(getRoles("doctor")));
        consultation.setRequest(testRequest);
        consultation.setUpdatedOn(LocalDate.now());
        consultation.setSuggestion(suggestion);
        return consultation;
    }

    public static LabResult getMockedLabResult(TestRequest request) {
        LabResult labResult = new LabResult();
        labResult.setResultId(13223L);
        labResult.setRequest(request);
        labResult.setBloodPressure("123");
        labResult.setHeartBeat("79");
        labResult.setTemperature("35.6C");
        labResult.setOxygenLevel("97");
        labResult.setComments("Normal");
        labResult.setResult(TestStatus.NEGATIVE);
        labResult.setUpdatedOn(LocalDate.now());
        labResult.setTester(getMockedUser(getRoles("tester")));
        return labResult;
    }
}
