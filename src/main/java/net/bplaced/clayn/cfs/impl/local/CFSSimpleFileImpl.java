package net.bplaced.clayn.cfs.impl.local;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import net.bplaced.clayn.cfs.CFileSystem;
import net.bplaced.clayn.cfs.Directory;
import net.bplaced.clayn.cfs.SimpleFile;

/**
 *
 * @author Clayn
 * @since 0.1
 * @version $Revision: 331 $
 */
public class CFSSimpleFileImpl implements SimpleFile
{

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
        Files.delete(realFile);
    }

    @Override
    public InputStream openRead() throws IOException
    {
        if (filesystem.getFileSettings().getCreateOnAccess())
        {
            createSafe();
        }
        return Files.newInputStream(realFile);
    }

    @Override
    public OutputStream openWrite() throws IOException
    {
        if (filesystem.getFileSettings().getCreateOnAccess())
        {
            createSafe();
        }
        return Files.newOutputStream(realFile);
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
        return parent.toString()+getName();
    }
    
    
}
