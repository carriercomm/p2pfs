package net.f4fs.filesystem;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.f4fs.fspeer.FSPeer;
import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.ModeWrapper;
import net.fusejna.util.FuseFilesystemAdapterFull;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


/*
 * TODO: 
 * Put and Get of files into file system
 * 
 */
public class P2PFS
        extends FuseFilesystemAdapterFull {

    private final MemoryDirectory rootDirectory = new MemoryDirectory("");
    private FSPeer                mPeer;

    public P2PFS(FSPeer pPeer)
            throws IOException {
        mPeer = pPeer;

        String filename = "README.txt";
        String filecontent = "Welcome to the p2p filesystem of f4fs.";
        rootDirectory.add(new MemoryFile(filename, filecontent));

        mPeer.put(Number160.createHash(filename), new Data(filecontent));
    }

    @Override
    public int access(final String path, final int access) {
        return 0;
    }

    @Override
    public int create(final String path, final ModeWrapper mode, final FileInfoWrapper info) {
        if (getPath(path) != null) {
            return -ErrorCodes.EEXIST();
        }
        final AMemoryPath parent = getParentPath(path);
        if (parent instanceof MemoryDirectory) {
            ((MemoryDirectory) parent).mkfile(getLastComponent(path));
            return 0;
        }

        return -ErrorCodes.ENOENT();
    }

    @Override
    public int getattr(final String path, final StatWrapper stat) {
        final AMemoryPath p = getPath(path);
        if (p != null) {
            p.getattr(stat);
            return 0;
        }
        return -ErrorCodes.ENOENT();
    }

    private String getLastComponent(String path) {
        while (path.substring(path.length() - 1).equals("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path.isEmpty()) {
            return "";
        }
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private AMemoryPath getParentPath(final String path) {
        return rootDirectory.find(path.substring(0, path.lastIndexOf("/")));
    }

    private AMemoryPath getPath(final String path) {
        return rootDirectory.find(path);
    }

    @Override
    public int mkdir(final String path, final ModeWrapper mode) {
        if (getPath(path) != null) {
            return -ErrorCodes.EEXIST();
        }
        final AMemoryPath parent = getParentPath(path);
        if (parent instanceof MemoryDirectory) {
            ((MemoryDirectory) parent).mkdir(getLastComponent(path));
            return 0;
        }

        return -ErrorCodes.ENOENT();
    }

    @Override
    public int open(final String path, final FileInfoWrapper info) {
        return 0;
    }

    @Override
    public int read(final String path, final ByteBuffer buffer, final long size, final long offset, final FileInfoWrapper info) {
        final AMemoryPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof MemoryFile)) {
            return -ErrorCodes.EISDIR();
        }

        System.out.println("BLABLA2");
        return ((MemoryFile) p).read(buffer, size, offset);
    }

    @Override
    public int readdir(final String path, final DirectoryFiller filler) {
        final AMemoryPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof MemoryDirectory)) {
            return -ErrorCodes.ENOTDIR();
        }
        ((MemoryDirectory) p).read(filler);
        return 0;
    }

    @Override
    public int rename(final String path, final String newName) {
        final AMemoryPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        final AMemoryPath newParent = getParentPath(newName);
        if (newParent == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(newParent instanceof MemoryDirectory)) {
            return -ErrorCodes.ENOTDIR();
        }
        p.delete();
        p.rename(newName.substring(newName.lastIndexOf("/")));
        ((MemoryDirectory) newParent).add(p);
        return 0;
    }

    @Override
    public int rmdir(final String path) {
        final AMemoryPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof MemoryDirectory)) {
            return -ErrorCodes.ENOTDIR();
        }
        p.delete();
        return 0;
    }

    @Override
    public int truncate(final String path, final long offset) {
        final AMemoryPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof MemoryFile)) {
            return -ErrorCodes.EISDIR();
        }
        ((MemoryFile) p).truncate(offset);
        return 0;
    }

    @Override
    public int unlink(final String path) {
        final AMemoryPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        p.delete();
        return 0;
    }

    @Override
    public int write(final String path, final ByteBuffer buf, final long bufSize, final long writeOffset,
            final FileInfoWrapper wrapper) {
        final AMemoryPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof MemoryFile)) {
            return -ErrorCodes.EISDIR();
        }
        return ((MemoryFile) p).write(buf, bufSize, writeOffset);
    }
}
