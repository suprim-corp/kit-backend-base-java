package dev.suprim.kit.grpc;

import io.grpc.Metadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetadataUtilsTest {

    @Test
    void metadataKeys_areNotNull() {
        assertNotNull(MetadataUtils.REQUEST_ID);
        assertNotNull(MetadataUtils.TRACE_ID);
        assertNotNull(MetadataUtils.ERROR_CODE);
        assertNotNull(MetadataUtils.ERROR_DATA);
        assertNotNull(MetadataUtils.USER_ID);
        assertNotNull(MetadataUtils.TENANT_ID);
    }

    @Test
    void getString_returnsValueWhenPresent() {
        Metadata metadata = new Metadata();
        metadata.put(MetadataUtils.REQUEST_ID, "test-value");

        String result = MetadataUtils.getString(metadata, MetadataUtils.REQUEST_ID);

        assertEquals("test-value", result);
    }

    @Test
    void getString_returnsNullWhenNotPresent() {
        Metadata metadata = new Metadata();

        String result = MetadataUtils.getString(metadata, MetadataUtils.REQUEST_ID);

        assertNull(result);
    }

    @Test
    void getString_returnsNullWhenMetadataIsNull() {
        String result = MetadataUtils.getString(null, MetadataUtils.REQUEST_ID);

        assertNull(result);
    }

    @Test
    void getStringWithDefault_returnsValueWhenPresent() {
        Metadata metadata = new Metadata();
        metadata.put(MetadataUtils.REQUEST_ID, "test-value");

        String result = MetadataUtils.getString(metadata, MetadataUtils.REQUEST_ID, "default");

        assertEquals("test-value", result);
    }

    @Test
    void getStringWithDefault_returnsDefaultWhenNotPresent() {
        Metadata metadata = new Metadata();

        String result = MetadataUtils.getString(metadata, MetadataUtils.REQUEST_ID, "default");

        assertEquals("default", result);
    }

    @Test
    void getStringWithDefault_returnsDefaultWhenMetadataIsNull() {
        String result = MetadataUtils.getString(null, MetadataUtils.REQUEST_ID, "default");

        assertEquals("default", result);
    }

    @Test
    void withRequestId_createsMetadataWithRequestId() {
        Metadata metadata = MetadataUtils.withRequestId("req-123");

        assertEquals("req-123", metadata.get(MetadataUtils.REQUEST_ID));
    }

    @Test
    void withError_createsMetadataWithCodeAndData() {
        byte[] data = "error data".getBytes();

        Metadata metadata = MetadataUtils.withError(404, data);

        assertEquals("404", metadata.get(MetadataUtils.ERROR_CODE));
        assertArrayEquals(data, metadata.get(MetadataUtils.ERROR_DATA));
    }

    @Test
    void withError_createsMetadataWithCodeOnly_whenDataIsNull() {
        Metadata metadata = MetadataUtils.withError(500, null);

        assertEquals("500", metadata.get(MetadataUtils.ERROR_CODE));
        assertNull(metadata.get(MetadataUtils.ERROR_DATA));
    }

    @Test
    void metadataKeys_haveCorrectNames() {
        assertEquals("x-request-id", MetadataUtils.REQUEST_ID.name());
        assertEquals("x-trace-id", MetadataUtils.TRACE_ID.name());
        assertEquals("x-error-code", MetadataUtils.ERROR_CODE.name());
        assertEquals("x-error-data-bin", MetadataUtils.ERROR_DATA.name());
        assertEquals("x-user-id", MetadataUtils.USER_ID.name());
        assertEquals("x-tenant-id", MetadataUtils.TENANT_ID.name());
    }
}
