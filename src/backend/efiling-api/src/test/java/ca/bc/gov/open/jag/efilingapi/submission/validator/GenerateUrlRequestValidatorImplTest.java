package ca.bc.gov.open.jag.efilingapi.submission.validator;

import ca.bc.gov.open.jag.efilingapi.api.model.CourtBase;
import ca.bc.gov.open.jag.efilingapi.api.model.GenerateUrlRequest;
import ca.bc.gov.open.jag.efilingapi.api.model.InitialPackage;
import ca.bc.gov.open.jag.efilingapi.api.model.Party;
import ca.bc.gov.open.jag.efilingapi.court.services.CourtService;
import ca.bc.gov.open.jag.efilingapi.submission.service.SubmissionService;
import ca.bc.gov.open.jag.efilingapi.utils.Notification;
import ca.bc.gov.open.jag.efilingcommons.model.CourtDetails;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GenerateUrlRequestValidatorImplTest {

    private static final String COURT_CLASSIFICATION = "COURT_CLASSIFICATION";
    private static final String[] ROLE_TYPES = new String[] { "ADJ", "CIT" };
    private static final String COURT_LEVEL = "COURT_LEVEL";
    private static final String APPLICATION_CODE = "app_code";
    private static final String COURT_DESCRIPTION = "courtDescription";
    private static final String CLASS_DESCRIPTION = "classDescription";
    private static final String LEVEL_DESCRIPTION = "levelDescription";
    private static final BigDecimal COURT_ID = BigDecimal.ONE;
    private static final String CASE_1 = "CASE1";
    private static final String CASE_2 = "case2";
    private static final BigDecimal COURT_ID_2 = BigDecimal.TEN;

    private GenerateUrlRequestValidatorImpl sut;

    @Mock
    private SubmissionService submissionService;

    @Mock
    private CourtService courtServiceMock;

    @BeforeEach
    public void setup() {

        MockitoAnnotations.initMocks(this);

        Mockito.when(submissionService
                .getValidPartyRoles(
                        ArgumentMatchers.argThat(x -> x.getCourtClassification().equals(COURT_CLASSIFICATION))))
                .thenReturn(Arrays.asList(ROLE_TYPES));

        CourtDetails courtDetails = new CourtDetails(COURT_ID, COURT_DESCRIPTION, CLASS_DESCRIPTION, LEVEL_DESCRIPTION);

        Mockito
                .when(courtServiceMock.getCourtDetails(ArgumentMatchers.argThat(x -> x.getCourtLocation().equals(CASE_1))))
                .thenReturn(courtDetails);

        Mockito
                .when(courtServiceMock.isValidCourt(ArgumentMatchers.argThat(x -> x.getCourtId().equals(COURT_ID)))).thenReturn(true);

        CourtDetails courtDetails2 = new CourtDetails(COURT_ID_2, COURT_DESCRIPTION, CLASS_DESCRIPTION, LEVEL_DESCRIPTION);

        Mockito
                .when(courtServiceMock.getCourtDetails(ArgumentMatchers.argThat(x -> x.getCourtLocation().equals(CASE_2))))
                .thenReturn(courtDetails2);

        Mockito
                .when(courtServiceMock.isValidCourt(ArgumentMatchers.argThat(x -> x.getCourtId().equals(COURT_ID_2)))).thenReturn(false);


        sut = new GenerateUrlRequestValidatorImpl(submissionService, courtServiceMock);

    }

    @Test
    @DisplayName("ok: without error should return a notification without error")
    public void withoutErrorShouldReturnNoError() {

        GenerateUrlRequest generateUrlRequest = new GenerateUrlRequest();
        InitialPackage initialFilingPackage = new InitialPackage();

        CourtBase court = new CourtBase();
        court.setLevel(COURT_LEVEL);
        court.setCourtClass(COURT_CLASSIFICATION);
        court.setLocation(CASE_1);
        initialFilingPackage.setCourt(court);

        List<Party> parties = new ArrayList<>();
        Party party = new Party();
        party.setRoleType(Party.RoleTypeEnum.ADJ);
        parties.add(party);
        Party party2 = new Party();
        party2.setRoleType(Party.RoleTypeEnum.CIT);
        parties.add(party2);
        initialFilingPackage.setParties(parties);

        generateUrlRequest.setFilingPackage(initialFilingPackage);
        Notification actual = sut.validate(generateUrlRequest, APPLICATION_CODE);

        Assertions.assertFalse(actual.hasError());

    }

    @Test
    @DisplayName("error: with invalid court should return an error")
    public void withInvalidCourtShouldReturnError() {

        GenerateUrlRequest generateUrlRequest = new GenerateUrlRequest();
        InitialPackage initialFilingPackage = new InitialPackage();

        CourtBase court = new CourtBase();
        court.setLevel(COURT_LEVEL);
        court.setCourtClass(COURT_CLASSIFICATION);
        court.setLocation(CASE_2);
        initialFilingPackage.setCourt(court);

        List<Party> parties = new ArrayList<>();
        Party party = new Party();
        party.setRoleType(Party.RoleTypeEnum.ADJ);
        parties.add(party);
        Party party2 = new Party();
        party2.setRoleType(Party.RoleTypeEnum.CIT);
        parties.add(party2);
        initialFilingPackage.setParties(parties);

        generateUrlRequest.setFilingPackage(initialFilingPackage);
        Notification actual = sut.validate(generateUrlRequest, APPLICATION_CODE);

        Assertions.assertFalse(actual.hasError());

    }

    @Test
    @DisplayName("Error: without filing package should return an error")
    public void withoutFilingPackageShouldReturnError() {

        GenerateUrlRequest generateUrlRequest = new GenerateUrlRequest();

        Notification actual = sut.validate(generateUrlRequest, APPLICATION_CODE);

        Assertions.assertTrue(actual.hasError());
        Assertions.assertEquals("Initial Package is required.", actual.getErrors().get(0));

    }


    @Test
    @DisplayName("Error: without filing package should return an error")
    public void withoutFilingPackageShouldReturnError2() {

        GenerateUrlRequest generateUrlRequest = new GenerateUrlRequest();
        InitialPackage initialFilingPackage = new InitialPackage();

        CourtBase court = new CourtBase();
        court.setLevel(COURT_LEVEL);
        court.setCourtClass(COURT_CLASSIFICATION);
        initialFilingPackage.setCourt(court);

        generateUrlRequest.setFilingPackage(initialFilingPackage);
        Notification actual = sut.validate(generateUrlRequest, APPLICATION_CODE);

        Assertions.assertTrue(actual.hasError());
        Assertions.assertEquals("At least 1 party is required for new submission.", actual.getErrors().get(0));

    }

    @Test
    @DisplayName("error: with role type not in list should return multiple errors")
    public void withRoleTypeNotInListShouldReturnMultipleErrors() {

        GenerateUrlRequest generateUrlRequest = new GenerateUrlRequest();
        InitialPackage initialFilingPackage = new InitialPackage();

        CourtBase court = new CourtBase();
        court.setLevel(COURT_LEVEL);
        court.setCourtClass(COURT_CLASSIFICATION);
        initialFilingPackage.setCourt(court);

        List<Party> parties = new ArrayList<>();
        Party party = new Party();
        party.setRoleType(Party.RoleTypeEnum.CAV);
        parties.add(party);
        Party party2 = new Party();
        party2.setRoleType(Party.RoleTypeEnum.DEO);
        parties.add(party2);
        initialFilingPackage.setParties(parties);

        generateUrlRequest.setFilingPackage(initialFilingPackage);
        Notification actual = sut.validate(generateUrlRequest, APPLICATION_CODE);

        Assertions.assertTrue(actual.hasError());
        Assertions.assertEquals("Role type [CAV] is invalid.", actual.getErrors().get(0));
        Assertions.assertEquals("Role type [DEO] is invalid.", actual.getErrors().get(1));

    }

    @Test
    @DisplayName("error: with role type Null should return error")
    public void withRoleTypeNullNotInListShouldReturnMultipleErrors() {

        GenerateUrlRequest generateUrlRequest = new GenerateUrlRequest();
        InitialPackage initialFilingPackage = new InitialPackage();

        CourtBase court = new CourtBase();
        court.setLevel(COURT_LEVEL);
        court.setCourtClass(COURT_CLASSIFICATION);
        initialFilingPackage.setCourt(court);

        List<Party> parties = new ArrayList<>();
        Party party = new Party();
        parties.add(party);
        initialFilingPackage.setParties(parties);

        generateUrlRequest.setFilingPackage(initialFilingPackage);
        Notification actual = sut.validate(generateUrlRequest, APPLICATION_CODE);

        Assertions.assertTrue(actual.hasError());
        Assertions.assertEquals("Role type [null] is invalid.", actual.getErrors().get(0));

    }

}
