package org.openlmis.referencedata.util;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeRequest {

  @Getter
  @Setter
  @NotNull
  private UUID token;

  @Getter
  @Setter
  @NotNull
  private String username;

  @Getter
  @Setter
  @NotNull
  @Size(min = 8, max = 16)
  @Pattern.List({
      @Pattern(regexp = "(?=.*[0-9]).+", message = "must contain at least 1 number"),
      @Pattern(regexp = "(?=\\S+$).+", message = "must not contain spaces")
      })
  private String newPassword;
}
