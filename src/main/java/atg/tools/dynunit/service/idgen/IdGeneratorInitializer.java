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

package atg.tools.dynunit.service.idgen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/*
 * IdGeneratorInitializer contains logic used to create and drop the schema used
 * for IdGenerators.
 * $Id: //test/UnitTests/base/main/src/Java/atg/service/idgen/IdGeneratorInitializer.java#4 $
 * @author Adam Belmont
 */
public class IdGeneratorInitializer {

    private static final String SELECT_COUNT_FROM_TEMPLATE = "select count(*) from";

    private static final String DROP_TABLE_TEMPLATE = "DROP TABLE";

    private final InitializingIdGenerator mGenerator;

    private static final Logger logger = LogManager.getLogger();

    /**
     * Creates a new IdGeneratorInitializer used for the given generator,
     * pGenerator.
     *
     * @param pGenerator
     */
    public IdGeneratorInitializer(InitializingIdGenerator pGenerator) {
        mGenerator = pGenerator;
    }

    /**
     * Creates a new schema for the current generator. If the schema exists, it's
     * dropped and a new one is created.
     *
     * @throws SQLException
     */
    public void initialize()
            throws SQLException {
        if ( tablesExist() ) {
            dropTables();
        }
        initializeTables();
    }

    // --------------------------

    /**
     * Drops the tables required for this component
     *
     * @throws SQLException
     */
    void dropTables()
            throws SQLException {
        executeUpdateStatement(
                DROP_TABLE_TEMPLATE + " " + mGenerator.getTableName()
        );
    }

    private final Map<DataSource, DatabaseMetaData> mMetaDataMap = new HashMap<DataSource, DatabaseMetaData>();

    /**
     * Returns a cached instance of the DB metadata for the current connection
     *
     * @return
     */
    public DatabaseMetaData getDatabaseMetaData() {
        DataSource ds = mGenerator.getDataSource();
        if ( mMetaDataMap.get(ds) == null ) {
            try {
                mMetaDataMap.put(ds, ds.getConnection().getMetaData());
            } catch ( SQLException e ) {
                logger.catching(e);
            }
        }
        return mMetaDataMap.get(ds);
    }

    // --------------------------

    /**
     * Returns true if the tables required for this component exist
     *
     * @return
     */
    public boolean tablesExist() {
        String[] types = { "TABLE" };
        boolean exists = false;
        try {
            ResultSet rs = getDatabaseMetaData().getTables(
                    null, null, mGenerator.getTableName(), types
            );
            while ( rs.next() ) {
                exists = true;
            }
        } catch ( SQLException e ) {
            logger.catching(e);
        }
        return exists;
    }

    // --------------------------

    /**
     * Creates the table required for this component
     *
     * @throws SQLException
     */
    void initializeTables()
            throws SQLException {
        String statement = mGenerator.getCreateStatement();
        logger.info("Creating IdGenerator tables : " + statement);
        executeUpdateStatement(statement);
    }

    // --------------------------

    /**
     * @return TODO
     * @throws SQLException
     */
    private boolean executeUpdateStatement(String pStatement)
            throws SQLException {
        boolean success = false;
        Statement st = null;
        try {
            st = mGenerator.getDataSource().getConnection().createStatement(); // statements
            int i = st.executeUpdate(pStatement); // run the query
            if ( i == -1 ) {
                logger.error("Error creating tables with statement {}", pStatement);
            }
            success = true;
        } finally {
            try {
                if ( st != null ) {
                    st.close();
                }
            } catch ( SQLException e ) {
                logger.catching(e);
            }
        }
        return success;
    }
}
