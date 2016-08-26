package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.repository.RightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
public class RightController {

  @Autowired
  private RightRepository rightRepository;

  /**
   * Create a new right.
   *
   * @return the new right
   */
  @RequestMapping(value = "/rights", method = RequestMethod.POST)
  public ResponseEntity<?> createRight(@RequestBody Right rightDto) {

    Right right = new Right(rightDto.getName(), rightDto.getType(), rightDto.getDescription());
    List<Right> attachments = rightDto.getAttachments();
    right.attach(attachments.toArray(new Right[attachments.size()]));

    rightRepository.save(right);

    return new ResponseEntity<>(right, HttpStatus.CREATED);
  }
}
