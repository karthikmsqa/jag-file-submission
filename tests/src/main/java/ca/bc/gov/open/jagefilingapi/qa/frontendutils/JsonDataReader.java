package ca.bc.gov.open.jagefilingapi.qa.frontendutils;

import ca.bc.gov.open.jagefilingapi.qa.testdatatypes.CsoAccountGuid;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.List;

public class JsonDataReader {

    private JsonDataReader() {
        throw new IllegalStateException("Json Reader Utility class");
    }

    public static CsoAccountGuid getCsoAccountGuid() throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = new FileInputStream("src/test/java/testdatasource/account-data.json");
        TypeReference<List<CsoAccountGuid>> typeReference= new TypeReference<List<CsoAccountGuid>>() {};
        List<CsoAccountGuid> csoAccountGuids = mapper.readValue(inputStream, typeReference);
        CsoAccountGuid account = null;

        for (CsoAccountGuid csoAccountGuid : csoAccountGuids) {
            account = csoAccountGuid;
        }
        return account;
    }
}
