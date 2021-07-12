/*
 * Copyright (c) 2021, Roger Araújo, All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package com.rogeraraujo.jwtcodec.components;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class to exercise the functionality of the ListTableModel class.
 * It also exercises the TableFormat interface, which is directly intertwined
 * with the ListTableModel class.
 */
@Slf4j
class ListTableModelTest {
    public static class IntegerStringPair extends KeyValue<Integer, String> {
        public IntegerStringPair(Integer key, String value) {
            super(key, value);
        }
    }
    
    public static class IntegerStringPairFormat implements
            TableFormat<IntegerStringPair> {
        public static String[] COLUMN_NAMES = { "Key", "Value" };

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public Class<?> getColumnClass(int column) {
            switch (column) {
                case 0:  // Key
                    return Integer.class;
                case 1:  // Value
                    return String.class;
            }

            throw new IllegalArgumentException(
                "Column [" + column + "] is invalid");
        }

        @Override
        public Object getValueAt(IntegerStringPair baseObject,
                int row, int column) {
            switch (column) {
                case 0:  // Key
                    return (baseObject != null) ?
                        baseObject.getKey() : null;
                case 1:  // Value
                    return (baseObject != null) ?
                        baseObject.getValue() : null;
            }

            throw new IllegalArgumentException(
                "Column [" + column + "] is invalid");
        }
    }
    
    public static ListTableModel<IntegerStringPair> createTableModel() {
        return new ListTableModel<>(new ArrayList<>(),
            new IntegerStringPairFormat());
    }

    @Test
    public void getColumnNameTest() {
        ListTableModel<IntegerStringPair> tableModel = createTableModel();
        assertEquals(tableModel.getColumnName(0),
            IntegerStringPairFormat.COLUMN_NAMES[0]);
        assertEquals(tableModel.getColumnName(1),
            IntegerStringPairFormat.COLUMN_NAMES[1]);
        assertThrows(RuntimeException.class,
            () -> tableModel.getColumnName(2));
    }

    @Test
    public void getColumnClassTest() {
        ListTableModel<IntegerStringPair> tableModel = createTableModel();
        assertEquals(tableModel.getColumnClass(0), Integer.class);
        assertEquals(tableModel.getColumnClass(1), String.class);
        assertThrows(RuntimeException.class, () -> tableModel.getColumnClass(2));
    }

    @Test
    public void getRowCountTest() {
        ListTableModel<IntegerStringPair> tableModel = createTableModel();
        assertEquals(tableModel.getRowCount(), 0);

        tableModel.addRow(new IntegerStringPair(1, "a"), false);
        tableModel.addRow(new IntegerStringPair(2, "b"), false);
        assertEquals(tableModel.getRowCount(), 2);

        tableModel.removeRow(tableModel.getRowCount() - 1, false);
        assertEquals(tableModel.getRowCount(), 1);
    }

    @Test
    public void getColumnCountTest() {
        ListTableModel<IntegerStringPair> tableModel = createTableModel();
        assertEquals(tableModel.getColumnCount(), 2);
    }

    @Test
    public void getValueAtTest() {
        ListTableModel<IntegerStringPair> tableModel = createTableModel();
        assertThrows(RuntimeException.class, () -> tableModel.getValueAt(0, 0));

        tableModel.addRow(new IntegerStringPair(1, "a"), false);
        assertEquals(tableModel.getValueAt(0, 0), 1);
        assertEquals(tableModel.getValueAt(0, 1), "a");
        assertThrows(RuntimeException.class, () -> tableModel.getValueAt(0, 2));

        tableModel.addRow(new IntegerStringPair(2, "b"), false);
        assertEquals(tableModel.getValueAt(1, 0), 2);
        assertEquals(tableModel.getValueAt(1, 1), "b");
        assertThrows(RuntimeException.class, () -> tableModel.getValueAt(1, 2));
    }
}
