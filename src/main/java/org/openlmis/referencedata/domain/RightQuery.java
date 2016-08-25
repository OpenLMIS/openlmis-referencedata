package org.openlmis.referencedata.domain;

public class RightQuery {
  private Facility warehouse;

  private Right right;
  private Program program;
  private SupervisoryNode supervisoryNode;

  /**
   * Constructor to create query if user has a right. This is for general admin and report rights.
   *
   * @param right the right to check
   */
  public RightQuery(Right right) {
    this.right = right;
  }

  /**
   * Constructor to create query if user has a right in a specified program. This is for home
   * facility supervision rights.
   *
   * @param right   the right to check
   * @param program the program to check
   */
  public RightQuery(Right right, Program program) {
    this.right = right;
    this.program = program;
  }

  /**
   * Constructor to create query if user has a right in a specified program at a specified
   * supervisory node. This is for supervisory supervision rights.
   *
   * @param right           the right to check
   * @param program         the program to check
   * @param supervisoryNode the supervisory node to check
   */
  public RightQuery(Right right, Program program, SupervisoryNode supervisoryNode) {
    this.right = right;
    this.program = program;
    this.supervisoryNode = supervisoryNode;
  }

  /**
   * Constructor to create query if user has a right at a specified warehouse. This is for order
   * fulfillment rights.
   *
   * @param right     the right to check
   * @param warehouse the warehouse to check
   */
  public RightQuery(Right right, Facility warehouse) {
    this.right = right;
    this.warehouse = warehouse;
  }

  public Right getRight() {
    return right;
  }

  public Program getProgram() {
    return program;
  }

  public SupervisoryNode getSupervisoryNode() {
    return supervisoryNode;
  }

  public Facility getWarehouse() {
    return warehouse;
  }
}
