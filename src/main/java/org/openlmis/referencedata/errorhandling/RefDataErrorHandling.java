package org.openlmis.referencedata.errorhandling;

import org.openlmis.referencedata.exception.CsvInputNotValidException;
import org.openlmis.referencedata.exception.ExceptionDetail;
import org.openlmis.referencedata.exception.InvalidIdException;
import org.openlmis.referencedata.exception.RequisitionGroupProgramScheduleException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.ErrorResponse;
import org.openlmis.referencedata.util.LocalizedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Date;

@ControllerAdvice
public class RefDataErrorHandling extends BaseHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(RefDataErrorHandling.class);

  /**
   * Handles data integrity violation and returns status 409 CONFLICT.
   * @param ex the exception to handle
   * @return the error response for the user
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  @ResponseBody
  public ErrorResponse handleDataIntegrityViolation(DataIntegrityViolationException ex) {
    return logAndRespond("Data integrity violation error occurred", ex);
  }

  /**
   * Handles Message exceptions and returns status 400 Bad Request.
   * @param ex the ValidationMessageException to handle
   * @return the error response for the user
   */
  @ExceptionHandler(ValidationMessageException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public LocalizedMessage handleMessageException(ValidationMessageException ex) {
    return getLocalizedMessage(ex);
  }

  /**
   * Handles exceptions and returns proper response.
   *
   * @param ex Exception to handle.
   * @return ResponseEntity with exception details
   */
  @ExceptionHandler({CsvInputNotValidException.class, InvalidIdException.class,
        RequisitionGroupProgramScheduleException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ExceptionDetail exceptionHandler(Exception ex) {
    String title = "Resource Property Validation Failure";
    LOGGER.error(title, ex);
    return getExceptionDetail(ex, HttpStatus.BAD_REQUEST, title);
  }

  private static ExceptionDetail getExceptionDetail(
          Exception exception, HttpStatus status, String title) {
    ExceptionDetail exceptionDetail = new ExceptionDetail();
    exceptionDetail.setTimeStamp(new Date().getTime());
    exceptionDetail.setStatus(status.value());
    exceptionDetail.setTitle(title);
    exceptionDetail.setDetail(exception.getMessage());
    exceptionDetail.setDeveloperMessage(exception.getClass().getName());
    return exceptionDetail;
  }

  private ErrorResponse logAndRespond(String message, Exception ex) {
    LOGGER.error(message, ex);
    return new ErrorResponse(message, ex.getMessage());
  }
}
