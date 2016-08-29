package net.bplaced.clayn.cfs.impl.local;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import net.bplaced.clayn.cfs.ActiveDirectory;
import net.bplaced.clayn.cfs.CFileSystem;
import net.bplaced.clayn.cfs.FileSettings;
import net.bplaced.clayn.cfs.SimpleFileSettings;

/**
 * A {@link CFileSystem} implementation that uses the local filesystem. Files
 * and directories created by this filesystem may not be created locally. The
 * separation of the directories is platform independent. Paths for this
 * filesystem follow the unix stlye which means directories are separated by
 * {@code '/'}, the current directory is {@code '.'} and {@code '..'} is the
 * parent one. Paths starting with {@code '/'} will be relative to the root no
 * matter in which directory used. The implementation provides
 * {@link ActiveDirectory active directories} to getting informed when files are
 * changed.
 *
 * @author Clayn
 * @since 0.1
 * @version $Revision: 331 $
 */
public class ClaynFileSystem implements CFileSystem
{

    final FileSettings SETTINGS = new SimpleFileSettings();
    private Charset charset;

    private final ActiveDirectory root;

    /**
     * Creates a new filesystem with the given directory as root directory. The
     * filesystem tries to create the root directory. So you can be sure either
     * the given directory will exist or an exception gets thrown
     *
     * @param root
     * @throws IOException
     */
    public ClaynFileSystem(File root) throws IOException
    {
        this.root = new CFSDirectoryImpl(this, root, null, null);
        this.root.mkDirs();
    }

    /**
     * {@inheritDoc }
     *
     * @return {@code true}
     */
    @Override
    public boolean isActiveDirectorySupported()
    {
        return true;
    }

    @Override
    public ActiveDirectory getActiveRoot() throws IOException
    {
        return getRoot();
    }

    @Override
    public ActiveDirectory getActiveDirectory(String path) throws IOException
    {
        return getDirectory(path);
    }

    @Override
    public ActiveDirectory getDirectory(String path) throws IOException
    {
        return root.changeDirectory(path);
    }

    @Override
    public ActiveDirectory getRoot() throws IOException
    {
        return root;
    }

    /**
     * {@inheritDoc }
     *
     * @return {@inheritDoc }
     */
    @Override
    public FileSettings getFileSettings()
    {
        return SETTINGS;
    }

    /**
     * Sets the charset for this filesystem. {@code null} values are allowed.
     *
     * @param charset the charset for the filesystem. If {@code null}
     * {@link Charset#defaultCharset()} will be used.
     */
    public void setCharset(Charset charset)
    {
        this.charset = charset == null ? Charset.defaultCharset() : charset;
    }

    public Charset getCharset()
    {
        return charset;
    }

}
