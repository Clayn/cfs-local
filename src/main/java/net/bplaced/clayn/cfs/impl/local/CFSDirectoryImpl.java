package net.bplaced.clayn.cfs.impl.local;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.bplaced.clayn.cfs.AbstractActiveDirectory;
import net.bplaced.clayn.cfs.ActiveDirectory;
import net.bplaced.clayn.cfs.CFileSystem;
import net.bplaced.clayn.cfs.Deletable;
import net.bplaced.clayn.cfs.Directory;
import net.bplaced.clayn.cfs.FileModification;
import net.bplaced.clayn.cfs.SimpleFile;
import net.bplaced.clayn.cfs.SimpleFileFilter;
import net.bplaced.clayn.cfs.util.IOUtils;

/**
 * Implementation of an {@link ActiveDirectory} that represents a directory in
 * the local filesystem. The notification of filechanges depends on the
 * underlying filesystem and the reportings from it. A directory should never be
 * created other than with the {@link CFileSystem filesystem}.
 *
 * @author Clayn
 * @since 0.1
 * @version $Revision: 331 $
 */
public class CFSDirectoryImpl extends AbstractActiveDirectory
{

    private Timer watchTimer;
    private Charset charset=Charset.defaultCharset();
    private final CFileSystem cfs;
    private final ActiveDirectory parent;
    private final File directory;
    private final String partName;
    private WatchService watchService;

    File getDirectory()
    {
        return directory;
    }

    
    
    CFileSystem getCfs()
    {
        return cfs;
    }

    void setCharset(Charset charset)
    {
        this.charset = charset;
    }

    CFSDirectoryImpl(CFileSystem cfs, File dir, ActiveDirectory parent,
            String partName) throws IOException
    {
        this.cfs = cfs;
        this.directory = dir;
        this.parent = parent;
        this.partName = partName;
        installWatch();
    }

    private void installWatch() throws IOException
    {
        if (!exists())
        {
            return;
        }
        FileSystem fs = FileSystems.getDefault();
        watchService = fs.newWatchService();
        directory.toPath().register(watchService, ENTRY_CREATE, ENTRY_DELETE,
                ENTRY_MODIFY);
    }

    @Override
    public ActiveDirectory changeDirectory(String path) throws IOException
    {
        boolean fromRoot = path.startsWith("/");
        path = IOUtils.cleanPath(path);
        String parts[] = path.split("/");
        CFSDirectoryImpl end = this;
        if (fromRoot)
        {
            end = (CFSDirectoryImpl) cfs.getActiveRoot();
        }
        for (String part : parts)
        {
            if (!".".equals(part) && !"..".equals(part))
            {
                end = new CFSDirectoryImpl(cfs, new File(end.directory, part), end,
                        part);
            } else if (".".equals(part))
            {
                //This directory
            } else
            {
                if (end.getParent() == null)
                {
                    throw new IOException(
                            "No parent directory available for 'root' directory");
                }
                end = (CFSDirectoryImpl) end.getParent();
            }
        }

        return end;
    }

    @Override
    public ActiveDirectory getParent()
    {
        return parent;
    }

    @Override
    public SimpleFile getFile(String name) throws IOException
    {
        return new CFSSimpleFileImpl(new File(directory, name).toPath(), this,
                charset);
    }

    @Override
    public String toString()
    {
        if (parent == null)
        {
            return "/";
        }
        return parent.toString() + partName + cfs.getSeparator();
    }

    @Override
    public void mkDir() throws IOException
    {
        if (parent!=null&&!parent.exists())
        {
            throw new IOException("Parent " + parent + " does not exist");
        }
        System.out.println("Directory: "+directory.toPath());
        Files.createDirectory(directory.toPath());
    }

    @Override
    public boolean exists()
    {
        return Files.exists(directory.toPath());
    }

    @Override
    public List<SimpleFile> listFiles(SimpleFileFilter sff) throws IOException
    {
        return Files.list(directory.toPath()).map(Path::toFile).filter(
                File::isFile).map(
                        this::createFile).filter(sff).collect(
                Collectors.toList());
    }

    private SimpleFile createFile(File f)
    {
        return new CFSSimpleFileImpl(f.toPath(), this, charset);
    }

    @Override
    public synchronized void deactivate()
    {
        watchTimer.cancel();
    }

    @Override
    public synchronized void activate()
    {
        if (watchTimer != null)
        {
            return;
        }
        if (!exists() || watchService == null)
        {
            return;
        }
        watchTimer = new Timer();
        Directory dir = this;
        watchTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                WatchKey poll = watchService.poll();
                long time = System.currentTimeMillis();
                if (poll == null)
                {
                    return;
                }
                poll.pollEvents().stream().forEach((evt)
                        -> 
                        {
                            if (evt.kind() == ENTRY_CREATE)
                            {
                                Path p = (Path) evt.context();
                                Optional.ofNullable(onCreate).ifPresent(
                                        (oc) -> oc.accept(new FileModification(
                                                new CFSSimpleFileImpl(p, dir,
                                                        charset),
                                                FileModification.Modification.CREATE,
                                                time)));
                            } else if (evt.kind() == ENTRY_DELETE)
                            {
                                Path p = (Path) evt.context();
                                Optional.ofNullable(onDelete).ifPresent(
                                        (oc) -> oc.accept(new FileModification(
                                                new CFSSimpleFileImpl(p, dir,
                                                        charset),
                                                FileModification.Modification.DELETE,
                                                time)));
                            } else if (evt.kind() == ENTRY_MODIFY)
                            {
                                Path p = (Path) evt.context();
                                Optional.ofNullable(onModification).ifPresent(
                                        (oc) -> oc.accept(new FileModification(
                                                new CFSSimpleFileImpl(p, dir,
                                                        charset),
                                                FileModification.Modification.MODIFY,
                                                time)));
                            }
                });
            }
        }, 100, 100);
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.parent);
        hash = 97 * hash + Objects.hashCode(this.partName);
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
        final CFSDirectoryImpl other = (CFSDirectoryImpl) obj;
        if (!Objects.equals(this.partName, other.partName))
        {
            return false;
        }
        return Objects.equals(this.parent, other.parent);
    }

    @Override
    public List<Directory> listDirectories() throws IOException
    {
        if (!exists())
        {
            return new ArrayList<>();
        }
        ActiveDirectory dir = this;
        return Arrays.stream(directory.listFiles()).filter(File::isDirectory).map(
                (File t)
                -> 
                {
                    try
                    {
                        String parts[] = t.toString().split(
                                "\\"+File.separator);
                        return new CFSDirectoryImpl(cfs, t, dir,
                                parts.length == 0 ? null : parts[parts.length - 1]);
                    } catch (IOException ex)
                    {
                        Logger.getLogger(
                                CFSDirectoryImpl.class.getName()).log(
                                        Level.SEVERE, null, ex);
                        throw new RuntimeException(ex);
                    }
        }).collect(Collectors.toList());
    }

    @Override
    public void delete() throws IOException
    {
        deleteDirectory(directory);
    }

    private void delete0(Deletable del)
    {
        try
        {
            del.delete();
        } catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private void deleteDirectory(File f) throws IOException
    {
        Stream.concat(listDirectories().stream(), listFiles().stream()).forEach(
                this::delete0);
        if (!exists())
        {
            return;
        }
        if (!f.delete())
        {
            throw new IOException("Failed to delete " + this);
        }
    }

    @Override
    public String getName()
    {
        return parent == null ? "/" : partName;
    }

}
