package net.f4fs.persistence;

import net.f4fs.persistence.data.IDataPersistence;
import net.f4fs.persistence.path.IPathPersistence;
import net.f4fs.persistence.data.DHTOperations;
import net.f4fs.persistence.path.ConsensusPathOperations;
import net.f4fs.persistence.path.DirectPathOperations;
import net.f4fs.persistence.data.VDHTOperations;
import net.f4fs.persistence.data.VersionedDHTOperations;


/**
 * Factory to retrieve multiple adapters which differ in how
 * they manage data storage in the DHT
 */
public class PersistenceFactory {

    private static DHTOperations           dhtOperations;
    private static VDHTOperations          vDhtOperations;
    private static VersionedDHTOperations  versionedDhtOperations;
    private static DirectPathOperations    directPathOperations;
    private static ConsensusPathOperations consensusPathOperations;

    private PersistenceFactory() {
    }

    /**
     * Returns an adapter to write, read, remove
     * data from the DHT. <br>
     * <b>Note:</b> No versions of stored data are supported
     * 
     * @return An adapter to store data without versions
     */
    public synchronized static IDataPersistence getDhtOperations() {
        if (null == dhtOperations) {
            dhtOperations = new DHTOperations();
        }

        return dhtOperations;
    }

    public synchronized static IDataPersistence getVdhtOperations() {
        if (null == vDhtOperations) {
            vDhtOperations = new VDHTOperations();
        }

        return vDhtOperations;
    }

    public synchronized static IDataPersistence getVersionedDhtOperations() {
        if (null == versionedDhtOperations) {
            versionedDhtOperations = new VersionedDHTOperations();
        }

        return versionedDhtOperations;
    }

    /**
     * Returns an adapter to store, get and remove
     * paths of files directly from the DHT
     * 
     * @return The adapter
     */
    public synchronized static IPathPersistence getDirectPathOperations() {
        if (null == directPathOperations) {
            directPathOperations = new DirectPathOperations();
        }

        return directPathOperations;
    }

    /**
     * Returns an adapter to store, get and remove
     * paths of files through a consensus mechanism from the DHT,
     * i.e. peers agree on the returned value or the value is null
     * 
     * @return The adapter
     */
    public synchronized static IPathPersistence getConsensusPathOperations() {
        if (null == consensusPathOperations) {
            consensusPathOperations = new ConsensusPathOperations();
        }

        return consensusPathOperations;
    }
}
