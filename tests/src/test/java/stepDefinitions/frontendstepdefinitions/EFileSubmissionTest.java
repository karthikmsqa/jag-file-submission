package stepDefinitions.frontendstepdefinitions;

import ca.bc.gov.open.jagefilingapi.qa.backendutils.GenerateUrlHelper;
import ca.bc.gov.open.jagefilingapi.qa.backendutils.TestUtil;
import ca.bc.gov.open.jagefilingapi.qa.frontend.pages.*;
import ca.bc.gov.open.jagefilingapi.qa.frontendutils.FrontendTestUtil;
import com.google.common.collect.ImmutableList;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.*;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EFileSubmissionTest extends ca.bc.gov.open.jagefilingapi.qa.frontendutils.DriverClass {

    EFileSubmissionPage eFileSubmissionPage;
    PackageConfirmationPage packageConfirmationPage;
    FrontendTestUtil frontendTestUtil;
    private static final String EFILE_SUBMISSION_PAGE_TITLE = "E-File submission";
    private static final String BASE_PATH = "user.dir";
    private static final String SECOND_PDF_PATH = "/src/test/java/testdatasource/test-document-2.pdf";
    private final List<String> expectedUploadedFilesList = ImmutableList.of("test-document.pdf", "test-document-2.pdf");

    @Before
    public void setUp() throws IOException {
        TestUtil testUtil = new TestUtil();
        driverSetUp();
        testUtil.restAssuredConfig();
    }

    @After
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            String testName = scenario.getName();
            log.info(testName + "is Failed");
            final byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "Failed test");
        }
        driver.close();
        driver.quit();
        log.info("Browser closed");
    }

    @Given("user is on the eFiling submission page")
    public void userIsOnTheEfilingSubmissionPage() throws IOException {
        eFileSubmissionPage = new EFileSubmissionPage(driver);

        String username = System.getProperty("BCEID_USERNAME");
        String password = System.getProperty("BCEID_PASSWORD");

        try {
            for (int i = 0; i < 3; i++) {
                GenerateUrlHelper generateUrlHelper = new GenerateUrlHelper();
                String respUrl = generateUrlHelper.getGeneratedUrl();

                driver.get(respUrl);
                log.info("EFiling submission page url is accessed successfully");

                AuthenticationPage authenticationPage = new AuthenticationPage(driver);
                authenticationPage.clickBceid();
                Thread.sleep(4000L);
                authenticationPage.signInWithBceid(username, password);
                log.info("user is authenticated before reaching eFiling hub page");

                if (eFileSubmissionPage.verifyCreateCsoAccountBtnIsDisplayed()) {
                    break;
                }
            }
        } catch (TimeoutException | InterruptedException tx) {
            log.info("Efiling hub page is not displayed");
        }
        Assert.assertEquals(EFILE_SUBMISSION_PAGE_TITLE, eFileSubmissionPage.verifyEfilingPageTitle());
        log.info("Efiling submission page title is verified");
    }

    @Then("user accepts agreement and clicks cancel button")
    public void userAcceptsAgreementAndClicksCancelButton() {
        frontendTestUtil = new FrontendTestUtil();
        eFileSubmissionPage = new EFileSubmissionPage(driver);

        eFileSubmissionPage.selectCheckbox();
        eFileSubmissionPage.clickAcceptTermsCancelButton();
    }

    @Then("user clicks resume E-File submission in the confirmation window")
    public void userClicksResumeEFileSubmissionInTheConfirmationWindow() {
        frontendTestUtil = new FrontendTestUtil();
        eFileSubmissionPage = new EFileSubmissionPage(driver);

        eFileSubmissionPage.clickResumeSubmission();
        log.info("Confirmation modal is closed and E-File submission page is retained");
    }

    @And("the user stays on the E-File submission page")
    public void theUserStaysOnTheEFileSubmissionPage() {
        frontendTestUtil = new FrontendTestUtil();
        eFileSubmissionPage = new EFileSubmissionPage(driver);

        String actualTitle = eFileSubmissionPage.verifyEfilingPageTitle();
        Assert.assertEquals(EFILE_SUBMISSION_PAGE_TITLE, actualTitle);
        log.info("eFiling Frontend page title is verified");
    }

    @Then("user clicks on create CSO account")
    public void userClicksOnCreateCSOAccount() {
        frontendTestUtil = new FrontendTestUtil();
        CreateCsoAccountPage createCsoAccountPage = new CreateCsoAccountPage(driver);
        createCsoAccountPage.clickCreateCsoAccountBtn();
    }

    @Then("the CSO account is created successfully")
    public void theCSOAccountIsCreatedSuccessfully() {
        frontendTestUtil = new FrontendTestUtil();
        packageConfirmationPage = new PackageConfirmationPage(driver);
        assertTrue(packageConfirmationPage.verifyContinuePaymentBtnIsDisplayed());
    }

    @When("user can upload an additional document")
    public void userCanUploadAnAdditionalDocument() throws IOException {
        frontendTestUtil = new FrontendTestUtil();
        eFileSubmissionPage = new EFileSubmissionPage(driver);
        packageConfirmationPage = new PackageConfirmationPage(driver);

        packageConfirmationPage.clickUploadLink();

        DocumentUploadPage documentUploadPage = new DocumentUploadPage(driver);

        String filePath = System.getProperty(BASE_PATH) + SECOND_PDF_PATH;
        documentUploadPage.selectFileToUpload(filePath);

        documentUploadPage.clickIsAmendmentRadioBtn();
        documentUploadPage.clickIsSupremeCourtBtn();
        log.info("Additional document is added successfully.");
    }

    @And("submit and verify the document is uploaded")
    public void submitAndVerifyTheDocumentIsUploaded() {
        DocumentUploadPage documentUploadPage = new DocumentUploadPage(driver);
        packageConfirmationPage = new PackageConfirmationPage(driver);

        documentUploadPage.clickContinueBtn();

        List<String>uploadedFiles = packageConfirmationPage.getUploadedFilesList();
        assertEquals(uploadedFiles, expectedUploadedFilesList);
        log.info("Additional file is uploaded successfully.");
    }

    @Then("user clicks continue payment button")
    public void userClicksContinuePaymentButton() {
        eFileSubmissionPage = new EFileSubmissionPage(driver);
        packageConfirmationPage = new PackageConfirmationPage(driver);

        packageConfirmationPage.clickContinuePaymentBtn();
    }

    @And("delete the selected additional document")
    public void userCanDeleteTheSelectedAdditionalDocument() {
        DocumentUploadPage documentUploadPage = new DocumentUploadPage(driver);

        documentUploadPage.clickRemoveFileIcon();
        log.info("Added file is removed.");
    }

    @Then("user clicks cancel upload button")
    public void userClicksCancelUploadButton() {
        DocumentUploadPage documentUploadPage = new DocumentUploadPage(driver);
        packageConfirmationPage = new PackageConfirmationPage(driver);

        documentUploadPage.clickCancelUpload();
        packageConfirmationPage.verifyContinuePaymentBtnIsDisplayed();
        log.info("Document upload is cancelled in upload page.");
    }

    @Then("verify there are no broken links in the page")
    public void verifyThereAreNoBrokenLinksInThePage() throws IOException {
        List<WebElement> links = driver.findElements(By.tagName("a"));

        log.info("Total links are " + links.size());

        for (WebElement element : links) {
            String url = element.getAttribute("href");
            FrontendTestUtil.verifyLinkActive(url);
        }
    }
}
