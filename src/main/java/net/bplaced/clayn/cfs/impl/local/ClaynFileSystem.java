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

    /**
     * Creates a new CFileSystem that operates on the local file system. The
     * root for this filesystem is the directory returned by {@code user.dir}
     *
     * @return a new CFileSystem with {@code user.dir} as root
     * @throws IOException if an I/O Exception occures
     * @since 0.3.0
     */
    public static CFileSystem getLocalFileSystem() throws IOException
    {
        return new ClaynFileSystem(new File(System.getProperty("user.dir")));
    }

    /**
     * Creates a new CFileSystem that operates on the local filesystem. The root
     * for this filesystem will be a directory with the given name inside of the
     * OS directory meant for user data. For windows this is
     * {@code System.getenv("APPDATA")} and {@code user.home} for all other
     * operating system.
     *
     * @param name the name that should be used for the filesystems directory
     * @return a new CFileSystem with directory {@code name} inside the app directory
     * @throws IOException if an I/O Exception occures
     * @since 0.3.0
     */
    public static CFileSystem getAppDataFileSystem(String name) throws IOException
    {
        return new ClaynFileSystem(new File(getAppDataDir(), name));
    }

    private static File getAppDataDir()
    {
        boolean windows = System.getProperty("os.name").toLowerCase().contains(
                "windows");
        String dirName = windows ? System.getenv("APPDATA") : System.getProperty(
                "user.home");
        return new File(dirName);
    }

    @Override
    public CFileSystem subFileSystem(String dir) throws IOException
    {
        CFSDirectoryImpl nRoot=(CFSDirectoryImpl) getDirectory(dir);
        nRoot.mkDirs();
        return new ClaynFileSystem(nRoot.getDirectory());
    }
}
