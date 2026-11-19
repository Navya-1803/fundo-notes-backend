package com.fundoo.notes.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExcelTestFilesTest {

    @Test
    void testWorkbook_IsXlsx() throws Exception {
        byte[] bytes = ExcelTestFiles.workbook(new String[]{"Title", "Body", "a@example.com", "no", "no"});
        // xlsx files are zip archives starting with "PK"
        assertEquals('P', bytes[0]);
        assertEquals('K', bytes[1]);
    }
}
