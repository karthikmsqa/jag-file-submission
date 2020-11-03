package ca.bc.gov.open.jag.efilingapi.submission.validator;

import ca.bc.gov.open.jag.efilingapi.api.model.GenerateUrlRequest;
import ca.bc.gov.open.jag.efilingapi.api.model.InitialPackage;
import ca.bc.gov.open.jag.efilingapi.api.model.Party;
import ca.bc.gov.open.jag.efilingapi.court.models.GetCourtDetailsRequest;
import ca.bc.gov.open.jag.efilingapi.court.models.IsValidCourtRequest;
import ca.bc.gov.open.jag.efilingapi.court.services.CourtService;
import ca.bc.gov.open.jag.efilingapi.submission.models.GetValidPartyRoleRequest;
import ca.bc.gov.open.jag.efilingapi.submission.service.SubmissionService;
import ca.bc.gov.open.jag.efilingapi.utils.Notification;
import ca.bc.gov.open.jag.efilingcommons.model.CourtDetails;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateUrlRequestValidatorImpl implements GenerateUrlRequestValidator {

    private final SubmissionService submissionService;
    private final CourtService courtService;

    public GenerateUrlRequestValidatorImpl(SubmissionService submissionService,
                                           CourtService courtService) {
        this.submissionService = submissionService;
        this.courtService = courtService;
    }

    @Override
    public Notification validate(GenerateUrlRequest generateUrlRequest, String applicationCode) {

        Notification notification = new Notification();

        if (generateUrlRequest.getFilingPackage() == null) {
            notification.addError("Initial Package is required.");
            return notification;
        }

        notification.addError(validateParties(generateUrlRequest.getFilingPackage()));

        notification.addError(validateCourt(generateUrlRequest.getFilingPackage(), applicationCode));

        return notification;

    }

    private List<String> validateCourt(InitialPackage initialPackage, String applicationCode) {

        List<String> result = new ArrayList<>();


        try {

            CourtDetails courtDetails = this.courtService.getCourtDetails(GetCourtDetailsRequest
                    .builder()
                    .courtLocation(initialPackage.getCourt().getLocation())
                    .courtLevel(initialPackage.getCourt().getLevel())
                    .courtClassification(initialPackage.getCourt().getCourtClass())
                    .create());

            if (!this.courtService.isValidCourt(IsValidCourtRequest
                    .builder()
                    .applicationCode(applicationCode)
                    .courtClassification(initialPackage.getCourt().getLocation())
                    .courtLevel(initialPackage.getCourt().getLevel())
                    .courtId(courtDetails.getCourtId())
                    .create())) {
                result.add("Court with Location: [{0}], Level: [{1}], Classification: [{2}] is not a valid court.");
            }

        } catch (Exception ex) {
            result.add(MessageFormat.format("Court details not valid: {0}", ex.getMessage()));
        }

        return result;

    }

    /**
     * Validates parties
     *
     * if court file number is empty then at least 1 party is required
     *
     * Party types must be valid based on the document types submitted
     *
     * @param initialPackage
     */
    private List<String> validateParties(InitialPackage initialPackage) {

        List<String> result = new ArrayList<>();

        if (StringUtils.isBlank(initialPackage.getCourt().getFileNumber()) && initialPackage.getParties().isEmpty()) {
            result.add("At least 1 party is required for new submission.");
            return result;
        }

        List<String> validPartyRoles = submissionService.getValidPartyRoles(GetValidPartyRoleRequest
                .builder()
                .courtClassification(initialPackage.getCourt().getCourtClass())
                .courtLevel(initialPackage.getCourt().getLevel())
                .documents(initialPackage.getDocuments())
                .create());

        List<Party> invalidParties = initialPackage
                .getParties()
                .stream()
                .filter(party -> party.getRoleType() == null || !validPartyRoles.contains(party.getRoleType().toString()))
                .collect(Collectors.toList());

        if(!invalidParties.isEmpty()) {

            invalidParties.stream().forEach(party -> {
                result.add(MessageFormat.format("Role type [{0}] is invalid.", party.getRoleType()));
            });

        }

        return result;

    }

}
