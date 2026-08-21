package vn.io.oldmoon.shopizer.user.infra.model.profile;

import java.time.LocalTime;
import lombok.Getter;

@Getter
public enum Shift {
  MORNING(LocalTime.of(7, 0), LocalTime.of(15, 30)),
  AFTERNOON(LocalTime.of(15, 0), LocalTime.of(23, 30)),
  NIGHT(LocalTime.of(23, 0), LocalTime.of(7, 30));

  private final LocalTime startTime;
  private final LocalTime endTime;

  Shift(LocalTime startTime, LocalTime endTime) {
    this.startTime = startTime;
    this.endTime = endTime;
  }

  @Override
  public String toString() {
    return name() + " (" + startTime + " to " + endTime + ")";
  }
}
