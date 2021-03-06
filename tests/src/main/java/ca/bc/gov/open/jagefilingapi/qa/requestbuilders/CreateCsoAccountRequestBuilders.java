package ca.bc.gov.open.jagefilingapi.qa.requestbuilders;

import ca.bc.gov.open.jagefilingapi.qa.backend.createcsoaccountpayload.CreateCsoAccountPayload;
import ca.bc.gov.open.jagefilingapi.qa.backendutils.APIResources;
import ca.bc.gov.open.jagefilingapi.qa.backendutils.TestUtil;
import ca.bc.gov.open.jagefilingapi.qa.frontendutils.JsonDataReader;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;

import static io.restassured.RestAssured.given;

public class CreateCsoAccountRequestBuilders {

    private RequestSpecification request;
    private CreateCsoAccountPayload csoAccountPayloadData;
    private static final String X_TRANSACTION_ID = "X-Transaction-Id";
    private String validExistingCSOGuid;

    public Response requestWithValidRequestBody(String resourceValue, String userJwt) throws IOException {
        csoAccountPayloadData = new CreateCsoAccountPayload();
        APIResources validCreateAccountResourceAPI = APIResources.valueOf(resourceValue);
        validExistingCSOGuid = JsonDataReader.getCsoAccountGuid().getValidExistingCSOGuid();

        request = given().spec(TestUtil.requestSpecification())
                .auth().preemptive().oauth2(userJwt)
                .header(X_TRANSACTION_ID, validExistingCSOGuid)
                .body(csoAccountPayloadData.createCsoAccountPayload());

        return request.when()
                .post(validCreateAccountResourceAPI.getResource())
                .then().spec(TestUtil.createCsoAccountResponseSpecification())
                .extract().response();
    }

    public Response requestToGetUserCsoAccount(String resourceValue, String userJwt) throws IOException {
        csoAccountPayloadData = new CreateCsoAccountPayload();
        APIResources validCreateAccountResourceAPI = APIResources.valueOf(resourceValue);
        validExistingCSOGuid = JsonDataReader.getCsoAccountGuid().getValidExistingCSOGuid();

        request = given().spec(TestUtil.requestSpecification())
                .auth().preemptive().oauth2(userJwt)
                .header(X_TRANSACTION_ID, validExistingCSOGuid);

        return request.when()
                .get(validCreateAccountResourceAPI.getResource())
                .then().spec(TestUtil.validResponseSpecification())
                .extract().response();
    }

    public Response requestToUpdateUserCsoAccount(String resourceValue, String userJwt) throws IOException {
        csoAccountPayloadData = new CreateCsoAccountPayload();
        APIResources validCreateAccountResourceAPI = APIResources.valueOf(resourceValue);
        validExistingCSOGuid = JsonDataReader.getCsoAccountGuid().getValidExistingCSOGuid();

        request = given().spec(TestUtil.requestSpecification())
                .auth().preemptive().oauth2(userJwt)
                .header(X_TRANSACTION_ID, validExistingCSOGuid)
                .body(csoAccountPayloadData.updateCsoAccountInternalClientNumber());

        return request.when()
                .put(validCreateAccountResourceAPI.getResource())
                .then().spec(TestUtil.validResponseSpecification())
                .extract().response();
    }

    public Response requestToGetUserBceidAccount(String resourceValue, String userJwt) throws IOException {
        APIResources validCreateAccountResourceAPI = APIResources.valueOf(resourceValue);
        validExistingCSOGuid = JsonDataReader.getCsoAccountGuid().getValidExistingCSOGuid();

        request = given().spec(TestUtil.requestSpecification())
                .auth().preemptive().oauth2(userJwt)
                .header(X_TRANSACTION_ID, validExistingCSOGuid);

        return request.when()
                .get(validCreateAccountResourceAPI.getResource())
                .then().spec(TestUtil.validResponseSpecification())
                .extract().response();
    }

    public Response requestWithIncorrectPath(String resourceValue) throws IOException {
        csoAccountPayloadData = new CreateCsoAccountPayload();
        APIResources incorrectPathCreateAccountResourceAPI = APIResources.valueOf(resourceValue);

        request = given().spec(TestUtil.requestSpecification())
                .body(csoAccountPayloadData.createCsoAccountPayload());

        return request.when()
                .post(incorrectPathCreateAccountResourceAPI.getResource())
                .then()
                .spec(TestUtil.createCsoAccountIncorrectPathErrorResponseSpecification())
                .extract().response();
    }
}
