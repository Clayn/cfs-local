package net.bplaced.clayn.cfs.impl.local;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import net.bplaced.clayn.cfs.CFileSystem;
import net.bplaced.clayn.cfs.Directory;
import net.bplaced.clayn.cfs.FileAttributes;
import net.bplaced.clayn.cfs.SimpleFile;
import net.bplaced.clayn.cfs.err.CFSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Clayn
 * @since 0.1
 * @version $Revision: 331 $
 */
public class CFSSimpleFileImpl implements SimpleFile
{

    private static final Logger LOG=LoggerFactory.getLogger(CFSSimpleFileImpl.class);
    private final Path realFile;
    private final Directory parent;
    private final Charset charset;
    private final CFileSystem filesystem;

    CFSSimpleFileImpl(Path realFile, Directory parent, Charset set)
    {
        this.realFile = realFile;
        this.parent = parent;
        this.charset = set;
        filesystem = ((CFSDirectoryImpl) parent).getCfs();
    }

    @Override
    public boolean exists()
    {
        return Files.exists(realFile);
    }

    @Override
    public void create() throws IOException
    {
        Files.createFile(realFile);
    }

    @Override
    public void delete() throws IOException
    {
        if (!exists())
        {
            return;
        }
        Files.delete(realFile);
    }

    @Override
    public InputStream openRead() throws IOException
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("Open {0} for reading",getPath());
        }
        if (filesystem.getFileSettings().getCreateOnAccess())
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("Create {0} before accessing");
            }
            createSafe();
        }
        return Files.newInputStream(realFile);
    }

    @Override
    public OutputStream openWrite() throws IOException
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("Open {0} for writing",getPath());
        }
        if (filesystem.getFileSettings().getCreateOnAccess())
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("Create {0} before accessing");
            }
            createSafe();
        }
        return Files.newOutputStream(realFile);
    }

    @Override
    public OutputStream openAppend() throws IOException
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("Open {0} for appending",getPath());
        }
        if (filesystem.getFileSettings().getCreateOnAccess())
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("Create {0} before accessing");
            }
            createSafe();
        }
        return Files.newOutputStream(realFile, StandardOpenOption.APPEND);
    }

    @Override
    public Directory getParent()
    {
        return parent;
    }

    File getFile()
    {
        return realFile.toFile();
    }

    @Override
    public String getName()
    {
        return realFile.toFile().getName();
    }

    @Override
    public long getSize() throws IOException
    {
        return Files.size(realFile);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.realFile);
        hash = 37 * hash + Objects.hashCode(this.parent);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final CFSSimpleFileImpl other = (CFSSimpleFileImpl) obj;
        if (!Objects.equals(this.realFile, other.realFile))
        {
            return false;
        }
        return Objects.equals(this.parent, other.parent);
    }

    @Override
    public Charset getCharset()
    {
        return charset;
    }

    @Override
    public String toString()
    {
        return parent.toString() + getName();
    }

    @Override
    public FileAttributes getFileAttributes()
    {
        return new FileAttributes()
        {
            BasicFileAttributes bfa;

            private void update()
            {
                if(!exists())
                    return;
                try
                {
                    bfa = Files.readAttributes(realFile,
                            BasicFileAttributes.class);
                } catch (IOException ex)
                {
                    throw new CFSException(ex);
                }
            }

            @Override
            public long lastModified()
            {
                update();
                return bfa==null?-1:bfa.lastModifiedTime().toMillis();
            }

            @Override
            public long creationTime()
            {
                update();
                return bfa==null?-1:bfa.creationTime().toMillis();
            }

            @Override
            public long lastUsed()
            {
                update();
                return bfa==null?-1:bfa.lastAccessTime().toMillis();
            }
        };
    }

    @Override
    public String getPath()
    {
        return toString();
    }
}
