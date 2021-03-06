package ca.bc.gov.open.jag.efilingapi.submission.submissionApiDelegateImpl;

import ca.bc.gov.open.jag.efilingapi.Keys;
import ca.bc.gov.open.clamav.starter.ClamAvService;
import ca.bc.gov.open.jag.efilingapi.TestHelpers;
import ca.bc.gov.open.jag.efilingapi.account.service.AccountService;
import ca.bc.gov.open.jag.efilingapi.api.model.*;
import ca.bc.gov.open.jag.efilingapi.config.NavigationProperties;
import ca.bc.gov.open.jag.efilingapi.document.DocumentStore;
import ca.bc.gov.open.jag.efilingapi.error.ErrorResponse;
import ca.bc.gov.open.jag.efilingapi.submission.SubmissionApiDelegateImpl;
import ca.bc.gov.open.jag.efilingapi.submission.mappers.FilingPackageMapper;
import ca.bc.gov.open.jag.efilingapi.submission.mappers.FilingPackageMapperImpl;
import ca.bc.gov.open.jag.efilingapi.submission.mappers.GenerateUrlResponseMapperImpl;
import ca.bc.gov.open.jag.efilingapi.submission.models.Submission;
import ca.bc.gov.open.jag.efilingapi.submission.service.SubmissionService;
import ca.bc.gov.open.jag.efilingapi.submission.service.SubmissionStore;
import ca.bc.gov.open.jag.efilingapi.submission.validator.GenerateUrlRequestValidator;
import ca.bc.gov.open.jag.efilingapi.utils.Notification;
import ca.bc.gov.open.jag.efilingcommons.exceptions.CSOHasMultipleAccountException;
import ca.bc.gov.open.jag.efilingcommons.exceptions.EfilingDocumentServiceException;
import ca.bc.gov.open.jag.efilingcommons.exceptions.InvalidAccountStateException;
import ca.bc.gov.open.jag.efilingcommons.exceptions.StoreException;
import org.junit.jupiter.api.*;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.validation.Valid;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static ca.bc.gov.open.jag.efilingapi.error.ErrorResponse.INVALIDUNIVERSAL;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("SubmissionApiDelegateImpl test suite")
public class GenerateUrlTest {

    private static final String CODE = "CODE";
    private static final String CLIENT_APP_NAME = "clientAppName";
    private static final UUID USER_WITH_CSO_ACCOUNT = UUID.fromString("1593769b-ac4b-43d9-9e81-38877eefcca5");
    private static final UUID USER_WITH_NO_CSO_ACCOUNT = UUID.fromString("1593769b-ac4b-43d9-9e81-38877eefcca6");


    private SubmissionApiDelegateImpl sut;

    @Mock
    private SubmissionService submissionServiceMock;

    @Mock
    private SubmissionStore submissionStoreMock;

    @Mock
    private DocumentStore documentStoreMock;

    @Mock
    private AccountService accountServiceMock;

    @Mock
    private SecurityContext securityContextMock;

    @Mock
    private Authentication authenticationMock;

    @Mock
    private KeycloakPrincipal keycloakPrincipalMock;

    @Mock
    private KeycloakSecurityContext keycloakSecurityContextMock;

    @Mock
    private AccessToken tokenMock;


    @Mock
    private ClamAvService clamAvServiceMock;
    private UUID transactionId = UUID.randomUUID();

    @Mock
    private GenerateUrlRequestValidator generateUrlRequestValidatorMock;


    @BeforeAll
    public void setUp() {

        MockitoAnnotations.initMocks(this);

        Mockito.when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        Mockito.when(authenticationMock.getPrincipal()).thenReturn(keycloakPrincipalMock);
        Mockito.when(keycloakPrincipalMock.getKeycloakSecurityContext()).thenReturn(keycloakSecurityContextMock);
        Mockito.when(keycloakSecurityContextMock.getToken()).thenReturn(tokenMock);

        SecurityContextHolder.setContext(securityContextMock);

        NavigationProperties navigationProperties = new NavigationProperties();
        navigationProperties.setBaseUrl("http://localhost");

        Submission submission = Submission.builder().id(TestHelpers.CASE_1).transactionId(transactionId).expiryDate(10).create();

        Mockito.when(accountServiceMock.getCsoAccountDetails(Mockito.eq(USER_WITH_CSO_ACCOUNT))).thenReturn(TestHelpers.createCSOAccountDetails(true));
        Mockito.when(accountServiceMock.getCsoAccountDetails(Mockito.eq(USER_WITH_NO_CSO_ACCOUNT))).thenReturn(null);

        Mockito
                .when(submissionServiceMock.generateFromRequest(
                        ArgumentMatchers.argThat(x -> x.getSubmissionId().equals(TestHelpers.CASE_1)),
                        Mockito.any()))
                .thenReturn(submission);

        Mockito.doThrow(new CSOHasMultipleAccountException("CSOHasMultipleAccountException message"))
                .when(submissionServiceMock).generateFromRequest(
                ArgumentMatchers.argThat(x -> x.getSubmissionId().equals(TestHelpers.CASE_2)),
                Mockito.any());

        Mockito.doThrow(new InvalidAccountStateException("InvalidAccountStateException message"))
                .when(submissionServiceMock).generateFromRequest(
                ArgumentMatchers.argThat(x -> x.getSubmissionId().equals(TestHelpers.CASE_3)),
                Mockito.any());

        Mockito.doThrow(new StoreException("StoreException message"))
                .when(submissionServiceMock).generateFromRequest(
                ArgumentMatchers.argThat(x -> x.getSubmissionId().equals(TestHelpers.CASE_4)),
                Mockito.any());

        Mockito.doThrow(new EfilingDocumentServiceException("EfilingDocumentServiceException message"))
                .when(submissionServiceMock).generateFromRequest(
                ArgumentMatchers.argThat(x -> x.getSubmissionId().equals(TestHelpers.CASE_5)),
                Mockito.any());

        Notification notification = new Notification();
        Mockito.doReturn(notification).when(generateUrlRequestValidatorMock)
                .validate(
                        ArgumentMatchers.argThat(x -> x.getFilingPackage().getCourt().getLocation().equals("valid")),
                        Mockito.anyString());

        Notification invalidNotification = new Notification();
        invalidNotification.addError("a random error");
        Mockito.doReturn(invalidNotification).when(generateUrlRequestValidatorMock).validate(
                ArgumentMatchers.argThat(x -> x.getFilingPackage().getCourt().getLocation().equals("invalid")),
                Mockito.anyString());

        FilingPackageMapper filingPackageMapper = new FilingPackageMapperImpl();
        sut = new SubmissionApiDelegateImpl(submissionServiceMock, accountServiceMock, new GenerateUrlResponseMapperImpl(), navigationProperties, submissionStoreMock, documentStoreMock, clamAvServiceMock, filingPackageMapper, generateUrlRequestValidatorMock);

    }


    @Test
    @DisplayName("200: Valid request should generate a URL")
    public void withValidRequestShouldGenerateUrl() {

        @Valid GenerateUrlRequest generateUrlRequest = new GenerateUrlRequest();

        Map<String, Object> otherClaims = new HashMap<>();
        otherClaims.put(Keys.CSO_APPLICATION_CODE, CODE);
        Mockito.when(tokenMock.getOtherClaims()).thenReturn(otherClaims);

        generateUrlRequest.setClientAppName(CLIENT_APP_NAME);
        generateUrlRequest.setNavigationUrls(TestHelpers.createNavigation(TestHelpers.SUCCESS_URL, TestHelpers.CANCEL_URL, TestHelpers.ERROR_URL));
        generateUrlRequest.setNavigationUrls(TestHelpers.createNavigation(TestHelpers.SUCCESS_URL, TestHelpers.CANCEL_URL, TestHelpers.ERROR_URL));
        InitialPackage initialPackage = new InitialPackage();
        CourtBase court = new CourtBase();
        court.setLocation("valid");
        initialPackage.setCourt(court);
        generateUrlRequest.setFilingPackage(initialPackage);

        ResponseEntity<GenerateUrlResponse> actual = sut.generateUrl(transactionId, USER_WITH_CSO_ACCOUNT.toString().replace("-", ""), TestHelpers.CASE_1, generateUrlRequest);

        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertEquals("http://localhost?submissionId=" + TestHelpers.CASE_1.toString() + "&transactionId="  + transactionId, actual.getBody().getEfilingUrl());
        Assertions.assertNotNull(actual.getBody().getExpiryDate());

    }


    @Test
    @DisplayName("200: with user having no CSO account should return a valid url")
    public void withUserHavingNoCSOAccountShouldReturnValidUrl() {

        @Valid GenerateUrlRequest generateUrlRequest = new GenerateUrlRequest();

        Map<String, Object> otherClaims = new HashMap<>();
        otherClaims.put(Keys.CSO_APPLICATION_CODE, CODE);
        Mockito.when(tokenMock.getOtherClaims()).thenReturn(otherClaims);

        generateUrlRequest.setClientAppName(CLIENT_APP_NAME);
        generateUrlRequest.setNavigationUrls(TestHelpers.createNavigation(TestHelpers.SUCCESS_URL, TestHelpers.CANCEL_URL, TestHelpers.ERROR_URL));
        generateUrlRequest.setNavigationUrls(TestHelpers.createNavigation(TestHelpers.SUCCESS_URL, TestHelpers.CANCEL_URL, TestHelpers.ERROR_URL));
        InitialPackage initialPackage = new InitialPackage();
        CourtBase court = new CourtBase();
        court.setLocation("valid");
        initialPackage.setCourt(court);
        generateUrlRequest.setFilingPackage(initialPackage);

        ResponseEntity<GenerateUrlResponse> actual = sut.generateUrl(transactionId, USER_WITH_NO_CSO_ACCOUNT.toString().replace("-", ""), TestHelpers.CASE_1, generateUrlRequest);

        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertEquals("http://localhost?submissionId=" + TestHelpers.CASE_1.toString() + "&transactionId="  + transactionId, actual.getBody().getEfilingUrl());
        Assertions.assertNotNull(actual.getBody().getExpiryDate());

    }

    @Test
    @DisplayName("400: with initialPackage validation failure should return 400")
    public void whenInitialPackageValidationFailureShouldReturn() {

        @Valid GenerateUrlRequest generateUrlRequest = new GenerateUrlRequest();

        Map<String, Object> otherClaims = new HashMap<>();
        otherClaims.put(Keys.CSO_APPLICATION_CODE, CODE);
        Mockito.when(tokenMock.getOtherClaims()).thenReturn(otherClaims);

        generateUrlRequest.setClientAppName(CLIENT_APP_NAME);
        generateUrlRequest.setNavigationUrls(TestHelpers.createNavigation(TestHelpers.SUCCESS_URL, TestHelpers.CANCEL_URL, TestHelpers.ERROR_URL));
        InitialPackage initialPackage = new InitialPackage();
        CourtBase court = new CourtBase();
        court.setLocation("invalid");
        initialPackage.setCourt(court);
        generateUrlRequest.setFilingPackage(initialPackage);

        ResponseEntity actual = sut.generateUrl(transactionId, USER_WITH_NO_CSO_ACCOUNT.toString().replace("-", ""), TestHelpers.CASE_1, generateUrlRequest);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());

        EfilingError actualError = (EfilingError) actual.getBody();

        Assertions.assertEquals("Initial Submission payload invalid, find more in the details array.", actualError.getMessage());
        Assertions.assertEquals("INVALID_INITIAL_SUBMISSION_PAYLOAD", actualError.getError());
        Assertions.assertEquals("a random error", actualError.getDetails().get(0));

    }

    @Test
    @DisplayName("400: when CSOHasMultipleAccountException should return Bad Request")
    public void whenCSOHasMultipleAccountExceptionShouldReturnBadRequest() {
        @Valid GenerateUrlRequest generateUrlRequest = new GenerateUrlRequest();

        generateUrlRequest.setClientAppName(CLIENT_APP_NAME);
        generateUrlRequest.setNavigationUrls(TestHelpers.createNavigation(TestHelpers.SUCCESS_URL, TestHelpers.CANCEL_URL, TestHelpers.ERROR_URL));
        InitialPackage initialPackage = new InitialPackage();
        CourtBase court = new CourtBase();
        court.setLocation("valid");
        initialPackage.setCourt(court);
        generateUrlRequest.setFilingPackage(initialPackage);

        ResponseEntity actual = sut.generateUrl(UUID.randomUUID(), UUID.randomUUID().toString().replace("-", ""), TestHelpers.CASE_2, generateUrlRequest);

        EfilingError actualError = (EfilingError) actual.getBody();

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
        Assertions.assertEquals(ErrorResponse.ACCOUNTEXCEPTION.getErrorCode(), actualError.getError());
        Assertions.assertEquals(ErrorResponse.ACCOUNTEXCEPTION.getErrorMessage(), actualError.getMessage());
    }

    @Test
    @DisplayName("403: when InvalidAccountStateException should return FORBIDDEN")
    public void whenInvalidAccountStateExceptionShouldReturnForbidden() {
        @Valid GenerateUrlRequest generateUrlRequest = new GenerateUrlRequest();

        generateUrlRequest.setClientAppName(CLIENT_APP_NAME);
        generateUrlRequest.setNavigationUrls(TestHelpers.createNavigation(TestHelpers.SUCCESS_URL, TestHelpers.CANCEL_URL, TestHelpers.ERROR_URL));
        InitialPackage initialPackage = new InitialPackage();
        CourtBase court = new CourtBase();
        court.setLocation("valid");
        initialPackage.setCourt(court);
        generateUrlRequest.setFilingPackage(initialPackage);

        ResponseEntity actual = sut.generateUrl(UUID.randomUUID(), UUID.randomUUID().toString().replace("-", ""), TestHelpers.CASE_3, generateUrlRequest);

        EfilingError actualError = (EfilingError) actual.getBody();

        Assertions.assertEquals(HttpStatus.FORBIDDEN, actual.getStatusCode());
        Assertions.assertEquals(ErrorResponse.INVALIDROLE.getErrorCode(), actualError.getError());
        Assertions.assertEquals(ErrorResponse.INVALIDROLE.getErrorMessage(), actualError.getMessage());
    }

    @Test
    @DisplayName("403: when EFileRole not present should return FORBIDDEN")
    public void whenEFileRoleNotPresentShouldReturnForbidden() {
        @Valid GenerateUrlRequest generateUrlRequest = new GenerateUrlRequest();

        Map<String, Object> otherClaims = new HashMap<>();
        otherClaims.put(Keys.CSO_APPLICATION_CODE, CODE);
        Mockito.when(tokenMock.getOtherClaims()).thenReturn(otherClaims);

        Mockito.when(accountServiceMock.getCsoAccountDetails(any())).thenReturn(TestHelpers.createCSOAccountDetails(false));

        generateUrlRequest.setClientAppName(CLIENT_APP_NAME);
        generateUrlRequest.setNavigationUrls(TestHelpers.createNavigation(TestHelpers.SUCCESS_URL, TestHelpers.CANCEL_URL, TestHelpers.ERROR_URL));
        InitialPackage initialPackage = new InitialPackage();
        CourtBase court = new CourtBase();
        court.setLocation("valid");
        initialPackage.setCourt(court);
        generateUrlRequest.setFilingPackage(initialPackage);

        ResponseEntity<GenerateUrlResponse> actual = sut.generateUrl(transactionId, UUID.randomUUID().toString().replace("-", ""), TestHelpers.CASE_1, generateUrlRequest);

        Assertions.assertEquals(HttpStatus.FORBIDDEN, actual.getStatusCode());
    }

    @Test
    @DisplayName("500: when StoreException should return INTERNAL SERVER ERROR")
    public void whenStoreExceptionShouldReturnInternalServerError() {
        @Valid GenerateUrlRequest generateUrlRequest = new GenerateUrlRequest();

        generateUrlRequest.setClientAppName(CLIENT_APP_NAME);
        generateUrlRequest.setNavigationUrls(TestHelpers.createNavigation(TestHelpers.SUCCESS_URL, TestHelpers.CANCEL_URL, TestHelpers.ERROR_URL));
        InitialPackage initialPackage = new InitialPackage();
        CourtBase court = new CourtBase();
        court.setLocation("valid");
        initialPackage.setCourt(court);
        generateUrlRequest.setFilingPackage(initialPackage);

        ResponseEntity actual = sut.generateUrl(UUID.randomUUID(), UUID.randomUUID().toString().replace("-", ""), TestHelpers.CASE_4, generateUrlRequest);

        EfilingError actualError = (EfilingError) actual.getBody();

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode());
        Assertions.assertEquals(ErrorResponse.CACHE_ERROR.getErrorCode(), actualError.getError());
        Assertions.assertEquals(ErrorResponse.CACHE_ERROR.getErrorMessage(), actualError.getMessage());
    }

    @Test
    @DisplayName("500: when DocumentException should return INTERNAL SERVER ERROR")
    public void whenDocumentExceptionShouldReturnInternalServerError() {
        @Valid GenerateUrlRequest generateUrlRequest = new GenerateUrlRequest();

        generateUrlRequest.setClientAppName(CLIENT_APP_NAME);
        generateUrlRequest.setNavigationUrls(TestHelpers.createNavigation(TestHelpers.SUCCESS_URL, TestHelpers.CANCEL_URL, TestHelpers.ERROR_URL));
        InitialPackage initialPackage = new InitialPackage();
        CourtBase court = new CourtBase();
        court.setLocation("valid");
        initialPackage.setCourt(court);
        generateUrlRequest.setFilingPackage(initialPackage);

        ResponseEntity actual = sut.generateUrl(UUID.randomUUID(), UUID.randomUUID().toString().replace("-", ""), TestHelpers.CASE_5, generateUrlRequest);

        EfilingError actualError = (EfilingError) actual.getBody();

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode());
        Assertions.assertEquals(ErrorResponse.DOCUMENT_TYPE_ERROR.getErrorCode(), actualError.getError());
        Assertions.assertEquals(ErrorResponse.DOCUMENT_TYPE_ERROR.getErrorMessage(), actualError.getMessage());
    }

    @Test
    @DisplayName("403: with invalid userId then return forbidden 403")
    public void withInvalidUserIDThenReturnForbidden() {


        ResponseEntity actual = sut.generateUrl(UUID.randomUUID(), "BADUUID", UUID.randomUUID(), null);

        Assertions.assertEquals(HttpStatus.FORBIDDEN, actual.getStatusCode());
        Assertions.assertEquals(INVALIDUNIVERSAL.getErrorCode(), ((EfilingError)actual.getBody()).getError());
        Assertions.assertEquals(INVALIDUNIVERSAL.getErrorMessage(), ((EfilingError)actual.getBody()).getMessage());
    }


}

