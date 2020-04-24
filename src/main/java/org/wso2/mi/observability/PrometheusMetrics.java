package org.wso2.mi.observability;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;

public final class PrometheusMetrics {

    /**
     * Counters for instrumenting metrics for Proxy Services
     **/
    public static final Counter TOTAL_REQUESTS_RECEIVED_PROXY = Counter.build("total_request_count_proxy_serv", "Total number of requests. "
            + "to a proxy service").labelNames("service", "remoteAddress").register();

    /**
     * Counters for instrumenting metrics for APIs
     **/
    public static final Counter TOTAL_REQUESTS_RECEIVED_API = Counter.build("total_request_count_api",
            "Total number of requests to an API.").labelNames("apiName", "invocationUrl", "remoteAddress").register();

    /**
     * Counters for instrumenting metrics for Inbound Endpoints
     **/
    public static final Counter TOTAL_REQUESTS_RECEIVED_INBOUND_ENDPOINT = Counter.build("total_request_count_inbound_endpoint",
            "Total number of requests to an Inbound Endpoint.").labelNames("inboundEndpointName", "remoteHost").register();

    /**
     * Histograms for instrumenting metrics for Proxy Services
     **/
    public static final Histogram PROXY_REQUEST_DURATION_HISTOGRAM = Histogram.build()
            .name("proxy_request_time_seconds")
            .help("Proxy service request time in seconds")
            .labelNames("proxy_name")
            .buckets(0.05, 0.07, 0.1, 0.5, 1, 2, 5)
            .register();

//    public static final Histogram PROXY_RESPONSE_DURATION_HISTOGRAM = Histogram.build()
//            .name("proxy_response_time_seconds")
//            .help("Proxy service response time in seconds")
//            .labelNames("proxy_name")
//            .buckets(0.25, 0.5, 0.75, 1, 2, 5)
//            .register();

    public static final Histogram PROXY_LATENCY_DURATION_HISTOGRAM = Histogram.build()
            .name("proxy_latency_seconds")
            .help("Proxy service latency in seconds")
            .labelNames("proxy_name")
            .buckets(0.19, 0.20, 0.25, 0.30, 0.35, 0.40, 0.50, 0.60, 1, 5)
            .register();

    /**
     * Histograms for instrumenting metrics for APIs
     **/
    public static final Histogram API_REQUEST_DURATION_HISTOGRAM = Histogram.build()
            .name("api_request_time_seconds")
            .help("API request time in seconds")
            .labelNames("api_name", "invocation_url")
            .buckets(0.19, 0.20, 0.25, 0.30, 0.35, 0.40, 0.50, 0.60, 0.75, 1)
            .register();

    public static final Histogram API_REQUEST_LATENCY_HISTOGRAM = Histogram.build()
            .name("api_latency_time_seconds")
            .help("API latency time in seconds")
            .labelNames("api_name", "invocation_url")
            .buckets(0.0005, 0.0007, 0.001, 0.005, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 1)
            .register();

    /**
     * Histograms for instrumenting metrics for Inbound Endpoints
     **/
    public static final Histogram INBOUND_ENDPOINT_LATENCY_HISTOGRAM = Histogram.build()
            .name("inbound_endpoint_latency_time_seconds")
            .help("Inbound Endpoint latency time in seconds")
            .labelNames("inound_endpoint_name", "remote_host")
            .buckets(0.0005, 0.0007, 0.001, 0.005, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 1)
            .register();

    /**
     * Histograms timers for instrumenting metrics for Proxy Services
     **/
    public static Histogram.Timer proxyRequestTimer;
    // public static Histogram.Timer proxyResponseTimer;
    public static Histogram.Timer proxyLatencyTimer;

    /**
     * Histograms timers for instrumenting metrics for APIs
     **/
    public static Histogram.Timer apiRequestTimer;
    public static Histogram.Timer apiLatencyTimer;

    /**
     * Histograms timers for instrumenting metrics for Inbound Endpoints
     **/
    public static Histogram.Timer inboundEndpointLatencyTimer;
}