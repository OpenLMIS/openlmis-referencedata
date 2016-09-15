package org.openlmis.referencedata.web;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.common.collect.Sets;

import org.junit.Test;
import org.mockito.Mock;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.dto.RightDto;
import org.openlmis.referencedata.repository.RightRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;
import java.util.UUID;

public class RightControllerTest {

  @Mock
  private RightRepository repository;

  private RightController controller;

  private String right1Name;
  private Right right1;
  private Set<Right> rights;
  private RightDto right1Dto;

  /**
   * Constructor for test.
   */
  public RightControllerTest() {
    initMocks(this);
    controller = new RightController(repository);

    right1Name = "right1";
    right1 = Right.newRight(right1Name, RightType.GENERAL_ADMIN);
    rights = Sets.newHashSet(right1);

    right1Dto = new RightDto();
    right1.export(right1Dto);
  }

  @Test
  public void shouldGetAllRights() {
    //given
    Set<RightDto> expectedRightDtos = Sets.newHashSet(right1Dto);
    when(repository.findAll()).thenReturn(rights);

    //when
    ResponseEntity responseEntity = controller.getAllRights();
    HttpStatus httpStatus = responseEntity.getStatusCode();
    Set<RightDto> rightDtos = (Set<RightDto>) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(expectedRightDtos, rightDtos);
  }

  @Test
  public void shouldGetRight() {
    //given
    UUID rightId = UUID.randomUUID();
    when(repository.findOne(rightId)).thenReturn(right1);

    //when
    ResponseEntity responseEntity = controller.getRight(rightId);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    RightDto rightDto = (RightDto) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(right1Dto, rightDto);
  }

  @Test
  public void shouldCreateNewRight() {
    //given
    when(repository.findFirstByName(right1Name)).thenReturn(null);

    //when
    ResponseEntity responseEntity = controller.saveRight(right1Dto);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    RightDto rightDto = (RightDto) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(right1Dto, rightDto);
    verify(repository).save(right1);
  }

  @Test
  public void shouldUpdateExistingRight() {
    //given
    when(repository.findFirstByName(right1Name)).thenReturn(right1);
    right1Dto.setType(RightType.SUPERVISION);
    Right updatedRight1 = Right.newRight(right1Dto);

    //when
    ResponseEntity responseEntity = controller.saveRight(right1Dto);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    RightDto rightDto = (RightDto) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(right1Dto, rightDto);
    verify(repository).save(updatedRight1);
  }

  @Test
  public void shouldDeleteExistingRight() {
    //given
    UUID rightId = UUID.randomUUID();
    when(repository.findOne(rightId)).thenReturn(right1);

    //when
    ResponseEntity responseEntity = controller.deleteRight(rightId);
    HttpStatus httpStatus = responseEntity.getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.NO_CONTENT));
    verify(repository).delete(rightId);
  }

  @Test
  public void shouldNotDeleteNonExistingRight() {
    //given
    UUID rightId = UUID.randomUUID();
    when(repository.findOne(rightId)).thenReturn(null);

    //when
    ResponseEntity responseEntity = controller.deleteRight(rightId);
    HttpStatus httpStatus = responseEntity.getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.NOT_FOUND));
    verify(repository, never()).delete(rightId);
  }
}
