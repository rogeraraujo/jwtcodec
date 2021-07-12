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
import lombok.Setter;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * A specialized JTable that allows the user to customize the cell rendering
 * components returned by the JTable#getCellRenderer() method.
 */
public class CustomTable extends JTable {
    @Getter @Setter
    private CustomTableCellRenderer customCellRenderer;

    public CustomTable(TableModel tableModel) {
        super(tableModel);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        TableCellRenderer baseRenderer = super.getCellRenderer(row, column);

        if (customCellRenderer != null) {
            customCellRenderer.setBaseRenderer(baseRenderer);
            return customCellRenderer;
        }
        else {
            return baseRenderer;
        }
    }
}
