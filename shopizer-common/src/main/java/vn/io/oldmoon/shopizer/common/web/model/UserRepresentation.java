package vn.io.oldmoon.shopizer.common.web.model;

import lombok.Data;

@Data
public class UserRepresentation {
  private String username;
  private String id;
  private String email;
  private String firstName;
  private String lastName;
}
