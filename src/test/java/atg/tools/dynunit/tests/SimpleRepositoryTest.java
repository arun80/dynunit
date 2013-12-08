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

package atg.tools.dynunit.tests;

import atg.adapter.gsa.GSARepository;
import atg.dtm.TransactionDemarcation;
import atg.nucleus.Nucleus;
import atg.repository.MutableRepository;
import atg.repository.MutableRepositoryItem;
import atg.repository.RepositoryItem;
import atg.tools.dynunit.adapter.gsa.GSATest;
import atg.tools.dynunit.adapter.gsa.GSATestUtils;
import atg.tools.dynunit.test.util.DBUtils;
import atg.tools.dynunit.test.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

/**
 * This test starts a repository, adds an item to that repository, then shuts
 * down. The repository is started up against an in-memory Hypersonic Database.
 * <br/><br/>Based on {@link GSATest}
 */
public class SimpleRepositoryTest
        extends GSATest {

    private static final Logger log = LogManager.getLogger(SimpleRepositoryTest.class);

    public void testSimple()
            throws Exception {

        // setup the repository
        File configpath = new File(
                "target/test-classes/config".replace(
                        "/", File.separator
                )
        );

        // Define the path to our repository definition file called
        // "simpleRepository.xml"
        final String[] definitionFiles = { "/test/simpleRepository.xml" };
        log.info("definitionFile[0]={}", definitionFiles[0]);

        // Copy all related properties and definition files to the previously
        // configured configpath
        FileUtil.copyDirectory(
                "src/test/resources/config",
                configpath.getPath(),
                Arrays.asList(".svn")
        );

        // Use the DBUtils utility class to get JDBC properties for an in memory
        // HSQL DB called "testdb".
        Properties props = DBUtils.getHSQLDBInMemoryDBConnection("testdb");

        // Start up our database
        DBUtils db = initDB(props);

        boolean rollback = true;

        // Setup our testing configpath
        // RH: disabled logging (last argument to false) to get rid of the double
        // logging statements
        GSATestUtils.getGSATestUtils().initializeMinimalConfigpath(
                configpath, "/SimpleRepository", definitionFiles, props, null, null, null, false
        );

        // Start Nucleus
        Nucleus n = startNucleus(configpath);

        TransactionDemarcation td = new TransactionDemarcation();
        MutableRepository r = (MutableRepository) n.resolveName("/SimpleRepository");

        try {
            // Start a new transaction
            td.begin(((GSARepository) r).getTransactionManager());
            // Create the item
            MutableRepositoryItem item = r.createItem("simpleItem");
            item.setPropertyValue("name", "simpleName");
            // Persist to the repository
            r.addItem(item);
            // Try to get it back from the repository
            String id = item.getRepositoryId();
            RepositoryItem item2 = r.getItem(id, "simpleItem");
            assertNotNull(
                    " We did not get back the item just created from the repository.", item2
            );
            rollback = false;
        } finally {
            // End the transaction, rollback on error
            td.end(rollback);
            // shut down Nucleus
            n.stopService();
            // Shut down HSQLDB
            db.shutdown();
        }
    }
}