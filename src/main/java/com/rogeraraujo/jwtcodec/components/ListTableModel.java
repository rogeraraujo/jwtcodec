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

import lombok.Getter;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * A TableModel that stores rows in an instance of java.util.List and delegates
 * some cell-related tasks (e.g., column names and classes, reading the values
 * of specific cells) to an instance of the TableFormat interface.
 *
 * @param <T> Type of the rows stored
 */
public class ListTableModel<T> extends AbstractTableModel {
    @Getter
    private List<T> items;

    @Getter
    private TableFormat<T> format;

    public ListTableModel(List<T> items, TableFormat<T> format) {
        if (format == null) {
            throw new IllegalArgumentException("Table format cannot be null");
        }

        this.items = (items != null) ? items : new ArrayList<>();
        this.format = format;
    }

    /**
     * Adds a row to the model, optionally notifying registered listeners.
     *
     * @param newRow Row to add
     * @param notify Flag indicating whether registered listeners should be
     *               notified
     *
     * @return true (as specified by Collection.add(T))
     */
    public boolean addRow(T newRow, boolean notify) {
        int rows = items.size();
        boolean result = items.add(newRow);

        if (notify) {
            fireTableRowsInserted(rows, rows);
        }

        return result;
    }

    /**
     * Removes a row from the model, optionally notifying registered listeners.
     *
     * @param rowIndex Index of the row to remove
     * @param notify Flag indicating whether registered listeners should be
     *               notified
     *
     * @return The row that was removed from the model
     */
    public T removeRow(int rowIndex, boolean notify) {
        T result = items.remove(rowIndex);

        if (notify) {
            fireTableRowsDeleted(rowIndex, rowIndex);
        }

        return result;
    }

    /**
     * Removes a row from the model, optionally notifying registered listeners.
     *
     * @param row Row to remove
     * @param notify Flag indicating whether registered listeners should be
     *               notified
     *
     * @return true if the model contained the specified row
     */
    public boolean removeRow(T row, boolean notify) {
        int rowIndex = items.indexOf(row);

        if (rowIndex == -1) {
            return false;
        }

        items.remove(rowIndex);

        if (notify) {
            fireTableRowsDeleted(rowIndex, rowIndex);
        }

        return true;
    }

    /**
     * Checks whether the model contains the specified row.
     *
     * @param row Row to check
     *
     * @return true if the model contains the specified row
     */
    public boolean contains(T row) {
        return items.contains(row);
    }

    /**
     * Returns a row of the model.
     *
     * @param rowIndex Index of the row to return
     *
     * @return The row at the specified index
     */
    public T getRow(int rowIndex) {
        return items.get(rowIndex);
    }

    /**
     * Updates a row of the model.
     *
     * @param rowIndex Index of the row to update
     * @param value New value for the row
     * @param notify Flag indicating whether registered listeners should be
     *               notified
     *
     * @return The row previously contained in the model
     */
    public T setRow(int rowIndex, T value, boolean notify) {
        T result = items.set(rowIndex, value);

        if (notify) {
            fireTableRowsUpdated(rowIndex, rowIndex);
        }

        return result;
    }

    @Override
    public String getColumnName(int column) {
        return format.getColumnName(column);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return format.getColumnClass(columnIndex);
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return format.getColumnCount();
    }

    @Override
    public Object getValueAt(int row, int column) {
        return format.getValueAt(items.get(row), row, column);
    }
}
