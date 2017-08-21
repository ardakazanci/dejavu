package uk.co.glass_software.android.cache_interceptor.interceptors.error;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;

import uk.co.glass_software.android.cache_interceptor.utils.Function;

public class ApiError extends Exception implements Function<ApiError, Boolean> {
    
    private final Throwable throwable;
    
    public ApiError(Throwable throwable) {
        this.throwable = throwable;
    }
    
    @Override
    public Boolean get(ApiError apiError) {
        return apiError.isNetworkError();
    }
    
    private Boolean isNetworkError() {
        return throwable instanceof IOException;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        ApiError apiError = (ApiError) o;
        
        return new EqualsBuilder()
                .append(throwable, apiError.throwable)
                .isEquals();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(throwable)
                .toHashCode();
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("throwable", throwable)
                .toString();
    }
}
