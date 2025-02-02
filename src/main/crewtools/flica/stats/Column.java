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

package crewtools.flica.stats;

import java.util.ArrayList;
import java.util.List;

public class Column<T> {
  private String label;
  private List<T> data;
  
  public Column(String label) {
    this.label = label;
    this.data = new ArrayList<T>();
  }

  public void add(T datum) {
    data.add(datum);
  }
  
  public String getDatum(int index) {
    T datum = data.get(index);
    if (datum == null) {
      return "";
    } else {
      return datum.toString();
    }
  }
  
  public int size() {
    return data.size();
  }
  
  public String getLabel() {
    return label;
  }
}
