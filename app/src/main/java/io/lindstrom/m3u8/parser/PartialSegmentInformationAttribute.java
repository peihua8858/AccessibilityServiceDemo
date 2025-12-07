package io.lindstrom.m3u8.parser;

import java.util.Map;

import io.lindstrom.m3u8.model.PartialSegmentInformation;

public enum PartialSegmentInformationAttribute implements Attribute<PartialSegmentInformation, PartialSegmentInformation.Builder> {
    PART_TARGET {
        @Override
        public void read(PartialSegmentInformation.Builder builder, String value) throws PlaylistParserException {
            builder.partTargetDuration(Double.parseDouble(value));
        }

        @Override
        public void write(PartialSegmentInformation value, TextBuilder textBuilder) {
            textBuilder.add(key(), value.partTargetDuration());
        }
    };

    final static Map<String, PartialSegmentInformationAttribute> attributeMap = ParserUtils.toMap(values(), Attribute::key);

    static PartialSegmentInformation parse(String attributes, ParsingMode parsingMode) throws PlaylistParserException {
        PartialSegmentInformation.Builder builder = PartialSegmentInformation.builder();
        ParserUtils.readAttributes(attributeMap, attributes, builder, parsingMode);
        return builder.build();
    }
}
