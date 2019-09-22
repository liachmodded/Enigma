package cuchaz.enigma.command;

import org.junit.Test;

import java.io.IOException;

import cuchaz.enigma.throwables.MappingParseException;

public class TestEnigmaToTinyV2 {
    private static final String DIR = "src/test/resources/tinyv2/";
    private void convertMappings(String fromFormat, String fromLocation, String toFormat, String toLocation){
        try {
            new ConvertMappingsCommand().run(fromFormat,fromLocation,toFormat,toLocation);
        } catch (IOException | MappingParseException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void testEnigmaToTinyV2(){
        convertMappings("enigma", DIR + "enigma-1.14.4",
                "tinyv2:intermediary:named", DIR + "yarn-mappings.tinyv2");
    }
}
