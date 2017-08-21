package uk.co.glass_software.android.cache_interceptor.retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.reactivex.Observable;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import uk.co.glass_software.android.cache_interceptor.interceptors.cache.CacheInterceptor;
import uk.co.glass_software.android.cache_interceptor.interceptors.error.ErrorInterceptor;
import uk.co.glass_software.android.cache_interceptor.response.ResponseMetadata;
import uk.co.glass_software.android.cache_interceptor.utils.Function;
import uk.co.glass_software.android.cache_interceptor.utils.Logger;

public class RetrofitCacheAdapterFactory<E extends Exception & Function<E, Boolean>>
        extends CallAdapter.Factory {
    
    private final RxJava2CallAdapterFactory rxJava2CallAdapterFactory;
    private final Logger logger;
    private final ErrorInterceptor.Factory<E> errorFactory;
    private final CacheInterceptor.Factory cacheInterceptorFactory;
    
    public RetrofitCacheAdapterFactory(Logger logger,
                                       ErrorInterceptor.Factory<E> errorFactory,
                                       CacheInterceptor.Factory<E> cacheInterceptorFactory) {
        this.logger = logger;
        this.errorFactory = errorFactory;
        this.cacheInterceptorFactory = cacheInterceptorFactory;
        rxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public CallAdapter<?, ?> get(Type returnType,
                                 Annotation[] annotations,
                                 Retrofit retrofit) {
        Class<?> rawType = getRawType(returnType);
        CallAdapter callAdapter = rxJava2CallAdapterFactory.get(returnType, annotations, retrofit);
        
        if (rawType == Observable.class
            && returnType instanceof ParameterizedType) {
            
            Type observableType = getParameterUpperBound(0, (ParameterizedType) returnType);
            Class<?> rawObservableType = getRawType(observableType);
            
            if (ResponseMetadata.Holder.class.isAssignableFrom(rawObservableType)) {
                Class<?> responseClass = getRawType(rawObservableType);
                
                return new RetrofitCacheAdapter(logger,
                                                errorFactory,
                                                cacheInterceptorFactory,
                                                responseClass,
                                                callAdapter
                );
            }
        }
        
        return callAdapter;
    }
    
}