/**
 * Copyright 2018 Iron City Software LLC
 *
 * This file is part of CrewTools.
 *
 * CrewTools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CrewTools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CrewTools.  If not, see <http://www.gnu.org/licenses/>.
 */

package crewtools.flica.bid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import crewtools.flica.pojo.PairingKey;

public class Transition {
  private final List<PairingKey> addTrips;
  private final List<PairingKey> dropTrips;
  
  public Transition(List<PairingKey> addTrips, List<PairingKey> dropTrips) {
    this.addTrips = new ArrayList<>(addTrips);
    this.dropTrips = new ArrayList<>(dropTrips);
    Collections.sort(this.addTrips);
    Collections.sort(this.dropTrips);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(addTrips, dropTrips);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof Transition)) {
      return false;
    }
    Transition that = (Transition) o;
    return addTrips.equals(that.addTrips)
        && dropTrips.equals(that.dropTrips);
  }

  @Override
  public String toString() {
    return String.format("Add %s; Drop %s", addTrips, dropTrips);
  }
}
