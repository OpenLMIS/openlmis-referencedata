package org.openlmis.referencedata.errorhandling;

import org.openlmis.referencedata.exception.IntegrityViolationException;
import org.openlmis.referencedata.exception.InternalErrorException;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.LocalizedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class RefDataErrorHandling extends BaseHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(RefDataErrorHandling.class);

  /**
   * Handles data integrity violation and returns status 409 CONFLICT.
   *
   * @param ex the exception to handle
   * @return the error response for the user
   */
  @ExceptionHandler(IntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  @ResponseBody
  public LocalizedMessage handleDataIntegrityViolation(IntegrityViolationException ex) {
    LOGGER.error(ex.getMessage());
    return getLocalizedMessage(ex.getMessage());
  }

  /**
   * Handles Message exceptions and returns status 400 Bad Request.
   *
   * @param ex the ValidationMessageException to handle
   * @return the error response for the user
   */
  @ExceptionHandler(ValidationMessageException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public LocalizedMessage handleMessageException(ValidationMessageException ex) {
    LOGGER.info(ex.getMessage());
    return getLocalizedMessage(ex.asMessage());
  }

  /**
   * Handles Message exceptions and returns status 500 Internal Server Error.
   *
   * @param ex the InternalErrorException to handle
   * @return the error response for the user
   */
  @ExceptionHandler(InternalErrorException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public LocalizedMessage handleInternalErrorException(InternalErrorException ex) {
    LOGGER.info(ex.getMessage());
    return getLocalizedMessage(ex.asMessage());
  }

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ResponseBody
  public LocalizedMessage handleNotFoundException(NotFoundException ex) {
    LOGGER.info(ex.getMessage());
    return getLocalizedMessage(ex.asMessage());
  }

  /**
   * Handles unauthorized exceptions and returns proper response.
   *
   * @param ex Exception to handle.
   * @return ResponseEntity with exception details
   */
  @ExceptionHandler(UnauthorizedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ResponseBody
  public LocalizedMessage handleUnauthorizedException(UnauthorizedException ex) {
    LOGGER.info(ex.getMessage());
    return getLocalizedMessage(ex.asMessage());
  }
}
