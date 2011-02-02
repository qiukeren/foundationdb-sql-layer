/**
 * Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

package com.akiban.cserver.store;

import com.akiban.ais.model.HKey;
import com.akiban.ais.model.HKeySegment;
import com.akiban.ais.model.UserTable;
import com.akiban.cserver.CServerUtil;
import com.akiban.cserver.InvalidOperationException;
import com.akiban.cserver.RowData;
import com.akiban.cserver.RowDef;
import com.akiban.cserver.api.dml.scan.LegacyRowWrapper;
import com.akiban.cserver.api.dml.scan.NewRow;
import com.persistit.Exchange;
import com.persistit.Key;
import com.persistit.Value;
import com.persistit.exception.PersistitException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TreeRecordVisitor
{
    public void visit() throws PersistitException, InvalidOperationException
    {
        NewRow row = row();
        Object[] key = key(row.getRowDef());
        visit(key, row);
    }

    public abstract void visit(Object[] key, NewRow row);

    public void initialize(PersistitStore store, Exchange exchange)
    {
        this.store = store;
        this.exchange = exchange;
        for (RowDef rowDef : store.rowDefCache.getRowDefs()) {
            if (rowDef.isUserTable()) {

                UserTable table = rowDef.userTable();
                if (!table.getName().getSchemaName().equals("akiba_information_schema")) {
                    // Not sure why ais types show up when run from maven, but not run from intellij.
                    ordinalToTable.put(rowDef.getOrdinal(), table);
                }
            }
        }
    }

    private NewRow row() throws PersistitException, InvalidOperationException
    {
        Value value = exchange.getValue();
        int rowDefId = CServerUtil.getInt(value.getEncodedBytes(), RowData.O_ROW_DEF_ID - RowData.LEFT_ENVELOPE_SIZE);
        rowDefId = store.treeService.storeToAis(exchange.getVolume(), rowDefId);
        RowDef rowDef = store.rowDefCache.getRowDef(rowDefId);
        RowData rowData = new RowData(EMPTY_BYTE_ARRAY);
        store.expandRowData(exchange, rowDef, rowData);
        return new LegacyRowWrapper(rowData);
    }

    private Object[] key(RowDef rowDef)
    {
        // Key traversal
        Key key = exchange.getKey();
        int keySize = key.getDepth();
        // HKey traversal
        HKey hKey = rowDef.userTable().hKey();
        List<HKeySegment> hKeySegments = hKey.segments();
        int k = 0;
        // Traverse key, guided by hKey, populating result
        Object[] keyArray = new Object[keySize];
        int h = 0;
        while (k < hKeySegments.size()) {
            HKeySegment hKeySegment = hKeySegments.get(k++);
            UserTable table = hKeySegment.table();
            int ordinal = (Integer) key.decode();
            assert ordinalToTable.get(ordinal) == table : ordinalToTable.get(ordinal);
            keyArray[h++] = table;
            for (int i = 0; i < hKeySegment.columns().size(); i++) {
                keyArray[h++] = key.decode();
            }
        }
        return keyArray;
    }

    private final static byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private PersistitStore store;
    private Exchange exchange;
    private final Map<Integer, UserTable> ordinalToTable = new HashMap<Integer, UserTable>();
}
