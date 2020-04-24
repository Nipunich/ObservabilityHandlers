package org.wso2.mi.observability;

import io.prometheus.client.Counter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractSynapseErrorHandler;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.FaultHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.core.axis2.ProxyServiceMessageReceiver;
import org.apache.synapse.mediators.AbstractListMediator;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.RESTConstants;
import io.prometheus.client.hotspot.DefaultExports;

public class CustomObservabilitySynapseErrorHandler extends AbstractSynapseErrorHandler {

    public static final Counter ERROR_REQUESTS_RECEIVED_PROXY_SERVICE = Counter.build("total_error_request_count_proxy_service",
            "Total number of error requests to a proxy service").labelNames("service", "remoteAddress").register();

    @Override
    public boolean handleErrorRequest(MessageContext synCtx) {

        return false;
    }

    @Override
    public boolean handleErrorResponse(MessageContext synCtx) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx)
                .getAxis2MessageContext();

        String name = synCtx.getProperty("proxy.name").toString();
        String host = axis2MessageContext.getProperty("REMOTE_HOST").toString();

        ERROR_REQUESTS_RECEIVED_PROXY_SERVICE.labels(name, host).inc();
        return true;
    }

    private static final Log log = LogFactory.getLog(CustomObservabilitySynapseErrorHandler.class);
    boolean proxyState = true;
    int i = 0;

    @Override
    public boolean handleRequestInFlow(MessageContext synCtx) {

        DefaultExports.initialize();

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx)
                .getAxis2MessageContext();

        String remoteAddr = (String) axis2MessageContext.getProperty(
                org.apache.axis2.context.MessageContext.REMOTE_ADDR);

        if (null != synCtx.getProperty(SynapseConstants.PROXY_SERVICE)) {
            String proxyName = axis2MessageContext.getAxisService().getName();

            PrometheusMetrics.proxyRequestTimer = PrometheusMetrics.PROXY_REQUEST_DURATION_HISTOGRAM.labels(proxyName).startTimer();
            PrometheusMetrics.proxyLatencyTimer = PrometheusMetrics.PROXY_LATENCY_DURATION_HISTOGRAM.labels(proxyName).startTimer();
            PrometheusMetrics.TOTAL_REQUESTS_RECEIVED_PROXY.labels(proxyName, remoteAddr).inc();

        } else if (null != synCtx.getProperty(SynapseConstants.IS_INBOUND)) {
            String inboundEndpointName = axis2MessageContext.getProperty("REST_URL_POSTFIX").toString();
            String remoteHost = axis2MessageContext.getProperty("REMOTE_HOST").toString();

            PrometheusMetrics.TOTAL_REQUESTS_RECEIVED_INBOUND_ENDPOINT.labels(inboundEndpointName, remoteHost).inc();
            PrometheusMetrics.inboundEndpointLatencyTimer = PrometheusMetrics.INBOUND_ENDPOINT_LATENCY_HISTOGRAM.
                    labels(inboundEndpointName, remoteHost).startTimer();

        } else {
            String context = axis2MessageContext.getProperty("TransportInURL").toString();
            String apiInvocationUrl = axis2MessageContext.getProperty("SERVICE_PREFIX").toString() +
                    context.replaceFirst("/", "");
            String apiName = "";

            for (API api : synCtx.getEnvironment().getSynapseConfiguration().getAPIs()) {
                if (api.getContext().equals(context)) {
                    apiName = api.getAPIName();
                }
            }

            PrometheusMetrics.TOTAL_REQUESTS_RECEIVED_API.labels(apiName, apiInvocationUrl, remoteAddr).inc();
            PrometheusMetrics.apiRequestTimer = PrometheusMetrics.API_REQUEST_DURATION_HISTOGRAM.
                    labels(apiName, apiInvocationUrl).startTimer();
            PrometheusMetrics.apiLatencyTimer = PrometheusMetrics.API_REQUEST_LATENCY_HISTOGRAM.
                    labels(apiName, apiInvocationUrl).startTimer();
        }

        return true;
    }

    @Override
    public boolean handleRequestOutFlow(MessageContext synCtx) {

        synCtx.getProperty(RESTConstants.SYNAPSE_REST_API);
        // PrometheusMetrics.proxyRequestTimer.observeDuration();
        return true;
    }

    @Override
    public boolean handleResponseInFlow(MessageContext synCtx) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx).
                getAxis2MessageContext();
        String name = axis2MessageContext.getAxisService().getName();
        //PrometheusMetrics.proxyResponseTimer = PrometheusMetrics.PROXY_RESPONSE_DURATION_HISTOGRAM.labels(name).startTimer();

        return true;
    }

    @Override
    public boolean handleResponseOutFlow(MessageContext synCtx) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx).
                getAxis2MessageContext();

        String remoteAddr = (String) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
//        String name = ProxyServiceMessageReceiver.proxyName;
//
//        proxyState = FaultHandler.proxySuccess;

//        if (!proxyState && (null != name)) {
//            PrometheusMetrics.ERROR_REQUESTS_RECEIVED_PROXY.labels(name, remoteAddr).inc();
//        }
        // PrometheusMetrics.proxyResponseTimer.observeDuration();

        if (null != synCtx.getProperty("REST_FULL_REQUEST_PATH") && (synCtx.getProperty("REST_FULL_REQUEST_PATH").equals("/metric-service/metrics"))) {

        } else {
            if (null != PrometheusMetrics.proxyLatencyTimer) {
                PrometheusMetrics.proxyLatencyTimer.observeDuration();
            }
            if (null != PrometheusMetrics.apiLatencyTimer) {
                PrometheusMetrics.apiLatencyTimer.observeDuration();
            }
            if (null != PrometheusMetrics.inboundEndpointLatencyTimer) {
                PrometheusMetrics.inboundEndpointLatencyTimer.observeDuration();
            }

        }
        return true;
    }
}
