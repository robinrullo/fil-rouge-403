/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.rullo.mulhousetransportrouting.components.graphhopper;

import com.graphhopper.GraphHopper;
import com.graphhopper.GraphHopperConfig;
import java.util.HashMap;
import java.util.Map;

/**
 * Modified version of GraphHopper to optimize working with Postgis.
 *
 * @author Phil
 * @author Robin Boldt
 * @author mbasa
 */
public class GraphHopperPostgis extends GraphHopper {

  private final Map<String, Object> postgisParams = new HashMap<>();

  @Override
  public GraphHopper init(GraphHopperConfig ghConfig) {

    postgisParams.put("dbtype", "postgis");
    postgisParams.put("host", ghConfig.getString("db.host", "localhost"));
    postgisParams.put("port", ghConfig.getString("db.port", "5432"));
    postgisParams.put("schema", ghConfig.getString("db.schema", "public"));
    postgisParams.put("database", ghConfig.getString("db.database", ""));
    postgisParams.put("table", ghConfig.getString("db.table", ""));
    postgisParams.put("geometry_column", ghConfig.getString("db.geometry_column", "geom"));
    postgisParams.put("user", ghConfig.getString("db.user", "postgres"));
    postgisParams.put("passwd", ghConfig.getString("db.passwd", ""));
    postgisParams.put("tags_to_copy", ghConfig.getString("db.tags_to_copy", ""));
    postgisParams.put("geomfilter", ghConfig.asPMap().getObject("db.geomfilter", null));

    return super.init(ghConfig);
  }

  @Override
  protected void importOSM() {
    OSMPostgisReader reader = new OSMPostgisReader(getGraphHopperStorage(), postgisParams);
    reader.readGraph();
  }
}
