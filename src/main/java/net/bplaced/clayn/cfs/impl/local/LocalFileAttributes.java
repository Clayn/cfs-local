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

import net.bplaced.clayn.cfs.FileAttributes;

/**
 *
 * @author Clayn <clayn_osmato@gmx.de>
 */
public class LocalFileAttributes implements FileAttributes
{

    private long mod;
    private long used;
    private long create;

    void setCreate(long create)
    {
        this.create = create;
    }

    void setMod(long mod)
    {
        this.mod = mod;
    }

    void setUsed(long used)
    {
        this.used = used;
    }

    @Override
    public long lastModified()
    {
        return mod;
    }

    @Override
    public long creationTime()
    {
        return create;
    }

    @Override
    public long lastUsed()
    {
        return used;
    }

}
