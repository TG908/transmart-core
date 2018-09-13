package org.transmartproject.rest.serialization.tabular

import com.opencsv.CSVWriter
import groovy.transform.CompileStatic
import org.transmartproject.core.users.User

import java.time.Instant
import java.util.zip.ZipOutputStream

@CompileStatic
class AbstractTSVSerializer {

    final static char COLUMN_SEPARATOR = '\t' as char

    final User user
    final ZipOutputStream zipOutStream

    AbstractTSVSerializer(User user, ZipOutputStream zipOutputStream) {
        this.user = user
        this.zipOutStream = zipOutputStream
    }

    protected String[] formatRowValues(List<? extends Object> valuesRow) {
        def s = new String[valuesRow.size()]

        for (int i = 0; i < s.length; i++) {
            def value = valuesRow[i]
            if (value == null) continue
            if (value instanceof Date) {
                s[i] = Instant.ofEpochMilli(((Date) value).time).toString()
            } else {
                s[i] = value.toString()
            }
        }

        return s
    }

    void writeRow(CSVWriter csvWriter, List row) {
        csvWriter.writeNext(formatRowValues(row))
    }

    CSVWriter getCSVWriter() {
        new CSVWriter(new OutputStreamWriter(zipOutStream), COLUMN_SEPARATOR)
    }
}
