/*
 * Copyright (C) 2016 Clayn <clayn_osmato@gmx.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.bplaced.clayn.test.base.local;

import java.io.File;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import net.bplaced.clayn.cfs.CFileSystem;
import net.bplaced.clayn.cfs.impl.local.ClaynFileSystem;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Clayn <clayn_osmato@gmx.de>
 */
public interface LocalBaseTest
{

    public static final TemporaryFolder FOLDER = new TemporaryFolder();
    public static final BooleanProperty CREATED = new SimpleBooleanProperty(
            false);

    public default CFileSystem getLocalFileSystem() throws Exception
    {
        if (!CREATED.get())
        {
            FOLDER.create();
            CREATED.set(true);
        }
        File root = FOLDER.newFolder();
        return new ClaynFileSystem(root);
    }

}
