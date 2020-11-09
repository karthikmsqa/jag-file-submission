package ca.bc.gov.open.jag.efilingapi.courts;

import ca.bc.gov.open.jag.efilingapi.api.model.Address;
import ca.bc.gov.open.jag.efilingapi.api.model.CourtLocation;
import ca.bc.gov.open.jag.efilingapi.api.model.CourtLocations;
import ca.bc.gov.open.jag.efilingapi.api.model.EfilingError;
import ca.bc.gov.open.jag.efilingceisapiclient.api.handler.ApiException;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.util.Arrays;

import static ca.bc.gov.open.jag.efilingapi.error.ErrorResponse.COURT_LOCATION_ERROR;
import static ca.bc.gov.open.jag.efilingapi.error.ErrorResponse.INVALIDUNIVERSAL;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("CourtsApiDelegateImpl test suite")
public class GetCourtLocationsTest {
    private final String COURTLEVEL = "COURTLEVEL";
    private final String COURTLEVELERROR = "COURTLEVELERROR";
    CourtsApiDelegateImpl sut;

    @Mock
    CeisLookupAdapter ceisLookupAdapter;

    @BeforeAll
    public void setUp() throws ApiException {
        MockitoAnnotations.initMocks(this);

        Mockito.when(ceisLookupAdapter.getCourLocations(COURTLEVEL)).thenReturn(buildMockData());

        Mockito.when(ceisLookupAdapter.getCourLocations(COURTLEVELERROR)).thenThrow(new ApiException());

        sut = new CourtsApiDelegateImpl(ceisLookupAdapter);
    }

    @Test
    @DisplayName("200")
    public void withValidCourtNameReturnCourtLocations() {

        ResponseEntity<CourtLocations> actual = sut.getCourtLocations(COURTLEVEL);

        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertEquals(2, actual.getBody().getCourts().size());
        Assertions.assertEquals(BigDecimal.valueOf(1031), actual.getBody().getCourts().get(0).getId());
        Assertions.assertEquals("Campbell River",actual.getBody().getCourts().get(0).getName());
        Assertions.assertEquals("MockCode",actual.getBody().getCourts().get(0).getCode());
        Assertions.assertEquals(true,actual.getBody().getCourts().get(0).getIsSupremeCourt());
        Assertions.assertEquals("500 - 13th Avenue",actual.getBody().getCourts().get(0).getAddress().getAddressLine1());
        Assertions.assertEquals(null,actual.getBody().getCourts().get(0).getAddress().getAddressLine2());
        Assertions.assertEquals(null,actual.getBody().getCourts().get(0).getAddress().getAddressLine3());
        Assertions.assertEquals("V9W 6P1",actual.getBody().getCourts().get(0).getAddress().getPostalCode());
        Assertions.assertEquals("Campbell River",actual.getBody().getCourts().get(0).getAddress().getCityName());
        Assertions.assertEquals("British Columbia",actual.getBody().getCourts().get(0).getAddress().getProvinceName());
        Assertions.assertEquals("Canada",actual.getBody().getCourts().get(0).getAddress().getCountryName());

        Assertions.assertEquals(BigDecimal.valueOf(3521), actual.getBody().getCourts().get(1).getId());
        Assertions.assertEquals("Chilliwack",actual.getBody().getCourts().get(1).getName());
        Assertions.assertEquals("MockCode",actual.getBody().getCourts().get(1).getCode());
        Assertions.assertEquals(true,actual.getBody().getCourts().get(1).getIsSupremeCourt());
        Assertions.assertEquals("46085 Yale Road",actual.getBody().getCourts().get(1).getAddress().getAddressLine1());
        Assertions.assertEquals("  ",actual.getBody().getCourts().get(1).getAddress().getAddressLine2());
        Assertions.assertEquals("  ",actual.getBody().getCourts().get(1).getAddress().getAddressLine3());
        Assertions.assertEquals("V2P 2L8",actual.getBody().getCourts().get(1).getAddress().getPostalCode());
        Assertions.assertEquals("Chilliwack",actual.getBody().getCourts().get(1).getAddress().getCityName());
        Assertions.assertEquals("British Columbia",actual.getBody().getCourts().get(1).getAddress().getProvinceName());
        Assertions.assertEquals("Canada",actual.getBody().getCourts().get(1).getAddress().getCountryName());

    }

    @Test
    @DisplayName("500")
    public void withValidAdapterThrowsException() {

        ResponseEntity actual = sut.getCourtLocations(COURTLEVELERROR);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode());
        Assertions.assertEquals(COURT_LOCATION_ERROR.getErrorCode(), ((EfilingError)actual.getBody()).getError());
        Assertions.assertEquals(COURT_LOCATION_ERROR.getErrorMessage(), ((EfilingError)actual.getBody()).getMessage());

    }

    private CourtLocations buildMockData() {
        CourtLocation courtLocationOne = new CourtLocation();
        courtLocationOne.setId(BigDecimal.valueOf(1031));
        courtLocationOne.setName("Campbell River");
        courtLocationOne.setCode("MockCode");
        courtLocationOne.setIsSupremeCourt(true);
        Address addressOne = new Address();
        addressOne.setAddressLine1("500 - 13th Avenue");
        addressOne.setPostalCode("V9W 6P1");
        addressOne.setCityName("Campbell River");
        addressOne.setProvinceName("British Columbia");
        addressOne.setCountryName("Canada");
        courtLocationOne.setAddress(addressOne);

        CourtLocation courtLocationTwo = new CourtLocation();
        courtLocationTwo.setId(BigDecimal.valueOf(3521));
        courtLocationTwo.setName("Chilliwack");
        courtLocationTwo.setCode("MockCode");
        courtLocationTwo.setIsSupremeCourt(true);
        Address addressTwo = new Address();
        addressTwo.setAddressLine1("46085 Yale Road");
        addressTwo.setAddressLine2("  ");
        addressTwo.setAddressLine3("  ");
        addressTwo.setPostalCode("V2P 2L8");
        addressTwo.setCityName("Chilliwack");
        addressTwo.setProvinceName("British Columbia");
        addressTwo.setCountryName("Canada");
        courtLocationTwo.setAddress(addressTwo);

        CourtLocations courtLocations = new CourtLocations();
        courtLocations.setCourts(Arrays.asList(courtLocationOne,courtLocationTwo));
        return courtLocations;
    }
}