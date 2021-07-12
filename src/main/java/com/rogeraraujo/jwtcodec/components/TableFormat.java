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

/**
 * An interface that centralizes some cell-related tasks of a TableModel. Meant
 * for use with the ListTableModel class.
 *
 * @param <T> Type of the rows handled
 */
public interface TableFormat<T> {
    /**
     * Returns the number of columns in a JTable.
     *
     * @return Number of columns
     */
    int getColumnCount();

    /**
     * Returns the name of a column in a JTable.
     *
     * @param column Zero-based index of the column whose name must be returned
     *
     * @return The name of the column
     */
    String getColumnName(int column);

    /**
     * Returns the class of the values contained in a column of a JTable.
     *
     * @param column Zero-based index of the column whose class must be returned
     *
     * @return The class of the column
     */
    Class<?> getColumnClass(int column);

    /**
     * Reads the value of a specific cell in a JTable.
     *
     * @param baseObject Base object that represents the entire row denoted by
     *                   the [row] argument
     * @param row Zero-based index of the row of the desired cell
     * @param column Zero-based index of the column of the desired cell
     *
     * @return Value of the cell
     */
    Object getValueAt(T baseObject, int row, int column);
}
