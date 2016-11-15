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
package net.bplaced.clayn.cfs.impl.local;

import java.util.ArrayList;
import java.util.Arrays;
import net.bplaced.clayn.cfs.CFileSystem;
import net.bplaced.clayn.cfs.test.DirectoryTest;
import net.bplaced.clayn.test.base.local.LocalBaseTest;

/**
 *
 * @author Clayn <clayn_osmato@gmx.de>
 */
public class LocalDirectoryTest extends DirectoryTest implements LocalBaseTest
{

    public LocalDirectoryTest()
    {
        ArrayList<String> list = new ArrayList<>(Arrays.asList(TEST_ALL));
        list.remove(TEST_DELETE);
        runningTests.addAll(list);
    }

    @Override
    public CFileSystem getFileSystem() throws Exception
    {
        return getLocalFileSystem();
    }

}
