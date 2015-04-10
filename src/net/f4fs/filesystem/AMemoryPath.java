package net.f4fs.filesystem;

import java.io.IOException;
import java.util.logging.Logger;

import net.f4fs.fspeer.FSPeer;
import net.fusejna.StructStat.StatWrapper;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


public abstract class AMemoryPath {

    /**
     * Logger instance
     */
    private static final Logger logger = Logger.getLogger("AMemoryPath.class");

    private String              name;
    private MemoryDirectory     parent;
    /**
     * The peer which mounted this file system
     */
    private FSPeer              peer;

    public AMemoryPath(final String name, final FSPeer peer) {
        this(name, null, peer);
    }

    public AMemoryPath(final String name, final MemoryDirectory parent, final FSPeer peer) {
        this.name = name;
        this.parent = parent;
        this.peer = peer;

        // Store an empty element
        try {
            // If some data already exists in the DHT, do not update the value of the key
            // (E.g. could be the case, when invoked from FSFileSyncer)
            FutureGet futureGet = peer.getData(Number160.createHash(getPath()));
            futureGet.await();
            
            if (null != futureGet.data()) {
                logger.info("MemoryPath " + name + " already existed on path " + getPath());
                return;
            }
            
            FuturePut futurePut = peer.putData(Number160.createHash(getPath()), new Data(""));
            futurePut.await();
            futurePut = peer.putPath(Number160.createHash(getPath()), new Data(getPath()));
            futurePut.await();

            logger.info("Created new MemoryPath " + name + " successfully on path " + getPath());
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            logger.warning("Could not create MemoryPath " + name + ". Message: " + e.getMessage());
        }
    }

    public synchronized void delete() {
        if (parent != null) {
            parent.deleteChild(this);
            parent = null;
            try {
                FutureRemove futureRemove = peer.removeData(Number160.createHash(getPath()));
                futureRemove.await();
                futureRemove = peer.removePath(Number160.createHash(getPath()));
                futureRemove.await();
            } catch (InterruptedException e) {
                logger.warning("Could not delete MemoryPath " + name + ". Message: " + e.getMessage());
            }
        }
    }

    protected AMemoryPath find(String path) {
        path.replace("^/*", ""); // removes / suffixes.
        if (path.equals(name) || path.isEmpty()) {
            return this;
        }
        return null;
    }

    protected abstract void getattr(StatWrapper stat);

    /**
     * Renames the memory path to the given name
     * 
     * @param newName The new name
     */
    public void rename(String newName) {
        while (newName.startsWith("/")) {
            newName = newName.substring(1);
        }

        String oldName = this.name;
        try {
            FutureGet futureGet = peer.getData(Number160.createHash(getPath()));
            futureGet.await();

            Object content = new Object();
            if (null == futureGet.data()) {
                // memoryPath is a directory and has no content
                content = new String("");
            } else {
                content = futureGet.data().object();
            }

            // remove content key and the corresponding value from the dht
            FutureRemove futureRemove = peer.removeData(Number160.createHash(getPath()));
            futureRemove.await();
            futureRemove = peer.removePath(Number160.createHash(getPath()));
            futureRemove.await();
            
            name = newName;

            // update content key and store the files content on the updated key again
            FuturePut futurePut = peer.putData(Number160.createHash(getPath()), new Data(content));
            futurePut.await();
            futurePut = peer.putPath(Number160.createHash(getPath()), new Data(getPath()));
            futurePut.await();
        } catch (InterruptedException | ClassNotFoundException | IOException e) {
            logger.warning("Could not rename to " + newName + ". Message: " + e.getMessage());
            // reset in case renaming didn't work as expected
            name = oldName;
        }
    }


    /**
     * Returns the name of this path segment
     * 
     * @return The name
     */
    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public MemoryDirectory getParent() {
        return parent;
    }

    public void setParent(MemoryDirectory pParent) {
        parent = pParent;
    }

    public FSPeer getPeer() {
        return this.peer;
    }

    public void setPeer(FSPeer peer) {
        this.peer = peer;
    }

    /**
     * Returns the path of this path segment (incl. its name)
     * 
     * @return The memory path
     */
    public String getPath() {
        String path = this.name;

        if (null != this.parent) {
            if ("/" != this.parent.getPath()) {
                path = this.parent.getPath() + "/" + path;
            } else {
                path = "/" + path;
            }
        }

        return path;
    }
}
