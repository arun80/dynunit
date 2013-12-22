/*
 * Copyright 2013 Matt Sicker and Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package atg.tools.dynunit.service.jdbc;

import atg.nucleus.ServiceException;
import atg.tools.dynunit.test.util.DBUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * This datasource is used for testing. It starts up an HSQLDB in memory
 * instance on localhost automatically. The database will be named "testdb" by
 * default. If you need to name it something else set the "databaseName"
 * property on this component. You may want to change the name if your test
 * requires running two databases at the same time.
 *
 * @author adamb
 * @version $Id:n HSQLDB
 *          //test/UnitTests/base/main/src/Java/atg/service/jdbc/HSQLDBDataSource
 *          .java#2 $
 */
public class HSQLDBDataSource
        extends InitializingDataSourceBase {

    // Don't shutdown HSQLDB by default. It might stop before other components
    // that require it.
    private boolean shutdownHSQLDB;

    /**
     * Returns true if the "SHUTDOWN" sql statement should be sent to HSQLDB
     * when doStopService is called on this component.
     *
     * @return
     */
    public boolean isShutdownHSQLDB() {
        return shutdownHSQLDB;
    }

    /**
     * Sets the boolean which controls if HSQLDB should be shutdown when doStopService
     * is called on this component.
     *
     * @param shutdownHSQLDB
     */
    public void setShutdownHSQLDB(boolean shutdownHSQLDB) {
        this.shutdownHSQLDB = shutdownHSQLDB;
    }

    // --------------------------

    /**
     * Starts this DataSource. Since the data source uses an in memory HSQL
     * database, the database actually is started on the first call to
     * getConnection().
     */
    @Override
    public void doStartService()
            throws ServiceException {
        Properties props = DBUtils.getHSQLDBInMemoryDBConnection(getName());
        // set our properties from this object
        this.setDriver(props.getProperty("driver"));
        this.setURL(props.getProperty("URL"));
        this.setUser(props.getProperty("user"));
        this.setPassword(props.getProperty("password"));
        vlogInfo("HSQLDB DataSource starting with properties " + props.toString());
        super.doStartService();
    }

    // --------------------------

    /**
     * Called when Nucleus is shutdown. Issues the "SHUTDOWN" command to the
     * HSQLDB database.
     */
    @Override
    public void doStopService() {
        if (shutdownHSQLDB) {
            vlogInfo("HSQLDB DataSource shutting down.");
            Connection connection = null;
            try {
                connection = this.getConnection();
                Statement st = connection.createStatement();
                st.execute("SHUTDOWN");
            } catch ( SQLException e ) {
                vlogError(e.getMessage());
            } finally {
                if ( connection != null ) {
                    try {
                        connection.close();
                    } catch ( SQLException e ) {
                        // eat it
                    }
                }
            }
        }

    }
}
