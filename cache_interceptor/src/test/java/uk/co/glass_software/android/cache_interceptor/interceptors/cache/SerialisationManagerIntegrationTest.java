package uk.co.glass_software.android.cache_interceptor.interceptors.cache;


import android.support.annotation.Nullable;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

import uk.co.glass_software.android.cache_interceptor.base.BaseIntegrationTest;
import uk.co.glass_software.android.cache_interceptor.base.network.model.TestResponse;
import uk.co.glass_software.android.cache_interceptor.interceptors.error.ApiError;
import uk.co.glass_software.android.cache_interceptor.response.ResponseMetadata;
import uk.co.glass_software.android.cache_interceptor.utils.Action;
import uk.co.glass_software.android.cache_interceptor.utils.Logger;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class SerialisationManagerIntegrationTest extends BaseIntegrationTest {
    
    private TestResponse stubbedResponse;
    
    private SerialisationManager target;
    
    @Before
    public void setUp() throws Exception {
        stubbedResponse = assetHelper.getStubbedResponse(TestResponse.STUB_FILE, TestResponse.class)
                                     .blockingFirst();
        
        target = new SerialisationManager(mock(Logger.class), new Gson());
    }
    
    @Test
    public void testCompress() throws Exception {
        byte[] compressed = target.compress(stubbedResponse);
        assertEquals("Wrong compressed size", 2566, compressed.length);
    }
    
    @Test
    public void testUncompressSuccess() throws Exception {
        byte[] compressed = target.compress(stubbedResponse);
        Action mockAction = mock(Action.class);
        
        TestResponse uncompressed = target.uncompress(TestResponse.class, compressed, mockAction);
        
        stubbedResponse.setMetadata(null); //no need to test metadata in this test
        assertEquals("Responses didn't match", stubbedResponse, uncompressed);
        verify(mockAction, never()).act();
    }
    
    @Test
    public void testUncompressFailure() throws Exception {
        byte[] compressed = target.compress(stubbedResponse);
        for (int i = 0; i < 50; i++) {
            compressed[i] = 0;
        }
        Action mockAction = mock(Action.class);
        
        target.uncompress(TestResponse.class, compressed, mockAction);
        
        verify(mockAction).act();
    }
}