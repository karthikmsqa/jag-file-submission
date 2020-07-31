package ca.bc.gov.open.jag.efilingapi.account;

import ca.bc.gov.open.jag.efilingapi.Keys;
import ca.bc.gov.open.jag.efilingapi.api.CsoAccountApiDelegate;
import ca.bc.gov.open.jag.efilingapi.api.model.Account;
import ca.bc.gov.open.jag.efilingapi.api.model.EfilingError;
import ca.bc.gov.open.jag.efilingapi.api.model.UserDetails;
import ca.bc.gov.open.jag.efilingapi.error.ErrorResponse;
import ca.bc.gov.open.jag.efilingapi.submission.SubmissionApiDelegateImpl;
import ca.bc.gov.open.jag.efilingcommons.exceptions.EfilingAccountServiceException;
import ca.bc.gov.open.jag.efilingcommons.model.AccountDetails;
import ca.bc.gov.open.jag.efilingcommons.model.CreateAccountRequest;
import ca.bc.gov.open.jag.efilingcommons.service.EfilingAccountService;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.security.RolesAllowed;
import java.util.Optional;
import java.util.UUID;

/**
 * Suite of services to manage cso account
 */
@Service
public class CsoAccountApiDelegateImpl implements CsoAccountApiDelegate {

    Logger logger = LoggerFactory.getLogger(SubmissionApiDelegateImpl.class);

    private final EfilingAccountService efilingAccountService;


    public CsoAccountApiDelegateImpl(EfilingAccountService efilingAccountService) {
        this.efilingAccountService = efilingAccountService;
    }


    @Override
    @RolesAllowed("efiling-user")
    public ResponseEntity<UserDetails> createAccount(UUID xTransactionId, UserDetails userDetails) {

        try {

            logger.debug("attempting to create a cso account");
            
            AccountDetails accountDetails = efilingAccountService.createAccount(CreateAccountRequest
                    .builder()
                    .universalId(getUniversalIdFromContext().get())
                    .firstName(userDetails.getFirstName())
                    .lastName(userDetails.getLastName())
                    .middleName(userDetails.getMiddleName())
                    .email(userDetails.getEmail())
                    .create());

            logger.info("Account successfully created");
            
            UserDetails result = totUserDetails(accountDetails);

            return new ResponseEntity<>(result, HttpStatus.CREATED);

        } catch (EfilingAccountServiceException e) {

            logger.error("Exception while trying to create a CSO Account", e);

            EfilingError response = new EfilingError();
            response.setError(ErrorResponse.CREATE_ACCOUNT_EXCEPTION.getErrorCode());
            response.setMessage(ErrorResponse.CREATE_ACCOUNT_EXCEPTION.getErrorMessage());
            return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);

        }

    }

    private UserDetails totUserDetails(AccountDetails accountDetails) {

        // TODO: replace with mapstruct

        UserDetails result = new UserDetails();
        result.setUniversalId(accountDetails.getUniversalId());
        Account csoAccount = new Account();
        csoAccount.setType(Account.TypeEnum.CSO);
        csoAccount.setIdentifier(accountDetails.getAccountId().toString());
        result.addAccountsItem(csoAccount);
        result.setEmail(accountDetails.getEmail());
        result.setFirstName(accountDetails.getFirstName());
        result.setLastName(accountDetails.getLastName());
        result.setMiddleName(accountDetails.getMiddleName());
        return result;
    }

    private Optional<UUID> getUniversalIdFromContext() {

        try {
            return Optional.of(UUID.fromString(
                    ((KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                            .getKeycloakSecurityContext().getToken().getOtherClaims().get(Keys.UNIVERSAL_ID_CLAIM_KEY).toString()));
        } catch (Exception e) {
            logger.error("Unable to extract universal Id from token", e);
            return Optional.empty();
        }
    }

}
