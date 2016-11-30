package org.transmartproject.rest.protobuf

import com.google.protobuf.Empty
import com.google.protobuf.Message
import groovy.util.logging.Slf4j
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.multidimquery.Dimension
import org.transmartproject.core.multidimquery.Hypercube
import org.transmartproject.core.multidimquery.HypercubeValue
import org.transmartproject.db.metadata.DimensionDescription
import org.transmartproject.db.multidimquery.AssayDimension
import org.transmartproject.db.multidimquery.BioMarkerDimension
import org.transmartproject.db.multidimquery.EndTimeDimension
import org.transmartproject.db.multidimquery.LocationDimension
import org.transmartproject.db.multidimquery.ProjectionDimension
import org.transmartproject.db.multidimquery.ProviderDimension
import org.transmartproject.db.multidimquery.StartTimeDimension
import org.transmartproject.db.multidimquery.StudyDimension
import org.transmartproject.db.multidimquery.query.DimensionMetadata

import static com.google.protobuf.util.JsonFormat.*
import static org.transmartproject.rest.hypercubeProto.ObservationsProto.*

@Slf4j
public class ObservationsSerializer {

    enum Format {
        JSON('application/json'),
        PROTOBUF('application/x-protobuf'),
        NONE('none')

        private String format

        Format(String format) {
            this.format = format
        }

        private static final Map<String, Format> mapping = Format.values().collectEntries {
            [(it.format): it]
        }

        public static Format from(String format) {
            if (mapping.containsKey(format)) {
                return mapping[format]
            } else {
                throw new Exception("Unknown format: ${format}")
            }
        }

        public String toString() {
            format
        }
    }

    protected Hypercube cube
    protected Dimension packedDimension
    protected boolean packingEnabled
    protected Printer jsonPrinter
    protected Writer writer
    protected Format format

    protected Map<Dimension, List<Object>> dimensionElements = [:]
    protected Map<Dimension, DimensionDeclaration> dimensionDeclarations = [:]


    ObservationsSerializer(Hypercube cube, Format format, Dimension packedDimension = null) {
        this.cube = cube
        this.packedDimension = packedDimension
        this.packingEnabled = packedDimension != null
        if (format == Format.NONE) {
            throw new InvalidArgumentsException("No format selected.")
        } else if (format == Format.JSON) {
            jsonPrinter = printer()
        }
        this.format = format
    }

    protected boolean first = true

    protected void begin(OutputStream out) {
        first = true
        if (format == Format.JSON) {
            writer = new PrintWriter(new BufferedOutputStream(out))
            writer.print('[')
        }
    }

    protected void writeMessage(OutputStream out, Message message) {
        if (format == Format.JSON) {
            if (!first) {
                writer.print(', ')
            }
            jsonPrinter.appendTo(message, writer)
        } else {
            message.writeDelimitedTo(out)
        }
        if (first) {
            first = false
        }
    }

    protected void end(OutputStream out) {
        if (format == Format.JSON) {
            writer.print(']')
            writer.flush()
        } else {
            out.flush()
        }
    }

    void writeEmptyMessage(OutputStream out) {
        if (format == Format.PROTOBUF) {
            Empty empty = Empty.newBuilder().build()
            empty.writeDelimitedTo(out)
        }
    }

    static Type getFieldType(Class type) {
        if (Float.isAssignableFrom(type)) {
            return Type.DOUBLE
        } else if (Double.isAssignableFrom(type)) {
            return Type.DOUBLE
        } else if (Number.isAssignableFrom(type)) {
            return Type.INT
        } else if (Date.isAssignableFrom(type)) {
            return Type.TIMESTAMP
        } else if (String.isAssignableFrom(type)) {
            return Type.STRING
        } else {
            // refer to objects by their identifier
            return Type.INT
        }
    }

    protected getDimensionsDefs() {
        def declarations = cube.dimensions.collect { dim ->
            def builder = DimensionDeclaration.newBuilder()
            String dimensionName = dim.toString()
            builder.setName(dimensionName)
            if (dim.density == Dimension.Density.SPARSE) {
                // Sparse dimensions are inlined, dense dimensions are referred to by indexes
                // (referring to objects in the footer message).
                builder.setInline(true)
            }
            if (dim == packedDimension) {
                builder.setPacked(true)
            }
            def publicFacingFields = SerializableProperties.SERIALIZABLES.get(dimensionName)
            switch(dim.class) {
                case BioMarkerDimension:
                    break
                case AssayDimension:
                    break
                case ProjectionDimension:
                    break
                case StartTimeDimension:
                case EndTimeDimension:
                    builder.type = Type.TIMESTAMP
                    break
                case ProviderDimension:
                case LocationDimension:
                    builder.type = Type.STRING
                    break
                default:
                    builder.type = Type.OBJECT
                    def metadata = DimensionMetadata.forDimension(dim.class)
                    metadata.fields.each { field ->
                        if (field.fieldName in publicFacingFields) {
                            Class valueType = metadata.fieldTypes[field.fieldName]
                            def fieldDef = FieldDefinition.newBuilder()
                                    .setName(field.fieldName)
                                    .setType(getFieldType(valueType))
                                    .build()
                            builder.addFields(fieldDef)
                        }
                    }
                    break
            }
            def dimensionDeclaration = builder.build()
            dimensionDeclarations[dim] = dimensionDeclaration
            dimensionDeclaration
        }
        declarations
    }

    protected Header buildHeader() {
        Header.newBuilder().addAllDimensionDeclarations(dimensionsDefs).build()
    }

    protected List<HypercubeValue> currentValues = []

    /**
     * Checks if the value can be appended to <code>currentValues</code> to form
     * a combined packed observation message.
     */
    protected boolean canBeAppended(HypercubeValue value) {
        assert packingEnabled && packedDimension != null
        HypercubeValue sampleValue = currentValues[0]
        if (value.value == null) {
            throw new Exception("Null values not supported.")
        }
        boolean dimensionDifferent = cube.dimensions.any { dim ->
            dim != packedDimension && !value[dim].equals(sampleValue[dim])
        }
        if (dimensionDifferent) {
            return false
        }
        def valueIsNumber = value.value instanceof Number
        def sampleIsNumber = sampleValue.value instanceof Number
        if (valueIsNumber != sampleIsNumber) {
            def sampleType = sampleValue.value?.class?.simpleName
            def valueType = value.value?.class?.simpleName
            throw new Exception("Different value types within a packed message not supported: ${sampleType} != ${valueType}")
        }
        return true
    }

    protected void writePackedValues(OutputStream out, List<HypercubeValue> values, boolean last) {
        def message = createPackedCell(values)
        if (last) {
            message.last = last
        }
        writeMessage(out, message.build())
    }

    protected void addToPackedValues(OutputStream out, HypercubeValue value, boolean last) {
        if (currentValues.empty) {
            currentValues.add(value)
        } else if (canBeAppended(value)) {
            currentValues.add(value)
        } else {
            writePackedValues(out, currentValues, false)
            currentValues = [value]
        }
        if (last) {
            writePackedValues(out, currentValues, last)
        }
    }

    protected void writeCells(OutputStream out, Iterator<HypercubeValue> it) {
        while (it.hasNext()) {
            HypercubeValue value = it.next()
            if (packingEnabled) {
                addToPackedValues(out, value, !it.hasNext())
            } else {
                def message = createCell(value)
                def last = !it.hasNext()
                if (last) {
                    message.last = last
                }
                writeMessage(out, message.build())
            }
        }
    }

    protected Observation.Builder createCell(HypercubeValue value) {
        Observation.Builder builder = Observation.newBuilder()
        if (value.value != null) {
            if (value.value instanceof Number) {
                builder.numericValue = value.value as Double
            } else {
                builder.stringValue = value.value
            }
        }
        for (Dimension dim : cube.dimensions) {
            Object dimElement = value[dim]
            if (dim.density == Dimension.Density.SPARSE) {
                // Add the value element inline
                builder.addInlineDimensions(buildDimensionElement(dim, dimElement))
            } else {
                // Add index to footer element inline
                builder.addDimensionIndexes(determineFooterIndex(dim, dimElement))
            }
        }
        builder
    }

    protected PackedObservation.Builder createPackedCell(List<HypercubeValue> values) {
        PackedObservation.Builder builder = PackedObservation.newBuilder()
        assert values.size() > 0
        builder.numObervations = values.size()
        // make sure that packed dimension elements are added to the footer
        values.each { elements ->
            determineFooterIndex(packedDimension, elements[packedDimension])
        }
        if (dimensionElements[packedDimension].size() != values.size()) {
            throw new Exception("Not for every packed dimension element, there is an observation.")
        }
        // serialise packed observation values
        def packedValues = values.collect { it.value }
        if (packedValues.every { it instanceof Number }) {
            builder.addAllNumericValues(packedValues.collect { it as Double })
        } else {
            builder.addAllStringValues(packedValues.collect { it.toString() })
        }
        // serialise shared values of the packed message
        HypercubeValue sampleValue = values[0]
        for (Dimension dim : cube.dimensions) {
            if (dim != packedDimension) {
                Object dimElement = sampleValue[dim]
                if (dim.density == Dimension.Density.SPARSE) {
                    // Create a singleton array with the shared value of all observations
                    List<Object> objects = [sampleValue[dim]]
                    def dimensionElements = buildDimensionElements(dimensionDeclarations[dim], objects, false)
                    builder.addInlineDimensions(dimensionElements)
                } else {
                    // Add index to single footer element inline
                    builder.addDimensionIndexes(determineFooterIndex(dim, dimElement))
                }
            }
        }
        builder
    }

    static buildValue(Type type, Object value) {
        def builder = Value.newBuilder()
        switch (type) {
            case Type.TIMESTAMP:
                def timestampValue = TimestampValue.newBuilder()
                if (value == null) {
                    //
                } else if (value instanceof Date) {
                    timestampValue.val = value.time
                } else {
                    throw new Exception("Type not supported: ${value?.class?.simpleName}.")
                }
                builder.timestampValue = timestampValue.build()
                break
            case Type.DOUBLE:
                def doubleValue = DoubleValue.newBuilder()
                if (value instanceof Float) {
                    doubleValue.val = value.doubleValue()
                } else if (value instanceof Double) {
                    doubleValue.val = value.doubleValue()
                } else {
                    throw new Exception("Type not supported: ${value?.class?.simpleName}.")
                }
                builder.doubleValue = doubleValue.build()
                break
            case Type.INT:
                def intValue = IntValue.newBuilder()
                if (value == null) {
                    //
                } else if (value instanceof Number) {
                    intValue.val = value.longValue()
                } else {
                    Long id = value.getAt('id') as Long
                    if (id != null) {
                        intValue.val = id
                    }
                }
                builder.intValue = intValue.build()
                break
            case Type.STRING:
                def stringValue = StringValue.newBuilder()
                if (value != null) {
                    stringValue.val = value.toString()
                }
                builder.stringValue = stringValue.build()
                break
            default:
                throw new Exception("Type not supported: ${type.name()}.")
        }
        builder.build()
    }

    static addValue(DimElementField.Builder builder, Type type, Object value) {
        switch (type) {
            case Type.TIMESTAMP:
                def timestampValue = TimestampValue.newBuilder()
                if (value == null) {
                    //
                } else if (value instanceof Date) {
                    timestampValue.val = value.time
                } else {
                    throw new Exception("Type not supported for type ${type.name()}: ${value?.class?.simpleName}.")
                }
                builder.addTimestampValue timestampValue.build()
                break
            case Type.DOUBLE:
                def doubleValue = DoubleValue.newBuilder()
                if (value instanceof Float) {
                    doubleValue.val = value.doubleValue()
                } else if (value instanceof Double) {
                    doubleValue.val = value.doubleValue()
                } else {
                    throw new Exception("Type not supported: ${value?.class?.simpleName}.")
                }
                builder.addDoubleValue doubleValue.build()
                break
            case Type.INT:
                def intValue = IntValue.newBuilder()
                if (value == null) {
                    // skip
                } else if (value instanceof Number) {
                    intValue.val = value.longValue()
                } else {
                    Long id = value.getAt('id') as Long
                    if (id != null) {
                        intValue.val = id
                    }
                }
                builder.addIntValue intValue
                break
            case Type.STRING:
                def stringValue = StringValue.newBuilder()
                if (value != null) {
                    stringValue.val = value.toString()
                }
                builder.addStringValue stringValue.build()
                break
            default:
                throw new Exception("Type not supported: ${type.name()}.")
        }
        builder.build()
    }

    protected DimensionElement buildDimensionElement(Dimension dim, Object dimElement) {
        def builder = DimensionElement.newBuilder()
        DimensionDeclaration declaration = dimensionDeclarations[dim]
        if (declaration.type == Type.OBJECT) {
            declaration.fieldsList.each { field ->
                if (dim.class == StudyDimension) {
                    builder.addFields(buildValue(field.type, dimElement[field.name]))
                } else {
                    builder.addFields(buildValue(field.type, dimElement))
                }
            }
        } else {
            builder.addFields(buildValue(declaration.type, dimElement))
        }
        builder.build()
    }

    protected DimensionElements buildDimensionElements(
            DimensionDeclaration declaration,
            List<Object> objects,
            boolean perSample = false) {
        def elementsBuilder = DimensionElements.newBuilder()
        if (declaration.type == Type.OBJECT) {
            declaration.fieldsList.each { field ->
                def elementFieldBuilder = DimElementField.newBuilder()
                objects.each { elements ->
                    elements.each { element ->
                        addValue(elementFieldBuilder, field.type, element[field.name])
                    }
                }
                elementsBuilder.addFields(elementFieldBuilder)
                elementsBuilder.setPerSample(perSample)
            }
        } else {
            def elementFieldBuilder = DimElementField.newBuilder()
            objects.each { element ->
                addValue(elementFieldBuilder, declaration.type, element)
            }
            elementsBuilder.addFields(elementFieldBuilder)
            elementsBuilder.setPerSample(perSample)
        }
        elementsBuilder.build()
    }

    protected getFooter() {
        cube.dimensions.findAll({ it.density != Dimension.Density.SPARSE
        }).collect { dim ->
            buildDimensionElements(dimensionDeclarations[dim], dimensionElements[dim])
        }
    }

    protected Footer buildFooter() {
        Footer.newBuilder().addAllDimension(footer).build()
    }

    protected Long determineFooterIndex(Dimension dim, Object element) {
        if (dimensionElements[dim] == null) {
            dimensionElements[dim] = []
        }
        int index = dimensionElements[dim].indexOf(element)
        if (index == -1) {
            dimensionElements[dim].add(element)
            index = dimensionElements[dim].indexOf(element)
        }
        index.longValue()
    }

    void write(OutputStream out) {
        begin(out)
        Iterator<HypercubeValue> iterator = cube.iterator()
        if (!iterator.hasNext()) {
            writeEmptyMessage(out)
        }
        else {
            writeMessage(out, buildHeader())
            writeCells(out, iterator)
            writeMessage(out, buildFooter())
        }
        end(out)
    }

}

